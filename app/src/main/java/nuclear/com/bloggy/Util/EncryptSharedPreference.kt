package nuclear.com.bloggy.Util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.ArrayMap

class EncryptSharedPreference(context: Context, name: String? = "encrypt_prefs") : SharedPreferences {
    private val mContext = context
    private val mSharedPreference =
            if (TextUtils.isEmpty(name)) PreferenceManager.getDefaultSharedPreferences(mContext)
            else mContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun handleTransistion() {
        val oldMap = mSharedPreference.all
        val newMap = ArrayMap<String, String?>(oldMap.size)
        oldMap.forEach {
            newMap.put(encryptPreference(it.key), encryptPreference(it.value.toString()))
        }
        val editor = mSharedPreference.edit()
        editor.clear().apply()
        newMap.forEach {
            editor.putString(it.key, it.value)
        }
        editor.apply()
    }

    private fun encryptPreference(plainText: String): String
            = EncryptUtil.getInstance(mContext).encrypt(plainText)!!

    private fun decryptPreference(encryptedText: String): String
            = EncryptUtil.getInstance(mContext).decrypt(encryptedText)!!

    override fun contains(key: String): Boolean
            = mSharedPreference.contains(encryptPreference(key))

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val encryptValue = mSharedPreference.getString(encryptPreference(key), null)
        encryptValue ?: return defValue
        return decryptPreference(encryptValue).toBoolean()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        mSharedPreference.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        mSharedPreference.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun edit(): EncryptEditor = EncryptEditor()

    override fun getInt(key: String, defValue: Int): Int {
        val encryptValue = mSharedPreference.getString(encryptPreference(key), null)
        encryptValue ?: return defValue
        return decryptPreference(encryptValue).toInt()
    }

    override fun getAll(): MutableMap<String, *> {
        val encryptMap = mSharedPreference.all
        val decryptMap = ArrayMap<String, String>(encryptMap.size)
        encryptMap.forEach {
            val encrypted = it.value
            if (encrypted != null)
                decryptMap.put(decryptPreference(it.key), decryptPreference(encrypted.toString()))
        }
        return decryptMap
    }

    override fun getLong(key: String, defValue: Long): Long {
        val encryptValue = mSharedPreference.getString(encryptPreference(key), null)
        encryptValue ?: return defValue
        return decryptPreference(encryptValue).toLong()
    }

    override fun getFloat(key: String, defValue: Float): Float {
        val encryptValue = mSharedPreference.getString(encryptPreference(key), null)
        encryptValue ?: return defValue
        return decryptPreference(encryptValue).toFloat()
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val encryptSet = mSharedPreference.getStringSet(encryptPreference(key), null)
        encryptSet ?: return defValues
        return encryptSet.map { decryptPreference(it) }.toMutableSet()
    }

    override fun getString(key: String, defValue: String?): String? {
        val encryptedValue = mSharedPreference.getString(encryptPreference(key), null)
        encryptedValue ?: return defValue
        return decryptPreference(encryptedValue)
    }

    inner class EncryptEditor internal constructor() : SharedPreferences.Editor {
        private val mEditor = mSharedPreference.edit()

        override fun clear(): SharedPreferences.Editor {
            mEditor.clear()
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            mEditor.putString(encryptPreference(key), encryptPreference(value.toString()))
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            mEditor.putString(encryptPreference(key), encryptPreference(value.toString()))
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            mEditor.remove(encryptPreference(key))
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            mEditor.putString(encryptPreference(key), encryptPreference(value.toString()))
            return this
        }

        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
            val encryptSet = values?.map { encryptPreference(it) }?.toSet()
            mEditor.putStringSet(encryptPreference(key), encryptSet)
            return this
        }

        override fun commit(): Boolean = mEditor.commit()

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            mEditor.putString(encryptPreference(key), encryptPreference(value.toString()))
            return this
        }

        override fun apply() = mEditor.apply()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            mEditor.putString(encryptPreference(key), if (value == null) null else encryptPreference(value))
            return this
        }
    }
}