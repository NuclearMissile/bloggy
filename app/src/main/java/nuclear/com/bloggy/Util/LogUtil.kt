package nuclear.com.bloggy.Util

import android.text.TextUtils
import android.util.Log
import nuclear.com.bloggy.Settings


/**
 * Created by torri on 2017/10/10.
 */

object LogUtil {

    private val DEBUG_FLAG = Settings.INSTANCE.DebugMode

    fun i(o: Any, msg: String?) {
        if (DEBUG_FLAG)
            Log.i(getTag(o), msg ?: "null message")
    }

    fun i(tag: String, msg: String?) {
        if (DEBUG_FLAG)
            Log.i(tag, msg ?: "null message")
    }

    fun e(o: Any, msg: String?) {
        if (DEBUG_FLAG)
            Log.e(getTag(o), msg ?: "null message")
    }

    fun e(tag: String, msg: String?) {
        if (DEBUG_FLAG)
            Log.e(tag, msg ?: "null message")
    }

    fun w(o: Any, msg: String?) {
        if (DEBUG_FLAG)
            Log.w(getTag(o), msg ?: "null message")
    }

    fun w(tag: String, msg: String?) {
        if (DEBUG_FLAG)
            Log.w(tag, msg ?: "null message")
    }

    private fun getTag(o: Any): String {
        var tag = o.javaClass.simpleName
        if (TextUtils.isEmpty(tag))
            tag = "AnonymousClass"
        return tag
    }
}
