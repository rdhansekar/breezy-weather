package org.breezyweather.common.ui.widgets.trend.item

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorInt
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTypefaceFromTextAppearance
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView
import org.breezyweather.common.ui.widgets.trend.chart.AbsChartItemView

/**
 * Hourly trend item view.
 */
class HourlyTrendItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : AbsTrendItemView(context, attrs, defStyleAttr, defStyleRes) {
    private var mChartItem: AbsChartItemView? = null
    private val mHourTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val mDateTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private var mClickListener: OnClickListener? = null
    private var mHourText: String? = null
    private var mDayText: String? = null
    private var mIconDrawable: Drawable? = null

    @ColorInt
    private var mContentColor = 0

    @ColorInt
    private var mSubTitleColor = 0
    private var mDayTextBaseLine = 0f
    private var mHourTextBaseLine = 0f
    private var mIconLeft = 0f
    private var mIconTop = 0f
    private var mTrendViewTop = 0f
    private val mIconSize: Int
    override var chartTop: Int = 0
        private set
    override var chartBottom: Int = 0
        private set

    init {
        setWillNotDraw(false)
        mHourTextPaint.apply {
            typeface = getContext().getTypefaceFromTextAppearance(R.style.title_text)
            textSize =
                getContext().resources.getDimensionPixelSize(R.dimen.title_text_size).toFloat()
        }
        mDateTextPaint.apply {
            typeface = getContext().getTypefaceFromTextAppearance(R.style.content_text)
            textSize =
                getContext().resources.getDimensionPixelSize(R.dimen.content_text_size).toFloat()
        }
        setTextColor(Color.BLACK, Color.GRAY)
        mIconSize = getContext().dpToPx(ICON_SIZE_DIP.toFloat()).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var y = 0f
        val textMargin = context.dpToPx(TEXT_MARGIN_DIP.toFloat())
        val iconMargin = context.dpToPx(ICON_MARGIN_DIP.toFloat())

        // hour text.
        var fontMetrics = mHourTextPaint.fontMetrics
        y += textMargin
        mHourTextBaseLine = y - fontMetrics.top
        y += fontMetrics.bottom - fontMetrics.top
        y += textMargin

        // day text.
        fontMetrics = mDateTextPaint.fontMetrics
        y += textMargin
        mDayTextBaseLine = y - fontMetrics.top
        y += fontMetrics.bottom - fontMetrics.top
        y += textMargin

        // day icon.
        if (mIconDrawable != null) {
            y += iconMargin
            mIconLeft = (width - mIconSize) / 2f
            mIconTop = y
            y += mIconSize.toFloat()
            y += iconMargin
        }

        // margin bottom.
        val marginBottom = context.dpToPx(TrendRecyclerView.ITEM_MARGIN_BOTTOM_DIP.toFloat())

        // chartItem item view.
        mChartItem?.measure(
            MeasureSpec.makeMeasureSpec(
                width,
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(
                (height - marginBottom - y).toInt(),
                MeasureSpec.EXACTLY
            )
        )

        mTrendViewTop = y
        chartTop = (mTrendViewTop + mChartItem!!.marginTop).toInt()
        chartBottom =
            (mTrendViewTop + mChartItem!!.measuredHeight - mChartItem!!.marginBottom).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mChartItem?.layout(
            0, mTrendViewTop.toInt(),
            mChartItem!!.measuredWidth,
            mTrendViewTop.toInt() + mChartItem!!.measuredHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        // hour text.
        if (mHourText != null) {
            mHourTextPaint.color = mContentColor
            canvas.drawText(mHourText!!, measuredWidth / 2f, mHourTextBaseLine, mHourTextPaint)
        }

        // day text.
        if (mDayText != null) {
            mDateTextPaint.color = mSubTitleColor
            canvas.drawText(mDayText!!, measuredWidth / 2f, mDayTextBaseLine, mDateTextPaint)
        }

        // day icon.
        if (mIconDrawable != null) {
            val restoreCount = canvas.save()
            canvas.translate(mIconLeft, mIconTop)
            mIconDrawable!!.draw(canvas)
            canvas.restoreToCount(restoreCount)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            mClickListener?.onClick(this)
        }
        return super.onTouchEvent(event)
    }

    fun setDayText(dayText: String?) {
        mDayText = dayText
        invalidate()
    }

    fun setHourText(hourText: String?) {
        mHourText = hourText
        invalidate()
    }

    fun setTextColor(@ColorInt contentColor: Int, @ColorInt subTitleColor: Int) {
        mContentColor = contentColor
        mSubTitleColor = subTitleColor
        invalidate()
    }

    fun setIconDrawable(d: Drawable?) {
        val nullDrawable = mIconDrawable == null
        mIconDrawable = d
        if (d != null) {
            d.setVisible(true, true)
            d.callback = this
            d.setBounds(0, 0, mIconSize, mIconSize)
        }
        if (nullDrawable != (d == null)) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
        super.setOnClickListener { }
    }

    override var chartItemView: AbsChartItemView?
        get() = mChartItem
        set(t) {
            mChartItem = t
            removeAllViews()
            addView(mChartItem)
            requestLayout()
        }

    companion object {
        private const val ICON_SIZE_DIP = 32
        private const val TEXT_MARGIN_DIP = 2
        private const val ICON_MARGIN_DIP = 8
    }
}