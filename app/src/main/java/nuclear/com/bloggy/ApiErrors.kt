package nuclear.com.bloggy

import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.ToastUtil

class ApiUnknownError(msg: String) : Exception(msg)

class ApiNotFoundError(msg: String) : Exception(msg)

class ApiForbiddenError(msg: String) : Exception(msg)

class ApiAuthError(msg: String) : Exception(msg)

class ApiBadRequestError(msg: String) : Exception(msg)

class TokenInvalidError(msg: String = "cached token invalid, may be expired, retry") : Exception(msg)

fun handleError(o: Any, throwable: Throwable) {
    if (Settings.INSTANCE.DebugMode) {
        ToastUtil.showLongToast(throwable.message)
        throwable.printStackTrace()
    }
    LogUtil.e(o, throwable.message)
}