package com.zhangyf.storypath.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.support.v4.util.LruCache
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.zhangyf.storypath.App
import com.zhangyf.storypath.R
import com.zhangyf.storypath.bean.StoryBean
import java.lang.ref.WeakReference


/**
 * 故事线
 *
 * @author zhangyf
 * @date 2018/5/11 0011.
 */
class StoryPathView : View {

    val MAX_COLUMN_DEFAULT = 3
    val RADIUS_DEFAULT = App.screenWidth * 0.09f
    val OFFSET_X_DEFAULT = App.screenWidth * 0.15f
    val OFFSET_Y_DEFAULT = (App.screenWidth * 0.15f)
    val LINE_STROKE_WIDTH_DEFAULT = 30f
    val CORNER_STROKE_WIDTH_DEFAULT = 8f
    val LINE_STROKE_COLOR_DEFAULT = Color.GRAY
    val BORDER_COLOR_DEFAULT = R.color.colorBoder
    val COVER_COLOR_DEFAULT = R.color.colorCover
    val LOCK_DRAWABLE_DEFAULT = R.drawable.lock_icon_46
    var viewWidth: Int = 0
    var viewHeight: Int = 0
    var linePaint: Paint? = null
    var mContext: Context? = null
    var mPath: Path? = null
    var mTextPaint: TextPaint? = null
    var measureWidth: Int = 0
    var measureHeight: Int = 0

    var downX: Float = 0.toFloat()
    var downY: Float = 0.toFloat()
    var upX: Float = 0.toFloat()
    var upY: Float = 0.toFloat()
    var downPos: Int = 0
    var upPos: Int = 0
    var dataList: MutableList<StoryBean> = mutableListOf()

    var mScale: Float = 0.toFloat() //图片的缩放比例
    var MAX_COLUMN_COUNT = MAX_COLUMN_DEFAULT
    var CIRCLE_RADIUS = RADIUS_DEFAULT
    var OFFSET_X = OFFSET_X_DEFAULT
    var OFFSET_Y = OFFSET_Y_DEFAULT
    var DELTA_Y = (CIRCLE_RADIUS * 2.4).toFloat()
    var LINE_STROKE_WIDTH = LINE_STROKE_WIDTH_DEFAULT
    var CORNER_STROKE_WIDTH = CORNER_STROKE_WIDTH_DEFAULT
    var LINE_STROKE_COLOR = LINE_STROKE_COLOR_DEFAULT
    var BORDER_COLOR = BORDER_COLOR_DEFAULT
    var COVER_COLOR = COVER_COLOR_DEFAULT
    var LOCK_DRAWABLE = LOCK_DRAWABLE_DEFAULT

    var listener:((Boolean) -> Unit)? = null

    private var mMemoryCache: LruCache<String, Bitmap>? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        MAX_COLUMN_COUNT = attrs?.getAttributeIntValue(R.styleable.StoryPathView_max_column, MAX_COLUMN_DEFAULT) as Int
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
        MAX_COLUMN_COUNT = attrs?.getAttributeIntValue(R.styleable.StoryPathView_max_column, MAX_COLUMN_DEFAULT) as Int
    }

    private fun init(context: Context) {
        linePaint = Paint()
        linePaint!!.color = LINE_STROKE_COLOR
        linePaint!!.strokeWidth = LINE_STROKE_WIDTH
        linePaint!!.isAntiAlias = true
        linePaint!!.style = Paint.Style.STROKE
        linePaint!!.pathEffect = CornerPathEffect(10f)

        mContext = context

        mPath = Path()

        mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
        mTextPaint!!.color = Color.WHITE

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        val cacheSize = maxMemory / 4

        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return bitmap!!.byteCount / 1024
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        measureWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        viewWidth = measureWidth
        measureHeight = (OFFSET_Y + DELTA_Y * (dataList.size - 1) + CIRCLE_RADIUS * 2).toInt()
        viewHeight = measureHeight
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawView(canvas)

    }

    private fun initData() {
        for (i in dataList.indices) {
            val pointF = dataList[i]

            if (i / MAX_COLUMN_COUNT % 2 == 0) {
                pointF.x = (i % MAX_COLUMN_COUNT + 1)
            } else {
                pointF.x = (MAX_COLUMN_COUNT - i % MAX_COLUMN_COUNT)
            }

            pointF.y = ((OFFSET_Y / 2 + CIRCLE_RADIUS + DELTA_Y * i).toInt())

            dataList[i] = pointF
        }
    }

    private fun initYData() {
        for (i in dataList.indices) {
            val pointF = dataList[i]

            pointF.y = ((OFFSET_Y / 2 + CIRCLE_RADIUS + DELTA_Y * i).toInt())

            dataList[i] = pointF
        }
    }

    private fun drawView(canvas: Canvas) {
        mPath!!.reset()

        if (dataList.size == 0) {
            return
        } else {
            /*
             * 生成Path和绘制Point
             */
            for (i in dataList.indices) {
                // 计算x坐标
                val x = OFFSET_X / 2 + ((viewWidth - OFFSET_X) / (MAX_COLUMN_COUNT) * dataList[i].x) - (viewWidth - OFFSET_X) / (MAX_COLUMN_COUNT) / 2
                // 计算y坐标
                val y = dataList[i].y.toFloat()
                /*
                 * 如果是第一个点则将其设置为Path的起点
                 */
                if (i == 0) {
                    mPath!!.moveTo(x, y)
                }

                // 连接各点
                mPath!!.lineTo(x, y)
            }

            // 将Path绘制到我们自定的Canvas上
            canvas.drawPath(mPath!!, linePaint!!)

            for (i in dataList.indices) {
                var index = i
                val bitmapPaint = Paint()
                bitmapPaint.isAntiAlias = true
                bitmapPaint.isFilterBitmap = true
                if (getBitmapFromMemCache(dataList[i].cover) == null) {
                    Glide.with(mContext!!)
                            .asBitmap()
                            .load(dataList[index].cover)
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                    addBitmapToMemoryCache(dataList[i].cover,bitmap)
                                    invalidate()
                                }
                            })
                }

                // 计算x坐标
                val x = OFFSET_X / 2 + ((viewWidth - OFFSET_X) / (MAX_COLUMN_COUNT) * dataList[index].x) - (viewWidth - OFFSET_X) / (MAX_COLUMN_COUNT) / 2
                // 计算y坐标
                val y = dataList[index].y.toFloat()

                val cache = getBitmapFromMemCache(dataList[i].cover)
                if (cache != null) {
                    var dimensionX = Math.min(cache.width!!, cache.height!!)
                    var bitmap = ThumbnailUtils.extractThumbnail(cache, dimensionX, dimensionX)

                    canvas.save()
                    canvas.translate(x - CIRCLE_RADIUS, y - CIRCLE_RADIUS)

                    //初始化BitmapShader，传入bitmap对象
                    val bitmapShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

                    //计算缩放比例
                    mScale = CIRCLE_RADIUS * 2f / dimensionX
                    val matrix = Matrix()
                    matrix.postScale(mScale, mScale)
                    bitmapShader.setLocalMatrix(matrix)

                    bitmapPaint.shader = bitmapShader

                    // 绘制Point
                    canvas.drawCircle(CIRCLE_RADIUS, CIRCLE_RADIUS, CIRCLE_RADIUS, bitmapPaint)
                    canvas.restore()

                }

                val mBorderPaint = Paint()
                mBorderPaint.style = Paint.Style.STROKE
                mBorderPaint.isAntiAlias = true
                mBorderPaint.color = resources.getColor(BORDER_COLOR)
                mBorderPaint.strokeWidth = CORNER_STROKE_WIDTH
                canvas.drawCircle(x, y, CIRCLE_RADIUS, mBorderPaint)

                if (dataList[index].lockStatus == 0) {
                    val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
                    mBorderPaint.isAntiAlias = true
                    coverPaint.style = Paint.Style.FILL
                    coverPaint.color = resources.getColor(COVER_COLOR)

                    canvas.drawCircle(x, y, CIRCLE_RADIUS, coverPaint)

                    val icPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
                    var icBitmap = drawableToBitmap(resources.getDrawable(LOCK_DRAWABLE) as BitmapDrawable)
                    canvas.drawBitmap(icBitmap, x - icBitmap.width / 2, y - icBitmap.height / 2, icPaint)
                }
            }
        }
    }

    private fun drawableToBitmap(drawable: BitmapDrawable): Bitmap {
        var w = drawable.intrinsicWidth
        var h = drawable.intrinsicHeight
        var bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 获取点击屏幕时的点的坐标
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downPos = -1
                downX = event.x
                downY = event.y
                downPos = whichCircle(downX, downY)
                if (downPos != -1) {
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                upX = event.x
                upY = event.y
                upPos = whichCircle(upX, upY)
                if (downPos != -1 && downPos == upPos) {
                    if (dataList[downPos].lockStatus == 0) {
                        listener?.invoke(true)
                    } else {
                        listener?.invoke(false)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun whichCircle(x: Float, y: Float): Int {
        for (i in dataList.indices) {
            // 圆心X坐标
            val cx = OFFSET_X / 2 + ((measureWidth - OFFSET_X) / (MAX_COLUMN_COUNT + 1) * dataList[i].x)
            // 圆心Y坐标
            val cy = dataList[i].y.toFloat()

            if ((cx - x) * (cx - x) + (cy - y) * (cy - y) < CIRCLE_RADIUS * CIRCLE_RADIUS) {

                return i
            }
        }
        return -1
    }

    /**
     * 设置数据源设置默认线性排序
     */
    open fun setData(list: MutableList<StoryBean>) {
        dataList = list
        initData()
        invalidate()
    }

    /**
     * 设置数据源更改X位置(1..MAX_COLUMN_COUNT)
     */
    open fun setDataWithCustomX(list: MutableList<StoryBean>) {
        dataList = list
        initYData()
        invalidate()
    }

    /**
     * 设置数据源更改XY位置
     */
    open fun setDataWithCustomXY(list: MutableList<StoryBean>) {
        dataList = list
        invalidate()
    }

    /**
     * 设置一行的point个数
     */
    open fun setMaxColumn(max: Int) {
        MAX_COLUMN_COUNT = max
    }

    /**
     * 设置垂直圆心距离
     */
    open fun setDeltaY(delta: Int) {
        DELTA_Y = delta.toFloat()
    }

    /**
     * 设置左右边距和
     */
    open fun setOffsetX(offset: Int) {
        OFFSET_X = offset.toFloat()
    }

    /**
     * 设置上下边距和
     */
    open fun setOffsetY(offset: Int) {
        OFFSET_Y = offset.toFloat()
    }

    /**
     * 设置path线的宽度
     */
    open fun setStrokerWidth(width: Int) {
        LINE_STROKE_WIDTH = width.toFloat()
    }

    /**
     * 设置point边框宽度
     */
    open fun setBoderWidth(width: Int) {
        CORNER_STROKE_WIDTH = width.toFloat()
    }

    /**
     * 设置point边框宽度
     */
    open fun setLineStrokeColor(color: Int) {
        LINE_STROKE_COLOR = color
    }

    /**
     * 设置point边框颜色
     */
    open fun setBorderColor(color: Int) {
        BORDER_COLOR = color
    }

    /**
     * 设置Point上锁之后的cover色值
     */
    open fun setCoverColor(color: Int) {
        COVER_COLOR = color
    }

    /**
     * 设置Point上锁之后的图标
     */
    open fun setLockDrawable(drawable: Int) {
        LOCK_DRAWABLE = drawable
    }

    /**
     * 设置点击point监听
     */
    open fun setOnPointClickListener(listener: (Boolean)->Unit) {
        this.listener = listener
    }

    open fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache?.put(key, bitmap)
        }
    }

    open fun getBitmapFromMemCache(key: String): Bitmap? {
        return mMemoryCache?.get(key)
    }

}
