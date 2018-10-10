/*
 * Copyright (C) 2016 venshine.cn@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wx.wheelview.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.adapter.BaseWheelAdapter
import com.wx.wheelview.adapter.SimpleWheelAdapter
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.common.WheelViewException
import com.wx.wheelview.graphics.DrawableFactory
import com.wx.wheelview.util.WheelUtils
import java.util.*

/**
 * 滚轮控件
 *
 * @author venshine
 */
class WheelView<T> : ListView, IWheelView<T> {

    private var mItemH = 0 // 每一项高度
    private var mWheelSize = IWheelView.WHEEL_SIZE    // 滚轮个数
    private var mLoop = IWheelView.LOOP   // 是否循环滚动
    private var mList: List<T>? = null   // 滚轮数据列表
    /**
     * 获取当前滚轮位置
     *
     * @return
     */
    var currentPosition = -1
        private set    // 记录滚轮当前刻度
    private var mExtraText: String? = null  // 添加滚轮选中位置附加文本
    private var mExtraTextColor: Int = 0    // 附加文本颜色
    private var mExtraTextSize: Int = 0 // 附加文本大小
    private var mExtraMargin: Int = 0   // 附加文本外边距
    private var mSelection = 0 // 选中位置
    private var mClickable = IWheelView.CLICKABLE // 是否可点击

    private var mTextPaint: Paint? = null   // 附加文本画笔

    /**
     * 获得皮肤风格
     *
     * @return
     */
    /**
     * 设置皮肤风格
     *
     * @param skin
     */
    var skin = Skin.None // 皮肤风格

    /**
     * 获得滚轮样式
     *
     * @return
     */
    /**
     * 设置滚轮样式
     *
     * @param style
     */
    var style: WheelViewStyle? = null  // 滚轮样式

    private var mJoinWheelView: WheelView<*>? = null   // 副WheelView

    private var mJoinMap: HashMap<Int, List<T>>? = null    // 副滚轮数据列表

    private var mWheelAdapter: BaseWheelAdapter<T>? = null

    private var mOnWheelItemSelectedListener: OnWheelItemSelectedListener<T>? = null

    private var mOnWheelItemClickListener: OnWheelItemClickListener<T>? = null

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == WheelConstants.WHEEL_SCROLL_HANDLER_WHAT) {
                if (mOnWheelItemSelectedListener != null) {
                    mOnWheelItemSelectedListener!!.onItemSelected(currentPosition, selectionItem)
                }
                if (mJoinWheelView != null) {
                    if (!mJoinMap!!.isEmpty()) {
                        //mJoinWheelView!!.resetDataFromTop(mJoinMap!![mList!![currentPosition]])//get(mList!![currentPosition]))

                        val list = mJoinMap!![currentPosition]!!

                        if (WheelUtils.isEmpty(list)) {
                            throw WheelViewException("join map data is error.")
                        }
                        this@WheelView.postDelayed({
                            setWheelData(list)
                            super@WheelView.setSelection(mSelection)
                            refreshCurrentPosition(true)
                        }, 10)
                    } else {
                        throw WheelViewException("JoinList is error.")
                    }
                }
            }
        }
    }

    private val mOnItemClickListener = OnItemClickListener { parent, view, position, id ->
        if (mOnWheelItemClickListener != null) {
            mOnWheelItemClickListener!!.onItemClick(currentPosition, selectionItem)
        }
    }

    private val mTouchListener = OnTouchListener { v, event ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        false
    }

    private val mOnScrollListener = object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                val itemView = getChildAt(0)
                if (itemView != null) {
                    val deltaY = itemView.y
                    if (deltaY == 0f || mItemH == 0) {
                        return
                    }
                    if (Math.abs(deltaY) < mItemH / 2) {
                        val d = getSmoothDistance(deltaY)
                        smoothScrollBy(d, WheelConstants
                                .WHEEL_SMOOTH_SCROLL_DURATION)
                    } else {
                        val d = getSmoothDistance(mItemH + deltaY)
                        smoothScrollBy(d, WheelConstants
                                .WHEEL_SMOOTH_SCROLL_DURATION)
                    }
                }
            }
        }

        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (visibleItemCount != 0) {
                refreshCurrentPosition(false)
            }
        }
    }

    /**
     * 获取当前滚轮位置的数据
     *
     * @return
     */
    val selectionItem: T?
        get() {
            var position = currentPosition
            position = if (position < 0) 0 else position
            return if (mList != null && mList!!.size > position) {
                mList!![position]
            } else null
        }

    /**
     * 获得滚轮数据总数
     *
     * @return
     */
    val wheelCount: Int
        get() = if (!WheelUtils.isEmpty(mList)) mList!!.size else 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, style: WheelViewStyle) : super(context) {
        this.style = style
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * 设置滚轮滑动停止时事件，监听滚轮选中项
     *
     * @param onWheelItemSelectedListener
     */
    fun setOnWheelItemSelectedListener(onWheelItemSelectedListener: OnWheelItemSelectedListener<T>) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener
    }

    /**
     * 设置滚轮选中项点击事件
     *
     * @param onWheelItemClickListener
     */
    fun setOnWheelItemClickListener(onWheelItemClickListener: OnWheelItemClickListener<T>) {
        mOnWheelItemClickListener = onWheelItemClickListener
    }

    /**
     * 初始化
     */
    private fun init() {
        if (style == null) {
            style = WheelViewStyle()
        }

        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        tag = WheelConstants.TAG
        isVerticalScrollBarEnabled = false
        isScrollingCacheEnabled = false
        cacheColorHint = Color.TRANSPARENT
        setFadingEdgeLength(0)
        overScrollMode = View.OVER_SCROLL_NEVER
        dividerHeight = 0
        onItemClickListener = mOnItemClickListener
        setOnScrollListener(mOnScrollListener)
        setOnTouchListener(mTouchListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isNestedScrollingEnabled = true
        }
        addOnGlobalLayoutListener()
    }

    private fun addOnGlobalLayoutListener() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                if (childCount > 0 && mItemH == 0) {
                    mItemH = getChildAt(0).height
                    if (mItemH != 0) {
                        val params = layoutParams
                        params.height = mItemH * mWheelSize
                        refreshVisibleItems(firstVisiblePosition,
                                currentPosition + mWheelSize / 2,
                                mWheelSize / 2)
                        setBackground()
                    } else {
                        throw WheelViewException("wheel item is error.")
                    }
                }
            }
        })
    }

    /**
     * 设置背景
     */
    private fun setBackground() {
        val drawable = DrawableFactory.createDrawable(skin, width,
                mItemH * mWheelSize, style, mWheelSize, mItemH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable)
        } else {
            setBackgroundDrawable(drawable)
        }
    }

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    override fun setWheelSize(wheelSize: Int) {
        if (wheelSize and 1 == 0) {
            throw WheelViewException("wheel size must be an odd number.")
        }
        mWheelSize = wheelSize
        if (mWheelAdapter != null) {
            mWheelAdapter!!.setWheelSize(wheelSize)
        }
    }

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    override fun setLoop(loop: Boolean) {
        if (loop != mLoop) {
            mLoop = loop
            setSelection(0)
            if (mWheelAdapter != null) {
                mWheelAdapter!!.setLoop(loop)
            }
        }
    }

    /**
     * 设置滚轮选中项是否可点击
     *
     * @param clickable
     */
    override fun setWheelClickable(clickable: Boolean) {
        if (clickable != mClickable) {
            mClickable = clickable
            if (mWheelAdapter != null) {
                mWheelAdapter!!.setClickable(clickable)
            }
        }
    }

    /**
     * 重置数据
     *
     * @param list
     */
    fun resetDataFromTop(list: List<T>) {
        if (WheelUtils.isEmpty(list)) {
            throw WheelViewException("join map data is error.")
        }
        this@WheelView.postDelayed({
            setWheelData(list)
            super@WheelView.setSelection(mSelection)
            refreshCurrentPosition(true)
        }, 10)
    }

    /**
     * 获取滚轮位置
     *
     * @return
     */
    fun getSelection(): Int {
        return mSelection
    }

    /**
     * 设置滚轮位置
     *
     * @param selection
     */
    override fun setSelection(selection: Int) {
        mSelection = selection
        visibility = View.INVISIBLE
        this@WheelView.postDelayed({
            super@WheelView.setSelection(getRealPosition(selection))
            refreshCurrentPosition(false)
            visibility = View.VISIBLE
        }, 500)
    }

    /**
     * 连接副WheelView
     *
     * @param wheelView
     */
    override fun join(wheelView: WheelView<*>?) {
        if (wheelView == null) {
            throw WheelViewException("wheelview cannot be null.")
        }
        mJoinWheelView = wheelView
    }

    /**
     * 副WheelView数据
     *
     * @param map
     */
    override fun joinDatas(map: HashMap<String, List<T>>) {
        //mJoinMap = map
    }

    /**
     * 获得滚轮当前真实位置
     *
     * @param positon
     * @return
     */
    private fun getRealPosition(positon: Int): Int {
        if (WheelUtils.isEmpty(mList)) {
            return 0
        }
        if (mLoop) {
            val d = Integer.MAX_VALUE / 2 / mList!!.size
            return positon + d * mList!!.size - mWheelSize / 2
        }
        return positon
    }

    /**
     * 设置滚轮数据适配器，已弃用，具体使用[.setWheelAdapter]
     *
     * @param adapter
     */
    @Deprecated("")
    override fun setAdapter(adapter: ListAdapter) {
        if (adapter is BaseWheelAdapter<*>) {
            setWheelAdapter(adapter as BaseWheelAdapter<T>)
        } else {
            throw WheelViewException("please invoke setWheelAdapter " + "method.")
        }
    }

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter asdf
     */
    override fun setWheelAdapter(adapter: BaseWheelAdapter<T>) {
        super.setAdapter(adapter)
        mWheelAdapter = adapter
        mWheelAdapter!!.setData(mList).setWheelSize(mWheelSize).setLoop(mLoop).setClickable(mClickable)
    }

    /**
     * 设置滚轮数据
     *
     * @param list adsf
     */
    override fun setWheelData(list: List<T>) {
        if (WheelUtils.isEmpty(list)) {
            throw WheelViewException("wheel datas are error.")
        }
        mList = list
        if (mWheelAdapter != null) {
            mWheelAdapter!!.setData(list)
        }
    }

    /**
     * 设置选中行附加文本
     *
     * @param text
     * @param textColor
     * @param textSize
     * @param margin
     */
    fun setExtraText(text: String, textColor: Int, textSize: Int, margin: Int) {
        mExtraText = text
        mExtraTextColor = textColor
        mExtraTextSize = textSize
        mExtraMargin = margin
    }

    /**
     * 平滑的滚动距离
     *
     * @param scrollDistance
     * @return
     */
    private fun getSmoothDistance(scrollDistance: Float): Int {
        return if (Math.abs(scrollDistance) <= 2) {
            scrollDistance.toInt()
        } else if (Math.abs(scrollDistance) < 12) {
            if (scrollDistance > 0) 2 else -2
        } else {
            (scrollDistance / 6).toInt()  // 减缓平滑滑动速率
        }
    }

    /**
     * 刷新当前位置
     *
     * @param join
     */
    private fun refreshCurrentPosition(join: Boolean) {
        if (getChildAt(0) == null || mItemH == 0) {
            return
        }
        val firstPosition = firstVisiblePosition
        if (mLoop && firstPosition == 0) {
            return
        }
        var position = 0
        if (Math.abs(getChildAt(0).y) <= mItemH / 2) {
            position = firstPosition
        } else {
            position = firstPosition + 1
        }
        refreshVisibleItems(firstPosition, position + mWheelSize / 2,
                mWheelSize / 2)
        if (mLoop) {
            position = (position + mWheelSize / 2) % wheelCount
        }
        if (position == currentPosition && !join) {
            return
        }
        currentPosition = position
        mWheelAdapter!!.setCurrentPosition(position)
        mHandler.removeMessages(WheelConstants.WHEEL_SCROLL_HANDLER_WHAT)
        mHandler.sendEmptyMessageDelayed(WheelConstants
                .WHEEL_SCROLL_HANDLER_WHAT, WheelConstants
                .WHEEL_SCROLL_DELAY_DURATION.toLong())
    }

    /**
     * 刷新可见滚动列表
     *
     * @param firstPosition
     * @param curPosition
     * @param offset
     */
    private fun refreshVisibleItems(firstPosition: Int, curPosition: Int, offset: Int) {
        for (i in curPosition - offset..curPosition + offset) {
            val itemView = getChildAt(i - firstPosition) ?: continue
            if (mWheelAdapter is ArrayWheelAdapter<*> || mWheelAdapter is SimpleWheelAdapter) {
                val textView = itemView.findViewWithTag<View>(WheelConstants.WHEEL_ITEM_TEXT_TAG) as TextView
                refreshTextView(i, curPosition, itemView, textView)
                Log.d("Refresh", "Visible")
            } else {    // 自定义类型
                Log.e("Refresh", "Visible $curPosition")
                /*val textView = WheelUtils.findTextView(itemView)
                if (textView != null) {
                    refreshTextView(i, curPosition, itemView, textView)
                }*/
                /*val textView = WheelUtils.findImageView(itemView)
                if (textView != null) {
                    refreshImageView(i, curPosition, itemView, textView)
                }*/

                if (curPosition == i) {
                    itemView.alpha = 1f
                } else {
                    itemView.alpha = .5f
                    itemView.setOnClickListener {

                    }
                }

            }
        }
    }

    /**
     * 刷新文本
     *
     * @param position
     * @param curPosition
     * @param itemView
     * @param textView
     */
    private fun refreshTextView(position: Int, curPosition: Int, itemView: View, textView: TextView) {
        if (curPosition == position) { // 选中
            Log.d("a;dslkfj", "Here we is not")
            val textColor = if (style!!.selectedTextColor != -1)
                style!!
                        .selectedTextColor
            else
                if (style!!.textColor != -1)
                    style!!
                            .textColor
                else
                    WheelConstants.WHEEL_TEXT_COLOR
            val defTextSize = (if (style!!.textSize != -1) style!!.textSize else WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val textSize = when {
                style!!.selectedTextSize != -1 -> style!!.selectedTextSize.toFloat()
                style!!.selectedTextZoom != -1f -> defTextSize * style!!.selectedTextZoom
                else -> defTextSize
            }
            setTextView(itemView, textView, textColor, textSize, 1.0f)
        } else {    // 未选中
            Log.d("a;dslkfj", "Here we is")
            val textColor = if (style!!.textColor != -1)
                style!!.textColor
            else
                WheelConstants.WHEEL_TEXT_COLOR
            val textSize = (if (style!!.textSize != -1)
                style!!.textSize
            else
                WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val delta = Math.abs(position - curPosition)
            val alpha = Math.pow((if (style!!.textAlpha != -1f)
                style!!.textAlpha
            else
                WheelConstants
                        .WHEEL_TEXT_ALPHA).toDouble(), delta.toDouble()).toFloat()
            setTextView(itemView, textView, textColor, textSize, alpha)
        }
    }

    private fun refreshImageView(position: Int, curPosition: Int, itemView: View, textView: ImageView) {
        if (curPosition == position) { // 选中
            Log.d("a;dslkfj", "Here we is not")
            val textColor = when {
                style!!.selectedTextColor != -1 -> style!!
                        .selectedTextColor
                style!!.textColor != -1 -> style!!
                        .textColor
                else -> WheelConstants.WHEEL_TEXT_COLOR
            }
            val defTextSize = (if (style!!.textSize != -1) style!!.textSize else WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val textSize = when {
                style!!.selectedTextSize != -1 -> style!!.selectedTextSize.toFloat()
                style!!.selectedTextZoom != -1f -> defTextSize * style!!.selectedTextZoom
                else -> defTextSize
            }
            setImageView(itemView, textView, textColor, textSize, 1.0f)
        } else {    // 未选中
            Log.d("a;dslkfj", "Here we is")
            val textColor = if (style!!.textColor != -1)
                style!!.textColor
            else
                WheelConstants.WHEEL_TEXT_COLOR
            val textSize = (if (style!!.textSize != -1)
                style!!.textSize
            else
                WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val delta = Math.abs(position - curPosition)
            val alpha = Math.pow((if (style!!.textAlpha != -1f)
                style!!.textAlpha
            else
                WheelConstants
                        .WHEEL_TEXT_ALPHA).toDouble(), delta.toDouble()).toFloat()
            setImageView(itemView, textView, textColor, textSize, alpha)
            itemView.alpha = .5f
        }
    }

    /**
     * 设置TextView
     *
     * @param itemView
     * @param textView
     * @param textColor
     * @param textSize
     * @param textAlpha
     */
    private fun setTextView(itemView: View, textView: TextView, textColor: Int, textSize: Float, textAlpha: Float) {
        textView.setTextColor(textColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        itemView.alpha = textAlpha
    }

    private fun setImageView(itemView: View, textView: ImageView, textColor: Int, textSize: Float, textAlpha: Float) {
        //textView.setTextColor(textColor)
        //textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        itemView.alpha = textAlpha
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!TextUtils.isEmpty(mExtraText)) {
            val targetRect = Rect(0, mItemH * (mWheelSize / 2), width, mItemH * (mWheelSize / 2 + 1))
            mTextPaint!!.textSize = mExtraTextSize.toFloat()
            mTextPaint!!.color = mExtraTextColor
            val fontMetrics = mTextPaint!!.fontMetricsInt
            val baseline = (targetRect.bottom + targetRect.top - fontMetrics
                    .bottom - fontMetrics.top) / 2
            mTextPaint!!.textAlign = Paint.Align.CENTER
            canvas.drawText(mExtraText!!, (targetRect.centerX() + mExtraMargin).toFloat(),
                    baseline.toFloat(), mTextPaint!!)
        }
    }

    enum class Skin { // 滚轮皮肤
        Common, Holo, None
    }

    interface OnWheelItemSelectedListener<T> {
        fun onItemSelected(position: Int, t: T?)
    }

    interface OnWheelItemClickListener<T> {
        fun onItemClick(position: Int, t: T?)
    }

    class WheelViewStyle {

        var backgroundColor = -1 // 背景颜色
        var holoBorderColor = -1   // holo样式边框颜色
        var holoBorderWidth = -1//holo样式边框宽度
        var textColor = -1 // 文本颜色
        var selectedTextColor = -1 // 选中文本颜色
        var textSize = -1// 文本大小
        var selectedTextSize = -1   // 选中文本大小
        var textAlpha = -1f  // 文本透明度(0f ~ 1f)
        var selectedTextZoom = -1f // 选中文本放大倍数

        constructor() {}

        constructor(style: WheelViewStyle) {
            this.backgroundColor = style.backgroundColor
            this.holoBorderColor = style.holoBorderColor
            this.holoBorderWidth = style.holoBorderWidth
            this.textColor = style.textColor
            this.selectedTextColor = style.selectedTextColor
            this.textSize = style.textSize
            this.selectedTextSize = style.selectedTextSize
            this.textAlpha = style.textAlpha
            this.selectedTextZoom = style.selectedTextZoom
        }

    }

}
