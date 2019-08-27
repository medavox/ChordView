/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.airsaid.library.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.github.airsaid.library.R
import com.github.airsaid.library.widget.ChordView.ShowMode.SIMPLE_SHOW_MODE

/**
 * Android custom view for rendering guitar chords.
 *
 * @author airsaid
 */
class ChordView @JvmOverloads constructor(context:Context,
                attrs:AttributeSet?=null,
                private var mChord:Chord=Chord.defaultC,
                defStyleAttr:Int=0)
    : View(context, attrs, defStyleAttr) {

    //constructor(context: Context, attrs: AttributeSet?=null) :this(context, attrs)

    /** Number of strings */
    private val NUM_STRINGS:Int = 6
    /** Number of frets */
    private val FRET:Int = 4

    enum class ShowMode {
        /** Default display mode */
        NORMAL_SHOW_MODE,
        /** Simple display mode, only three frets are displayed by default */
        SIMPLE_SHOW_MODE
    }
    fun showModeFrom(int:Int):ShowMode {
        return ShowMode.values().first { it.ordinal == int }
    }
    /** 显示模式 */
    private var mShowMode :ShowMode

    /** A picture representing a closed string symbol.
    If it is NULL, the closed string prompt symbol will not be drawn. */
    private var mClosedStringBitmap:Bitmap
    /** A picture representing an empty string symbol.
    If it is NULL, the empty string prompt symbol will not be drawn. */
    var mEmptyStringBitmap:Bitmap
    /** Y-axis offset of empty string and closed string hints */
    private var mStringOffsetY :Float

    /** 琴头弧度 */
    var mHeadRadius :Float =0F
    /** 琴头颜色 */
    private var mHeadColor :Int

    /** 品文字大小 */
    private var mFretTextSize :Float
    /** 品文字颜色 */
    private var mFretTextColor :Int
    /** 品文字 x 轴偏移量 */
    private var mFretTextOffsetX :Float

    /** 网格线的宽度 */
    private var mGridLineWidth :Float
    /** 网格线的颜色 */
    private var mGridLineColor :Int

    /** 节点颜色 */
    private var mNoteColor :Int
    /** 节点圆的半径 */
    private var mNoteRadius :Float
    /** 节点文字大小 */
    private var mNoteTextSize :Float
    /** 节点文字颜色 */
    private var mNoteTextColor :Int
    /** 节点边框 */
    private var mNoteStrokeWidth :Float
    /** 节点边框颜色 */
    private var mNoteStrokeColor :Int
    /** 节点透明度 */
    @android.support.annotation.IntRange(from = 0, to = 255)
    private var mNoteAlpha :Int

    /** 横按区域颜色 */
    private var mBarreColor :Int
    /** 横按区域透明度 */
    @android.support.annotation.IntRange(from = 0, to = 255)
    private var mBarreAlpha :Int
    /** 横按区域边框 */
    var mBarreStrokeWidth :Float
    /** 横按区域边框颜色 */
    private var mBarreStrokeColor :Int

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mHeadPath = Path()

    init {
        mPaint.color = Color.WHITE
        val a:TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ChordView)
        mShowMode = showModeFrom(a.getInt(R.styleable.ChordView_cv_showMode, ShowMode.NORMAL_SHOW_MODE.ordinal))
        mClosedStringBitmap = BitmapFactory.decodeResource(resources,
                a.getResourceId(R.styleable.ChordView_cv_closedStringImage, 0))
        mEmptyStringBitmap = BitmapFactory.decodeResource(resources,
                a.getResourceId(R.styleable.ChordView_cv_emptyStringImage, 0))
        mStringOffsetY = a.getDimension(R.styleable.ChordView_cv_stringOffsetY, 0f)
        mHeadRadius = a.getDimension(R.styleable.ChordView_cv_headRadius, 0f)
        mHeadColor = a.getColor(R.styleable.ChordView_cv_headColor, Color.WHITE)
        mFretTextSize = a.getDimension(R.styleable.ChordView_cv_fretTextSize, 40f)
        mFretTextColor = a.getColor(R.styleable.ChordView_cv_fretTextColor, Color.WHITE)
        mFretTextOffsetX = a.getDimension(R.styleable.ChordView_cv_fretTextOffsetX, 0f)
        mGridLineWidth = a.getDimension(R.styleable.ChordView_cv_gridLineWidth, 10f)
        mGridLineColor = a.getColor(R.styleable.ChordView_cv_gridLineColor, Color.WHITE)
        mNoteColor = a.getColor(R.styleable.ChordView_cv_noteColor, Color.WHITE)
        mNoteRadius = a.getDimension(R.styleable.ChordView_cv_noteRadius, 40f)
        mNoteTextSize = a.getDimension(R.styleable.ChordView_cv_noteTextSize, 40f)
        mNoteTextColor = a.getColor(R.styleable.ChordView_cv_noteTextColor, Color.BLACK)
        mNoteStrokeWidth = a.getDimension(R.styleable.ChordView_cv_noteStrokeWidth, 0f)
        mNoteStrokeColor = a.getColor(R.styleable.ChordView_cv_noteStrokeColor, Color.WHITE)
        mNoteAlpha = a.getInt(R.styleable.ChordView_cv_noteAlpha, 255)
        mBarreColor = a.getColor(R.styleable.ChordView_cv_barreColor, Color.WHITE)
        mBarreAlpha = a.getInt(R.styleable.ChordView_cv_barreAlpha, 255)
        mBarreStrokeWidth = a.getDimension(R.styleable.ChordView_cv_barreStrokeWidth, 0f)
        mBarreStrokeColor = a.getColor(R.styleable.ChordView_cv_barreStrokeColor, Color.WHITE)
        a.recycle()
    }

    /**
     * Sets the chord object and starts rendering based on the chord.
     *
     * @param chord Chord object.
     */
    fun setChord(chord:Chord) {
        mChord = chord
        invalidate()
    }

    /**
     * Get the current chord object.
     *
     * @return Chord object.
     */
    fun getChord():Chord {
        return mChord
    }

    /**
     * Set the display mode. 
     The parameters that can be set are 
     the default {@link #NORMAL_SHOW_MODE} and 
     {@link #SIMPLE_SHOW_MODE}。
     *
     * @param mode Display mode
     */
    fun setShowMode(mode:ShowMode) {
        mShowMode = mode
        invalidate()
    }

    /**
     * 获取显示模式。
     *
     * @return 显示模式。
     */
    fun getShowMode():ShowMode {
        return mShowMode
    }


    protected override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        drawClosedEmptyString(canvas)
        drawFrets(canvas)
        drawHead(canvas)
        drawGrid(canvas)
        drawNotes(canvas)
//        drawDebug(canvas)
    }

    /**
     * Draw closed and empty strings.
     *
     * @param canvas 画布对象
     */
    private fun drawClosedEmptyString(canvas:Canvas) {
        if (!isDrawStrings()) return

        for (i in 0 until NUM_STRINGS) {
            val bitmap = getStringBitmap(i) ?: continue
            val left:Float = getFretWidth() - bitmap.getWidth() / 2 + (getGridColumnWidth() * i)
            val top:Float = Math.max(bitmapHeight(mClosedStringBitmap)
                    , bitmapHeight(mEmptyStringBitmap)) / 2F
            canvas.drawBitmap(bitmap, left, top, mPaint)
        }
    }

    private fun bitmapHeight(bitmap: Bitmap?): Int {
        return bitmap?.height ?: 0
    }

    /**
     * Draw the text.
     *
     * @param canvas Canvas object
     */
    private fun drawFrets(canvas:Canvas) {
        //If the highest product does not exceed 4 items, it will not be drawn.
        if (!isExceedDefaultFret()) {
            return
        }

        mPaint.style = Paint.Style.FILL
        mPaint.textSize = mFretTextSize
        mPaint.color = mFretTextColor
        mPaint.alpha = 255

        var index = 1
        val leastFret = getLeastFret()
        val fretWidth:Float = getFretWidth()

        for (i in leastFret until NUM_STRINGS + leastFret) {
            val fret:String = i.toString()
            val fretTextWidth:Float = mPaint.measureText(fret)
            val x:Float = fretWidth - fretTextWidth - mFretTextOffsetX
            val y:Float = getStringHeight() + getHeadHeight() + (getGridRowHeight() * index)
            canvas.drawText(fret, x, y, mPaint)
            // In simple mode, if there are more than three items in the chord,
            // only the first item number will be displayed.
            if (mShowMode == SIMPLE_SHOW_MODE) {
                // Do not continue drawing
                break
            }
            index++
        }
    }

    /**
     * Draw the head.
     *
     * @param canvas Canvas object
     */
    private fun drawHead(canvas:Canvas) {
        if (!isDrawHead()) return

        mPaint.setStyle(Paint.Style.FILL)
        mPaint.setColor(mHeadColor)

        val width = getGridWidth()
        val x = getFretWidth()
        val y = getStringHeight()
        mHeadPath.rewind()
        mHeadPath.moveTo(x, y + mHeadRadius)
        mHeadPath.quadTo(x, y, x + mHeadRadius, y)
        mHeadPath.lineTo(x + width - mHeadRadius, y)
        mHeadPath.quadTo(x + width, y, x + width, y + mHeadRadius)
        canvas.drawPath(mHeadPath, mPaint)
    }

    /**
     * 绘制指板网格。
     *
     * @param canvas 画布对象
     */
    private fun drawGrid(canvas:Canvas) {
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setStrokeWidth(mGridLineWidth)
        mPaint.setColor(mGridLineColor)

        val row = getRow()
        val width = getGridWidth()
        val height = getGridHeight()
        var x = getFretWidth()
        val y = getStringHeight() + getHeadHeight()
        // Draw a horizontal line
        var ry = y
        val rh = (height - mGridLineWidth * (row + 1)) / (row)
        for (i in 0 .. row) {
            ry = if(i != 0) ry + rh + mGridLineWidth else ry + mGridLineWidth / 2
            canvas.drawLine(x, ry, x + width, ry, mPaint)
        }
        // Draw vertical lines
        val cw = (width - mGridLineWidth * NUM_STRINGS) / (NUM_STRINGS - 1)
        for (i in 0 until NUM_STRINGS) {
            x = if(i != 0) x + cw + mGridLineWidth else x + mGridLineWidth / 2
            canvas.drawLine(x, y, x, y + height, mPaint)
        }
    }

    /**
     * Draw a node.
     *
     * @param canvas 画布对象
     */
    private fun drawNotes(canvas:Canvas) {
        if (mChord == null) return

        // Draw a barre
        val barreChord:IntArray? = mChord.getBarreChordData()
        var barreFret = 0
        var barreString = 0
        // Determine if there is a horizontal situation
        if (barreChord != null) {
            // With a barre, draw a horizontal rectangle
            barreFret = barreChord[0]
            barreString = barreChord[1]

            mPaint.setStyle(Paint.Style.FILL)
            mPaint.setColor(mBarreColor)
            mPaint.setAlpha(mBarreAlpha)

            val left = getFretWidth() + mGridLineWidth / 2 + (getGridColumnWidth() * (NUM_STRINGS - barreString))
            var top = getStringHeight() + getHeadHeight()
            if (isExceedDefaultFret()) {
                // Displayed at the 1 fret position
                top += getGridRowHeight() / 2 - mNoteRadius
            } else {
                // Displayed in the smallest position
                top += getGridRowHeight() * barreFret - (getGridRowHeight() / 2) - mNoteRadius
            }
            val right = left + getGridColumnWidth() * (barreString - 1)
            val bottom = top + mNoteRadius * 2
            canvas.drawRect(left, top, right, bottom, mPaint)

            // Draw a horizontal border
            if (mBarreStrokeWidth > 0) {
                mPaint.setAlpha(255)
                mPaint.setColor(mBarreStrokeColor)
                mPaint.setStyle(Paint.Style.STROKE)
                mPaint.setStrokeWidth(mBarreStrokeWidth)
                canvas.drawLine(left, top + mBarreStrokeWidth / 2, right, top + mBarreStrokeWidth / 2, mPaint)
                canvas.drawLine(left, bottom - mBarreStrokeWidth / 2, right, bottom - mBarreStrokeWidth / 2, mPaint)
            }

            // Draw horizontally on both ends of the node
            drawNote(canvas, barreFret, NUM_STRINGS, if(mChord.fingers != null)  1 else 0, 255, 0F, 0)
            drawNote(canvas, barreFret, NUM_STRINGS - (barreString - 1), if(mChord.fingers != null)  1 else 0, 255, 0F, 0)
        }
        // Draw other nodes
        for (index in mChord.frets.indices) {
            val fret = mChord.frets[index]
            // Do not draw closed and empty strings
            if (fret < 1) {
                continue
            }
            // Do not draw nodes that are barred
            if (barreChord != null && barreFret == fret && mChord.frets.size - index <= barreString) {
                continue
            }
            drawNote(canvas, mChord.frets[index], index + 1, if(mChord.fingers != null) mChord.fingers[index] else 0, mNoteAlpha, mNoteStrokeWidth, mNoteStrokeColor)
        }
    }

    private fun drawNote(canvas:Canvas, fret:Int, string:Int, finger:Int, alpha:Int,
                         strokeWidth:Float, strokeColor:Int) {
        mPaint.setStyle(Paint.Style.FILL)
        mPaint.setColor(mNoteColor)
        mPaint.setAlpha(alpha)
        // Draw a solid circle of nodes
        val columnWidth = getGridColumnWidth()
        val rowHeight = getGridRowHeight()
        val cx = ((getFretWidth() + mGridLineWidth / 2) + (columnWidth * (string - 1)))
                - if(string == NUM_STRINGS) mGridLineWidth else mGridLineWidth / 2

        var f = 1
        val leastFret = getLeastFret()
        if (isExceedDefaultFret()) {
            if (fret != leastFret) {
                val result = fret % leastFret
                f = if(result != 0) result + 1 else fret - leastFret + 1
            }
        } else {
            f = fret
        }
        val cy = (getStringHeight() + getHeadHeight()) + (rowHeight * f) - (rowHeight / 2)
        canvas.drawCircle(cx, cy, mNoteRadius, mPaint)
        // Draw node text
        if (mShowMode != SIMPLE_SHOW_MODE && finger > 0) {
            mPaint.color = mNoteTextColor
            mPaint.textSize = mNoteTextSize
            val fingerStr = finger.toString()
            canvas.drawText(fingerStr, cx - mPaint.measureText(fingerStr) / 2,
                    cy - (mPaint.ascent() + mPaint.descent()) / 2, mPaint)
        }
        // Draw a node border
        if (strokeWidth > 0) {
            mPaint.strokeWidth = strokeWidth
            mPaint.style = Paint.Style.STROKE
            mPaint.color = strokeColor
            mPaint.alpha = 255
            canvas.drawCircle(cx, cy, mNoteRadius, mPaint)
        }
    }

    /**
     * 获取品文字区域的宽度。
     *
     * @return 品文字区域的宽度。
     */
    private fun getFretWidth() :Float {
        if (mChord != null) {
            mPaint.textSize = mFretTextSize
            val largestFret = (getLeastFret() + getRow() - 1).toString()
            return mPaint.measureText(largestFret) + mFretTextOffsetX
        }
        return 0f
    }

    /**
     * 获取最小品。
     *
     * @return 最小品。默认 1。
     */
    private fun getLeastFret() :Int {
        return mChord?.getLeastFret() ?: 1
    }

    /**
     * 获取最大品。
     *
     * @return 最大品，默认为 1。
     */
    private fun getLargestFret() :Int {
        return mChord.getLargestFret()
    }

    /**
     * Get the corresponding empty or closed string representation picture
     * Bitmap by the specified string.
     *
     * @param string String (0 = 6 strings, 1 = 5 strings, and so on)
     * @return Empty or closed strings represent pictures.
     */
    private fun getStringBitmap(string:Int):Bitmap? {
        val fret:Int = mChord.frets[string]
        if (fret == -1) { // Closed string
            return mClosedStringBitmap
        } else if (fret == 0) { // Empty string
            return mEmptyStringBitmap
        }
        return null
    }

    /**
     * 获取弦区域的高度。
     *
     * @return 弦区域的高度。
     */
    private fun getStringHeight():Float {
        return if(isDrawStrings())  Math.max(bitmapHeight(mClosedStringBitmap),
                bitmapHeight(mEmptyStringBitmap)) + mStringOffsetY else 0f
    }

    /**
     * Whether you need to draw a string area.
     *
     * @return Returns true, indicating that you need to draw,
     no need to draw to return false.
     */
    private fun isDrawStrings():Boolean {
        // As long as there are closed or empty strings in the chord,
        //you need to draw
        return mChord != null && (mChord.isClosedString() || mChord.isEmptyString())
    }

    /**
     * Whether you need to draw the head.
     *
     * @return Returns true, indicating that you need to draw,
     //no need to draw to return false.
     */
    private fun isDrawHead():Boolean {
        // If the maximum fret does not exceed 5 items,
        //it is considered to be drawn.
        return mChord != null && mChord.getLargestFret() <= 5
    }

    /**
     * 获取琴头高度。
     *
     * @return 琴头的高度，如果琴头不绘制时直接返回 0。
     */
    private fun getHeadHeight() :Float {
        return if(isDrawHead()) mHeadRadius else 0F
    }

    /**
     * 获取网格区域宽度。
     *
     * @return 网格区域宽度。
     */
    private fun getGridWidth() :Float {
        return getWidth() - getFretWidth() - mNoteRadius
    }

    /**
     * 获取网格区域高度。
     *
     * @return 网格区域高度。
     */
    private fun getGridHeight() :Float {
        return getHeight() - getStringHeight() - getHeadHeight()
    }

    /**
     * 获取网格每格的宽度。
     *
     * @return 网格每格的宽度。
     */
    private fun getGridColumnWidth() :Float {
        val column = NUM_STRINGS - 1
        return getGridWidth() / column
    }

    /**
     * 获取网格每格的高度。
     *
     * @return 网格每格的高度。
     */
    private fun getGridRowHeight() :Float {
        return getGridHeight() / getRow()
    }

    /**
     * Get the number of rows.
     *
     * @return Row
     */
    private fun getRow():Int {
        //In simple mode, if the span of the largest and smallest products in
        // the chord does not exceed three products,
        //and the first product is the lowest product,
        //the number of lines is three lines.
        if (mShowMode == SIMPLE_SHOW_MODE) {
            val leastFret = getLeastFret()
            val largestFret = getLargestFret()
            val diffFret = largestFret - leastFret
            if (diffFret < 3 && leastFret == 1) {
                return 3
            }
        }
        return 4
    }

    /**
     * Whether the maximum fret exceeds the default display.
     *
     * @return Returns true if it is exceeded, otherwise returns false.
     */
    private fun isExceedDefaultFret():Boolean {
        return getLargestFret() > FRET
    }

    private fun drawDebug(canvas:Canvas) {
        // draw grid rect
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.RED
        mPaint.strokeWidth = 2f
        val left = getFretWidth()
        val top = getStringHeight() + getHeadHeight()
        canvas.drawRect(left, top, left + getGridWidth(), top + getGridHeight(), mPaint)
    }

}
