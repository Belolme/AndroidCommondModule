package com.billin.com.playground

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.billin.com.playground.databinding.FragmentBottomsheetBehaviorBinding
import com.billin.com.playground.databinding.ItemTextBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val TAG = "BottomSheetBehaviorFrag"

/**
 * BottomSheetBehavior Demo
 *
 * Usage Resource
 * - [Android Bottom Sheet - Android Bits - Medium](https://medium.com/android-bits/android-bottom-sheet-30284293f066)
 *
 * 值得注意的是, [BottomSheetBehavior] 不支持 [ViewPager] 的嵌套, 如果需要支持 [ViewPager] 的嵌套,
 * 需要在 [ViewPager] 页面发生变动的时候调用 [BottomSheetBehavior.findScrollingChild] 方法重新
 * 让 [BottomSheetBehavior] 重新设定可滑动的子 View.
 * [ViewPager] 中使用 [BottomSheetBehavior] 可以参考以下资料.
 * - [Android ViewPager with RecyclerView works incorrectly inside BottomSheet - StackOverflow](https://stackoverflow.com/questions/37715822/android-viewpager-with-recyclerview-works-incorrectly-inside-bottomsheet)
 * - [laenger/ViewPagerBottomSheet - Github](https://github.com/laenger/ViewPagerBottomSheet)
 *
 * > 获取 ViewPager 当前的 View 切换不能通过 [ViewPager.OnPageChangeListener.onPageSelected] 方法监听,
 * 因为这个方法调用的时候, 通过 [PagerAdapter.setPrimaryItem] 获取到的 View 在切换的时候事实还是原来的 View.
 * 需要通过 [ViewPager.OnPageChangeListener.onPageScrollStateChanged] 监听到 [ViewPager.SCROLL_STATE_IDLE]
 * 后再 post 通过 [PagerAdapter.setPrimaryItem] 获取到的 View 才是真正当前的 View.
 */
class BottomSheetBehaviorFragment : Fragment() {

    private lateinit var binding: FragmentBottomsheetBehaviorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentBottomsheetBehaviorBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomSheetRecyclerView.apply {
            binding.root.doOnLayout { root ->
                layoutParams.height = (root.measuredHeight * 4f / 5f).toInt()
                requestLayout()
            }
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<BSTextVH>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text, parent, false)
                        .let { BSTextVH(it) }

                override fun onBindViewHolder(holder: BSTextVH, position: Int) {
                    holder.bind("Item $position")
                }

                override fun getItemCount() = 100
            }
        }
        val bottomSheetBehavior =
            BottomSheetBehavior.from(binding.bottomSheetRecyclerViewContainer).apply {
                // BottomSheetBehavior 总共有 6 种状态. 其中比较常用的状态有
                // STATE_EXPANDED, STATE_HIDDEN, STATE_COLLAPSED.
                // |-----| STATE_EXPANDED 状态的高度为 BottomSheetBehavior 对应控件的高度
                // |     |
                // |-----| STATE_COLLAPSED 状态的高度为 STATE_EXPANDED - peek height
                // |     |
                // STATE_HIDDEN 的高度就是 0 了.
                state = BottomSheetBehavior.STATE_HIDDEN
                isHideable = true
                peekHeight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    0f,
                    resources.displayMetrics
                ).toInt()
                addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.d(TAG, "onStateChanged: $newState")

                        binding.btnCommunity.visibility =
                            if (newState == BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE
                            else View.INVISIBLE
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        Log.d(TAG, "onSlide: $slideOffset ${bottomSheet.top} ${bottomSheet.y}")
                        binding.imageContent.apply {
                            val bottomSheetTop = bottomSheet.top
                            if (layoutParams.height != bottomSheetTop) {
                                layoutParams.height = bottomSheetTop
                                requestLayout()
                            }
                        }
                    }
                })
            }

        binding.btnCommunity.setOnClickListener {
            // show bottom sheet recyclerview
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.imageContent.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }
}

private class BSTextVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemTextBinding.bind(itemView)

    fun bind(text: String) {
        binding.tv.text = text
    }
}