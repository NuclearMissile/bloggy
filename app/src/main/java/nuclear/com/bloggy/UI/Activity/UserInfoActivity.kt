package nuclear.com.bloggy.UI.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.header_user_info.*
import me.drakeet.multitype.MultiTypeAdapter
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.REST.FollowState
import nuclear.com.bloggy.Entity.REST.Post
import nuclear.com.bloggy.Entity.REST.User
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Fragment.BaseRVFragment
import nuclear.com.bloggy.UI.Fragment.IPostFragment
import nuclear.com.bloggy.UI.PostViewBinder
import nuclear.com.bloggy.UI.UserViewBinder
import nuclear.com.bloggy.UI.Widget.RxSwipeBackActivity
import nuclear.com.bloggy.Util.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class UserInfoActivity : RxSwipeBackActivity() {
    private lateinit var mFollowState: FollowState
    private lateinit var mUser: User
    private var mId: Int = -1

    companion object {
        fun tryStart(context: Context, id: Int?) {
            if (id == null || UserHolder.isAnonymous) {
                LogInActivity.tryStart(context)
            } else {
                val intent = Intent(context, UserInfoActivity::class.java)
                intent.putExtra("id", id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        setSupportActionBar(toolbar_user_info)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        follow_unfollow_btn_user_info.setOnClickListener { onFollowUnfollowClick() }
        edit_btn_user_info.setOnClickListener { onEditClick() }
        mId = intent.getIntExtra("id", -1)
        if (mId == -1)
            throw IllegalArgumentException("illegal id received")
        initViewPager()
    }

    override fun onStart() {
        if (UserHolder.isAnonymous)
            finish()
        else
            init()
        super.onStart()
    }

    private fun initViewPager() {
        if (view_pager_user_info.adapter != null)
            return
        view_pager_user_info.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout_user_info))
        tab_layout_user_info.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view_pager_user_info))
        view_pager_user_info.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                val bundle = Bundle()
                bundle.putInt("id", mId)
                val f = when (position) {
                    0 -> UserPostsRVFragment()
                    1 -> FollowingsRVFragment()
                    2 -> FollowedbysFragment()
                    else -> throw IllegalArgumentException("Illegal position: $position")
                }
                f.arguments = bundle
                return f
            }

            override fun getCount(): Int = 3
        }
    }

    private fun init() {
        Flowable.concat(Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    ServiceFactory.DEF_SERVICE.getFollowState(mId, UserHolder.getAuthHeaderByToken())
                }, ServiceFactory.DEF_SERVICE.getUserById(mId))
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    when (it.result) {
                        is User -> mUser = it.result
                        is FollowState -> mFollowState = it.result
                    }
                }, onError = {
                    handleError(this, it)
                    finish()
                }, onComplete = {
                    syncUI()
                })
    }

    private fun syncUI() {
        if (this.isDestroyed) {
            LogUtil.w(this, "call syncUI while activity is destroyed")
            return
        }

        var toolbarTitle = mUser.username
        if (UserHolder.isSelfId(mUser.id)) {
            follow_unfollow_btn_user_info.visibility = View.GONE
            edit_btn_user_info.visibility = View.VISIBLE
            toolbarTitle += " [Self]"
        } else {
            follow_unfollow_btn_user_info.visibility = View.VISIBLE
            if (mFollowState.isFollowedBy)
                toolbarTitle += " [Following Me]"
            if (mFollowState.isFollowing) {
                follow_unfollow_btn_user_info.setText(resources.getString(R.string.unfollow_user_info))
            } else {
                follow_unfollow_btn_user_info.setText(resources.getString(R.string.follow_user_info))
            }
        }

        if (mUser.aboutMe == null)
            about_me_tv_user_info.visibility = View.GONE
        else
            about_me_tv_user_info.text = mUser.aboutMe

        Glide.with(this)
                .load(UserHolder.getAvatarUrl(mUser.avatarHash, 120))
                .apply(GlideOptions.DEF_OPTION)
                .transition(withCrossFade())
                .into(avatar_iv_user_info)
        toolbar_user_info.title = toolbarTitle
    }

    private fun onEditClick() {
        // todo
    }

    private fun onFollowUnfollowClick() {
        follow_unfollow_btn_user_info.isEnabled = false
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    if (mFollowState.isFollowing)
                        ServiceFactory.DEF_SERVICE.unfollowUser(mUser.id, UserHolder.getAuthHeaderByToken())
                    else
                        ServiceFactory.DEF_SERVICE.followUser(mUser.id, UserHolder.getAuthHeaderByToken())
                }
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    mFollowState.isFollowing = !mFollowState.isFollowing
                    if (mFollowState.isFollowing) {
                        follow_unfollow_btn_user_info.setText(resources.getString(R.string.unfollow_user_info))
                    } else {
                        follow_unfollow_btn_user_info.setText(resources.getString(R.string.follow_user_info))
                    }
                }, onError = {
                    handleError(this, it)
                    follow_unfollow_btn_user_info.isEnabled = true
                }, onComplete = {
                    follow_unfollow_btn_user_info.isEnabled = true
                })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_info, menu)
        menu.findItem(R.id.action_log_out).isVisible = UserHolder.currUser!!.id == mId
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_share ->
                ShareUtil.shareText(this, "Share currUser link...", mUser.userLink)
            R.id.action_log_out -> {
                UserHolder.logout()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class FollowedbysFragment : BaseRVFragment() {
    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(User::class.java, UserViewBinder(activity!!))
    }

    override fun onNoMoreData() {}

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getFollowers(arguments!!.getInt("id"),
                                    UserHolder.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE.getFollowers(UserHolder.getAuthHeaderByToken(), current)
                }
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.result.next, it.result.list)
                }, onError = {
                    handleError(this, it)
                })
    }
}

class FollowingsRVFragment : BaseRVFragment() {
    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(User::class.java, UserViewBinder(activity!!))
    }

    override fun onNoMoreData() {}

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getFolloweds(arguments!!.getInt("id"),
                                    UserHolder.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE.getFolloweds(UserHolder.getAuthHeaderByToken(), current)
                }
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.result.next, it.result.list)
                }, onError = {
                    handleError(this, it)
                })
    }
}

class UserPostsRVFragment : BaseRVFragment(), IPostFragment {

    @Subscribe
    override fun onAddPost(event: AddPostEvent) = addItem(event.post)

    @Subscribe
    override fun onRemovePost(event: RemovePostEvent) = removeItem(event.post)

    @Subscribe
    override fun onUpdatePost(changeEvent: ChangePostEvent) = changeItem(changeEvent.oldPost, changeEvent.newPost)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(Post::class.java, PostViewBinder(activity!!, this))
    }

    override fun onNoMoreData() {}

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getUserPosts(arguments!!.getInt("id"))
                        else
                            ServiceFactory.DEF_SERVICE.getUserPosts(current)
                }
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.result.next, it.result.list)
                }, onError = {
                    handleError(this, it)
                })
    }
}
