package com.lws.infinitelooprv

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 无限循环的水平滚动LayoutManager
 *
 * https://juejin.im/post/5cfa198ff265da1b8c197c2f
 */
class LooperLayoutManager : RecyclerView.LayoutManager() {

    private var looperEnable = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount <= 0) {
            return
        }
        //如果当前是准备状态，直接返回
        if (state.isPreLayout) {
            return
        }
        //将视图分离放入scrap缓存中，以准备重新对view进行排版
        detachAndScrapAttachedViews(recycler)

        var actualWidth = 0
        for (i in 0..itemCount) {
            // 初始化，将在屏幕内的view填充
            val itemView = recycler.getViewForPosition(i)
            addView(itemView)
            // 测量itemView的宽高
            measureChildWithMargins(itemView, 0, 0)
            val width = getDecoratedMeasuredWidth(itemView)
            val height = getDecoratedMeasuredHeight(itemView)
            // 根据itemView的宽高进行布局
            layoutDecorated(itemView, actualWidth, 0, actualWidth + width, height)

            actualWidth += width

            // 如果当前布局过的itemView的宽度总和大于RecyclerView的宽，则不再进行布局
            if (actualWidth > getWidth()) {
                break
            }
        }
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        // 横向滑动的时候，对左右两边按顺序填充itemView
        val travel = fill(dx, recycler, state)
        if (travel == 0) {
            return 0
        }
        // 滑动
        offsetChildrenHorizontal(-travel)

        // 回收已经不可见的itemView
        recyclerHideView(dx, recycler, state)
        return travel
    }

    /**
     * 左右滑动的时候，填充
     */
    private fun fill(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        var result = dx
        if (dx > 0) { // 向左滚动
            val lastView = getChildAt(childCount - 1) ?: return 0
            val lastPos = getPosition(lastView)
            // 可见的最后一个itemView完全滑进来了，需要补充新的
            if (lastView.right < width) {
                var scrap: View? = null
                // 判断可见的最后一个itemView的索引，
                // 如果是最后一个，则将下一个itemView设置为第一个，否则设置为当前索引的下一个
                if (lastPos == itemCount - 1) {
                    if (looperEnable) {
                        scrap = recycler.getViewForPosition(0)
                    } else {
                        result = 0
                    }
                } else {
                    scrap = recycler.getViewForPosition(lastPos + 1)
                }
                if (scrap == null) {
                    return result
                }
                // 将新的itemView添加进来并对其测量和布局
                addView(scrap)
                measureChildWithMargins(scrap, 0, 0)
                val width = getDecoratedMeasuredWidth(scrap)
                val height = getDecoratedMeasuredHeight(scrap)
                layoutDecorated(scrap, lastView.right, 0, lastView.right + width, height)
            }
        } else { // 向右滚动
            val firstView = getChildAt(0) ?: return 0
            val firstPos = getPosition(firstView)
            if (firstView.left >= 0) {
                var scrap: View? = null
                if (firstPos == 0) {
                    if (looperEnable) {
                        scrap = recycler.getViewForPosition(itemCount - 1)
                    } else {
                        result = 0
                    }
                } else {
                    scrap = recycler.getViewForPosition(firstPos - 1)
                }
                if (scrap == null) {
                    return result
                }
                addView(scrap, 0)
                measureChildWithMargins(scrap, 0, 0)
                val width = getDecoratedMeasuredWidth(scrap)
                val height = getDecoratedMeasuredHeight(scrap)
                layoutDecorated(scrap, firstView.left - width, 0, firstView.left, height)
            }
        }
        return result
    }

    /**
     * 回收界面不可见的view
     */
    private fun recyclerHideView(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        for (i in 0..itemCount) {
            val view = getChildAt(i) ?: continue
            if (dx > 0) {
                // 向左滚动，移除左边不在内容里的view
                if (view.right < 0) {
                    removeAndRecycleView(view, recycler)
                }
            } else {
                // 向右滚动，移除右边不在内容里的view
                if (view.left > width) {
                    removeAndRecycleView(view, recycler)
                }
            }
        }
    }
}