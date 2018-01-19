package nuclear.com.bloggy.UI.Widget

import android.os.Bundle
import android.view.View
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import nuclear.com.swipeback.SwipeBackLayout
import nuclear.com.swipeback.Utils
import nuclear.com.swipeback.activity.ISwipeBackActivity
import nuclear.com.swipeback.activity.SwipeBackActivityHelper

abstract class SwipeBackRxActivity : RxAppCompatActivity(), ISwipeBackActivity {
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
}