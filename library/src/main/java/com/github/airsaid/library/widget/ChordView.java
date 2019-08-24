/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.github.airsaid.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.view.View;
import com.github.airsaid.library.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Android custom view for rendering guitar chords.
 *
 * @author airsaid
 */
public class ChordView extends View {

    @IntDef({NORMAL_SHOW_MODE, SIMPLE_SHOW_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShowMode {}

    /** Number of strings */
    private static final int STRING = 6;
    /** Number of frets */
    private static final int FRET = 4;
    /** Default display mode */
    public static final int NORMAL_SHOW_MODE = 1;
    /** Simple display mode, only three products are displayed by default */
    public static final int SIMPLE_SHOW_MODE = 2;

    /** 显示模式 */
    @ShowMode private int mShowMode;

    /** A picture representing a closed string symbol. 
    If it is NULL, the closed string prompt symbol will not be drawn. */
    private Bitmap mClosedStringBitmap;
    /** A picture representing an empty string symbol. 
    If it is NULL, the empty string prompt symbol will not be drawn. */
    private Bitmap mEmptyStringBitmap;
    /** Y-axis offset of empty string and closed string hints */
    private float mStringOffsetY;

    /** 琴头弧度 */
    private float mHeadRadius;
    /** 琴头颜色 */
    private int mHeadColor;

    /** 品文字大小 */
    private float mFretTextSize;
    /** 品文字颜色 */
    private int mFretTextColor;
    /** 品文字 x 轴偏移量 */
    private float mFretTextOffsetX;

    /** 网格线的宽度 */
    private float mGridLineWidth;
    /** 网格线的颜色 */
    private int mGridLineColor;

    /** 节点颜色 */
    private int mNoteColor;
    /** 节点圆的半径 */
    private float mNoteRadius;
    /** 节点文字大小 */
    private float mNoteTextSize;
    /** 节点文字颜色 */
    private int mNoteTextColor;
    /** 节点边框 */
    private float mNoteStrokeWidth;
    /** 节点边框颜色 */
    private int mNoteStrokeColor;
    /** 节点透明度 */
    private int mNoteAlpha;

    /** 横按区域颜色 */
    private int mBarreColor;
    /** 横按区域透明度 */
    private int mBarreAlpha;
    /** 横按区域边框 */
    private float mBarreStrokeWidth;
    /** 横按区域边框颜色 */
    private int mBarreStrokeColor;

    private Chord mChord;
    private Paint mPaint;
    private ChordHelper mChordHelper;
    private Path mHeadPath = new Path();

    public ChordView(Context context) {
        this(context, null);
    }

    public ChordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        mChordHelper = new ChordHelper();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ChordView);
        setShowMode(a.getInt(R.styleable.ChordView_cv_showMode, NORMAL_SHOW_MODE));
        setClosedStringImage(a.getResourceId(R.styleable.ChordView_cv_closedStringImage, 0));
        setEmptyStringImage(a.getResourceId(R.styleable.ChordView_cv_emptyStringImage, 0));
        setStringOffsetY(a.getDimension(R.styleable.ChordView_cv_stringOffsetY, 0f));
        setHeadRadius(a.getDimension(R.styleable.ChordView_cv_headRadius, 0f));
        setHeadColor(a.getColor(R.styleable.ChordView_cv_headColor, Color.WHITE));
        setFretTextSize(a.getDimension(R.styleable.ChordView_cv_fretTextSize, 40f));
        setFretTextColor(a.getColor(R.styleable.ChordView_cv_fretTextColor, Color.WHITE));
        setFretTextOffsetX(a.getDimension(R.styleable.ChordView_cv_fretTextOffsetX, 0f));
        setGridLineWidth(a.getDimension(R.styleable.ChordView_cv_gridLineWidth, 10f));
        setGridLineColor(a.getColor(R.styleable.ChordView_cv_gridLineColor, Color.WHITE));
        setNoteColor(a.getColor(R.styleable.ChordView_cv_noteColor, Color.WHITE));
        setNoteRadius(a.getDimension(R.styleable.ChordView_cv_noteRadius, 40f));
        setNoteTextSize(a.getDimension(R.styleable.ChordView_cv_noteTextSize, 40f));
        setNoteTextColor(a.getColor(R.styleable.ChordView_cv_noteTextColor, Color.BLACK));
        setNoteStrokeWidth(a.getDimension(R.styleable.ChordView_cv_noteStrokeWidth, 0f));
        setNoteStrokeColor(a.getColor(R.styleable.ChordView_cv_noteStrokeColor, Color.WHITE));
        setNoteAlpha(a.getInt(R.styleable.ChordView_cv_noteAlpha, 255));
        setBarreColor(a.getColor(R.styleable.ChordView_cv_barreColor, Color.WHITE));
        setBarreAlpha(a.getInt(R.styleable.ChordView_cv_barreAlpha, 255));
        setBarreStrokeWidth(a.getDimension(R.styleable.ChordView_cv_barreStrokeWidth, 0f));
        setBarreStrokeColor(a.getColor(R.styleable.ChordView_cv_barreStrokeColor, Color.WHITE));
        a.recycle();
    }

    /**
     * Sets the chord object and starts rendering based on the chord.
     *
     * @param chord Chord object.
     */
    public void setChord(Chord chord) {
        mChord = chord;
        invalidate();
    }

    /**
     * Get the current chord object.
     *
     * @return Chord object.
     */
    public Chord getChord() {
        return mChord;
    }

    /**
     * Set the display mode. 
     The parameters that can be set are 
     the default {@link #NORMAL_SHOW_MODE} and 
     {@link #SIMPLE_SHOW_MODE}。
     *
     * @param mode Display mode
     */
    public void setShowMode(@ShowMode int mode) {
        mShowMode = mode;
        invalidate();
    }

    /**
     * 获取显示模式。
     *
     * @return 显示模式。
     */
    @ShowMode public int getShowMode() {
        return mShowMode;
    }

    /**
     * Set the picture of the closed string symbol by 
     * the image resource id.
     *
     * @param resId Closed string picture resource id
     */
    public void setClosedStringImage(@IdRes int resId) {
        if (resId == 0) return;
        setClosedStringBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    /**
     * Set the picture of the closed string symbol.
     *
     * @param bitmap Bitmap image
     */
    public void setClosedStringBitmap(Bitmap bitmap) {
        mClosedStringBitmap = bitmap;
    }

    /**
     * Get a picture of a closed string symbol.
     *
     * @return Closed string Bitmap image.
     */
    public Bitmap getClosedStringBitmap() {
        return mClosedStringBitmap;
    }

    /**
     * Set the picture of the empty string symbol with the image resource id.
     *
     * @param resId Empty string symbol picture resource id
     */
    public void setEmptyStringImage(@IdRes int resId) {
        if (resId == 0) return;
        setEmptyStringBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    /**
     * Set the picture of the empty string symbol.
     *
     * @param bitmap Bitmap Image.
     */
    public void setEmptyStringBitmap(Bitmap bitmap) {
        mEmptyStringBitmap = bitmap;
    }

    /**
     * Get a picture of an empty string symbol.
     *
     * @return Empty string Bitmap image.
     */
    public Bitmap getEmptyStringBitmap() {
        return mEmptyStringBitmap;
    }

    /**
     * Set the y-axis offset of the empty string and closed string hints
     *
     * @param offsetY Y-axis offset
     */
    public void setStringOffsetY(float offsetY) {
        mStringOffsetY = offsetY;
    }

    /**
     * Get the y-axis offset of the empty string and closed string hints
     *
     * @return Y-axis offset.
     */
    public float getStringOffsetY() {
        return mStringOffsetY;
    }

    /**
     * 设置琴头的弧度。
     *
     * @param radius 弧度
     */
    public void setHeadRadius(float radius) {
        mHeadRadius = radius;
    }

    /**
     * 获取琴头的弧度。
     *
     * @return 琴头弧度。
     */
    public float getHeadRadius() {
        return mHeadRadius;
    }

    /**
     * 设置琴头的颜色。
     *
     * @param color 琴头颜色
     */
    public void setHeadColor(@ColorInt int color) {
        mHeadColor = color;
    }

    /**
     * 获取琴头的颜色。
     *
     * @return 琴头颜色。
     */
    @ColorInt
    public int getHeadColor() {
        return mHeadColor;
    }

    /**
     * 设置品文字的大小。
     *
     * @param textSize 文字大小
     */
    public void setFretTextSize(float textSize) {
        mFretTextSize = textSize;
    }

    /**
     * 获取品文字的大小。
     *
     * @return 品文字大小。
     */
    public float getFretTextSize() {
        return mFretTextSize;
    }

    /**
     * 设置品文字的颜色。
     *
     * @param textColor 文字颜色
     */
    public void setFretTextColor(int textColor) {
        mFretTextColor = textColor;
    }

    /**
     * 获取品文字的颜色。
     *
     * @return 文字颜色。
     */
    public int getFretTextColor() {
        return mFretTextColor;
    }

    /**
     * 设置品文字 x 轴偏移量。
     *
     * @param offsetX x 轴偏移量
     */
    public void setFretTextOffsetX(float offsetX) {
        mFretTextOffsetX = offsetX;
    }

    /**
     * 获取品文字 x 轴偏移量。
     *
     * @return x 轴偏移量。
     */
    public float getFretTextOffsetX() {
        return mFretTextOffsetX;
    }

    /**
     * 设置网格线的宽度。
     *
     * @param lineWidth 网格线的宽度
     */
    public void setGridLineWidth(float lineWidth) {
        mGridLineWidth = lineWidth;
    }

    /**
     * 获取网格线的宽度。
     *
     * @return 网格线的宽度。
     */
    public float getGridLineWidth() {
        return mGridLineWidth;
    }

    /**
     * 设置网格线的颜色。
     *
     * @param lineColor 网格线颜色
     */
    public void setGridLineColor(int lineColor) {
        mGridLineColor = lineColor;
    }

    /**
     * 获取网格线的颜色。
     *
     * @return 网格线颜色。
     */
    public int getGridLineColor() {
        return mGridLineColor;
    }

    /**
     * 设置节点的颜色。
     *
     * @param noteColor 节点颜色
     */
    public void setNoteColor(int noteColor) {
        mNoteColor = noteColor;
    }

    /**
     * 获取节点的颜色。
     *
     * @return 节点颜色。
     */
    public int getNoteColor() {
        return mNoteColor;
    }

    /**
     * 设置节点圆的半径。
     *
     * @param radius 节点圆半径
     */
    public void setNoteRadius(float radius) {
        mNoteRadius = radius;
    }

    /**
     * 获取节点圆的半径。
     *
     * @return 节点圆半径。
     */
    public float getNoteRadius() {
        return mNoteRadius;
    }

    /**
     * 设置节点文字的大小。
     *
     * @param textSize 节点文字大小
     */
    public void setNoteTextSize(float textSize) {
        mNoteTextSize = textSize;
    }

    /**
     * 获取节点文字的大小。
     *
     * @return 节点文字大小。
     */
    public float getNoteTextSize() {
        return mNoteTextSize;
    }

    /**
     * 设置节点文字的颜色。
     *
     * @param textColor 节点文字颜色
     */
    public void setNoteTextColor(int textColor) {
        mNoteTextColor = textColor;
    }

    /**
     * 获取节点文字的颜色。
     *
     * @return 节点文字颜色。
     */
    public int getNoteTextColor() {
        return mNoteTextColor;
    }

    /**
     * 设置节点边框的宽度。
     *
     * @param width 节点边框宽度
     */
    public void setNoteStrokeWidth(float width) {
        mNoteStrokeWidth = width;
    }

    /**
     * 获取节点边框的宽度。
     *
     * @return 节点边框宽度。
     */
    public float getNoteStrokeWidth() {
        return mNoteStrokeWidth;
    }

    /**
     * 设置节点边框的颜色。
     *
     * @param color 节点边框颜色
     */
    public void setNoteStrokeColor(int color) {
        mNoteStrokeColor = color;
    }

    /**
     * 获取节点边框的颜色。
     *
     * @return 节点边框颜色。
     */
    public int getNoteStrokeColor() {
        return mNoteStrokeColor;
    }

    /**
     * 设置节点的透明度。
     *
     * @param alpha 节点透明度
     */
    public void setNoteAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mNoteAlpha = alpha;
    }

    /**
     * 获取节点的透明度。
     *
     * @return 节点透明度。
     */
    @IntRange(from = 0, to = 255)
    public int getNoteAlpha() {
        return mNoteAlpha;
    }

    /**
     * 设置横按区域的颜色。
     *
     * @param color 横按区域颜色
     */
    public void setBarreColor(int color) {
        mBarreColor = color;
    }

    /**
     * 获取横按区域的颜色。
     *
     * @return 横按区域颜色。
     */
    public int getBarreColor() {
        return mBarreColor;
    }

    /**
     * 设置横按区域的透明度。
     *
     * @param alpha 横按区域透明度
     */
    public void setBarreAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mBarreAlpha = alpha;
    }

    /**
     * 获取横按区域的透明度。
     *
     * @return 横按区域透明度。
     */
    @IntRange(from = 0, to = 255)
    public int getBarreAlpha() {
        return mBarreAlpha;
    }

    /**
     * 设置横按区域的边框大小。
     *
     * @param width 横按区域边框大小
     */
    public void setBarreStrokeWidth(float width) {
        mBarreStrokeWidth = width;
    }

    /**
     * 获取横按区域的边框大小。
     *
     * @return 横按区域边框大小。
     */
    public float getBarreStrokeWidth() {
        return mBarreStrokeWidth;
    }

    /**
     * 设置横按区域边框的颜色。
     *
     * @param color 横按区域边框颜色
     */
    public void setBarreStrokeColor(int color) {
        mBarreStrokeColor = color;
    }

    /**
     * 获取横按区域边框的颜色。
     *
     * @return 横按区域边框颜色。
     */
    public int getBarreStrokeColor() {
        return mBarreStrokeColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawClosedEmptyString(canvas);
        drawFrets(canvas);
        drawHead(canvas);
        drawGrid(canvas);
        drawNotes(canvas);
//        drawDebug(canvas);
    }

    /**
     * Draw closed and empty strings.
     *
     * @param canvas 画布对象
     */
    private void drawClosedEmptyString(Canvas canvas) {
        if (!isDrawStrings()) return;

        for (int i = 0; i < STRING; i++) {
            Bitmap bitmap = getStringBitmap(i);
            if (bitmap == null) continue;
            float left = getFretWidth() - bitmap.getWidth() / 2 + (getGridColumnWidth() * i);
            float top = Math.max(bitmapHeight(mClosedStringBitmap)
                    , bitmapHeight(mEmptyStringBitmap)) / 2;
            canvas.drawBitmap(bitmap, left, top, mPaint);
        }
    }

    /**
     * Draw the text.
     *
     * @param canvas Canvas object
     */
    private void drawFrets(Canvas canvas) {
        //If the highest product does not exceed 4 items, it will not be drawn.
        if (!isExceedDefaultFret()) {
            return;
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(mFretTextSize);
        mPaint.setColor(mFretTextColor);
        mPaint.setAlpha(255);

        int index = 1;
        int leastFret = getLeastFret();
        float fretWidth = getFretWidth();

        for (int i = leastFret; i < STRING + leastFret; i++) {
            String fret = String.valueOf(i);
            float fretTextWidth = mPaint.measureText(fret);
            float x = fretWidth - fretTextWidth - mFretTextOffsetX;
            float y = getStringHeight() + getHeadHeight() + (getGridRowHeight() * index);
            canvas.drawText(fret, x, y, mPaint);
            // In simple mode, if there are more than three items in the chord,
            // only the first item number will be displayed.
            if (mShowMode == SIMPLE_SHOW_MODE) {
                // Do not continue drawing
                break;
            }
            index++;
        }
    }

    /**
     * Draw the head.
     *
     * @param canvas Canvas object
     */
    private void drawHead(Canvas canvas) {
        if (!isDrawHead()) return;

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mHeadColor);

        float width = getGridWidth();
        float x = getFretWidth();
        float y = getStringHeight();
        mHeadPath.rewind();
        mHeadPath.moveTo(x, y + mHeadRadius);
        mHeadPath.quadTo(x, y, x + mHeadRadius, y);
        mHeadPath.lineTo(x + width - mHeadRadius, y);
        mHeadPath.quadTo(x + width, y, x + width, y + mHeadRadius);
        canvas.drawPath(mHeadPath, mPaint);
    }

    /**
     * 绘制指板网格。
     *
     * @param canvas 画布对象
     */
    private void drawGrid(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mGridLineWidth);
        mPaint.setColor(mGridLineColor);

        int row = getRow();
        float width = getGridWidth(), height = getGridHeight();
        float x = getFretWidth(), y = getStringHeight() + getHeadHeight();
        // 绘制横线
        float ry = y;
        float rh = (height - mGridLineWidth * (row + 1)) / (row);
        for (int i = 0; i < row + 1; i++) {
            ry = i != 0 ? ry + rh + mGridLineWidth : ry + mGridLineWidth / 2;
            canvas.drawLine(x, ry, x + width, ry, mPaint);
        }
        // 绘制竖线
        float cw = (width - mGridLineWidth * STRING) / (STRING - 1);
        for (int i = 0; i < STRING; i++) {
            x = i != 0 ? x + cw + mGridLineWidth : x + mGridLineWidth / 2;
            canvas.drawLine(x, y, x, y + height, mPaint);
        }
    }

    /**
     * Draw a node.
     *
     * @param canvas 画布对象
     */
    private void drawNotes(Canvas canvas) {
        if (mChord == null) return;

        int[] frets = mChord.getFrets();
        int[] fingers = mChord.getFingers();

        // Draw a barre
        int[] barreChord = mChordHelper.getBarreChordData(mChord);
        int barreFret = 0, barreString = 0;
        // Determine if there is a horizontal situation
        if (barreChord != null) {
            // With a barre, draw a horizontal rectangle
            barreFret = barreChord[0];
            barreString = barreChord[1];

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mBarreColor);
            mPaint.setAlpha(mBarreAlpha);

            float left = getFretWidth() + mGridLineWidth / 2 + (getGridColumnWidth() * (STRING - barreString));
            float top = getStringHeight() + getHeadHeight();
            if (isExceedDefaultFret()) {
                // Displayed at the 1 fret position
                top += getGridRowHeight() / 2 - mNoteRadius;
            } else {
                // Displayed in the smallest position
                top += getGridRowHeight() * barreFret - (getGridRowHeight() / 2) - mNoteRadius;
            }
            float right = left + getGridColumnWidth() * (barreString - 1);
            float bottom = top + mNoteRadius * 2;
            canvas.drawRect(left, top, right, bottom, mPaint);

            // Draw a horizontal border
            if (mBarreStrokeWidth > 0) {
                mPaint.setAlpha(255);
                mPaint.setColor(mBarreStrokeColor);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mBarreStrokeWidth);
                canvas.drawLine(left, top + mBarreStrokeWidth / 2, right, top + mBarreStrokeWidth / 2, mPaint);
                canvas.drawLine(left, bottom - mBarreStrokeWidth / 2, right, bottom - mBarreStrokeWidth / 2, mPaint);
            }

            // Draw horizontally on both ends of the node
            drawNote(canvas, barreFret, STRING, fingers != null ? 1 : 0, 255, 0, 0);
            drawNote(canvas, barreFret, STRING - (barreString - 1), fingers != null ? 1 : 0, 255, 0, 0);
        }
        // Draw other nodes
        for (int index = 0; index < frets.length; index++) {
            int fret = frets[index];
            // Do not draw closed and empty strings
            if (fret < 1) {
                continue;
            }
            // Do not draw nodes that are barred
            if (barreChord != null && barreFret == fret && frets.length - index <= barreString) {
                continue;
            }
            drawNote(canvas, frets[index], index + 1, fingers != null ? fingers[index] : 0, mNoteAlpha, mNoteStrokeWidth, mNoteStrokeColor);
        }
    }

    private void drawNote(Canvas canvas, int fret, int string, int finger, int alpha, float strokeWidth, int strokeColor) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mNoteColor);
        mPaint.setAlpha(alpha);
        // Draw a solid circle of nodes
        float columnWidth = getGridColumnWidth();
        float rowHeight = getGridRowHeight();
        float cx = ((getFretWidth() + mGridLineWidth / 2) + (columnWidth * (string - 1)))
                - (string == STRING ? mGridLineWidth : mGridLineWidth / 2);

        int f = 1;
        int leastFret = getLeastFret();
        if (isExceedDefaultFret()) {
            if (fret != leastFret) {
                int result = fret % leastFret;
                f = result != 0 ? result + 1 : fret - leastFret + 1;
            }
        } else {
            f = fret;
        }
        float cy = (getStringHeight() + getHeadHeight()) + (rowHeight * f) - (rowHeight / 2);
        canvas.drawCircle(cx, cy, mNoteRadius, mPaint);
        // Draw node text
        if (mShowMode != SIMPLE_SHOW_MODE && finger > 0) {
            mPaint.setColor(mNoteTextColor);
            mPaint.setTextSize(mNoteTextSize);
            String fingerStr = String.valueOf(finger);
            canvas.drawText(fingerStr, cx - mPaint.measureText(fingerStr) / 2,
                    cy - (mPaint.ascent() + mPaint.descent()) / 2, mPaint);
        }
        // Draw a node border
        if (strokeWidth > 0) {
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(strokeColor);
            mPaint.setAlpha(255);
            canvas.drawCircle(cx, cy, mNoteRadius, mPaint);
        }
    }

    /**
     * 获取品文字区域的宽度。
     *
     * @return 品文字区域的宽度。
     */
    private float getFretWidth() {
        if (mChord != null) {
            mPaint.setTextSize(mFretTextSize);
            String largestFret = String.valueOf(getLeastFret() + getRow() - 1);
            return mPaint.measureText(largestFret) + mFretTextOffsetX;
        }
        return 0f;
    }

    /**
     * 获取最小品。
     *
     * @return 最小品。默认 1。
     */
    private int getLeastFret() {
        return mChord != null ? mChord.getLeastFret() : 1;
    }

    /**
     * 获取最大品。
     *
     * @return 最大品，默认为 1。
     */
    private int getLargestFret() {
        return mChord != null ? mChord.getLargestFret() : 1;
    }

    /**
     * Get the corresponding empty or closed string representation picture 
     * Bitmap by the specified string.
     *
     * @param string String (0 = 6 strings, 1 = 5 strings, and so on)
     * @return Empty or closed strings represent pictures.
     */
    @Nullable
    private Bitmap getStringBitmap(int string) {
        if (mChord != null) {
            int[] frets = mChord.getFrets();
            int fret = frets[string];
            if (fret == -1) { // Closed string
                return mClosedStringBitmap;
            } else if (fret == 0) { // Empty string
                return mEmptyStringBitmap;
            }
        }
        return null;
    }

    /**
     * 获取弦区域的高度。
     *
     * @return 弦区域的高度。
     */
    private float getStringHeight() {
        return isDrawStrings() ? Math.max(bitmapHeight(mClosedStringBitmap),
                bitmapHeight(mEmptyStringBitmap)) + mStringOffsetY : 0f;
    }

    /**
     * Whether you need to draw a string area.
     *
     * @return Returns true, indicating that you need to draw, 
     no need to draw to return false.
     */
    private boolean isDrawStrings() {
        // As long as there are closed or empty strings in the chord, 
        //you need to draw
        return mChord != null && (mChord.isClosedString() || mChord.isEmptyString());
    }

    /**
     * Whether you need to draw the head.
     *
     * @return Returns true, indicating that you need to draw, 
     //no need to draw to return false.
     */
    private boolean isDrawHead() {
        // If the maximum fret does not exceed 5 items, 
        //it is considered to be drawn.
        return mChord != null && mChord.getLargestFret() <= 5;
    }

    /**
     * 获取琴头高度。
     *
     * @return 琴头的高度，如果琴头不绘制时直接返回 0。
     */
    private float getHeadHeight() {
        return isDrawHead() ? mHeadRadius : 0;
    }

    /**
     * 获取网格区域宽度。
     *
     * @return 网格区域宽度。
     */
    private float getGridWidth() {
        return getWidth() - getFretWidth() - mNoteRadius;
    }

    /**
     * 获取网格区域高度。
     *
     * @return 网格区域高度。
     */
    private float getGridHeight() {
        return getHeight() - getStringHeight() - getHeadHeight();
    }

    /**
     * 获取网格每格的宽度。
     *
     * @return 网格每格的宽度。
     */
    private float getGridColumnWidth() {
        int column = STRING - 1;
        return getGridWidth() / column;
    }

    /**
     * 获取网格每格的高度。
     *
     * @return 网格每格的高度。
     */
    private float getGridRowHeight() {
        return getGridHeight() / getRow();
    }

    /**
     * 获取指定 Bitmap 的宽度。
     *
     * @param bitmap 指定的 Bitmap 对象
     * @return Bitmap 的宽度。
     */
    private int bitmapWidth(Bitmap bitmap) {
        return bitmap != null ? bitmap.getWidth() : 0;
    }

    /**
     * 获取指定 Bitmap 的高度。
     *
     * @param bitmap 指定的 Bitmap 对象
     * @return Bitmap 的高度。
     */
    private int bitmapHeight(Bitmap bitmap) {
        return bitmap != null ? bitmap.getHeight() : 0;
    }

    /**
     * Get the number of rows.
     *
     * @return Row
     */
    private int getRow() {
        //In simple mode, if the span of the largest and smallest products in
        // the chord does not exceed three products, 
        //and the first product is the lowest product, 
        //the number of lines is three lines.
        if (mShowMode == SIMPLE_SHOW_MODE) {
            int leastFret = getLeastFret();
            int largestFret = getLargestFret();
            int diffFret = largestFret - leastFret;
            if (diffFret < 3 && leastFret == 1) {
                return 3;
            }
        }
        return 4;
    }

    /**
     * Whether the maximum fret exceeds the default display.
     *
     * @return Returns true if it is exceeded, otherwise returns false.
     */
    private boolean isExceedDefaultFret() {
        return getLargestFret() > FRET;
    }

    private void drawDebug(Canvas canvas) {
        // draw grid rect
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2f);
        float left = getFretWidth();
        float top = getStringHeight() + getHeadHeight();
        canvas.drawRect(left, top, left + getGridWidth(), top + getGridHeight(), mPaint);
    }

}
