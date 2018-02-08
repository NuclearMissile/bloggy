package nuclear.com.bloggy.Network

import io.reactivex.Flowable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface IDefaultService {
    @GET
    fun load(@Url url: String): Flowable<ResponseBody>

    @GET
    @Streaming
    fun streamingLoad(@Url url: String): Flowable<ResponseBody>
}