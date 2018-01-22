package nuclear.com.bloggy.Util

import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import nuclear.com.bloggy.R

/**
 * Created by torri on 2017/12/27.
 */
object GlideOptions {
    val DEF_OPTION: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.IMMEDIATE)
            .transform(CircleCrop())
}