package nuclear.com.bloggy.UI.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.rengwuxian.materialedittext.validation.METValidator
import com.rengwuxian.materialedittext.validation.RegexpValidator
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.input_fields_login.*
import kotlinx.android.synthetic.main.title_login.*
import nuclear.com.bloggy.Entity.REST.NewUser
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.R
import nuclear.com.bloggy.UI.Widget.RxSwipeBackActivity
import nuclear.com.bloggy.UserHolder
import nuclear.com.bloggy.Util.*
import nuclear.com.bloggy.handleError
import okhttp3.RequestBody
import java.util.*

class LogInActivity : RxSwipeBackActivity() {

    companion object {
        fun tryStart(context: Context) {
            if (UserHolder.isAnonymous) {
                val intent = Intent(context, LogInActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    private var mDialog: MaterialDialog? = null
    private val DIALOG_DELAY: Long = 400
    private var mState = LoginActivityState.LOG_IN
    private val isInputFieldsValid: Boolean
        get() {
            var res = true
            Arrays.asList(email_field, password_field, repeat_password_field, username_field)
                    .forEach { if (it.visibility == View.VISIBLE && !it.validate()) res = false }
            return res
        }

    private enum class LoginActivityState {
        LOG_IN, SIGN_UP, FORGET_PASSWORD
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT

        email_field.addValidator(RegexpValidator(resources.getString(R.string.email_invalid_message),
                "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$"))
        password_field.addValidator(RegexpValidator(resources.getString(R.string.password_invalid_message),
                "^[A-Za-z0-9]+\$"))
        repeat_password_field.addValidator(RegexpValidator(resources.getString(R.string.password_invalid_message),
                "^[A-Za-z0-9]+\$"))
                .addValidator(object : METValidator(resources.getString(R.string.repeat_password_invalid_message)) {
                    override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {
                        return password_field.text.toString() == text.toString()
                    }
                })
        username_field.addValidator(RegexpValidator(resources.getString(R.string.username_invalid_message),
                "^[A-Za-z0-9]+\$"))

        Glide.with(this)
                .load(R.drawable.logo)
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(logo_iv_log_in)

        action_iv_login.setOnClickListener { onAction() }
        forget_password_tv.setOnClickListener { onForget() }
        login_back_iv.setOnClickListener { finish() }
        sign_up_or_login_tv.setOnClickListener { onSignUpOrLogIn() }
    }

    private fun showDialog(title: String, content: String) {
        mDialog = MaterialDialog.Builder(this)
                .progress(true, 0)
                .cancelable(false)
                .title(title)
                .content(content)
                .show()
    }

    private fun showDialog(titleId: Int, contentId: Int) {
        showDialog(resources.getString(titleId), resources.getString(contentId))
    }

    private fun closeDialog() {
        if (mDialog?.isShowing == true)
            mDialog?.dismiss()
    }

    private fun uiResetTo(toState: LoginActivityState) {
        if (mState == toState)
            return
        when (toState) {
            LoginActivityState.LOG_IN -> {
                password_field.visibility = View.VISIBLE
                repeat_password_field.visibility = View.GONE
                username_field.visibility = View.GONE

                action_iv_login.setImageResource(R.drawable.chevron_right)
                title_login_tv.text = resources.getString(R.string.login_title)
                forget_password_tv.text = resources.getString(R.string.forget_password)
                sign_up_or_login_tv.text = resources.getString(R.string.sign_up)
                mState = toState
            }
            LoginActivityState.SIGN_UP -> {
                password_field.visibility = View.VISIBLE
                repeat_password_field.visibility = View.VISIBLE
                username_field.visibility = View.VISIBLE

                action_iv_login.setImageResource(R.drawable.plus_small)
                title_login_tv.text = resources.getString(R.string.login_title_sign_up)
                forget_password_tv.text = resources.getString(R.string.forget_password_back_to_login)
                sign_up_or_login_tv.text = resources.getString(R.string.log_in)
                mState = toState
            }
            LoginActivityState.FORGET_PASSWORD -> {
                password_field.visibility = View.GONE
                repeat_password_field.visibility = View.GONE
                username_field.visibility = View.GONE

                action_iv_login.setImageResource(R.drawable.ic_done_white_128dp)
                title_login_tv.text = resources.getString(R.string.login_title_forget_password)
                forget_password_tv.text = resources.getString(R.string.forget_password_back_to_login)
                sign_up_or_login_tv.text = resources.getString(R.string.log_in)
                mState = toState
            }
        }
    }

    private fun onAction() {
        val set = AnimatorSet().setDuration(500)
        val objX = ObjectAnimator.ofFloat(action_iv_login, "scaleX", 1.0f, 0.8f, 1.0f)
        val objY = ObjectAnimator.ofFloat(action_iv_login, "scaleY", 1.0f, 0.8f, 1.0f)
        set.playTogether(objX, objY)
        set.interpolator = AccelerateInterpolator()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (!isInputFieldsValid) {
                    ToastUtil.showShortToast(R.string.input_fields_invalid_message)
                    return
                }
                when (mState) {
                    LoginActivityState.LOG_IN -> tryLogIn()
                    LoginActivityState.SIGN_UP -> trySignUp()
                    LoginActivityState.FORGET_PASSWORD -> tryForgetPassword()
                }
            }
        })
        set.start()
    }

    private fun onSignUpOrLogIn() {
        when (mState) {
            LoginActivityState.LOG_IN -> uiResetTo(LoginActivityState.SIGN_UP)
            LoginActivityState.SIGN_UP, LoginActivityState.FORGET_PASSWORD -> uiResetTo(LoginActivityState.LOG_IN)
        }
    }

    private fun onForget() {
        when (mState) {
            LoginActivityState.LOG_IN -> uiResetTo(LoginActivityState.FORGET_PASSWORD)
            LoginActivityState.SIGN_UP, LoginActivityState.FORGET_PASSWORD -> uiResetTo(LoginActivityState.LOG_IN)
        }
    }

    private fun tryLogIn() {
        val email = email_field.text.toString()
        val password = password_field.text.toString()
        val authHeader = OkHttpUtil.genAuthHeader(email, password)
        showDialog(R.string.progress_dialog_login_title, R.string.progress_dialog_content)

        Handler().postDelayed({
            ServiceFactory.DEF_SERVICE
                    .login(OkHttpUtil.genAuthHeader(email, password))
                    .checkApiError()
                    .defaultSchedulers()
                    .bindToLifecycle(this)
                    .subscribeBy(onNext = {
                        UserHolder.login(it.result, password)
                        LogUtil.i(this, "User(${it.result.username}, $${it.result.id}) log in success")
                        finish()
                    }, onError = {
                        handleError(this, it)
                        closeDialog()
                    }, onComplete = { closeDialog() })
        }, DIALOG_DELAY)
    }

    private fun trySignUp() {
        showDialog(R.string.progress_dialog_register_title, R.string.progress_dialog_content)
        val email = email_field.text.toString()
        val password = password_field.text.toString()
        val username = username_field.text.toString()
        val json = Gson().toJson(NewUser(email, password, username, null))
        val requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), json)
        Handler().postDelayed({
            ServiceFactory.DEF_SERVICE
                    .register(requestBody)
                    .checkApiError()
                    .bindToLifecycle(this)
                    .defaultSchedulers()
                    .subscribeBy(onNext = {
                        UserHolder.login(it.result, password)
                        LogUtil.i(this, "User(${it.result.username}, $${it.result.id}) sign up success")
                        finish()
                    }, onError = {
                        handleError(this, it)
                        closeDialog()
                    }, onComplete = { closeDialog() })
        }, DIALOG_DELAY)
    }

    private fun tryForgetPassword() {
        showDialog(R.string.progress_dialog_register_title, R.string.progress_dialog_content)
        val email = email_field.text.toString()
        Handler().postDelayed({
            ServiceFactory.DEF_SERVICE
                    .resetPassword(email)
                    .checkApiError()
                    .defaultSchedulers()
                    .bindToLifecycle(this)
                    .subscribeBy(onNext = {
                        ToastUtil.showShortToast(it.message)
                        LogUtil.i(this, it.message)
                    }, onError = {
                        handleError(this, it)
                        closeDialog()
                    }, onComplete = {
                        closeDialog()
                    })
        }, DIALOG_DELAY)
    }
}
