package nuclear.com.bloggy.Util

import java.lang.StringBuilder
import kotlin.experimental.and


object HexUtil {
    private const val HEXES = "0123456789ABCDEF"
    private const val MASK_HIGH = 0xF0.toByte()
    private const val MASK_LOW = 0x0F.toByte()

    fun Collection<Byte>.toHex(): String {
        val sb = StringBuilder(this.size shl 1)
        this.forEach {
            sb.append(HEXES[(it and MASK_HIGH).toInt() shr 4])
                    .append(HEXES[(it and MASK_LOW).toInt()])
        }
        return sb.toString()
    }

    fun String.toBytes(): Collection<Byte> {
        val cap = this.length shr 1
        val res = ArrayList<Byte>(cap)
        val hexString = this.toUpperCase()
        var pos: Int
        for (i in 0 until cap) {
            pos = i shl 1
            res[i] = ((HEXES[hexString[pos].toInt()].toInt() shl 4) or HEXES[hexString[pos + 1].toInt()].toInt()).toByte()
        }
        return res
    }
}