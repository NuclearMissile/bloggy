package nuclear.com.bloggy.Util

import android.text.TextUtils
import nuclear.com.bloggy.BaseApplication
import nuclear.com.bloggy.Settings
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit


object OkHttpUtil {
    private val DEF_TIMEOUT: Long = 5

    val INTERCEPTOR_FORCE_NETWORK: Interceptor by lazy {
        Interceptor { chain ->
            var req = chain.request()
            req = req.newBuilder()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
            return@Interceptor chain.proceed(req)
        }
    }

    val INTERCEPTOR_FORCE_CACHE: Interceptor by lazy {
        Interceptor { chain ->
            var req = chain.request()
            req = req.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build()
            chain.proceed(req)
        }
    }

    val INTERCEPTOR_LOGGING by lazy {
        val inter = HttpLoggingInterceptor()
        inter.level = if (Settings.INSTANCE.DebugMode)
            HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        inter
    }

    val INTERCEPTOR_JSON_HEADER by lazy {
        Interceptor { chain ->
            var req = chain.request()
            req = req.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
            chain.proceed(req)
        }
    }

    val INTERCEPTOR_AUTO_CACHE: Interceptor by lazy {
        Interceptor { chain ->
            var req = chain.request()
            if (!NetworkUtil.isConnected(BaseApplication.instance)) {
                req = req.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
                LogUtil.i(this, "*****No network, use cache.*****")
            }

            var resp = chain.proceed(req)

            if (NetworkUtil.isConnected(BaseApplication.instance)) {
                val maxAge = 60
                var cacheControl = req.cacheControl().toString()
                if (TextUtils.isEmpty(cacheControl))
                    cacheControl = "public, max-age=" + maxAge
                resp = resp.newBuilder().removeHeader("Pragma")
                        .header("Cache-Control", cacheControl)
                        .build()
            } else {
                val maxStale = 60 * 60 * 24 * 30
                resp = resp.newBuilder().removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build()
            }
            resp
        }
    }

    fun genAuthHeader(token: String) = Credentials.basic(token, "")

    fun genAuthHeader(email: String, password: String) = Credentials.basic(email, password)

    fun genAuthInterceptor(username: String, password: String): Interceptor {
        if (!TextUtils.isEmpty(username) and !TextUtils.isEmpty(password))
            return genAuthInterceptor(genAuthHeader(username, password))
        throw IllegalArgumentException("username or password cannot be empty.")
    }

    private fun genAuthInterceptor(tokenB64: String): Interceptor {
        if (TextUtils.isEmpty(tokenB64))
            throw IllegalArgumentException("tokenB64 cannot be empty.")
        return Interceptor { chain ->
            var req = chain.request()
            req = req.newBuilder().header("Authorization", tokenB64).build()
            return@Interceptor chain.proceed(req)
        }
    }

    fun genOkHttpClient(vararg interceptors: Interceptor): OkHttpClient {
        val cacheDir = File(BaseApplication.instance.cacheDir, "OkHttpCache")
        val builder = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(DEF_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEF_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEF_TIMEOUT, TimeUnit.SECONDS)
                .cache(Cache(cacheDir, 100 * 1024 * 1024))
        interceptors.forEach { builder.addInterceptor(it) }
        return builder.build()
    }
}