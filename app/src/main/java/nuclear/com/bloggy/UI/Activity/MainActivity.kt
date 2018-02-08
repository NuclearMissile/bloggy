package nuclear.com.bloggy.UI.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.MultiTypeAdapter
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.REST.NewArticle
import nuclear.com.bloggy.Entity.REST.Post
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.Service.WebSocketService
import nuclear.com.bloggy.UI.DraftViewBinder
import nuclear.com.bloggy.UI.Fragment.BaseRVFragment
import nuclear.com.bloggy.UI.Fragment.IPostFragment
import nuclear.com.bloggy.UI.PostViewBinder
import nuclear.com.bloggy.UI.Widget.RxSwipeBackActivity
import nuclear.com.bloggy.Util.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*

class MainActivity : RxSwipeBackActivity(), NavigationView.OnNavigationItemSelectedListener, FileChooserDialog.FileCallback {
    private val MAX_DOUBLE_BACK_DURATION: Long = 1000
    private var lastBackPressed: Long = 0
    private lateinit var mCurrentFragment: BaseRVFragment
    private lateinit var avatarImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        avatarImageView = nav_view.getHeaderView(0).findViewById(R.id.avatar_header_iv)
        usernameTextView = nav_view.getHeaderView(0).findViewById(R.id.username_header_tv)
        emailTextView = nav_view.getHeaderView(0).findViewById(R.id.email_header_tv)
        setSupportActionBar(toolbar)
        setSwipeBackEnable(false)

        supportActionBar?.setTitle(R.string.all_post)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        avatarImageView.setOnClickListener { onAvatarClick() }
        fab_main.setOnClickListener { onFabClick() }
        replaceFragment(PostsRVFragment())
    }

    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        syncUIState()
    }

    private fun replaceFragment(fragment: BaseRVFragment) {
        mCurrentFragment = fragment
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_main, mCurrentFragment)
                .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    FileChooserDialog.Builder(this)
                            .initialPath(Environment.getExternalStorageDirectory().path)
                            .extensionsFilter(".md", ".txt")
                            .show(this)
                else
                    ToastUtil.showShortToast("permission denied")
            }
        }
    }

    private fun onFabClick() {
        if (UserHolder.isAnonymous) {
            LogInActivity.tryStart(this)
            return
        }
        val adapter = MaterialSimpleListAdapter { dialog, index, _ ->
            when (index) {
                0 -> EditPostActivity.tryStart(this)
                1 -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        val array = ArrayList<String>()
                        array.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        ActivityCompat.requestPermissions(this, array.toTypedArray(), 1)
                    } else {
                        FileChooserDialog.Builder(this)
                                .initialPath(Environment.getExternalStorageDirectory().path)
                                .extensionsFilter(".md", ".txt")
                                .show(this)
                    }
                }
            }
            dialog.dismiss()
        }
        adapter.add(MaterialSimpleListItem.Builder(this)
                .content(R.string.create_new_post)
                .backgroundColor(Color.WHITE)
                .build())
        adapter.add(MaterialSimpleListItem.Builder(this)
                .content(R.string.import_from_file)
                .backgroundColor(Color.WHITE)
                .build())
        MaterialDialog.Builder(this)
                .title(R.string.new_post)
                .adapter(adapter, null)
                .show()
    }

    private fun onAvatarClick() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        UserInfoActivity.tryStart(this, UserHolder.currUser?.id)
    }

    private fun syncUIState() {
        if (UserHolder.isAnonymous) {
            Glide.with(this)
                    .load(R.mipmap.ic_launcher_round)
                    .apply(GlideOptions.DEF_OPTION)
                    .transition(withCrossFade())
                    .into(avatarImageView)
            usernameTextView.text = resources.getString(R.string.default_username_header)
            emailTextView.text = resources.getString(R.string.default_email_header)
            nav_view.setCheckedItem(R.id.nav_all)
            onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_all))
        } else {
            Glide.with(this)
                    .load(UserHolder.getAvatarUrl(128))
                    .apply(GlideOptions.DEF_OPTION)
                    .transition(withCrossFade())
                    .into(avatarImageView)
            usernameTextView.text = UserHolder.currUser!!.username
            emailTextView.text = UserHolder.currUser!!.email
        }
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {}

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        EditPostActivity.tryStart(this, file.readText())
        dialog.dismiss()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (mCurrentFragment.scroll2Top()) {
                val current = System.currentTimeMillis()
                if (current - lastBackPressed > MAX_DOUBLE_BACK_DURATION) {
                    ToastUtil.showShortToast(R.string.on_back_press_hint)
                    lastBackPressed = current
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                SettingsActivity.tryStart(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> SettingsActivity.tryStart(this)
            R.id.nav_all -> {
                supportActionBar?.setTitle(R.string.all_post)
                if (mCurrentFragment is PostsRVFragment)
                    mCurrentFragment.scroll2Top()
                else
                    replaceFragment(PostsRVFragment())
            }
            R.id.nav_time_line -> {
                if (UserHolder.isAnonymous) {
                    LogInActivity.tryStart(this)
                    drawer_layout.closeDrawer(GravityCompat.START)
                    return false
                }
                supportActionBar?.setTitle(R.string.timeline)
                if (mCurrentFragment is TimelineRVFragment)
                    mCurrentFragment.scroll2Top()
                else
                    replaceFragment(TimelineRVFragment())
            }
            R.id.nav_favorite -> {
                if (UserHolder.isAnonymous) {
                    LogInActivity.tryStart(this)
                    drawer_layout.closeDrawer(GravityCompat.START)
                    return false
                }
                supportActionBar?.setTitle(R.string.favorite)
                if (mCurrentFragment is FavoriteFragment)
                    mCurrentFragment.scroll2Top()
                else
                    replaceFragment(FavoriteFragment())
            }
            R.id.nav_draft -> {
                if (UserHolder.isAnonymous) {
                    LogInActivity.tryStart(this)
                    drawer_layout.closeDrawer(GravityCompat.START)
                    return false
                }
                supportActionBar?.setTitle(R.string.draft)
                if (mCurrentFragment is DraftFragment)
                    mCurrentFragment.scroll2Top()
                else
                    replaceFragment(DraftFragment())
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}

class DraftFragment : BaseRVFragment() {

    @Subscribe
    fun onAddDraft(event: AddDraftEvent) = addItem(event.draft)

    @Subscribe
    fun onRemoveDraft(event: RemoveDraftEvent) = removeItem(event.draft)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(NewArticle::class.java, DraftViewBinder(activity!!))
    }

    override fun setUp() {
        super.setUp()
        isLoadMoreEnabled = false
    }

    override fun loadData(current: String?) {
        val drafts = LinkedList<NewArticle>()
        BaseApplication.draftBox.all
                .toFlowable()
                .defaultSchedulers()
                .bindToLifecycle(this)
                .subscribeBy(onNext = {
                    drafts.add(it)
                }, onError = {
                    handleError(this, it)
                }, onComplete = {
                    mItems.clear()
                    mItems.addAll(drafts.sortedByDescending { it.timeStamp })
                    mAdapter.notifyDataSetChanged()
                })
    }
}

class FavoriteFragment : BaseRVFragment(), IPostFragment {

    @Subscribe
    override fun onAddPost(event: AddPostEvent) = addItem(event.post)

    @Subscribe
    override fun onRemovePost(event: RemovePostEvent) = removeItem(event.post)

    @Subscribe
    override fun onUpdatePost(changeEvent: ChangePostEvent) = changeItem(changeEvent.oldPost, changeEvent.newPost)

    @Subscribe
    fun onRemoveFavorite(event: RemoveFavoriteEvent) {
        val post = mItems.firstOrNull { event.favoritePost.postId == (it as? Post)?.id } ?: return
        val index = mItems.indexOf(post)
        mItems.remove(post)
        mAdapter.notifyItemRemoved(index)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun setUp() {
        super.setUp()
        isLoadMoreEnabled = false
    }

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(Post::class.java, PostViewBinder(this.activity!!, this))
    }

    override fun loadData(current: String?) {
        val posts = LinkedList<Post>()
        val allFavorite = BaseApplication.favoritePostBox.all
        allFavorite.toFlowable()
                .flatMap { ServiceFactory.DEF_SERVICE.getPostById(it.postId) }
                .filter { it.statusCode != 404 }
                .checkApiError()
                .defaultSchedulers()
                .bindToLifecycle(this)
                .subscribeBy(onNext = {
                    posts.add(it.result)
                }, onError = {
                    handleError(this, it)
                }, onComplete = {
                    BaseApplication.favoritePostBox.remove(allFavorite.filter { it.postId !in posts.map { it.id } })
                    mItems.clear()
                    mItems.addAll(posts.sortedByDescending { it.timeStamp })
                    mAdapter.notifyDataSetChanged()
                })
    }
}

class TimelineRVFragment : BaseRVFragment(), IPostFragment {

    @Subscribe
    override fun onAddPost(event: AddPostEvent) = addItem(event.post)

    @Subscribe
    override fun onRemovePost(event: RemovePostEvent) = removeItem(event.post)

    @Subscribe
    override fun onUpdatePost(changeEvent: ChangePostEvent) = changeItem(changeEvent.oldPost, changeEvent.newPost)

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(Post::class.java, PostViewBinder(this.activity!!, this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun loadData(current: String?) {
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    if (current == null)
                        ServiceFactory.DEF_SERVICE.getTimeline(UserHolder.getAuthHeaderByToken())
                    else
                        ServiceFactory.DEF_SERVICE.getTimeline(UserHolder.getAuthHeaderByToken(), current)
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

class PostsRVFragment : BaseRVFragment(), IPostFragment {

    @Subscribe
    override fun onAddPost(event: AddPostEvent) = addItem(event.post)

    @Subscribe
    override fun onRemovePost(event: RemovePostEvent) = removeItem(event.post)

    @Subscribe
    override fun onUpdatePost(changeEvent: ChangePostEvent) = changeItem(changeEvent.oldPost, changeEvent.newPost)

    override fun regAdapter(mAdapter: MultiTypeAdapter) {
        mAdapter.register(Post::class.java, PostViewBinder(activity!!, this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun loadData(current: String?) {
        val service = if (current == null)
            ServiceFactory.DEF_SERVICE.getAllPosts()
        else
            ServiceFactory.DEF_SERVICE.getAllPosts(current)
        service.checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    onDataReceived(current, it.result.next, it.result.list)
                }, onError = {
                    handleError(this, it)
                })
    }
}

