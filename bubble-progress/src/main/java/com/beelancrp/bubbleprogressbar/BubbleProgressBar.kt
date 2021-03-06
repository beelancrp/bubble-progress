package com.beelancrp.bubbleprogressbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.graphics.drawable.ArgbEvaluator
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class BubbleProgressBar(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {

    var bubbleCount = 1
    var bubbleSize = 42f
    var bubbleColor = Color.WHITE
    var bubbleHighlightColor = Color.GRAY
    var lineWidth = 16f
    var lineHeight = 4f
    var textStartColor = Color.BLACK
    var textEndColor = Color.WHITE

    private var yFillBg = 0f
    private var isField = false

    private val mBgPaint: Paint by lazy {
        Paint()
    }

    private val mBubblePaint by lazy {
        Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
    }

    private val mEmptyBgBitmap: Bitmap by lazy {
        makeBackgroundBitmap(width, height)
    }

    private val mFillBgBitmap: Bitmap by lazy {
        makeFillBackgroundBitmap(width, height)
    }

    private val mBubbleBitmap: Bitmap by lazy {
        makeBubbleBitmap(bubbleSize, bubbleSize)
    }

    private val mDividerBgBitmap: Bitmap by lazy {
        makeDividerBitmap(lineWidth, height.toFloat())
    }


    private val textPaints: ArrayList<TextPaint> by lazy {
        (0..bubbleCount).map {
            TextPaint().apply {
                textAlign = Paint.Align.CENTER
                color = if (it == 0) textEndColor else textStartColor
                typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
                textSize = sp(14f)
            }
        } as ArrayList
    }

    private lateinit var pager: ViewPager

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleProgressBar)
        bubbleSize = typedArray.getDimension(R.styleable.BubbleProgressBar_bpb_bubble_size, bubbleSize)
        bubbleColor = typedArray.getColor(R.styleable.BubbleProgressBar_bpb_color, bubbleColor)
        bubbleHighlightColor = typedArray.getColor(R.styleable.BubbleProgressBar_bpb_highlight_color, bubbleHighlightColor)
        lineWidth = typedArray.getDimension(R.styleable.BubbleProgressBar_bpb_line_width, lineWidth)
        lineHeight = typedArray.getDimension(R.styleable.BubbleProgressBar_bpb_line_height, lineHeight)
        textStartColor = typedArray.getColor(R.styleable.BubbleProgressBar_bpb_text_start_color, bubbleColor)
        textEndColor = typedArray.getColor(R.styleable.BubbleProgressBar_bpb_text_end_color, bubbleColor)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (bubbleCount * bubbleSize) + (lineWidth * (bubbleCount - 1))
        if (width == 0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(bubbleSize.toInt(), MeasureSpec.EXACTLY))
        }
    }

    override fun onDetachedFromWindow() {
        try {
            mBgPaint.reset()
            mBubblePaint.reset()
            mBubbleBitmap.recycle()
            mEmptyBgBitmap.recycle()
            mDividerBgBitmap.recycle()
            mFillBgBitmap.recycle()
            textPaints.forEach {
                it.reset()
            }
        } catch (e: IllegalArgumentException) {
        } finally {
            super.onDetachedFromWindow()
        }
    }

    fun setViewPager(pager: ViewPager) {
        this.pager = pager
        pager.addOnPageChangeListener(mPageListener)
        bubbleCount = pager.adapter?.count ?: 0
        requestLayout()
    }

    fun setFakeBubble(count: Int) {
        bubbleCount = count
        isField = true
        textPaints.forEach { it.color = textEndColor }

        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        drawEmptyBg(canvas)
        drawFillBg(canvas)
        drawBubbles(canvas)
        canvas.restore()
        drawTexts(canvas)
    }

    private fun drawBubbles(canvas: Canvas) {
        (0 until bubbleCount).forEach {
            drawBubble(it, canvas)
        }
    }

    private fun drawTexts(canvas: Canvas) {
        (0 until bubbleCount).forEach {
            drawText(it, canvas)
        }
    }

    private fun drawBubble(pos: Int, canvas: Canvas) {
        val x = (pos * (bubbleSize + lineWidth))
        val y = 0f
        canvas.drawBitmap(mBubbleBitmap, x, y, mBubblePaint)
        if (pos != bubbleCount - 1) {
            val xD = (pos + 1) * bubbleSize + pos * lineWidth
            canvas.drawBitmap(mDividerBgBitmap, xD, y, mBubblePaint)
        }
    }

    private fun drawFillBg(canvas: Canvas) {
        canvas.drawBitmap(mFillBgBitmap, yFillBg, 0f, mBgPaint)
    }

    private fun drawEmptyBg(canvas: Canvas) {
        canvas.drawBitmap(mEmptyBgBitmap, 0f, 0f, mBgPaint)
    }

    private fun drawText(pos: Int, canvas: Canvas) {
        val text = (pos + 1).toString()
        val paint = textPaints[pos]
        val y = (bubbleSize / 2f) + (paint.getTextHeight(text) / 2f)
        val x = (pos * (bubbleSize + lineWidth)) + bubbleSize / 2f
        canvas.drawText((pos + 1).toString(), x, y, textPaints[pos])
    }

    private fun makeBackgroundBitmap(w: Int, h: Int): Bitmap {
        var newWidth = (bubbleSize * bubbleCount) + (lineWidth * bubbleCount.dec())
        var newHeight = bubbleSize

        w.takeIf { it > 0 }?.apply { newWidth = this.toFloat() }
        h.takeIf { it > 0 }?.apply { newHeight = this.toFloat() }

        val bm = Bitmap.createBitmap(newWidth.toInt(), newHeight.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bubbleColor
        }
        c.drawRect(0f, 0f, newWidth, newHeight, p)
        return bm
    }

    private fun makeFillBackgroundBitmap(w: Int, h: Int): Bitmap {
        val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bubbleHighlightColor
        }
        yFillBg = if (!isField)
            -w.toFloat() + bubbleSize
        else
            0f

        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        return bm
    }

    private fun makeBubbleBitmap(w: Float, h: Float): Bitmap {
        val bm = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        c.drawOval(RectF(0f, 0f, w, h), p)
        return bm
    }

    private fun makeDividerBitmap(w: Float, h: Float): Bitmap {
        val bm = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        c.drawRect(RectF(0f, height / 2f - lineHeight / 2f, w, height / 2f + lineHeight / 2f), p)
        return bm
    }

    private val mPageListener = object : ViewPager.SimpleOnPageChangeListener() {
        @SuppressLint("RestrictedApi")
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            val realPos = position + positionOffset
            yFillBg = -width + bubbleSize + (realPos) * bubbleSize + realPos * lineWidth
            textPaints[position + 1].color = ArgbEvaluator.getInstance().evaluate(positionOffset, textStartColor, textEndColor) as Int
            invalidate()
        }
    }

    private fun sp(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }
}

fun TextPaint.getTextHeight(text: String): Int {
    val bounds = Rect()
    this.getTextBounds(text, 0, text.length, bounds)
    return bounds.height()
}