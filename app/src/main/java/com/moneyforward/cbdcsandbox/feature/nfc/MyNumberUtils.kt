package com.moneyforward.cbdcsandbox.feature.nfc

import com.moneyforward.cbdcsandbox.model.MyNumberCommandError
import com.moneyforward.cbdcsandbox.model.exception.MyNumberException

// 下記サイトがとても参考になります。
// https://qiita.com/gebo/items/fa35c1f725f4c443f3f3
object MyNumberUtils {
    val RESULT_SUCCESS = listOf(0x90.toByte(), 0x00)

    private const val RETRY_RESULT_SUCCESS = 0x63.toByte()

    // 公的個人認証APのselect file APDU
    val COMMAND_SELECT_FILE = byteArrayOf(
        0x00,
        0xA4.toByte(),
        0x04,
        0x0C,
        0x0A,
        0xD3.toByte(),
        0x92.toByte(),
        0xF0.toByte(),
        0x00,
        0x26,
        0x01,
        0x00,
        0x00,
        0x00,
        0x01
    )

    // 証明書全体のサイズを求める APDU
    val COMMAND_READ_BINARY = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, 0x04)

    // PINリトライ回数をGET
    val COMMAND_READ_RETRY_COUNT = byteArrayOf(0x00, 0x20, 0x00, 0x80.toByte())

    // 署名用PINの select file APDU
    val COMMAND_SELECT_FILE_PIN_SYNC =
        byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x1B)

    // 署名用証明書の select file APDU
    val COMMAND_SELECT_FILE_CERT = byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x01)

    // 署名用鍵の select file APDU
    val COMMAND_SELECT_FILE_KEY_SYNC =
        byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x1A)

    // 認証用証明書の select file APDU
    val COMMAND_SELECT_FILE_AUTH_CERT =
        byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x0A)

    // 認証用PINの select file APDU
    val COMMAND_SELECT_FILE_AUTH_PIN =
        byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x18)

    // 認証用鍵の select file APDI
    val COMMAND_SELECT_FILE_AUTH_KEY =
        byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, 0x02, 0x00, 0x17)

    // 認証用PINの verify APDUのヘッダ
    private val COMMAND_PIN_VERIFY = byteArrayOf(0x00, 0x20, 0x00, 0x80.toByte())

    // 暗号化用の APDU のヘッダ
    private val COMMAND_SIGNATURE_DATA_HEADER =
        byteArrayOf(0x80.toByte(), 0x2A, 0x00, 0x80.toByte())

    // 証明書の一部を取得する
    fun commandReadBlock(readIndex: Int) =
        byteArrayOf(0x00, 0xB0.toByte(), readIndex.toByte(), 0x00, 0x00)

    fun commandSignatureData(data: ByteArray): ByteArray {
        val result: ArrayList<Byte> = ArrayList()
        result.addAll(COMMAND_SIGNATURE_DATA_HEADER.asList())
        result.add(data.size.toByte())
        result.addAll(data.asList())
        result.add(0.toByte())
        return result.toByteArray()
    }

    fun commandSignaturePin(data: ByteArray): ByteArray {
        val result: ArrayList<Byte> = ArrayList()
        result.addAll(COMMAND_PIN_VERIFY.asList())
        result.add(data.size.toByte())
        result.addAll(data.asList())
        return result.toByteArray()
    }

    fun commandResultCheck(
        result: ByteArray,
        error: MyNumberCommandError = MyNumberCommandError.UNEXPECTED_COMMAND,
        retryCount: Int = 0
    ) {
        if (!result.takeLast(RESULT_SUCCESS.size).containsAll(RESULT_SUCCESS)) {
            throw MyNumberException(error, retryCount)
        }
    }

    fun commandRetryCountResultCheck(result: ByteArray) {
        if (result.first() != RETRY_RESULT_SUCCESS) {
            throw MyNumberException(MyNumberCommandError.UNEXPECTED_COMMAND)
        }
    }

    fun getRetryCountFromResultByte(result: Byte): Int {
        return ByteUtil.byteArrayToHexString(byteArrayOf(result)).last().toString().toInt()
    }
}