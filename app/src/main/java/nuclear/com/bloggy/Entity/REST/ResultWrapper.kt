package nuclear.com.bloggy.Entity.REST

import com.google.gson.annotations.SerializedName

typealias NoResultWrapper = ResultWrapper<Any?>

data class ResultWrapper<out T>(@SerializedName("is_success") val isSuccess: Boolean,
                                @SerializedName("status_code") val statusCode: Int,
                                @SerializedName("message") val message: String,
                                @SerializedName("result") val result: T)