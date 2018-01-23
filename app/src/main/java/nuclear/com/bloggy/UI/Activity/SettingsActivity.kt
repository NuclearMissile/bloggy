package nuclear.com.bloggy.UI.Activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.text.InputType
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import nuclear.com.bloggy.Network.ServiceFactory
import nuclear.com.bloggy.R
import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.UserManager
import nuclear.com.bloggy.Util.GlideOptions
import nuclear.com.bloggy.Util.ToastUtil
import nuclear.com.bloggy.Util.clearAllCache
import nuclear.com.bloggy.Util.getFormatCacheSize
import nuclear.com.swipeback.activity.SwipeBackActivity

class SettingsActivity : SwipeBackActivity() {
    companion object {
        fun tryStart(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_settings)
        fragmentManager.beginTransaction()
                .replace(R.id.frame_settings_activity, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
    }

    override fun onStart() {
        super.onStart()
        refreshUserInfo()
        refreshCacheInfo()
        refreshBaseUrl()
        refreshAboutApp()
    }

    private fun findPreference(id: Int): Preference = super.findPreference(resources.getString(id))!!

    private fun refreshCacheInfo() {
        val pref = findPreference(R.string.cache_info)
        pref.title = "Cache Size: ${getFormatCacheSize(activity)}"
        pref.summary = resources.getString(R.string.click_to_clear_cache)
        pref.setOnPreferenceClickListener {
            clearAllCache(activity)
            ToastUtil.showShortToast(R.string.app_cache_clear)
            pref.title = "Cache Size: ${getFormatCacheSize(activity)}"
            true
        }
    }

    private fun refreshBaseUrl() {
        val pref = findPreference(R.string.base_url)
        pref.title = "Base Url"
        pref.summary = Settings.INSTANCE.BaseUrl
        pref.setOnPreferenceClickListener {
            MaterialDialog.Builder(activity)
                    .title("Base Url")
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .input(null, Settings.INSTANCE.BaseUrl, { _, _ -> return@input })
                    .positiveText(R.string.ok)
                    .onPositive { dialog, _ ->
                        pref.summary = dialog.inputEditText?.text
                        Settings.INSTANCE.BaseUrl = pref.summary.toString()
                        ServiceFactory.refresh()
                    }.show()
            true
        }
    }

    private fun refreshAboutApp() {
        val pref = findPreference(R.string.about_bloggy)
        pref.title = resources.getString(R.string.about_bloggy)
        pref.setOnPreferenceClickListener {
            AboutActivity.tryStart(activity)
            true
        }
    }

    private fun refreshUserInfo() {
        val pref = findPreference(R.string.user_info)
        if (UserManager.isAnonymous) {
            pref.title = resources.getString(R.string.anonymous)
            pref.summary = resources.getString(R.string.click_to_login)
            pref.setOnPreferenceClickListener {
                LogInActivity.tryStart(activity)
                true
            }
        } else {
            pref.title = UserManager.self!!.username
            pref.summary = UserManager.self!!.email
            pref.setOnPreferenceClickListener {
                UserInfoActivity.tryStart(activity, UserManager.self!!.id)
                true
            }
            Glide.with(context)
                    .load(UserManager.getAvatarUrl(UserManager.self!!.avatarHash, 120))
                    .apply(GlideOptions.DEF_OPTION)
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            pref.icon = resource
                        }
                    })
        }
    }
}