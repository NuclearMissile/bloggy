package nuclear.com.bloggy

import android.content.Context
import io.reactivex.Flowable
import nuclear.com.bloggy.Entity.REST.User
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Activity.LogInActivity
import nuclear.com.bloggy.Util.DateUtil
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.OkHttpUtil
import nuclear.com.bloggy.Util.checkApiError

object UserHolder {
    @Volatile
    var self: User? = null
        private set
    val isAnonymous
        get() = self == null
    val isConfirmed
        get() = self?.confirmed ?: false
    val isAdmin
        get() = can(Permission.ADMIN)
    val isSavedTokenValid: Boolean
        get() = Settings.INSTANCE.TokenExpireAt > DateUtil.TimeStamp + 30000 && Settings.INSTANCE.AuthToken != null

    fun isSelfById(id: Int): Boolean {
        self ?: return false
        return self!!.id == id
    }

    fun handlePermissionError(context: Context, needed: Int) {
        when {
            UserHolder.isAnonymous -> LogInActivity.tryStart(context)
            else -> LogUtil.e(this, "permission error not be handled: need $needed")
        }
    }

    fun can(permission: Int): Boolean {
        self ?: return false
        return self!!.permissions and permission == permission
    }

    fun getAvatarUrl(size: Int): String? {
        self ?: return null
        return getAvatarUrl(self!!.avatarHash, size)
    }

    fun getAvatarUrl(avatarHash: String, size: Int, default: String = "identicon", rating: String = "g"): String {
        return "https://secure.gravatar.com/avatar/$avatarHash?s=$size&d=$default&r=$rating"
    }

    fun login(user: User, password: String) {
        self = user
        Settings.INSTANCE.SavedUser = user
        Settings.INSTANCE.Password = password
    }

    fun resume() {
        Settings.INSTANCE.Password ?: return
        self = Settings.INSTANCE.SavedUser
    }

    fun logout() {
        BaseApplication.favoritePostBox.removeAll()
        BaseApplication.draftBox.removeAll()
        self = null
        Settings.INSTANCE.AuthToken = null
        Settings.INSTANCE.TokenExpireAt = -1
        Settings.INSTANCE.Password = null
        Settings.INSTANCE.SavedUser = null
    }

    fun getAuthHeaderByToken(): String? {
        if (isAnonymous)
            throw IllegalStateException("call getAuthHeaderByToken while self is anonymous")
        return OkHttpUtil.genAuthHeader(Settings.INSTANCE.AuthToken!!)
    }

    fun retryForToken(throwable: Flowable<Throwable>): Flowable<*> {
        return throwable.flatMap {
            if (it is TokenInvalidError) {
                LogUtil.i(this, it.message)
                ServiceFactory.DEF_SERVICE
                        .getToken(getAuthHeaderByPassword())
                        .checkApiError()
                        .doOnNext {
                            Settings.INSTANCE.AuthToken = it.result.token
                            Settings.INSTANCE.TokenExpireAt = it.result.expireAt
                        }
            } else
                throw it
        }
    }

    private fun getAuthHeaderByPassword(): String {
        if (isAnonymous)
            throw IllegalStateException("call getAuthHeaderByToken while self is anonymous")
        return OkHttpUtil.genAuthHeader(self!!.email, Settings.INSTANCE.Password!!)
    }
}

object Permission {
    val NOT_ANONYMOUS = 1
    val FOLLOW = 2
    val COMMENT = 4
    val WRITE = 8
    val MODERATE = 16
    val ADMIN = 0xff
}
