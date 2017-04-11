package linlin.com.analogclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;

import linlin.com.tools.SizeUtil;

/**
 * Created by zhaopenglin on 2017/4/10.
 */

public class WatchBoard extends View {

    private float mRadius; //外圆半径
    private float mPadding; //边距
    private float mTextSize; //文字大小
    private float mHourPointWidth; //时针宽度
    private float mMinutePointWidth; //分针宽度
    private float mSecondPointWidth; //秒针宽度
    private int mPointRadius; // 指针圆角
    private float mPointEndLength; //指针末尾的长度
    private int mColorLong; //长线的颜色
    private int mColorShort; //短线的颜色
    private int mHourPointColor; //时针的颜色
    private int mMinutePointColor; //分针的颜色
    private int mSecondPointColor; //秒针的颜色
    private Paint mPaint; //画笔

    public WatchBoard(Context context) {
        this(context,null);
    }

    public WatchBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WatchBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        obtainStyledAttrs(attrs);
    }

    private void obtainStyledAttrs(AttributeSet attrs){
        TypedArray array = null;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.WatchBoard);
            mPadding = array.getDimension(R.styleable.WatchBoard_wb_padding, DptoPx(10));
            mTextSize = array.getDimension(R.styleable.WatchBoard_wb_text_size, SptoPx(16));
            mHourPointWidth = array.getDimension(R.styleable.WatchBoard_wb_hour_pointer_width, DptoPx(5));
            mMinutePointWidth = array.getDimension(R.styleable.WatchBoard_wb_minute_pointer_width, DptoPx(3));
            mSecondPointWidth = array.getDimension(R.styleable.WatchBoard_wb_second_pointer_width, DptoPx(2));
            mPointRadius = (int) array.getDimension(R.styleable.WatchBoard_wb_pointer_corner_radius, DptoPx(10));
            mPointEndLength = array.getDimension(R.styleable.WatchBoard_wb_pointer_end_length, DptoPx(10));

            mColorLong = array.getColor(R.styleable.WatchBoard_wb_scale_long_color, Color.argb(225, 0, 0, 0));
            mColorShort = array.getColor(R.styleable.WatchBoard_wb_scale_short_color, Color.argb(125, 0, 0, 0));
            mHourPointColor = array.getColor(R.styleable.WatchBoard_wb_hour_pointer_color, Color.BLACK);
            mMinutePointColor = array.getColor(R.styleable.WatchBoard_wb_minute_pointer_color, Color.BLACK);
            mSecondPointColor = array.getColor(R.styleable.WatchBoard_wb_second_pointer_color, Color.RED);
        }catch (Exception e){
            //一旦出现错误全部使用默认值
            mPadding = DptoPx(10);
            mTextSize = SptoPx(16);
            mHourPointWidth = DptoPx(5);
            mMinutePointWidth = DptoPx(3);
            mSecondPointWidth = DptoPx(2);
            mPointRadius = (int) DptoPx(10);
            mPointEndLength = DptoPx(10);

            mColorLong = Color.argb(225, 0, 0, 0);
            mColorShort = Color.argb(125, 0, 0, 0);
            mMinutePointColor = Color.BLACK;
            mSecondPointColor = Color.RED;
        } finally {
            if(array != null){
                array.recycle();
            }
        }
    }

    //Dp转px
    private float DptoPx(int value) {

        return SizeUtil.dp2px(getContext(), value);
    }

    //sp转px
    private float SptoPx(int value) {
        return SizeUtil.sp2px(getContext(),value);
    }

    //画笔初始化
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 1000;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if(widthMode == MeasureSpec.AT_MOST
                || widthMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST
                || heightMode == MeasureSpec.UNSPECIFIED){
            try {
                throw  new NoDetermineSizeException(
                        "宽度高度至少有一个确定的值，不能同时为wrap_content");
            }catch ( NoDetermineSizeException e){
                e.printStackTrace();
            }
        }else { //至少有一个为确定值，要获取其中的最小值
            if(widthMode == MeasureSpec.EXACTLY){
                width = Math.min(widthSize,width);
            }
            if(heightMode == MeasureSpec.EXACTLY){
                width = Math.min(widthSize,width);
            }
        }
        setMeasuredDimension(width,width);
    }

    //自定义的异常
    class NoDetermineSizeException extends Exception{
        public NoDetermineSizeException(String message) {
            super(message);
        }
    }

    /**
     * 个人对canvas的理解
     * canvas网上都是是画布，我个人理解它不单单是画布那么简单
     * 我理解它是透明的多层的带有坐标系的画布，最底层有一个superView（可以理解为是手机屏幕吧，反正就是想说它是不受canvas影响的）
     * canvas上画的东西最终都是在它上边显示的。
     * 比如canvas.translate(100 , 100);这个函数，就是以当前（记住这个词 当前）画布坐标系数为参考的（0,0）点移动到（100,100）
     * 刚刚提到“当前”这个词,比如执行上边的translate(100 , 100)方法前调用过canvas.rotate(20);那当前的canvas就是
     * 顺时针旋转了20度，在这个基础上（就是歪着的坐标系）向（100,100）移动（以手机屏幕为坐标系的话是想75度（20+45）方向移动）
     *每调用一次canvas.save()canvas就会往栈里压一层，之前画的东东都显示在superView上不会被改变了不管canvas怎么搞都不会改变了
     * canvas.save()只是保存了当前canvas的坐标系数，至于它画的东东都已经定型到superView上了
     * 所以canvas.restore()的也只是恢复了栈顶的canvas的坐标系数，也就是恢复了栈顶的那一层透明的canvas，
     * 至于保存时该层canvas上画的东东都已经定型到superView上了。
     * canvas.restore()只是想恢复上一次保存的canvas继续用来作画。大概这么理解吧
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth() / 2 , getHeight() / 2);
        //绘制外圆背景
        paintCircle(canvas);
        //绘制刻度
        paintScale(canvas);
        //绘制指针
        paintPointer(canvas);

        postInvalidateDelayed(1000);
    }

    //绘制外圆背景
    private void paintCircle(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0,0,mRadius,mPaint);
    }

    //绘制刻度
    private void paintScale(Canvas canvas) {
        mPaint.setStrokeWidth(DptoPx(1));
        int lineWidth = 0;
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) { //整点
                mPaint.setStrokeWidth(DptoPx(2));
                mPaint.setColor(mColorLong);
                lineWidth = 40;
                //===============绘制文字===复制网上的==start
                mPaint.setTextSize(mTextSize);
                String text = ((i / 5) == 0 ? 12 : (i / 5)) + "";
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                mPaint.setColor(Color.BLACK);

                canvas.save();
                canvas.translate(0, -mRadius + DptoPx(5) + lineWidth + mPadding + (textBound.bottom - textBound.top) / 2);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.rotate(-6 * i);
                canvas.drawText(text, -(textBound.right + textBound.left) / 2, -(textBound.bottom + textBound.top) / 2, mPaint);
                canvas.restore();
                //===============绘制文字===复制网上的==end
            } else { //非整点
                lineWidth = 30;
                mPaint.setColor(mColorShort);
                mPaint.setStrokeWidth(DptoPx(1));
            }
            canvas.drawLine(0, -mRadius + mPadding, 0, -mRadius + mPadding + lineWidth, mPaint);
            canvas.rotate(6);
        }
    }

    //绘制指针
    private void paintPointer(Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); //时
        int minute = calendar.get(Calendar.MINUTE); //分
        int second = calendar.get(Calendar.SECOND); //秒
        int angleHour = (hour % 12) * 360 / 12; //时针转过的角度
        int angleMinute = minute * 360 / 60; //分针转过的角度
        int angleSecond = second * 360 / 60; //秒针转过的角度
        //绘制时针
        canvas.save();
        canvas.rotate(angleHour); //旋转到时针的角度
        RectF rectFHour = new RectF(-mHourPointWidth / 2, -mRadius * 3 / 5, mHourPointWidth / 2, mPointEndLength);
        mPaint.setColor(mHourPointColor); //设置指针颜色
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mHourPointWidth); //设置边界宽度
        canvas.drawRoundRect(rectFHour, mPointRadius, mPointRadius, mPaint); //绘制时针
        canvas.restore();
        //绘制分针
        canvas.save();
        canvas.rotate(angleMinute);
        RectF rectFMinute = new RectF(-mMinutePointWidth / 2, -mRadius * 3.5f / 5, mMinutePointWidth / 2, mPointEndLength);
        mPaint.setColor(mMinutePointColor);
        mPaint.setStrokeWidth(mMinutePointWidth);
        canvas.drawRoundRect(rectFMinute, mPointRadius, mPointRadius, mPaint);
        canvas.restore();
        //绘制秒针
        canvas.save();
        canvas.rotate(angleSecond);
        RectF rectFSecond = new RectF(-mSecondPointWidth / 2, -mRadius + 15, mSecondPointWidth / 2, mPointEndLength);
        mPaint.setColor(mSecondPointColor);
        mPaint.setStrokeWidth(mSecondPointWidth);
        canvas.drawRoundRect(rectFSecond, mPointRadius, mPointRadius, mPaint);
        canvas.restore();
        //绘制中心小圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mSecondPointColor);
        canvas.drawCircle(0, 0, mSecondPointWidth * 4, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = (Math.min(w,h) - mPadding) / 2;
        mPointEndLength = mRadius / 6;//尾部指针默认为半径的六分之一
    }
}
