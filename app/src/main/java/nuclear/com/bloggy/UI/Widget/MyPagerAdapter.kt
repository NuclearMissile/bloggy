package nuclear.com.bloggy.UI.Widget

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.SparseArray
import android.view.ViewGroup

abstract class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val mFragments = SparseArray<Fragment>()

    override abstract fun getCount(): Int

    override abstract fun getItem(position: Int): Fragment

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val f = super.instantiateItem(container, position) as Fragment
        mFragments.put(position, f)
        return f
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        mFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getFragmentInstance(index: Int): Fragment? = mFragments.get(index)
}