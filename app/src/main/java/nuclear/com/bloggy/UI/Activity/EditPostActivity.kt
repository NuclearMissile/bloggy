package nuclear.com.bloggy.UI.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_edit.*
import nuclear.com.bloggy.*
import nuclear.com.bloggy.Entity.NewArticle
import nuclear.com.bloggy.Entity.Post
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.ToastUtil
import nuclear.com.bloggy.Util.defaultSchedulers
import nuclear.com.swipeback.activity.SwipeBackActivity
import org.greenrobot.eventbus.EventBus
import ru.noties.markwon.Markwon

class EditPostActivity : SwipeBackActivity() {
    private var mOriginPost: Post? = null
    private var mCurrentText: String = ""
    private var mOriginText: String = ""
    private var plainFlag = true

    companion object {
        fun tryStart(context: Context) {
            if (!UserManager.can(Permission.WRITE)) {
                UserManager.handlePermissionError(context, Permission.WRITE)
                return
            }
            val intent = Intent(context, EditPostActivity::class.java)
            context.startActivity(intent)
        }

        fun tryStart(context: Context, imported: String) {
            if (!UserManager.can(Permission.WRITE)) {
                UserManager.handlePermissionError(context, Permission.WRITE)
                return
            }
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("imported", imported)
            context.startActivity(intent)
        }

        fun tryStart(context: Context, postId: Int) {
            ServiceFactory.DEF_SERVICE
                    .getPostById(postId)
                    .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                    .defaultSchedulers()
                    .subscribeBy(onNext = {
                        if (!UserManager.can(Permission.WRITE)) {
                            UserManager.handlePermissionError(context, Permission.WRITE)
                        } else if (!UserManager.isSelfById(it.authorId)) {
                            LogUtil.w(this, "try to edit a post not by self")
                        } else {
                            val intent = Intent(context, EditPostActivity::class.java)
                            intent.putExtra("oldPost", it)
                            context.startActivity(intent)
                        }
                    }, onError = {
                        LogUtil.e(this, it.message)
                        ToastUtil.showLongToast(it.message)
                        it.printStackTrace()
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
            Markwon.setMarkdown(body_et_edit, mCurrentText)
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
        if (mOriginText != mCurrentText || (mOriginPost == null && !TextUtils.isEmpty(mCurrentText))) {
            MaterialDialog.Builder(this)
                    .title(R.string.text_edit_detected)
                    .content(R.string.text_edit_detected)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .neutralText(R.string.save_as_draft)
                    .onPositive { _, _ -> uploadPost() }
                    .onNegative { _, _ -> finish() }
                    .onNeutral { _, _ ->
                        BaseApplication.draftBox.put(NewArticle(mCurrentText))
                        finish()
                    }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun uploadPost() {
        Flowable.just(1)
                .flatMap {
                    if (!UserManager.isSavedTokenValid)
                        throw TokenInvalidException()
                    else {
                        if (mOriginPost == null)
                            ServiceFactory.DEF_SERVICE
                                    .newPost(NewArticle(mCurrentText), UserManager.getAuthHeaderByToken())
                        else
                            ServiceFactory.DEF_SERVICE
                                    .editPost(NewArticle(mCurrentText), mOriginPost!!.id, UserManager.getAuthHeaderByToken())
                    }
                }
                .retryWhen(UserManager::retryForToken)
                .map { if (it.isSuccess) it.result else throw Exception(it.message) }
                .bindToLifecycle(this)
                .defaultSchedulers()
                .subscribeBy(onNext = {
                    if (mOriginPost == null) {
                        EventBus.getDefault().post(AddPostEvent(it))
                        ToastUtil.showShortToast("upload new post success")
                    } else {
                        EventBus.getDefault().post(PostChangeEvent(mOriginPost!!, it))
                        ToastUtil.showShortToast("edit post success")
                    }
                    finish()
                }, onError = {
                    LogUtil.e(this, it.message)
                    ToastUtil.showLongToast(it.message)
                    it.printStackTrace()
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
