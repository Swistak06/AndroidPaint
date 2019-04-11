package com.swistak.androidpaint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.EmbossMaskFilter
import android.graphics.MaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View

import java.util.ArrayList
import android.content.ContextWrapper
import android.app.Activity
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import java.io.File.separator
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory
import android.os.Environment.MEDIA_MOUNTED
import java.io.File
import java.io.FileOutputStream
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import java.io.OutputStream
import java.lang.Exception


class PaintView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()
    private var mPath: Path? = null
    private val mPaint: Paint
    private val paths = ArrayList<FingerPath>()
    private var currentColor: Int = 0
    private var backgroundCol = DEFAULT_BG_COLOR
    private var strokeWidth: Int = 0
    private var emboss: Boolean = false
    private var blur: Boolean = false
    private val mEmboss: MaskFilter
    private val mBlur: MaskFilter
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private var activity : MainActivity? = null
    private var isEraserEnabled = false
    private var colorOfBrush = Color.RED

    init {
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = DEFAULT_COLOR
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.xfermode = null
        mPaint.alpha = 0xff

        mEmboss = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 6f, 3.5f)
        mBlur = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
    }

    fun init(metrics: DisplayMetrics) {
        val height = metrics.heightPixels
        val width = metrics.widthPixels

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)

        currentColor = DEFAULT_COLOR
        colorOfBrush = currentColor
        strokeWidth = BRUSH_SIZE
    }

    fun normal() {
        emboss = false
        blur = false
    }

    fun emboss() {
        emboss = true
        blur = false
    }

    fun blur() {
        emboss = false
        blur = true
    }

    fun undo(){
        val index = paths.lastIndex
        if(index>=0){
            paths.removeAt(index)
            invalidate()
        }
    }

    fun clear() {
        backgroundCol = DEFAULT_BG_COLOR
        paths.clear()
        normal()
        invalidate()
    }

    override fun onDraw(canvas: Canvas){
        canvas.save()
        mCanvas!!.drawColor(backgroundCol)

        for (fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            mPaint.maskFilter = null

            if (fp.emboss)
                mPaint.maskFilter = mEmboss
            else if (fp.blur)
                mPaint.maskFilter = mBlur

            mCanvas!!.drawPath(fp.path, mPaint)
        }

        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        activity!!.changeUndoArrowColor()
        activity!!.hideMenu()
        mPath = Path()
        val fp = FingerPath(currentColor, emboss, blur, strokeWidth, mPath!!)
        paths.add(fp)

        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath!!.lineTo(mX, mY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }

        return true
    }

    companion object {

        var BRUSH_SIZE = 60
        val DEFAULT_COLOR = Color.BLACK
        val DEFAULT_BG_COLOR = Color.WHITE
        private val TOUCH_TOLERANCE = 4f
    }

    fun getPaths(): ArrayList<FingerPath>{
        return paths;
    }

    fun setCurrentActivity(_activity: MainActivity) {
        this.activity = _activity
    }

    fun getCurrentActivity(): Activity? {
        return activity
    }

    fun saveAsImage(filename : String){

        var bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val dir = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!dir.exists()){
            dir.mkdirs()
        }
        val output = File(dir,"$filename.jpg")
        var os : OutputStream? = null

        try{
            os = FileOutputStream(output)
            this.draw(canvas)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,os)

            os.flush()
            os.close()
        }
        catch(e : Exception){
            e.printStackTrace()
        }


    }

    fun switchBetweenBrushAndEraser(){
        if(!isEraserEnabled){
            currentColor = backgroundCol
            isEraserEnabled = true
        }
        else{
            currentColor = colorOfBrush
            isEraserEnabled = false
        }
        activity!!.switchBrushAndEraserIcons(isEraserEnabled)
    }

    fun getCurrentBrushColor() : Int{
        return currentColor
    }

    fun setBrushColor(color : Int) {
        currentColor = color
    }
}
