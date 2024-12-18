package com.moneyforward.cbdcsandbox.feature.nfc

import android.util.Base64

object ByteUtil {
    fun byteArrayToHexString(inarray: ByteArray): String {
        var i: Int
        var inVar: Int
        val hex =
            arrayOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F"
            )
        var out = ""

        var j: Int = 0
        while (j < inarray.size) {
            inVar = inarray[j].toInt() and 0xff
            i = inVar shr 4 and 0x0f
            out += hex[i]
            i = inVar and 0x0f
            out += hex[i]
            ++j
        }
        return out
    }

    fun commandSignatureToBase64Encode(hex: ByteArray): String {
        return Base64.encodeToString(hex.dropLast(2).toByteArray(), Base64.NO_WRAP)
    }

    fun byteArrayToBase64EncodeString(hex: ByteArray): String {
        return Base64.encodeToString(hex, Base64.NO_WRAP)
    }

    fun stringToBase64byteArray(str: String): ByteArray {
        return Base64.decode(str, Base64.NO_WRAP)
    }

    fun bytesToUnsignedShort(byte1: Byte, byte2: Byte, bigEndian: Boolean): Int {
        if (bigEndian) return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))
        return (((byte2.toInt() and 255) shl 8) or (byte1.toInt() and 255))
    }
}