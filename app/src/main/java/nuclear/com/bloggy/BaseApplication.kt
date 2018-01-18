package nuclear.com.bloggy

import android.app.Application
import android.os.Environment
import io.objectbox.Box
import io.objectbox.BoxStore
import nuclear.com.bloggy.Entity.FavoritePost
import nuclear.com.bloggy.Entity.MyObjectBox
import nuclear.com.bloggy.Entity.NewArticle
import java.io.File

class BaseApplication : Application() {
    companion object {
        lateinit var instance: BaseApplication
            private set
        lateinit var boxStore: BoxStore
            private set
        lateinit var favoritePostBox: Box<FavoritePost>
            private set
        lateinit var draftBox: Box<NewArticle>
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        boxStore = MyObjectBox.builder().androidContext(this).build()
        favoritePostBox = boxStore.boxFor(FavoritePost::class.java)
        draftBox = boxStore.boxFor(NewArticle::class.java)

        Settings.INSTANCE.DebugMode = true
        Settings.INSTANCE.BaseUrl = "http://192.168.43.38:5000/api/v1.0/"
        UserManager.resume()
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