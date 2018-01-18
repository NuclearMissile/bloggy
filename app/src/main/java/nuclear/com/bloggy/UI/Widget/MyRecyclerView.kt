package nuclear.com.bloggy.UI.Widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import nuclear.com.bloggy.Util.LogUtil

class MyRecyclerView : RecyclerView {
    private var mUpDownScrollListener: RecyclerView.OnScrollListener? = null
    private var mLeftRightScrollListener: RecyclerView.OnScrollListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun setOnUpDownAction(onScrollUp: ((dy: Int) -> Unit)? = null,
                          onScrollDown: ((dy: Int) -> Unit)? = null,
                          onScroll2Top: (() -> Unit)? = null,
                          onScroll2Bottom: (() -> Unit)? = null,
                          onScrollStop: (() -> Unit)? = null) {
        if (mUpDownScrollListener != null) {
            LogUtil.w(this, "overwrite UpDownScrollListener")
            removeOnUpDownAction()
        }
        mUpDownScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    onScroll2Top?.invoke()
                else if (!recyclerView.canScrollVertically(1))
                    onScroll2Bottom?.invoke()
                if (dy > 5)
                    onScrollDown?.invoke(dy)
                else if (dy < -5)
                    onScrollUp?.invoke(dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    onScrollStop?.invoke()
            }
        }
        addOnScrollListener(mUpDownScrollListener)
    }

    fun removeOnUpDownAction() {
        removeOnScrollListener(mUpDownScrollListener)
        mUpDownScrollListener = null
    }

    fun setOnLeftRightAction(onScrollLeft: ((dx: Int) -> Unit)? = null,
                             onScrollRight: ((dx: Int) -> Unit)? = null,
                             onScroll2MostLeft: (() -> Unit)? = null,
                             onScroll2MostRight: (() -> Unit)? = null,
                             onScrollStop: (() -> Unit)? = null) {
        if (mLeftRightScrollListener != null) {
            LogUtil.w(this, "overwrite onLeftRightScrollListener")
            removeOnLeftRightAction()
        }
        mLeftRightScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(-1))
                    onScroll2MostLeft?.invoke()
                else if (!recyclerView.canScrollVertically(1))
                    onScroll2MostRight?.invoke()
                if (dx > 5)
                    onScrollRight?.invoke(dx)
                else if (dx < -5)
                    onScrollLeft?.invoke(dx)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    onScrollStop?.invoke()
            }
        }
        addOnScrollListener(mLeftRightScrollListener)
    }

    fun removeOnLeftRightAction() {
        removeOnScrollListener(mLeftRightScrollListener)
        mLeftRightScrollListener = null
    }
}