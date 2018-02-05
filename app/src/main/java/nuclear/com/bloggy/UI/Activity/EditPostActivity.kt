package nuclear.com.bloggy.UI.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_edit.*
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.REST.NewArticle
import nuclear.com.bloggy.Entity.REST.Post
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.UI.Widget.RxSwipeBackActivity
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.ToastUtil
import nuclear.com.bloggy.Util.checkApiError
import nuclear.com.bloggy.Util.defaultSchedulers
import org.greenrobot.eventbus.EventBus
import ru.noties.markwon.Markwon

class EditPostActivity : RxSwipeBackActivity() {
    private var mOriginPost: Post? = null
    private var mCurrentText = ""
    private var mOriginText = ""
    private var plainFlag = true
    private var editedFlag = false
        get() = mCurrentText != mOriginText && !TextUtils.isEmpty(mCurrentText)

    companion object {
        fun tryStart(context: Context) {
            if (!UserHolder.can(Permission.WRITE)) {
                UserHolder.handlePermissionError(context, Permission.WRITE)
                return
            }
            val intent = Intent(context, EditPostActivity::class.java)
            context.startActivity(intent)
        }

        fun tryStart(context: Context, imported: String) {
            if (!UserHolder.can(Permission.WRITE)) {
                UserHolder.handlePermissionError(context, Permission.WRITE)
                return
            }
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("imported", imported)
            context.startActivity(intent)
        }

        fun tryStart(context: Context, postId: Int) {
            ServiceFactory.DEF_SERVICE
                    .getPostById(postId)
                    .checkApiError()
                    .defaultSchedulers()
                    .subscribeBy(onNext = {
                        if (!UserHolder.can(Permission.WRITE)) {
                            UserHolder.handlePermissionError(context, Permission.WRITE)
                        } else if (!UserHolder.isSelfById(it.result.authorId)) {
                            LogUtil.w(this, "try to edit a post not by currUser")
                        } else {
                            val intent = Intent(context, EditPostActivity::class.java)
                            intent.putExtra("oldPost", it.result)
                            context.startActivity(intent)
                        }
                    }, onError = {
                        handleError(this, it)
                    })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(toolbar_edit)
        setSwipeBackEnable(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fab_edit.setOnClickListener { onFabClick() }
        supportActionBar?.setTitle(R.string.edit_post)

        mOriginPost = intent.getParcelableExtra("oldPost")
        mOriginText = mOriginPost?.body ?: intent.getStringExtra("imported") ?: ""
        mCurrentText = mOriginText
        body_et_edit.setText(mCurrentText)

        body_et_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (plainFlag)
                    mCurrentText = s.toString()
                supportActionBar?.title = resources.getString(R.string.edit_post) + if (editedFlag) "*" else ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun onFabClick() {
        if (plainFlag) {
            plainFlag = false
            supportActionBar?.setTitle(R.string.preview_markdown)
            fab_edit.setImageResource(R.drawable.pencil)
            Markwon.setMarkdown(body_et_edit, mCurrentText.toString())
            body_et_edit.isFocusable = false
            body_et_edit.isFocusableInTouchMode = false
        } else {
            plainFlag = true
            supportActionBar?.setTitle(R.string.edit_post)
            fab_edit.setImageResource(R.drawable.eye_outline)
            body_et_edit.setText(mCurrentText)
            body_et_edit.isFocusable = true
            body_et_edit.isFocusableInTouchMode = true
        }
    }

    override fun onBackPressed() {
        if (editedFlag) {
            MaterialDialog.Builder(this)
                    .title(R.string.text_edit_detected)
                    .content(R.string.edit_detected)
                    .positiveText(R.string.upload_post)
                    .negativeText(R.string.exit_without_saving)
                    .onPositive { _, _ -> uploadPost() }
                    .onNegative { _, _ -> finish() }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun uploadPost() {
        Flowable.just(1)
                .flatMap {
                    if (!UserHolder.isSavedTokenValid)
                        throw TokenInvalidError()
                    else {
                        if (mOriginPost == null)
                            ServiceFactory.DEF_SERVICE
                                    .newPost(NewArticle(mCurrentText.toString()), UserHolder.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE
                                    .editPost(NewArticle(mCurrentText.toString()), mOriginPost!!.id, UserHolder.getAuthHeaderByToken())
                    }
                }
                .retryWhen(UserHolder::retryForToken)
                .checkApiError()
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    if (mOriginPost == null) {
                        EventBus.getDefault().post(AddPostEvent(it.result))
                        ToastUtil.showShortToast("upload new post success")
                    } else {
                        EventBus.getDefault().post(ChangePostEvent(mOriginPost!!, it.result))
                        ToastUtil.showShortToast("edit post success")
                    }
                    finish()
                }, onError = {
                    handleError(this, it)
                })
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.new_post_edit).isVisible = mOriginPost == null && editedFlag
        menu.findItem(R.id.edit_post_edit).isVisible = mOriginPost != null && editedFlag
        menu.findItem(R.id.new_draft_edit).isVisible = editedFlag
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.new_post_edit, R.id.edit_post -> uploadPost()
            R.id.new_draft_edit -> {
                val draft = NewArticle(mCurrentText)
                BaseApplication.draftBox.put(draft)
                EventBus.getDefault().post(AddDraftEvent(draft))
                finish()
            }
            R.id.exit_edit -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
