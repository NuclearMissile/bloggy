package nuclear.com.bloggy.Util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class EncryptUtil private constructor(context: Context) {

    private val key by lazy { SHA256(getDeviceSerialNumber(context) + "vQg4CAa3bCztP8m54GSaGvZU").substring(0, 16) }

    @SuppressLint("HardwareIds")
    private fun getDeviceSerialNumber(context: Context): String {
        @Suppress("DEPRECATION")
        return try {
            val deviceSerial: String = Build.SERIAL
            if (TextUtils.isEmpty(deviceSerial))
                throw Exception()
            LogUtil.i(this, deviceSerial)
            deviceSerial
        } catch (e: Exception) {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    private fun SHA256(strText: String): String {
        if (TextUtils.isEmpty(strText))
            throw IllegalArgumentException()
        else {
            try {
                val messageDigest = MessageDigest.getInstance("SHA-256")
                messageDigest.update(strText.toByteArray())
                return Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        throw Exception()
    }

    fun encrypt(plainText: String?): String? {
        return try {
            plainText ?: return null
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(plainText.toByteArray())
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(encryptedText: String?): String? {
        return try {
            encryptedText ?: return null
            val encrypted = Base64.decode(encryptedText, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        @Volatile
        private var instance: EncryptUtil? = null

        fun getInstance(context: Context): EncryptUtil {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = EncryptUtil(context)
                    }
                }
            }
            return instance!!
        }
    }
}