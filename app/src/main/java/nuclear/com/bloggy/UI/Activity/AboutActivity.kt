package nuclear.com.bloggy.UI.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import me.drakeet.multitype.Items
import me.drakeet.support.about.AbsAboutActivity
import me.drakeet.support.about.Category
import me.drakeet.support.about.Contributor
import me.drakeet.support.about.License
import nuclear.com.bloggy.R
import nuclear.com.bloggy.Util.GlideOptions
import nuclear.com.swipeback.SwipeBackLayout
import nuclear.com.swipeback.Utils
import nuclear.com.swipeback.activity.ISwipeBackActivity
import nuclear.com.swipeback.activity.SwipeBackActivityHelper

class AboutActivity : AbsAboutActivity(), ISwipeBackActivity {
    companion object {
        fun tryStart(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            if (context !is Activity)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private lateinit var mHelper: SwipeBackActivityHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper.onPostCreate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : View> findViewById(id: Int): T? {
        val v: T? = super.findViewById(id)
        return v ?: mHelper.findViewById(id) as? T
    }

    override fun getSwipeBackLayout(): SwipeBackLayout = mHelper.swipeBackLayout

    override fun setSwipeBackEnable(enable: Boolean) = swipeBackLayout.setEnableGesture(enable)

    override fun scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this)
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onItemsCreated(items: Items) {
        items.add(Category("Developers"))
        items.add(Contributor(R.drawable.logo, "NuclearMissile", "Android Developer", "https://github.com/NuclearMissile"))

        items.add(Category("Open Source Licenses"))
        items.add(License("MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"))
        items.add(License("about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"))

        items.add(License("MaterialEditText", "rengwuxian", License.APACHE_2, "https://github.com/rengwuxian/MaterialEditText"))
        items.add(License("material-dialogs", "afollestad", License.MIT, "https://github.com/afollestad/material-dialogs"))
        items.add(License("Fancybuttons", "medyo", License.MIT, "https://github.com/medyo/Fancybuttons"))
        items.add(License("Markwon", "noties", License.APACHE_2, "https://github.com/noties/Markwon"))
        items.add(License("objectbox-java", "objectbox", License.APACHE_2, "https://github.com/objectbox/objectbox-java"))
        items.add(License("EventBus", "greenrobot", License.APACHE_2, "https://github.com/greenrobot/EventBus"))
        items.add(License("gson", "google", License.APACHE_2, "https://github.com/google/gson"))
        items.add(License("okhttp", "square", License.APACHE_2, "https://github.com/square/okhttp"))
        items.add(License("retrofit", "square", License.APACHE_2, "https://github.com/square/retrofit"))
        items.add(License("RxJava", "ReactiveX", License.APACHE_2, "https://github.com/ReactiveX/RxJava"))
        items.add(License("RxLifecycle", "trello", License.APACHE_2, "https://github.com/trello/RxLifecycle"))
        items.add(License("glide", "bumptech", License.APACHE_2, "https://github.com/bumptech/glide"))
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        Glide.with(this)
                .load(R.drawable.logo)
                .apply(GlideOptions.DEF_OPTION)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(icon)
        slogan.text = "Bloggy"
        version.text = "v 1.0"
    }
}