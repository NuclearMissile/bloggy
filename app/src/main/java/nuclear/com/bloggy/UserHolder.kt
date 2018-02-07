package nuclear.com.bloggy

import android.content.Context
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import nuclear.com.bloggy.Entity.REST.User
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Activity.LogInActivity
import nuclear.com.bloggy.Util.*

object UserHolder {
    var currUser: User? = null
        @Synchronized
        private set
    val isAnonymous
        get() = currUser == null
    val isAdmin
        get() = can(Permission.ADMIN)
    val isSavedTokenValid: Boolean
        @Synchronized
        get() = Settings.INSTANCE.TokenExpireAt > DateUtil.TimeStamp + 30000 && Settings.INSTANCE.AuthToken != null

    fun isSelfById(id: Int): Boolean {
        currUser ?: return false
        return currUser!!.id == id
    }

    fun handlePermissionError(context: Context, needed: Int) {
        when {
            UserHolder.isAnonymous -> LogInActivity.tryStart(context)
            else -> LogUtil.e(this, "permission error not be handled: need $needed")
        }
    }

    fun can(permission: Int): Boolean {
        currUser ?: return false
        return currUser!!.permissions and permission == permission
    }

    fun getAvatarUrl(size: Int): String? {
        currUser ?: return null
        return getAvatarUrl(currUser!!.avatarHash, size)
    }

    fun getAvatarUrl(avatarHash: String, size: Int, default: String = "identicon", rating: String = "g"): String {
        return "https://secure.gravatar.com/avatar/$avatarHash?s=$size&d=$default&r=$rating"
    }

    @Synchronized
    fun login(user: User, password: String) {
        currUser = user
        Settings.INSTANCE.SavedUser = user
        Settings.INSTANCE.Password = password
        refreshToken(getAuthHeader())
    }

    @Synchronized
    fun resume() {
        Settings.INSTANCE.Password ?: return
        currUser = Settings.INSTANCE.SavedUser
        refreshToken(getAuthHeader())
    }

    @Synchronized
    fun logout() {
        BaseApplication.favoritePostBox.removeAll()
        BaseApplication.draftBox.removeAll()
        currUser = null
        Settings.INSTANCE.AuthToken = null
        Settings.INSTANCE.TokenExpireAt = -1
        Settings.INSTANCE.Password = null
        Settings.INSTANCE.SavedUser = null
    }

    fun getAuthHeaderByToken(): String? {
        if (isAnonymous)
            throw IllegalStateException("call getAuthHeaderByToken while currUser is anonymous")
        return OkHttpUtil.genAuthHeader(Settings.INSTANCE.AuthToken!!)
    }

    @Synchronized
    fun retryForToken(throwable: Flowable<Throwable>): Flowable<Any> {
        return throwable.flatMap {
            if (it is TokenInvalidError) {
                LogUtil.i(this, it.message)
                ServiceFactory.DEF_SERVICE
                        .getToken(getAuthHeader())
                        .doOnNext {
                            Settings.INSTANCE.AuthToken = it.result.token
                            Settings.INSTANCE.TokenExpireAt = it.result.expireAt
                        }
            } else
                throw it
        }
    }

    private fun refreshToken(authHeader: String) {
        if (isSavedTokenValid)
            return
        ServiceFactory.DEF_SERVICE.getToken(authHeader)
                .checkApiError()
                .allIOSchedulers()
                .subscribeBy(onNext = {
                    Settings.INSTANCE.AuthToken = it.result.token
                    Settings.INSTANCE.TokenExpireAt = it.result.expireAt
                }, onError = {
                    handleError(this, it)
                })
    }

    private fun getAuthHeader(): String {
        if (isAnonymous)
            throw IllegalStateException("call getAuthHeaderByToken while currUser is anonymous")
        return OkHttpUtil.genAuthHeader(currUser!!.email, Settings.INSTANCE.Password!!)
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
