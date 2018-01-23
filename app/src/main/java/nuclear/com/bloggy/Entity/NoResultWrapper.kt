package nuclear.com.bloggy.Entity

import com.google.gson.annotations.SerializedName

data class NoResultWrapper(@SerializedName("is_success") val isSuccess: Boolean,
                           @SerializedName("status_code") val statusCode: Int,
                           @SerializedName("message") val message: String)