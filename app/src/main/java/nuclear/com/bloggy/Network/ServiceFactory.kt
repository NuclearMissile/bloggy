package nuclear.com.bloggy.Network

import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.Util.OkHttpUtil
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ServiceFactory {
    private val mDefClient = OkHttpUtil.genOkHttpClient(
            OkHttpUtil.INTERCEPTOR_LOGGING,
            OkHttpUtil.INTERCEPTOR_AUTO_CACHE,
            OkHttpUtil.INTERCEPTOR_JSON_HEADER)

    var DEF_SERVICE = createService(FlaskyService::class.java, Settings.INSTANCE.BaseUrl)
        private set

    fun refreshDefService() {
        DEF_SERVICE = createService(FlaskyService::class.java, Settings.INSTANCE.BaseUrl)
    }

    fun <S> createService(serviceClass: Class<S>, baseUrl: String,
                          okHttpClient: OkHttpClient = mDefClient): S {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(serviceClass)
    }
}