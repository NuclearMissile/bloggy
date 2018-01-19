package nuclear.com.bloggy

import nuclear.com.bloggy.Entity.Comment
import nuclear.com.bloggy.Entity.FavoritePost
import nuclear.com.bloggy.Entity.NewArticle
import nuclear.com.bloggy.Entity.Post

data class AddPostEvent(val post: Post)

data class RemovePostEvent(val post: Post)

data class ChangePostEvent(val oldPost: Post, val newPost: Post)

data class AddCommentEvent(val comment: Comment)

data class RemoveCommentEvent(val comment: Comment)

data class AddDraftEvent(val draft: NewArticle)

data class RemoveDraftEvent(val draft: NewArticle)

// data class AddFavoriteEvent(val favoritePost: FavoritePost)

data class RemoveFavoriteEvent(val favoritePost: FavoritePost)