package nuclear.com.bloggy.UI

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import me.drakeet.multitype.ItemViewBinder
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.*
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Activity.EditPostActivity
import nuclear.com.bloggy.UI.Activity.PostActivity
import nuclear.com.bloggy.UI.Activity.UserInfoActivity
import nuclear.com.bloggy.Util.*
import org.greenrobot.eventbus.EventBus
import ru.noties.markwon.Markwon

class PostViewBinder(private val activity: FragmentActivity) : ItemViewBinder<Post, PostViewBinder.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: Post) {
        fun onLongClick(): Boolean {
            val favorite = BaseApplication.favoritePostBox.query()
                    .equal(FavoritePost_.postId, item.id.toLong()).build()
                    .findFirst()
            val popup = PopupMenu(activity, holder.itemView, Gravity.END)
            popup.menuInflater.inflate(R.menu.popup_menu_post_item, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_popup_post_item -> {
                        EditPostActivity.tryStart(activity, item.id)
                        true
                    }
                    R.id.favorite_popup_post_item -> {
                        if (favorite == null) {
                            BaseApplication.favoritePostBox.put(FavoritePost(item.id))
                            // EventBus.getDefault().post(AddFavoriteEvent(f))
                        } else {
                            BaseApplication.favoritePostBox.remove(favorite)
                            EventBus.getDefault().post(RemoveFavoriteEvent(favorite))
                        }
                        true
                    }
                    R.id.delete_popup_post_item -> {
                        MaterialDialog.Builder(activity)
                                .title(R.string.delete_confirm)
                                .content(R.string.delete_confirm_content)
                                .positiveText(R.string.ok)
                                .negativeText(R.string.cancel)
                                .onPositive { _, _ ->
                                    Flowable.just(1)
                                            .flatMap {
                                                if (!UserManager.isSavedTokenValid)
                                                    throw TokenInvalidException()
                                                else
                                                    ServiceFactory.DEF_SERVICE.deletePost(item.id, UserManager.getAuthHeaderByToken())
                                            }
                                            .retryWhen(UserManager::retryForToken)
                                            .map { if (it.isSuccess) it else throw Exception(it.message) }
                                            .defaultSchedulers()
                                            .subscribeBy(onNext = {
                                                EventBus.getDefault().post(RemovePostEvent(item))
                                                ToastUtil.showShortToast(R.string.post_deleted)
                                            }, onError = {
                                                LogUtil.e(this, it.message)
                                                ToastUtil.showLongToast(it.message)
                                                it.printStackTrace()
                                            })
                                }.show()
                        true
                    }
                    R.id.share_popup_post_item -> {
                        ShareUtil.shareText(activity, "Share post's link...", item.link)
                        true
                    }
                    else -> true
                }
            }
            popup.menu.findItem(R.id.delete_popup_post_item).isVisible = UserManager.isAdmin
            popup.menu.findItem(R.id.edit_popup_post_item).isVisible = UserManager.isSelfById(item.authorId)
            popup.menu.findItem(R.id.delete_popup_post_item).isVisible = UserManager.isSelfById(item.authorId)
            popup.menu.findItem(R.id.favorite_popup_post_item).isVisible = !UserManager.isAnonymous
            popup.menu.findItem(R.id.favorite_popup_post_item).isChecked = favorite != null
            popup.show()
            return true
        }

        Markwon.setMarkdown(holder.bodyTV, item.body)
        holder.timestampTV.text = DateUtil.getFriendlyTime(item.timeStamp)
        Glide.with(activity)
                .load(UserManager.getAvatarUrl(item.authorAvatarHash, 120))
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.avatarIV)
        holder.avatarIV.setOnClickListener { UserInfoActivity.tryStart(activity, item.authorId) }
        holder.usernameTV.text = item.authorName
        holder.commentCountTV.text = "Comments: ${item.commentsCount}"
        holder.itemView.setOnClickListener { PostActivity.tryStart(activity, item.id) }
        holder.itemView.setOnLongClickListener { onLongClick() }
        holder.bodyTV.setOnLongClickListener { onLongClick() }
        holder.bodyTV.setOnClickListener { PostActivity.tryStart(activity, item.id) }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.post_item, parent, false))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val avatarIV: ImageView = itemView.findViewById(R.id.avatar_iv_post_item)
        internal val bodyTV: TextView = itemView.findViewById(R.id.post_body_tv_post_item)
        internal val timestampTV: TextView = itemView.findViewById(R.id.timestamp_tv_post_item)
        internal val usernameTV: TextView = itemView.findViewById(R.id.username_tv_post_item)
        internal val commentCountTV: TextView = itemView.findViewById(R.id.comment_count_tv_post_item)
    }
}

class CommentViewBinder(private val activity: FragmentActivity) : ItemViewBinder<Comment, CommentViewBinder.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: Comment) {
        fun onLongClick(): Boolean {
            if (!UserManager.isAdmin && !UserManager.isSelfById(item.authorId))
                return true
            val popup = PopupMenu(activity, holder.itemView, Gravity.END)
            popup.menuInflater.inflate(R.menu.popup_menu_comment_item, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete_popup_comment_item -> {
                        MaterialDialog.Builder(activity)
                                .title(R.string.delete_confirm)
                                .content(R.string.delete_confirm_content)
                                .positiveText(R.string.ok)
                                .negativeText(R.string.cancel)
                                .onPositive { _, _ ->
                                    Flowable.just(1)
                                            .flatMap {
                                                if (!UserManager.isSavedTokenValid)
                                                    throw TokenInvalidException()
                                                else
                                                    ServiceFactory.DEF_SERVICE.deleteComment(item.id, UserManager.getAuthHeaderByToken())
                                            }
                                            .retryWhen(UserManager::retryForToken)
                                            .map { if (it.isSuccess) it else throw Exception(it.message) }
                                            .defaultSchedulers()
                                            .subscribeBy(onNext = {
                                                EventBus.getDefault().post(RemoveCommentEvent(item))
                                                ToastUtil.showShortToast(R.string.comment_deleted)
                                            }, onError = {
                                                LogUtil.e(this, it.message)
                                                ToastUtil.showLongToast(it.message)
                                                it.printStackTrace()
                                            })
                                }.show()
                    }
                }
                true
            }
            return true
        }

        Glide.with(activity)
                .load(UserManager.getAvatarUrl(item.authorAvatarHash, 60))
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.avatarIV)
        holder.avatarIV.setOnClickListener { UserInfoActivity.tryStart(activity, item.authorId) }
        holder.usernameTV.text = item.authorName
        Markwon.setMarkdown(holder.commentTV, item.body)
        holder.timeStampTV.text = DateUtil.getFriendlyTime(item.timeStamp)
        holder.commentTV.setOnLongClickListener { onLongClick() }
        holder.itemView.setOnLongClickListener { onLongClick() }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.comment_item, parent, false))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val avatarIV: ImageView = itemView.findViewById(R.id.avatar_iv_comment_item)
        internal val usernameTV: TextView = itemView.findViewById(R.id.username_tv_comment_item)
        internal val commentTV: TextView = itemView.findViewById(R.id.comment_body_tv_comment_item)
        internal val timeStampTV: TextView = itemView.findViewById(R.id.timestamp_tv_comment_item)
    }
}

class PostContentViewBinder : ItemViewBinder<Post, PostContentViewBinder.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: Post) {
        Markwon.setMarkdown(holder.markdownTV, item.body)
        holder.commentCountTV.text = "Comments: ${item.commentsCount}"
        holder.timeStampTV.text = DateUtil.getFriendlyTime(item.timeStamp)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.post_content_item, parent, false))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val markdownTV: TextView = itemView.findViewById(R.id.markdown_tv_post_content_item)
        internal val commentCountTV: TextView = itemView.findViewById(R.id.comment_count_tv_post_content_item)
        internal val timeStampTV: TextView = itemView.findViewById(R.id.timestamp_tv_post_content_item)
    }
}

class UserViewBinder(val activity: FragmentActivity) : ItemViewBinder<User, UserViewBinder.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, item: User) {
        Glide.with(activity)
                .load(UserManager.getAvatarUrl(item.avatarHash, 120))
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.avatarIV)
        holder.usernameTV.text = "${item.username}\n[Posts:${item.postsCount} Followers:${item.followersCount} Followeds:${item.followedsCount}]"
        if (item.aboutMe == null)
            holder.aboutMeTV.visibility = View.GONE
        else {
            holder.aboutMeTV.visibility = View.VISIBLE
            holder.aboutMeTV.text = item.aboutMe
        }
        holder.itemView.setOnClickListener { UserInfoActivity.tryStart(activity, item.id) }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.user_item, parent, false))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val avatarIV: ImageView = itemView.findViewById(R.id.avatar_iv_user_item)
        internal val usernameTV: TextView = itemView.findViewById(R.id.username_tv_user_item)
        internal val aboutMeTV: TextView = itemView.findViewById(R.id.about_me_tv_user_item)
    }
}

class DraftViewBinder(val activity: FragmentActivity) : ItemViewBinder<NewArticle, DraftViewBinder.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, item: NewArticle) {
        fun onLongClick(): Boolean {
            val popup = PopupMenu(activity, holder.itemView, Gravity.END)
            popup.menuInflater.inflate(R.menu.popup_menu_draft_item, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_popup_draft_item -> {
                        EditPostActivity.tryStart(activity, item.body)
                        true
                    }
                    R.id.delete_popup_draft_item -> {
                        BaseApplication.draftBox.remove(item)
                        EventBus.getDefault().post(RemoveDraftEvent(item))
                        true
                    }
                    else -> true
                }
            }
            return true
        }

        holder.commentCountTV.visibility = View.GONE
        holder.usernameTV.text = UserManager.self?.username
        holder.timestampTV.text = DateUtil.getFriendlyTime(item.timeStamp)

        Glide.with(activity)
                .load(UserManager.getAvatarUrl(UserManager.self!!.avatarHash, 120))
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.avatarIV)
        holder.avatarIV.setOnClickListener { UserInfoActivity.tryStart(activity, UserManager.self!!.id) }

        Markwon.setMarkdown(holder.bodyTV, item.body)
        holder.itemView.setOnClickListener { EditPostActivity.tryStart(activity, item.body) }
        holder.bodyTV.setOnClickListener { EditPostActivity.tryStart(activity, item.body) }
        holder.itemView.setOnLongClickListener { onLongClick() }
        holder.bodyTV.setOnLongClickListener { onLongClick() }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.post_item, parent, false))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val avatarIV: ImageView = itemView.findViewById(R.id.avatar_iv_post_item)
        internal val bodyTV: TextView = itemView.findViewById(R.id.post_body_tv_post_item)
        internal val timestampTV: TextView = itemView.findViewById(R.id.timestamp_tv_post_item)
        internal val usernameTV: TextView = itemView.findViewById(R.id.username_tv_post_item)
        internal val commentCountTV: TextView = itemView.findViewById(R.id.comment_count_tv_post_item)
    }
}