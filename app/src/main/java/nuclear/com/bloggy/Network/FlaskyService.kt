package nuclear.com.bloggy.Network

import io.reactivex.Flowable
import nuclear.com.bloggy.Entity.REST.*
import okhttp3.RequestBody
import retrofit2.http.*

interface FlaskyService {
    @GET("token/")
    fun getToken(@Header("Authorization") authHeader: String?): Flowable<ResultWrapper<Token>>

    @POST("register/")
    fun register(@Body userRegInfo: RequestBody): Flowable<ResultWrapper<User>>

    @GET("login/")
    fun login(@Header("Authorization") authHeader: String?): Flowable<ResultWrapper<User>>

    @GET("users/{email}/reset_password/")
    fun resetPassword(@Path("email") email: String): Flowable<NoResultWrapper>

    @GET("resend_email/{id}/")
    fun resendEmail(@Path("id") id: Int, @Header("Authorization") authHeader: String?): Flowable<NoResultWrapper>

    @GET("comments/")
    fun getAllComments(): Flowable<ResultWrapper<Pagination<Comment>>>

    @GET("comments/{id}/")
    fun getCommentById(@Path("id") id: Int): Flowable<ResultWrapper<Comment>>

    @GET("posts/{id}/comments/")
    fun getPostComments(@Path("id") id: Int): Flowable<ResultWrapper<Pagination<Comment>>>

    @DELETE("comments/{id}/")
    fun deleteComment(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<NoResultWrapper>

    @POST("posts/{id}/comments/")
    fun newComment(@Path("id") id: Int, @Body newComment: NewArticle, @Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<Comment>>

    @GET("posts/")
    fun getAllPosts(): Flowable<ResultWrapper<Pagination<Post>>>

    @GET
    fun getAllPosts(@Url url: String): Flowable<ResultWrapper<Pagination<Post>>>

    @GET("posts/{id}/")
    fun getPostById(@Path("id") id: Int): Flowable<ResultWrapper<Post>>

    @DELETE("posts/{id}/")
    fun deletePost(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<NoResultWrapper>

    @PUT("posts/{id}/")
    fun editPost(@Body newPost: NewArticle, @Path("id") id: Int,
                 @Header("Authorization") authHeader: String?): Flowable<ResultWrapper<Post>>

    @POST("posts/")
    fun newPost(@Body newPost: NewArticle, @Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<Post>>

    @GET("users/{id}/posts/")
    fun getUserPosts(@Path("id") id: Int): Flowable<ResultWrapper<Pagination<Post>>>

    @GET
    fun getUserPosts(@Url url: String): Flowable<ResultWrapper<Pagination<Post>>>

    @GET("users/{id}/")
    fun getUserById(@Path("id") id: Int): Flowable<ResultWrapper<User>>

    @GET("users/{id}/followers/")
    fun getFollowers(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<Pagination<User>>>

    @GET
    fun getFollowers(@Header("Authorization") authHeader: String?, @Url url: String)
            : Flowable<ResultWrapper<Pagination<User>>>

    @GET("users/{id}/followeds/")
    fun getFolloweds(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<Pagination<User>>>

    @GET
    fun getFolloweds(@Header("Authorization") authHeader: String?, @Url url: String)
            : Flowable<ResultWrapper<Pagination<User>>>

    @PUT("users/followers/{id}/")
    fun followUser(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<NoResultWrapper>

    @DELETE("users/followers/{id}/")
    fun unfollowUser(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<NoResultWrapper>

    @GET("users/follow_state/{id}/")
    fun getFollowState(@Path("id") id: Int, @Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<FollowState>>

    @GET("timeline/")
    fun getTimeline(@Header("Authorization") authHeader: String?)
            : Flowable<ResultWrapper<Pagination<Post>>>

    @GET
    fun getTimeline(@Header("Authorization") authHeader: String?, @Url url: String)
            : Flowable<ResultWrapper<Pagination<Post>>>
}