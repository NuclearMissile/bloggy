package nuclear.com.bloggy

import nuclear.com.bloggy.Entity.Comment
import nuclear.com.bloggy.Entity.Post

data class AddPostEvent(val post: Post)

data class RemovePostEvent(val post: Post)

data class PostChangeEvent(val oldPost: Post, val newPost: Post)

data class AddCommentEvent(val comment: Comment)

data class RemoveCommentEvent(val comment: Comment)