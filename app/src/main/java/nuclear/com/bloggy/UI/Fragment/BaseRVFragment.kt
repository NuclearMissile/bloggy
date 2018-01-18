package nuclear.com.bloggy.UI.Fragment

import android.graphics.Color
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_fragment_layout.*
import me.drakeet.multitype.MultiTypeAdapter
import nuclear.com.bloggy.R
import java.util.*

abstract class BaseRVFragment : BaseFragment() {
    protected val mItems = LinkedList<Any>()
    protected var isRefreshEnabled = true
    protected var isLoadMoreEnabled = true
    protected val mAdapter = MultiTypeAdapter(mItems)

    private var mNext: String? = null

    override fun setLayoutResID(): Int = R.layout.rv_fragment_layout

    override fun setUp() {
        regAdapter(mAdapter)
        recycler_view.layoutManager = setLayoutManager()
        recycler_view.adapter = mAdapter
        recycler_view.itemAnimator = DefaultItemAnimator()
        refresh_layout.setOnRefreshListener { onRefresh() }
        refresh_layout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)
        recycler_view.setOnUpDownAction(onScroll2Bottom = { onLoadMore() })
        loadData(null)
    }

    private fun onLoadMore() {
        if (!isLoadMoreEnabled)
            return
        if (mNext != null)
            loadData(mNext)
        else if (mItems.isNotEmpty())
            onNoMoreData()
    }

    private fun onRefresh() {
        if (!isRefreshEnabled) {
            refresh_layout.isRefreshing = false
            return
        }
        Handler().postDelayed({
            reload()
            refresh_layout.isRefreshing = false
        }, 400)
    }

    abstract fun regAdapter(mAdapter: MultiTypeAdapter)

    protected abstract fun loadData(current: String?)

    open fun reload() = loadData(null)

    open fun addItem(item: Any, toTop: Boolean = true) {
        if (toTop) {
            mItems.addFirst(item)
            recycler_view.smoothScrollToPosition(0)
            mAdapter.notifyItemInserted(0)
        } else {
            mItems.addLast(item)
            mAdapter.notifyItemInserted(mItems.lastIndex)
        }
    }

    open fun changeItem(oldItem: Any, newItem: Any) {
        val index = mItems.indexOf(oldItem)
        if (index == -1)
            return
        else {
            mItems[index] = newItem
            mAdapter.notifyItemChanged(index)
        }
    }

    open fun removeItem(item: Any) {
        val index = mItems.indexOf(item)
        if (index == -1)
            return
        else {
            mItems.removeAt(index)
            mAdapter.notifyItemRemoved(index)
        }
    }

    protected open fun onDataReceived(current: String?, next: String?, items: List<Any>) {
        if (items.isEmpty()) {
            onNoData()
            mItems.clear()
            mAdapter.notifyDataSetChanged()
            return
        }
        if (current == null) {
            mItems.clear()
            mItems.addAll(items)
            mNext = next
            mAdapter.notifyDataSetChanged()
            return
        }
        if (next == null) {
            if (items.last() !in mItems) {
                mItems.addAll(items)
                mAdapter.notifyDataSetChanged()
            } else
                onNoMoreData()
            return
        } else {
            mNext = next
            mItems.addAll(items)
            mAdapter.notifyDataSetChanged()
            return
        }
    }

    protected open fun setLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }

    protected open fun onNoMoreData() {
        Snackbar.make(recycler_view, "No more data", Snackbar.LENGTH_SHORT).show()
    }

    protected open fun onNoData() {
        Snackbar.make(recycler_view, "No data", Snackbar.LENGTH_SHORT).show()
    }

    fun scroll2Top(): Boolean {
        return if (recycler_view.canScrollVertically(-1)) {
            recycler_view.smoothScrollToPosition(0)
            false
        } else
            true
    }
}