package nuclear.com.bloggy

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import io.objectbox.Box
import io.objectbox.BoxStore
import nuclear.com.bloggy.Entity.REST.FavoritePost
import nuclear.com.bloggy.Entity.REST.MyObjectBox
import nuclear.com.bloggy.Entity.REST.NewArticle
import nuclear.com.bloggy.Service.WebSocketService
import java.io.File


class BaseApplication : Application() {
    companion object {
        lateinit var INSTANCE: BaseApplication
            private set
        lateinit var boxStore: BoxStore
            private set
        lateinit var favoritePostBox: Box<FavoritePost>
            private set
        lateinit var draftBox: Box<NewArticle>
            private set

        fun openFromBackground(context: Context) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(100)
            if (!list.isEmpty() && list[0].topActivity.packageName == context.packageName) {
                return
            }
            list.forEach {
                if (it.topActivity.packageName == context.packageName) {
                    val intent = Intent()
                    intent.component = it.topActivity
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (context !is Activity)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                }
            }
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName))
        }
    }

    fun startWSService() {
        if (Settings.INSTANCE.EnableWSService) {
            startService(Intent(this, WebSocketService::class.java))
        }
    }

    fun stopWSService() {
        stopService(Intent(this, WebSocketService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        boxStore = MyObjectBox.builder().androidContext(this).build()
        favoritePostBox = boxStore.boxFor(FavoritePost::class.java)
        draftBox = boxStore.boxFor(NewArticle::class.java)
        Settings.INSTANCE.EnableWSService = true
        UserHolder.resume()
    }

    override fun getCacheDir(): File {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val cacheDir = externalCacheDir
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs()))
                return cacheDir
        }
        return super.getCacheDir()
    }
}