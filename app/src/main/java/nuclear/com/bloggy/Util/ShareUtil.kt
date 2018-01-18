package nuclear.com.bloggy.Util

import android.content.Context
import android.content.Intent

/**
 * Created by torri on 2018/1/9.
 */
object ShareUtil {
    fun shareText(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(intent)
    }
}