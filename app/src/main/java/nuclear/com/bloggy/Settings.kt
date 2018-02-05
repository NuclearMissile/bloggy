package nuclear.com.bloggy

import android.preference.PreferenceManager
import com.google.gson.Gson
import nuclear.com.bloggy.Entity.REST.User
import nuclear.com.bloggy.Util.EncryptSharedPreference
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.ToastUtil

class Settings private constructor() {
    private val mEncryptPrefs by lazy { EncryptSharedPreference(BaseApplication.instance) }
    private val mEncryptEditor by lazy { mEncryptPrefs.edit() }
    private val mPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(BaseApplication.instance) }
    private val mEditor by lazy { mPrefs.edit() }

    companion object {
        val INSTANCE = Settings()
    }

    private fun getResString(id: Int): String = BaseApplication.instance.resources.getString(id)

/*    var OnlyWifiLoadImage: Boolean
        get() = mPrefs.getBoolean("OnlyWifiLoadImage", false)
        set(value) = mEditor.putBoolean("OnlyWifiLoadImage", value).apply()*/

    var DebugMode: Boolean
        get() = mPrefs.getBoolean(getResString(R.string.debug_mode), false)
        set(value) = mEditor.putBoolean(getResString(R.string.debug_mode), value).apply()

    var Password: String?
        get() = mEncryptPrefs.getString("Password", null)
        set(value) = mEncryptEditor.putString("Password", value).apply()

    var AuthToken: String?
        get() = mEncryptPrefs.getString("AuthToken", null)
        set(value) = mEncryptEditor.putString("AuthToken", value).apply()

    var TokenExpireAt: Long
        get() = mEncryptPrefs.getLong("TokenExpireAt", -1)
        set(value) = mEncryptEditor.putLong("TokenExpireAt", value).apply()

    var BaseUrl: String
        get() = mPrefs.getString(getResString(R.string.base_url), "http://192.168.43.201:5000/api/v1.0/")
        set(value) {
            if (!value.matches("(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]".toRegex())) {
                ToastUtil.showShortToast("Url invalid")
                return
            }
            mEditor.putString(getResString(R.string.base_url), if (value.endsWith("/")) value else value + "/").apply()
        }

    var SavedUser: User?
        get() {
            return try {
                val jsonString = mEncryptPrefs.getString("SavedUser", null) ?: return null
                Gson().fromJson(jsonString, User::class.java)
            } catch (e: Exception) {
                LogUtil.e(this, e.message ?: "null message")
                e.printStackTrace()
                null
            }
        }
        set(value) {
            try {
                val v = if (value == null) null else Gson().toJson(value, User::class.java)
                mEncryptEditor.putString("SavedUser", v).apply()
            } catch (e: Exception) {
                LogUtil.e(this, e.message ?: "null message")
                e.printStackTrace()
            }
        }
}