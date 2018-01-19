package nuclear.com.bloggy.UI.Fragment

import nuclear.com.bloggy.AddPostEvent
import nuclear.com.bloggy.RemovePostEvent
import nuclear.com.bloggy.ChangePostEvent

interface IPostFragment {
    fun onAddPost(event: AddPostEvent)

    fun onRemovePost(event: RemovePostEvent)

    fun onUpdatePost(changeEvent: ChangePostEvent)
}