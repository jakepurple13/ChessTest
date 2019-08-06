package com.crestron.aurora.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.LinearInterpolator
import com.crestron.aurora.R

/**
 * Created by codeest on 16/11/7.
 *
 * 论如何从直线到曲线再到三角形 =。=
 */

class ENPlayView : View {

    var currentState = STATE_PAUSE
        private set

    private lateinit var mPaint: Paint
    private lateinit var  mBgPaint: Paint

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mCenterX: Int = 0
    private var mCenterY: Int = 0

    private var mCircleRadius: Int = 0

    private var mRectF: RectF? = null
    private var mBgRectF: RectF? = null

    private var mFraction = 1f

    private lateinit var  mPath: Path
    private lateinit var  mDstPath: Path

    private lateinit var  mPathMeasure: PathMeasure

    private var mPathLength: Float = 0.toFloat()

    private var mDuration: Int = 0

    var listener: ENPlayListener? = null

    constructor(context: Context) : super(context) {}

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.play)
        val lineColor = ta.getColor(R.styleable.play_play_line_color, DEFAULT_LINE_COLOR)
        val bgLineColor = ta.getColor(R.styleable.play_play_bg_line_color, DEFAULT_BG_LINE_COLOR)
        val lineWidth = ta.getInteger(R.styleable.play_play_line_width, DEFAULT_LINE_WIDTH)
        val bgLineWidth = ta.getInteger(R.styleable.play_play_bg_line_width, DEFAULT_BG_LINE_WIDTH)
        ta.recycle()

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.color = lineColor
        mPaint.strokeWidth = lineWidth.toFloat()
        mPaint.pathEffect = CornerPathEffect(1f)

        mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBgPaint.style = Paint.Style.STROKE
        mBgPaint.strokeCap = Paint.Cap.ROUND
        mBgPaint.color = bgLineColor
        mBgPaint.strokeWidth = bgLineWidth.toFloat()

        mPath = Path()
        mDstPath = Path()
        mPathMeasure = PathMeasure()

        mDuration = DEFAULT_DURATION

        setOnClickListener {
            togglePlayPause()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w * 9 / 10
        mHeight = h * 9 / 10
        mCircleRadius = mWidth / 10
        mCenterX = w / 2
        mCenterY = h / 2
        mRectF = RectF((mCenterX - mCircleRadius).toFloat(), mCenterY + 0.6f * mCircleRadius,
                (mCenterX + mCircleRadius).toFloat(), mCenterY + 2.6f * mCircleRadius)
        mBgRectF = RectF((mCenterX - mWidth / 2).toFloat(), (mCenterY - mHeight / 2).toFloat(), (mCenterX + mWidth / 2).toFloat(), (mCenterY + mHeight / 2).toFloat())
        mPath.moveTo((mCenterX - mCircleRadius).toFloat(), mCenterY + 1.8f * mCircleRadius)
        mPath.lineTo((mCenterX - mCircleRadius).toFloat(), mCenterY - 1.8f * mCircleRadius)
        mPath.lineTo((mCenterX + mCircleRadius).toFloat(), mCenterY.toFloat())
        mPath.close()
        mPathMeasure.setPath(mPath, false)
        mPathLength = mPathMeasure.length
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(mCenterX.toFloat(), mCenterY.toFloat(), (mWidth / 2).toFloat(), mBgPaint)
        if (mFraction < 0) {    //嗷~~ 弹性部分
            canvas.drawLine((mCenterX + mCircleRadius).toFloat(), mCenterY - 1.6f * mCircleRadius + 10f * mCircleRadius.toFloat() * mFraction,
                    (mCenterX + mCircleRadius).toFloat(), mCenterY.toFloat() + 1.6f * mCircleRadius + 10f * mCircleRadius.toFloat() * mFraction, mPaint)

            canvas.drawLine((mCenterX - mCircleRadius).toFloat(), mCenterY - 1.6f * mCircleRadius,
                    (mCenterX - mCircleRadius).toFloat(), mCenterY + 1.6f * mCircleRadius, mPaint)

            canvas.drawArc(mBgRectF!!, -105f, 360f, false, mPaint)
        } else if (mFraction <= 0.3) {  //嗷~~ 右侧直线和下方曲线
            canvas.drawLine((mCenterX + mCircleRadius).toFloat(), mCenterY - 1.6f * mCircleRadius + mCircleRadius * 3.2f / 0.3f * mFraction,
                    (mCenterX + mCircleRadius).toFloat(), mCenterY + 1.6f * mCircleRadius, mPaint)

            canvas.drawLine((mCenterX - mCircleRadius).toFloat(), mCenterY - 1.6f * mCircleRadius,
                    (mCenterX - mCircleRadius).toFloat(), mCenterY + 1.6f * mCircleRadius, mPaint)

            if (mFraction != 0f)
                canvas.drawArc(mRectF!!, 0f, 180f / 0.3f * mFraction, false, mPaint)

            canvas.drawArc(mBgRectF!!, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint)
        } else if (mFraction <= 0.6) {  //嗷~~ 下方曲线和三角形
            canvas.drawArc(mRectF!!, 180f / 0.3f * (mFraction - 0.3f), 180 - 180f / 0.3f * (mFraction - 0.3f), false, mPaint)

            mDstPath.reset()
            mPathMeasure.getSegment(0.02f * mPathLength, 0.38f * mPathLength + 0.42f * mPathLength / 0.3f * (mFraction - 0.3f),
                    mDstPath, true)
            canvas.drawPath(mDstPath, mPaint)

            canvas.drawArc(mBgRectF!!, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint)
        } else if (mFraction <= 0.8) {  //嗷~~ 三角形
            mDstPath.reset()
            mPathMeasure.getSegment(0.02f * mPathLength + 0.2f * mPathLength / 0.2f * (mFraction - 0.6f), 0.8f * mPathLength + 0.2f * mPathLength / 0.2f * (mFraction - 0.6f),
                    mDstPath, true)
            canvas.drawPath(mDstPath, mPaint)

            canvas.drawArc(mBgRectF!!, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint)
        } else {    //嗷~~ 弹性部分
            mDstPath.reset()
            mPathMeasure.getSegment(10f * mCircleRadius.toFloat() * (mFraction - 1), mPathLength,
                    mDstPath, true)
            canvas.drawPath(mDstPath, mPaint)
        }
    }

    fun play() {
        if (currentState == STATE_PLAY) {
            return
        }
        currentState = STATE_PLAY
        val valueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = mDuration.toLong()
        valueAnimator.interpolator = AnticipateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            mFraction = 1 - valueAnimator.animatedFraction
            invalidate()
        }
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
        listener?.onPlay()
    }

    fun pause() {
        if (currentState == STATE_PAUSE) {
            return
        }
        currentState = STATE_PAUSE
        val valueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = mDuration.toLong()
        valueAnimator.interpolator = AnticipateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            invalidate()
        }
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
        listener?.onPause()
    }

    fun togglePlayPause() {
        if(currentState == STATE_PAUSE)
            play()
        else if(currentState == STATE_PLAY)
            pause()
    }

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    companion object {

        var STATE_PLAY = 0

        var STATE_PAUSE = 1

        var DEFAULT_LINE_COLOR = Color.WHITE

        var DEFAULT_BG_LINE_COLOR = -0xbbb5b1

        var DEFAULT_LINE_WIDTH = 14

        var DEFAULT_BG_LINE_WIDTH = 12

        var DEFAULT_DURATION = 1200
    }
    
    interface ENPlayListener {
        fun onPlay()
        fun onPause()
    }
}

class ENVolumeView : View {

    private lateinit var mPaint: Paint
    private lateinit var mBgPaint: Paint

    private lateinit var mPath: Path
    private lateinit var mDstPath: Path

    private lateinit var mPathMeasure: PathMeasure

    private var mFraction: Float = 0.toFloat()

    private var mVolumeValue: Int = 0

    private var mCurrentState = STATE_SILENT.toFloat()

    private var mWidth: Float = 0.toFloat()
    private var mHeight: Float = 0.toFloat()

    private var mCenterX: Float = 0.toFloat()
    private var mCenterY: Float = 0.toFloat()

    private var mBaseLength: Float = 0.toFloat()
    private var mPathLength: Float = 0.toFloat()

    private var vibrateAnim: ValueAnimator? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.volume)
        val lineColor = ta.getColor(R.styleable.volume_volume_line_color, DEFAULT_LINE_COLOR)
        val lineWidth = ta.getInteger(R.styleable.volume_volume_line_width, DEFAULT_LINE_WIDTH)
        val bgLineColor = ta.getInteger(R.styleable.volume_volume_bg_line_color, DEFAULT_BG_LINE_COLOR)
        val bgLineWidth = ta.getInteger(R.styleable.volume_volume_bg_line_width, DEFAULT_BG_LINE_WIDTH)
        ta.recycle()

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.color = lineColor
        mPaint.strokeWidth = lineWidth.toFloat()

        mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBgPaint.style = Paint.Style.STROKE
        mBgPaint.strokeCap = Paint.Cap.ROUND
        mBgPaint.strokeJoin = Paint.Join.ROUND
        mBgPaint.color = bgLineColor
        mBgPaint.strokeWidth = bgLineWidth.toFloat()

        mPath = Path()
        mDstPath = Path()
        mPathMeasure = PathMeasure()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = (w * 5 / 6).toFloat()
        mHeight = h.toFloat()
        mBaseLength = mWidth / 6
        mCenterX = (w / 2).toFloat()
        mCenterY = (h / 2).toFloat()

        mPath.moveTo(mCenterX - 3 * mBaseLength, mCenterY)
        mPath.lineTo(mCenterX - 3 * mBaseLength, mCenterY - 0.5f * mBaseLength)
        mPath.lineTo(mCenterX - 2 * mBaseLength, mCenterY - 0.5f * mBaseLength)
        mPath.lineTo(mCenterX, mCenterY - 2 * mBaseLength)
        mPath.lineTo(mCenterX, mCenterY + 2 * mBaseLength)
        mPath.lineTo(mCenterX - 2 * mBaseLength, mCenterY + 0.5f * mBaseLength)
        mPath.lineTo(mCenterX - 3 * mBaseLength, mCenterY + 0.5f * mBaseLength)
        mPath.close()
        mPathMeasure.setPath(mPath, false)
        mPathLength = mPathMeasure.length

        vibrateAnim = ValueAnimator.ofFloat(1f, 100f)
        vibrateAnim!!.duration = 100
        vibrateAnim!!.interpolator = LinearInterpolator()
        vibrateAnim!!.repeatCount = ValueAnimator.INFINITE
        vibrateAnim!!.repeatMode = ValueAnimator.REVERSE
        vibrateAnim!!.addUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mCurrentState != STATE_VIBRATE.toFloat()) {
            if (mFraction <= 0.5) {
                mDstPath.reset()   //嗷~ 在这画喇叭
                mPathMeasure.getSegment(0f, mPathLength * 0.38f - mPathLength * 0.13f * mFraction, mDstPath, true)
                canvas.drawPath(mDstPath, mBgPaint)
                mDstPath.reset()
                mPathMeasure.getSegment(mPathLength * 0.62f + mPathLength * 0.13f * mFraction, mPathLength, mDstPath, true)
                canvas.drawPath(mDstPath, mBgPaint)

                canvas.save()  //嗷~ 在这画X
                canvas.translate(-mFraction * mBaseLength * 2f, 0f)
                canvas.drawLine(mCenterX - mBaseLength * (0.5f - mFraction), mCenterY - mBaseLength * (0.5f - mFraction), mCenterX + mBaseLength * (0.5f - mFraction), mCenterY + mBaseLength * (0.5f - mFraction), mBgPaint)
                canvas.drawLine(mCenterX - mBaseLength * (0.5f - mFraction), mCenterY + mBaseLength * (0.5f - mFraction), mCenterX + mBaseLength * (0.5f - mFraction), mCenterY - mBaseLength * (0.5f - mFraction), mBgPaint)
                canvas.restore()
            } else {
                mDstPath.reset()   //嗷~ 在这画喇叭
                mPathMeasure.getSegment(0f, mPathLength * 0.25f + mPathLength * 0.13f * (mFraction - 0.5f), mDstPath, true)
                canvas.drawPath(mDstPath, mBgPaint)
                mDstPath.reset()
                mPathMeasure.getSegment(mPathLength * 0.75f - mPathLength * 0.13f * (mFraction - 0.5f), mPathLength, mDstPath, true)
                canvas.drawPath(mDstPath, mBgPaint)
                mDstPath.reset()
                mPathMeasure.getSegment(0f, mPathLength * 0.38f / 0.5f * (mFraction - 0.5f), mDstPath, true)
                canvas.drawPath(mDstPath, mPaint)
                mDstPath.reset()
                mPathMeasure.getSegment(mPathLength - mPathLength * 0.38f / 0.5f * (mFraction - 0.5f), mPathLength, mDstPath, true)
                canvas.drawPath(mDstPath, mPaint)

                canvas.save()  //嗷~ 在这画小声波
                canvas.translate(-(1 - mFraction) * mBaseLength * 2f, 0f)
                canvas.drawArc(mCenterX - 0.5f * mBaseLength - mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY - mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterX - 0.5f * mBaseLength + mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY + mBaseLength / 0.5f * (mFraction - 0.5f), -55f, 110f, false, mBgPaint)
                var sVolume = mVolumeValue
                if (sVolume > 50)
                    sVolume = 50
                canvas.drawArc(mCenterX - 0.5f * mBaseLength - mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY - mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterX - 0.5f * mBaseLength + mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY + mBaseLength / 0.5f * (mFraction - 0.5f), (-55 / 50 * sVolume).toFloat(), (110 / 50 * sVolume).toFloat(), false, mPaint)
                canvas.restore()

                canvas.save()  //嗷~ 在这画大声波
                canvas.translate(-(1 - mFraction) * mBaseLength * 3f, 0f)
                canvas.drawArc(mCenterX - 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY - 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterX + 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY + 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f), -55f, 110f, false, mBgPaint)
                var lVolume = mVolumeValue - 50
                if (lVolume < 0)
                    lVolume = 0
                canvas.drawArc(mCenterX - 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY - 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterX + 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f),
                        mCenterY + 1.6f * mBaseLength / 0.5f * (mFraction - 0.5f), (-55 / 50 * lVolume).toFloat(), (110 / 50 * lVolume).toFloat(), false, mPaint)
                canvas.restore()
            }
        } else {
            mDstPath.reset()   //嗷~ 在这画喇叭
            mPathMeasure.getSegment(0f, mPathLength * 0.38f, mDstPath, true)
            canvas.drawPath(mDstPath, mPaint)
            mDstPath.reset()
            mPathMeasure.getSegment(mPathLength - mPathLength * 0.38f, mPathLength, mDstPath, true)
            canvas.drawPath(mDstPath, mPaint)

            canvas.save()  //嗷~ 在这画小声波
            canvas.translate(-(1 - mFraction) * mBaseLength / 5 * 2f * mVolumeValue.toFloat() / 100, 0f)
            canvas.drawArc(mCenterX - 1.5f * mBaseLength,
                    mCenterY - mBaseLength,
                    mCenterX + 0.5f * mBaseLength,
                    mCenterY + mBaseLength, -55f, 110f, false, mBgPaint)
            var sVolume = mVolumeValue
            if (sVolume > 50)
                sVolume = 50
            canvas.drawArc(mCenterX - 1.5f * mBaseLength,
                    mCenterY - mBaseLength,
                    mCenterX + 0.5f * mBaseLength,
                    mCenterY + mBaseLength, (-55 / 50 * sVolume).toFloat(), (110 / 50 * sVolume).toFloat(), false, mPaint)
            canvas.restore()

            canvas.save()  //嗷~ 在这画大声波
            canvas.translate(-(1 - mFraction) * mBaseLength / 5 * 2f * mVolumeValue.toFloat() / 100, 0f)
            canvas.drawArc(mCenterX - 1.6f * mBaseLength,
                    mCenterY - 1.6f * mBaseLength,
                    mCenterX + 1.6f * mBaseLength,
                    mCenterY + 1.6f * mBaseLength, -55f, 110f, false, mBgPaint)
            var lVolume = mVolumeValue - 50
            if (lVolume < 0)
                lVolume = 0
            canvas.drawArc(mCenterX - 1.6f * mBaseLength,
                    mCenterY - 1.6f * mBaseLength,
                    mCenterX + 1.6f * mBaseLength,
                    mCenterY + 1.6f * mBaseLength, (-55 / 50 * lVolume).toFloat(), (110 / 50 * lVolume).toFloat(), false, mPaint)
            canvas.restore()
        }
    }

    private fun closeVolume() {
        val valueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = 800
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            mFraction = 1 - valueAnimator.animatedFraction
            invalidate()
        }
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
    }

    private fun openVolume() {
        val valueAnimator = ValueAnimator.ofFloat(1f, 100f)
        valueAnimator.duration = 800
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            mFraction = valueAnimator.animatedFraction
            invalidate()
        }
        if (!valueAnimator.isRunning) {
            valueAnimator.start()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (mVolumeValue == 0) {
                    closeVolume()
                } else {
                    startVibration()
                }
            }
        })
    }

    private fun startVibration() {
        mCurrentState = STATE_VIBRATE.toFloat()
        if (!vibrateAnim!!.isRunning) {
            vibrateAnim!!.start()
        }
    }

    private fun stopVibration() {
        if (vibrateAnim!!.isRunning) {
            vibrateAnim!!.cancel()
        }
    }

    fun updateVolumeValue(value: Int) {
        if (value < 0 || value > 100) {
            return
        }
        mVolumeValue = value
        if (value == 0 && mCurrentState != STATE_SILENT.toFloat()) {
            mCurrentState = STATE_SILENT.toFloat()
            stopVibration()
            closeVolume()
        } else if (value != 0 && mCurrentState == STATE_SILENT.toFloat()) {
            mCurrentState = STATE_VOLUME.toFloat()
            openVolume()
        }
    }

    companion object {

        private val STATE_SILENT = 0

        private val STATE_VOLUME = 1

        private val STATE_VIBRATE = 2

        private val DEFAULT_LINE_COLOR = Color.WHITE

        private val DEFAULT_BG_LINE_COLOR = -0x9b9693

        private val DEFAULT_LINE_WIDTH = 10

        private val DEFAULT_BG_LINE_WIDTH = 10
    }
}