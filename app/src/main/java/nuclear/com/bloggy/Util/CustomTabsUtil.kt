package nuclear.com.bloggy.Util

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat

import nuclear.com.bloggy.R


/**
 * Created by torri on 2017/10/23.
 */

object CustomTabsUtil {
    fun openUrl(context: Context, url: String) {
        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .build()
                .launchUrl(context, Uri.parse(url))
    }
}
