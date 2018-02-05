package nuclear.com.bloggy

import android.app.Application
import android.os.Environment
import io.objectbox.Box
import io.objectbox.BoxStore
import nuclear.com.bloggy.Entity.REST.FavoritePost
import nuclear.com.bloggy.Entity.REST.MyObjectBox
import nuclear.com.bloggy.Entity.REST.NewArticle
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