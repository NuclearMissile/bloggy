package nuclear.com.bloggy.Entity

/**
 * Created by torri on 2017/12/8.
 */
data class Pagination<out T> constructor(
        val list: List<T>,
        val prev: String?,
        val next: String?,
        val count: Int
)