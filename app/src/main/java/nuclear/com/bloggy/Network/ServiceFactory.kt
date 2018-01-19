package nuclear.com.bloggy.Network

import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.Util.OkHttpUtil
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ServiceFactory private constructor() {
    private val mDefClient = OkHttpUtil.genOkHttpClient(
            OkHttpUtil.INTERCEPTOR_LOGGING,
            /* OkHttpUtil.INTERCEPTOR_AUTO_CACHE,*/
            OkHttpUtil.INTERCEPTOR_JSON_HEADER)

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

    companion object {
        val DEF_SERVICE by lazy {
            ServiceFactory().createService(FlaskyService::class.java, Settings.INSTANCE.BaseUrl!!)
        }
    }
}