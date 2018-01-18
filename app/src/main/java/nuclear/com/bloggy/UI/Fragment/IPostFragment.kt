package nuclear.com.bloggy.UI.Fragment

import nuclear.com.bloggy.AddPostEvent
import nuclear.com.bloggy.RemovePostEvent
import nuclear.com.bloggy.PostChangeEvent

/**
 * Created by torri on 2018/1/15.
 */
interface IPostFragment {
    fun onAddPost(event: AddPostEvent)

    fun onRemovePost(event: RemovePostEvent)

    fun onUpdatePost(changeEvent: PostChangeEvent)
}