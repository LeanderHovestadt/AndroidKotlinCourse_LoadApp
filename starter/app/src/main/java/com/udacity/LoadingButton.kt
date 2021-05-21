package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // progress of the downloading state, goes from 0 to 100
    private var progress = 0F

    private lateinit var valueAnimator: ValueAnimator

    // colors
    private var buttonDefaultColor = 0
    private var buttonLoadingColor = 0
    private var buttonCircleColor = 0

    // dimens
    private val buttonTextSize = resources.getDimension(R.dimen.default_text_size)
    private val circleOffset = resources.getDimension(R.dimen.circleOffset)
    private val circleRadius = resources.getDimension(R.dimen.circleRadius)

    // states
    private var buttonState: ButtonState by Delegates.observable(ButtonState.COMPLETED) { _, old, new ->
        when (new){
            ButtonState.COMPLETED -> {
                valueAnimator.cancel()
            }

            ButtonState.LOADING -> {
                if (old != ButtonState.LOADING){
                    valueAnimator.start()
                }
            }

            ButtonState.CLICKED -> {
                valueAnimator.cancel()
                buttonState = ButtonState.LOADING
            }
        }

        requestLayout()
        invalidate()
    }


    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonDefaultColor = getColor(R.styleable.LoadingButton_buttonColorDefault, 0)
            buttonLoadingColor = getColor(R.styleable.LoadingButton_buttonColorLoading, 0)
            buttonCircleColor = getColor(R.styleable.LoadingButton_buttonColorCircle, 0)
        }

        valueAnimator = ValueAnimator.ofFloat(0F, 20F, 40F, 60F, 62F, 63F, 64F, 68F, 72F, 77F, 80F, 85F, 90F, 95F, 100F).setDuration(3000).apply {
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    buttonState = ButtonState.COMPLETED
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    progress = 0F
                }
            })
            interpolator = LinearInterpolator()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when(buttonState){
            ButtonState.CLICKED -> onDrawButtonLoading(canvas)
            ButtonState.COMPLETED -> onDrawButtonCompleted(canvas)
            ButtonState.LOADING -> onDrawButtonLoading(canvas)
        }

    }

    private var textLoading = context.getString(R.string.button_loading)
    private var textCompleted = context.getString(R.string.button_completed)
    private fun onDrawButtonLoading(canvas: Canvas) {
        // paint the default color
        paint.color = buttonDefaultColor
        val rect = RectF(0.0F, 0.0F, canvas.width.toFloat(), canvas.height.toFloat())
        canvas.drawRect(rect, paint)

        // paint the loading color depending on the progress
        paint.color = buttonLoadingColor
        val progressWidth = progress.toFloat() / 100F * canvas.width.toFloat()
        val rectLoading = RectF(0.0F, 0.0F, progressWidth, canvas.height.toFloat())
        canvas.drawRect(rectLoading, paint)

        // paint the loading circle depending on the progress
        paint.color = buttonCircleColor
        val textRect = Rect()
        paint.getTextBounds(textLoading, 0, textLoading.length, textRect)
        val circleX = canvas.width / 2 + textRect.width() / 2 + circleOffset
        val circleY = canvas.height / 2 - circleRadius
        canvas.drawArc(circleX, circleY, circleX + 2 * circleRadius, circleY + 2* circleRadius, +270F, progress / 100F * 360F, true, paint)

        // paint the text
        paint.color = Color.WHITE
        val h = (canvas.height / 2) - (paint.descent() + paint.ascent() / 2)
        val w = canvas.width / 2
        canvas.drawText(textLoading, w.toFloat(), h, paint)
    }

    private fun onDrawButtonCompleted(canvas: Canvas) {
        // paint the button
        paint.color = buttonDefaultColor
        val rect = RectF(0.0F, 0.0F, canvas.width.toFloat(), canvas.height.toFloat())
        canvas.drawRect(rect, paint)

        // paint the text
        paint.color = Color.WHITE
        val h = (canvas.height / 2) - (paint.descent() + paint.ascent() / 2)
        val w = canvas.width / 2
        canvas.drawText(textCompleted, w.toFloat(), h, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        setMeasuredDimension(w, h)
    }

    fun onClicked(){
        buttonState = ButtonState.CLICKED
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = buttonTextSize
        typeface = Typeface.create( "", Typeface.NORMAL)
    }

}
