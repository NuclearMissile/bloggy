package nuclear.com.bloggy.UI.Activity

import android.annotation.SuppressLint
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
import nuclear.com.bloggy.Entity.FollowState
import nuclear.com.bloggy.Entity.Post
import nuclear.com.bloggy.Entity.User
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Fragment.BaseRVFragment
import nuclear.com.bloggy.UI.Fragment.IPostFragment
import nuclear.com.bloggy.UI.PostViewBinder
import nuclear.com.bloggy.UI.UserViewBinder
import nuclear.com.bloggy.UI.Widget.SwipeBackRxActivity
import nuclear.com.bloggy.Util.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class UserInfoActivity : SwipeBackRxActivity() {
    private lateinit var mFollowState: FollowState
    private lateinit var mUser: User
    private var mId: Int = -1

    companion object {
        fun tryStart(context: Context, id: Int?) {
            if (id == null || UserManager.isAnonymous) {
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
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        if (UserManager.isAnonymous)
            finish()
        else
            syncState()
        super.onResume()
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

    private fun syncState() {
        Flowable.concat(Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    ServiceFactory.DEF_SERVICE.getFollowState(mId, UserManager.getAuthHeaderByToken())
                }, ServiceFactory.DEF_SERVICE.getUserById(mId))
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    when (it) {
                        is User -> mUser = it
                        is FollowState -> mFollowState = it
                    }
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                    finish()
                }, onComplete = {
                    initViewPager()
                    syncUIState()
                })
    }

    private fun syncUIState() {
        var toolbarTitle = mUser.username
        if (UserManager.isSelfById(mUser.id)) {
            follow_unfollow_btn_user_info.visibility = View.GONE
            edit_btn_user_info.visibility = View.VISIBLE
            toolbarTitle += " [Self]"
        } else {
            follow_unfollow_btn_user_info.visibility = View.VISIBLE
            if (mFollowState.isFollowedBy)
                toolbarTitle += " [Now following me]"
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
                .load(UserManager.getAvatarUrl(mUser.avatarHash, 120))
                .apply(GlideOptions.DEF_OPTION)
                .transition(withCrossFade())
                .into(avatar_iv_user_info)
        toolbar_user_info.title = toolbarTitle
    }

    private fun onEditClick() {
        // todo
    }

    private fun onFollowUnfollowClick() {
        Settings.INSTANCE.TokenExpireAt = -1
        follow_unfollow_btn_user_info.isEnabled = false
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    if (mFollowState.isFollowing)
                        ServiceFactory.DEF_SERVICE.unfollowUser(mUser.id, UserManager.getAuthHeaderByToken())
                    else
                        ServiceFactory.DEF_SERVICE.followUser(mUser.id, UserManager.getAuthHeaderByToken())
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    syncState()
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                    follow_unfollow_btn_user_info.isEnabled = true
                }, onComplete = {
                    follow_unfollow_btn_user_info.isEnabled = true
                })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_info, menu)
        menu.findItem(R.id.action_log_out).isVisible = UserManager.self!!.id == mId
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_share ->
                ShareUtil.shareText(this, "Share self link...", UserManager.self!!.userLink)
            R.id.action_log_out -> {
                UserManager.logout()
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

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getFollowers(arguments!!.getInt("id"),
                                    UserManager.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE.getFollowers(UserManager.getAuthHeaderByToken(), current)
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.next, it.list)
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                })
    }
}

class FollowingsRVFragment : BaseRVFragment() {
    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(User::class.java, UserViewBinder(activity!!))
    }

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getFolloweds(arguments!!.getInt("id"),
                                    UserManager.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE.getFolloweds(UserManager.getAuthHeaderByToken(), current)
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.next, it.list)
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
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
        mAdapter.register(Post::class.java, PostViewBinder(activity!!))
    }

    override fun onNoData() {}

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    else
                        if (current == null)
                            ServiceFactory.DEF_SERVICE.getUserPosts(arguments!!.getInt("id"))
                        else
                            ServiceFactory.DEF_SERVICE.getUserPosts(current)
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.next, it.list)
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                })
    }
}
