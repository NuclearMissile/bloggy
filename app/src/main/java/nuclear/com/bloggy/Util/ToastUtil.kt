package nuclear.com.bloggy.Util

import android.widget.Toast
import nuclear.com.bloggy.BaseApplication

object ToastUtil {
    fun showShortToast(msg: String?)
            = Toast.makeText(BaseApplication.instance, msg ?: "null message", Toast.LENGTH_SHORT).show()

    fun showLongToast(msg: String?)
            = Toast.makeText(BaseApplication.instance, msg ?: "null message", Toast.LENGTH_LONG).show()

    fun showShortToast(resId: Int)
            = Toast.makeText(BaseApplication.instance, resId, Toast.LENGTH_SHORT).show()

    fun showLongToast(resId: Int)
            = Toast.makeText(BaseApplication.instance, resId, Toast.LENGTH_LONG).show()
}