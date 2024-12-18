package com.moneyforward.cbdcsandbox.feature.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.moneyforward.cbdcsandbox.model.DocumentSignature
import com.moneyforward.cbdcsandbox.model.ETaxDigest
import com.moneyforward.cbdcsandbox.model.MyNumberCommandError
import com.moneyforward.cbdcsandbox.model.MyNumberCommandResult
import com.moneyforward.cbdcsandbox.model.exception.MyNumberException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

interface MyNumberConnect {
    fun getSignatureAndCertificate(
        tag: Tag,
        digestValue: ETaxDigest,
        loginPassword: String,
        documentPassword: String
    ): MyNumberCommandResult
}

// @see: https://www.notion.so/indiv-all/Android-Research-of-signing-multiple-documents-by-Mynumber-Card-9206aaa64be140cba5007a654d13db64?pvs=4#56ec2e2a8e8f42529b3a6eef5db0bf31
@Singleton
class DefaultMyNumberConnect @Inject constructor() : MyNumberConnect {
    override fun getSignatureAndCertificate(
        tag: Tag,
        digestValue: ETaxDigest,
        loginPassword: String,
        documentPassword: String
    ): MyNumberCommandResult {
        val isoDep =
            IsoDep.get(tag) ?: throw MyNumberException(MyNumberCommandError.UNEXPECTED_COMMAND)
        isoDep.use { dep ->

            dep.timeout = 6000000
            dep.connect()

            setupJPKI(dep)

            val loginPinRetryCount = commandGetLoginPinRetryCount(dep)
            if (loginPinRetryCount == 0) {
                throw MyNumberException(MyNumberCommandError.NO_RETRY_COUNT_LOGIN)
            }
            val documentPinRetryCount = commandGetDocumentPinRetryCount(dep)
            if (documentPinRetryCount == 0) {
                throw MyNumberException(MyNumberCommandError.NO_RETRY_COUNT_DOCUMENT)
            }

            commandVerifyDocumentPassword(
                dep,
                documentPassword.toByteArray(),
                documentPinRetryCount
            )

            val documentCertificate = commandDocumentCertificate(dep)
            val documentSignatures = digestValue.documentDigests.map {
                val decodedDigestInfo = ByteUtil.stringToBase64byteArray(it.documentDigestInfo)
                val documentSignature = commandDocumentSignature(
                    dep,
                    decodedDigestInfo
                )
                DocumentSignature(
                    documentSignature = documentSignature,
                    documentDigestValue = it.documentDigestValue
                )
            }

            val loginCertificate = commandLoginCertificate(dep)

            commandVerifyLoginPassword(dep, loginPassword.toByteArray(), loginPinRetryCount)
            val decodedLoginDigestInfo =
                ByteUtil.stringToBase64byteArray(digestValue.loginDigestInfo)
            val loginSignature = commandLoginSignature(
                dep,
                decodedLoginDigestInfo,
            )

            return MyNumberCommandResult(
                loginSignature = loginSignature,
                loginCertificate = loginCertificate,
                documentCertificate = documentCertificate,
                documentSignatures = documentSignatures
            )
        }
    }

    /**
     * setup JPKI
     * we need to select the file to use the JPKI at first in the session.
     */
    private fun setupJPKI(isoDep: IsoDep) {
        // SELECT FILE 公的個人認証AP
        val selectFileApResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE)
        MyNumberUtils.commandResultCheck(selectFileApResult)
    }

    private fun commandGetLoginPinRetryCount(isoDep: IsoDep): Int {
        // SELECT FILE 認証用PIN
        val selectFileAuthPinResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_AUTH_PIN)
        MyNumberUtils.commandResultCheck(selectFileAuthPinResult)

        // retry回数をGET
        val retryCountResult = isoDep.transceive(MyNumberUtils.COMMAND_READ_RETRY_COUNT)
        MyNumberUtils.commandRetryCountResultCheck(retryCountResult)
        return MyNumberUtils.getRetryCountFromResultByte(retryCountResult.last())
    }

    /**
     * get login certificate (利用者証明用証明書)
     * we don't need to verify the login password to get the login certificate.
     */
    private fun commandLoginCertificate(isoDep: IsoDep): String {
        // SELECT FILE 認証用証明書
        val selectFileAuthCertResult =
            isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_AUTH_CERT)
        MyNumberUtils.commandResultCheck(selectFileAuthCertResult)

        // READ BINARY
        val selectReadBinaryResult = isoDep.transceive(MyNumberUtils.COMMAND_READ_BINARY)
        MyNumberUtils.commandResultCheck(selectReadBinaryResult)
        val encodedCertificateData = ByteUtil.byteArrayToBase64EncodeString(readBinary(isoDep))
        Timber.d("encodedCertificateData = $encodedCertificateData")
        return encodedCertificateData
    }

    /**
     * verify login password (利用者証明用パスワード)
     * we need to verify the login password to get the login signature.
     */
    private fun commandVerifyLoginPassword(
        isoDep: IsoDep,
        loginPassword: ByteArray,
        retryCount: Int
    ) {
        // SELECT FILE 認証用PIN
        val selectFileAuthPinResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_AUTH_PIN)
        MyNumberUtils.commandResultCheck(selectFileAuthPinResult)

        // VERIFY 認証用PIN
        val verifyUserCertificationResult =
            isoDep.transceive(MyNumberUtils.commandSignaturePin(loginPassword))
        MyNumberUtils.commandResultCheck(
            verifyUserCertificationResult,
            MyNumberCommandError.WRONG_LOGIN_PASSWORD,
            retryCount - 1
        )
    }

    /**
     * get login signature (利用者証明用署名)
     * we need to verify the login password to get the login signature.
     */
    private fun commandLoginSignature(
        isoDep: IsoDep,
        digestValue: ByteArray,
    ): String {
        // SELECT FILE 認証用鍵
        val selectFileAuthKeyResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_AUTH_KEY)
        MyNumberUtils.commandResultCheck(selectFileAuthKeyResult)

        // COMPUTE DIGITAL SIGNATURE
        val commandSignatureDataResult =
            isoDep.transceive(MyNumberUtils.commandSignatureData(digestValue))
        MyNumberUtils.commandResultCheck(commandSignatureDataResult)
        val encodedSignatureData =
            ByteUtil.commandSignatureToBase64Encode(commandSignatureDataResult)
        Timber.d("encodedSignatureData = $encodedSignatureData")
        return encodedSignatureData
    }

    private fun commandGetDocumentPinRetryCount(isoDep: IsoDep): Int {
        // SELECT FILE 認証用PIN
        val selectFilePinSyncResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_PIN_SYNC)
        MyNumberUtils.commandResultCheck(selectFilePinSyncResult)

        // retry回数をGET
        val retryCountResult = isoDep.transceive(MyNumberUtils.COMMAND_READ_RETRY_COUNT)
        MyNumberUtils.commandRetryCountResultCheck(retryCountResult)
        return MyNumberUtils.getRetryCountFromResultByte(retryCountResult.last())
    }

    /**
     * verify document password (署名用パスワード)
     * we need to verify the document password to get the document certificate and signatures.
     */
    private fun commandVerifyDocumentPassword(
        isoDep: IsoDep,
        documentPassword: ByteArray,
        retryCount: Int
    ) {
        // SELECT FILE 認証用PIN
        val selectFilePinSyncResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_PIN_SYNC)
        MyNumberUtils.commandResultCheck(selectFilePinSyncResult)

        // VERIFY 認証用PIN
        val verifySignaturePasswordResult =
            isoDep.transceive(MyNumberUtils.commandSignaturePin(documentPassword))
        MyNumberUtils.commandResultCheck(
            verifySignaturePasswordResult,
            MyNumberCommandError.WRONG_DOCUMENT_PASSWORD,
            retryCount - 1
        )
    }

    /**
     * get document certificate (署名用証明書)
     * we need to verify the document password to get the document certificate.
     */
    private fun commandDocumentCertificate(
        isoDep: IsoDep
    ): String {
        // SELECT FILE CERT
        val selectFileCertResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_CERT)
        MyNumberUtils.commandResultCheck(selectFileCertResult)

        // READ BINARY
        val selectReadBinary2Result = isoDep.transceive(MyNumberUtils.COMMAND_READ_BINARY)
        MyNumberUtils.commandResultCheck(selectReadBinary2Result)
        val encodedCertificateData = ByteUtil.byteArrayToBase64EncodeString(readBinary(isoDep))
        Timber.d("encodedCertificateData = $encodedCertificateData")
        return encodedCertificateData
    }

    /**
     * get document signature (署名)
     * we need to verify the document password to get the document signatures.
     */
    private fun commandDocumentSignature(
        isoDep: IsoDep,
        digestValue: ByteArray
    ): String {
        // SELECT FILE KEY
        val selectFileKeySyncResult = isoDep.transceive(MyNumberUtils.COMMAND_SELECT_FILE_KEY_SYNC)
        MyNumberUtils.commandResultCheck(selectFileKeySyncResult)

        // COMPUTE DIGITAL SIGNATURE
        val commandSignatureDataResult2 =
            isoDep.transceive(MyNumberUtils.commandSignatureData(digestValue))
        MyNumberUtils.commandResultCheck(commandSignatureDataResult2)
        val encodedSignatureData =
            ByteUtil.commandSignatureToBase64Encode(commandSignatureDataResult2)
        Timber.d("encodedSignatureData = $encodedSignatureData")
        return encodedSignatureData
    }

    private fun readBinary(isoDep: IsoDep): ByteArray {
        val resultLength = MyNumberUtils.RESULT_SUCCESS.size
        val outByte: ArrayList<Byte> = ArrayList()
        val response = isoDep.transceive(MyNumberUtils.COMMAND_READ_BINARY)
        if (response.size <= resultLength) return response
        val readLength = ByteUtil.bytesToUnsignedShort(
            response[2],
            response[3],
            true
        ) + 4
        val blockNum = ceil(readLength / BLOCK_LENGTH.toDouble()).toInt()
        for (index in 0..blockNum) {
            val ret = isoDep.transceive(MyNumberUtils.commandReadBlock(index))
            MyNumberUtils.commandResultCheck(ret)
            outByte.addAll(ret.dropLast(resultLength))
            if (ret.size <= resultLength) {
                break
            }
        }
        return outByte.take(readLength).toByteArray()
    }

    companion object {
        private const val BLOCK_LENGTH = 256
    }
}