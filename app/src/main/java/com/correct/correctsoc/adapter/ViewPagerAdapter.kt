import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(
    private var fragmentManager: FragmentManager,
    private var lifecycle: Lifecycle,
    private var fragmentsList: List<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getItemCount(): Int {
        return fragmentsList.size
    }
}