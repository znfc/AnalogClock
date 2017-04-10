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
            array = getContext().obtainStyledAttributes(attrs,R.styleable.WatchBoard);
            mRadius = array.getDimension(R.styleable.WatchBoard_wb_pointer_corner_radius,40);
            mPadding = array.getDimension(R.styleable.WatchBoard_wb_padding,10);
            mTextSize = array.getDimension(R.styleable.WatchBoard_wb_text_size,5);
            mHourPointWidth = array.getDimension(R.styleable.WatchBoard_wb_hour_pointer_width,5);
            mMinutePointWidth = array.getDimension(R.styleable.WatchBoard_wb_minute_pointer_width,5);
            mSecondPointWidth = array.getDimension(R.styleable.WatchBoard_wb_second_pointer_width,5);
            mPointRadius = (int)array.getDimension(R.styleable.WatchBoard_wb_pointer_corner_radius,3);
            mPointEndLength = array.getDimension(R.styleable.WatchBoard_wb_pointer_end_length,6);
            mColorLong = array.getColor(R.styleable.WatchBoard_wb_scale_long_color,0);
            mColorShort = array.getColor(R.styleable.WatchBoard_wb_scale_short_color,0);
            mHourPointColor = array.getColor(R.styleable.WatchBoard_wb_hour_pointer_color,5);
            mMinutePointColor = array.getColor(R.styleable.WatchBoard_wb_minute_pointer_color,6);
            mSecondPointColor = array.getColor(R.styleable.WatchBoard_wb_second_pointer_color,9);
        }catch (Exception e){
//            mColorLong = getContext().getColor(R.color.colorPrimaryDark);
//            mColorShort = getContext().getColor(R.color.colorPrimaryDark);
        } finally {
            if(array != null){
                array.recycle();
            }
        }
    }

//    //Dp2Px
//    private float DptoPx(int value){
//        return SizeUtil.Dp
//    }

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

        canvas.restore();
        postInvalidateDelayed(1000);
    }

    //绘制外圆背景
    private void paintCircle(Canvas canvas) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0,0,mRadius,mPaint);
    }

    //绘制刻度
    private void paintScale(Canvas canvas){
        mPaint.setStrokeWidth(SizeUtil.dp2px(getContext(),1));
        int lineWidth = 0;
        for (int i = 0; i < 60 ; i++){
            if(i % 5 == 0){//整点
                mPaint.setStrokeWidth(SizeUtil.dp2px(getContext(),1.5f));
                mPaint.setColor(getContext().getResources().getColor(R.color.colorLong));
                lineWidth =40;
                //===============绘制文字===复制网上的==start
                mPaint.setTextSize(70);
                String text = ((i / 5) == 0 ? 12 : (i / 5)) + "";
                Rect textBound = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), textBound);
                mPaint.setColor(Color.BLACK);
                canvas.save();
                canvas.translate(0, -mRadius + SizeUtil.dp2px(getContext(),5) + lineWidth + mPadding + (textBound.bottom - textBound.top) / 2);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.rotate(-6 * i);
                canvas.drawText(text, -(textBound.right + textBound.left) / 2, -(textBound.bottom + textBound.top) / 2, mPaint);
                canvas.restore();
                //===============绘制文字===复制网上的==end
            } else { //非整点
                lineWidth = 30;
                mPaint.setColor(getContext().getResources().getColor(R.color.colorShort));
                mPaint.setStrokeWidth(SizeUtil.dp2px(getContext(),1));
            }
            canvas.drawLine(0, -mRadius + SizeUtil.dp2px(getContext(),10),
                    0 , -mRadius + SizeUtil.dp2px(getContext(),10)+lineWidth , mPaint);
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
        mPaint.setColor(getContext().getResources().getColor(R.color.colorShort)); //设置指针颜色
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mHourPointWidth); //设置边界宽度
        canvas.drawRoundRect(rectFHour, mPointRadius, mPointRadius, mPaint); //绘制时针
        canvas.restore();
        //绘制分针
        canvas.save();
        canvas.rotate(angleMinute);
        RectF rectFMinute = new RectF(-mMinutePointWidth / 2, -mRadius * 3.5f / 5, mMinutePointWidth / 2, mPointEndLength);
        mPaint.setColor(getContext().getResources().getColor(R.color.colorShort));
        mPaint.setStrokeWidth(mHourPointWidth);
        canvas.drawRoundRect(rectFMinute, mPointRadius, mPointRadius, mPaint);
        canvas.restore();
        //绘制秒针
        canvas.save();
        canvas.rotate(angleSecond);
        RectF rectFSecond = new RectF(-mSecondPointWidth / 2, -mRadius + 15, mSecondPointWidth / 2, mPointEndLength);
        mPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        mPaint.setStrokeWidth(mSecondPointWidth);
        canvas.drawRoundRect(rectFSecond, mPointRadius, mPointRadius, mPaint);
        canvas.restore();
        //绘制中心小圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(550000);
        canvas.drawCircle(0, 0, mSecondPointWidth * 4, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = (Math.min(w,h) - mPadding) / 2;
        mPointEndLength = mRadius / 6;//尾部指针默认为半径的六分之一
    }
}
