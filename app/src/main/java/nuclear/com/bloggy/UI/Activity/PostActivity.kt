package nuclear.com.bloggy.UI.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.rengwuxian.materialedittext.MaterialEditText
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_post.*
import me.drakeet.multitype.MultiTypeAdapter
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.*
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.CommentViewBinder
import nuclear.com.bloggy.UI.Fragment.BaseRVFragment
import nuclear.com.bloggy.UI.PostContentViewBinder
import nuclear.com.bloggy.UI.UserViewBinder
import nuclear.com.bloggy.UI.Widget.SwipeBackRxActivity
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.ShareUtil
import nuclear.com.bloggy.Util.ToastUtil
import nuclear.com.bloggy.Util.defaultSchedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PostActivity : SwipeBackRxActivity() {
    private lateinit var mPost: Post
    private var mFavoritePost: FavoritePost? = null
        get() = BaseApplication.favoritePostBox.query()
                .equal(FavoritePost_.postId, mPost.id.toLong())
                .build()
                .findFirst()

    companion object {
        fun tryStart(context: Context, postId: Int) {
            ServiceFactory.DEF_SERVICE
                    .getPostById(postId)
                    .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                    .defaultSchedulers()
                    .subscribeBy(onNext = {
                        val intent = Intent(context, PostActivity::class.java)
                        intent.putExtra("post", it)
                        context.startActivity(intent)
                    }, onError = {
                        LogUtil.e(this, it.message)
                        ToastUtil.showLongToast(it.message)
                        it.printStackTrace()
                    })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        setSupportActionBar(toolbar_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.post)
        fab_post.setOnClickListener { onFabClick() }
        mPost = intent.getParcelableExtra("post")!!
        initViewPager()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (UserManager.isSelfById(mPost.authorId))
            Handler().postDelayed({
                fab_post.show()
            }, 200)
    }

    private fun onFabClick() {
        when (tabs_post.selectedTabPosition) {
            0 -> EditPostActivity.tryStart(this, mPost.id)
            1 -> {
                MaterialDialog.Builder(this)
                        .title(R.string.add_comment)
                        .customView(R.layout.comment_input_dialog, false)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .cancelable(false)
                        .onPositive({ dialog, _ ->
                            val met: MaterialEditText = (dialog.customView as FrameLayout).findViewById(R.id.met_input_dialog)
                            if (TextUtils.isEmpty(met.text) || !met.validate()) {
                                return@onPositive
                            }
                            Flowable.just(1)
                                    .flatMap {
                                        if (!UserManager.isSavedTokenValid)
                                            throw TokenInvalidException()
                                        else
                                            ServiceFactory.DEF_SERVICE.newComment(mPost.id, NewArticle(met.text.toString()),
                                                    UserManager.getAuthHeaderByToken())
                                    }
                                    .retryWhen(UserManager::retryForToken)
                                    .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                                    .bindToLifecycle(this)
                                    .defaultSchedulers()
                                    .subscribeBy(onNext = {
                                        EventBus.getDefault().post(AddCommentEvent(it))
                                        dialog.dismiss()
                                    }, onError = {
                                        LogUtil.e(this, it.message)
                                        ToastUtil.showLongToast(it.message)
                                        it.printStackTrace()
                                    })
                        })
                        .show()
            }
        }
    }

    private fun deletePost() {
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    else
                        ServiceFactory.DEF_SERVICE.deletePost(mPost.id, UserManager.getAuthHeaderByToken())
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it else throw Exception(it.message) }
                .defaultSchedulers()
                .bindToLifecycle(this)
                .subscribeBy(onNext = {
                    EventBus.getDefault().post(RemovePostEvent(mPost))
                    ToastUtil.showShortToast(R.string.post_deleted)
                    finish()
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                })
    }

    private fun initViewPager() {
        view_pager_post.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                val bundle = Bundle()
                bundle.putParcelable("post", mPost)
                val f = when (position) {
                    0 -> PostMarkDownFragment()
                    1 -> CommentFragment()
                    else -> throw IllegalArgumentException("illegal position: $position")
                }
                f.arguments = bundle
                return f
            }

            override fun getCount(): Int = 2
        }
        view_pager_post.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs_post))
        tabs_post.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view_pager_post))
        tabs_post.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        supportActionBar?.setTitle(R.string.post)
                        if (UserManager.isSelfById(mPost.authorId)) {
                            fab_post.setImageResource(R.drawable.pencil)
                            fab_post.show()
                        } else
                            fab_post.hide()
                    }
                    1 -> {
                        supportActionBar?.setTitle(R.string.comment)
                        if (UserManager.can(Permission.COMMENT)) {
                            fab_post.setImageResource(R.drawable.message)
                            fab_post.show()
                        } else
                            fab_post.hide()
                    }
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.delete_post).isVisible = UserManager.isAdmin
        menu.findItem(R.id.edit_post).isVisible = UserManager.isSelfById(mPost.authorId)
        menu.findItem(R.id.delete_post).isVisible = UserManager.isSelfById(mPost.authorId)
        menu.findItem(R.id.favorite_post).isVisible = !UserManager.isAnonymous
        menu.findItem(R.id.favorite_post).isChecked = mFavoritePost != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_post -> {
                ShareUtil.shareText(this, "Share post's link...", mPost.link)
                return true
            }
            R.id.edit_post -> {
                EditPostActivity.tryStart(this, mPost.id)
                return true
            }
            R.id.favorite_post -> {
                if (mFavoritePost == null) {
                    val f = FavoritePost(mPost.id)
                    BaseApplication.favoritePostBox.put(f)
                    // EventBus.getDefault().post(AddFavoriteEvent(f))
                } else {
                    val f = mFavoritePost!!
                    BaseApplication.favoritePostBox.remove(f)
                    EventBus.getDefault().post(RemoveFavoriteEvent(f))
                }
                return true
            }
            R.id.delete_post -> {
                MaterialDialog.Builder(this)
                        .title(R.string.delete_confirm)
                        .content(R.string.delete_confirm_content)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .onPositive { _, _ -> deletePost() }
                        .show()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class PostMarkDownFragment : BaseRVFragment() {
    private lateinit var mPost: Post

    @Subscribe
    fun onPostChange(event: ChangePostEvent) = changeItem(event.oldPost, event.newPost)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun setUp() {
        isLoadMoreEnabled = false
        mPost = arguments!!.getParcelable("post")
        super.setUp()
    }

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(User::class.java, UserViewBinder(activity!!))
        mAdapter.register(Post::class.java, PostContentViewBinder())
    }

    override fun reload() {
        ServiceFactory.DEF_SERVICE
                .getPostById(mPost.id)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    changeItem(mPost, it)
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                    // jh
                })
    }

    override fun loadData(current: String?) {
        ServiceFactory.DEF_SERVICE
                .getUserById(mPost.authorId)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    addItem(it)
                    addItem(mPost, false)
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                })
    }
}

class CommentFragment : BaseRVFragment() {
    private lateinit var mPost: Post

    @Subscribe
    fun onRemoveCommnet(event: RemoveCommentEvent) = removeItem(event.comment)

    @Subscribe
    fun onAddComment(event: AddCommentEvent) = addItem(event.comment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun setUp() {
        mPost = arguments?.getParcelable("post")!!
        super.setUp()
    }

    override fun onNoData() {}

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(Comment::class.java, CommentViewBinder(activity!!))
    }

    override fun loadData(current: String?) {
        ServiceFactory.DEF_SERVICE
                .getPostComments(mPost.id)
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