package nuclear.com.bloggy.UI.Activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import me.drakeet.multitype.Items
import me.drakeet.support.about.AbsAboutActivity
import nuclear.com.swipeback.SwipeBackLayout
import nuclear.com.swipeback.Utils
import nuclear.com.swipeback.activity.ISwipeBackActivity
import nuclear.com.swipeback.activity.SwipeBackActivityHelper

class AboutActivity : AbsAboutActivity(), ISwipeBackActivity {
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}