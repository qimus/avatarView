package ru.qimus.customview.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.qimus.customview.R
import ru.qimus.customview.extensions.px

class AvatarImageViewShader @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
    }

    @Px
    var borderWidth: Float = DEFAULT_BORDER_WIDTH.px.toFloat()
    @ColorInt
    private var borderColor: Int = Color.WHITE
    private var initials: String = "MM"

    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageViewShader)
            borderWidth = typedArray.getDimension(
                R.styleable.AvatarImageViewShader_aivs_borderWidth,
                DEFAULT_BORDER_WIDTH.px.toFloat()
            )

            borderColor = typedArray.getColor(
                R.styleable.AvatarImageViewShader_aivs_borderColor,
                Color.WHITE
            )

            initials = typedArray.getString(R.styleable.AvatarImageViewShader_aivs_initials) ?: "??"

            typedArray.recycle()
        }
        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    private fun setup() {
        with(avatarPaint) {
            color = Color.RED
            style = Paint.Style.FILL
        }
        with(borderPaint) {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareShader(w: Int, h: Int) {
        var srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader = BitmapShader(srcBm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("AvatarImageViewShader", "onAttachedToWindow")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("AvatarImageViewShader", """
            onMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}
        """.trimIndent())

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
        Log.d("AvatarImageViewShader", "onMeasure after set size: $measuredWidth, $measuredHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }
        prepareShader(w, h)
    }

    private fun resolveDefaultSize(spec: Int): Int =
        when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> DEFAULT_SIZE.px
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }

    override fun onDraw(canvas: Canvas) {
        canvas.drawOval(viewRect.toRectF(), avatarPaint)

        val half = (borderWidth / 2).toInt()
        viewRect.inset(half, half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
    }
}