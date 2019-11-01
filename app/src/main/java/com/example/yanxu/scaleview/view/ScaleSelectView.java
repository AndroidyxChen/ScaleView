package com.example.yanxu.scaleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.OverScroller;

import com.example.yanxu.scaleview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 2017/6/6.
 * 自定义的风险承受等级刻度尺
 */
public class ScaleSelectView extends View {

    private Paint mPaint;//背景刻度画笔
    private Paint mPaintRed;//中间刻度画笔
    private Paint mPaintText;//文字画笔
    private Paint mPaintBottomLine;//底部灰色线的画笔
    private Paint mPaintColorLine;//底部居中彩线（图片）的画笔
    private Paint mPaintRecommend;//数字下的推荐（图片）的画笔

    private float mTextSize = 0;
    private float mPointX = 0f;
    private int mPadding = dip2px(1);
    private OverScroller scroller;//控制滑动
    private VelocityTracker velocityTracker;

    private float mUnit = 50;
    private int mMaxValue = 200;
    private int mMinValue = 150;
    private int mMiddleValue = (mMaxValue + mMinValue) / 2;
    private int mUnitLongLine = 5;
    private boolean isCanvasLine = true;

    private int bgColor = Color.rgb(228, 228, 228);
    private int textColor = Color.rgb(151, 151, 151);
    private int textSelectColor = Color.rgb(151, 151, 151);

    private int minvelocity;
    private int selected;
    private int recommend;

    private boolean isActionUp = false;
    private float mLastX;
    private ScrollAnim mScrollAnim;

    private List<String> listValue = new ArrayList<>();
    private onSelect mOnSelect = null;

    public ScaleSelectView(Context context) {
        this(context, null);
    }

    public ScaleSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new OverScroller(context);
        minvelocity = ViewConfiguration.get(getContext())
                .getScaledMinimumFlingVelocity();
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScaleSelectView);
        if (typedArray != null) {
            isCanvasLine = typedArray.getBoolean(R.styleable.ScaleSelectView_isCanvasLine, true);
            mTextSize = typedArray.getDimension(R.styleable.ScaleSelectView_textDefaultSize, dip2px(12));
            bgColor = typedArray.getColor(R.styleable.ScaleSelectView_bgColor, Color.rgb(228, 228, 228));
            textColor = typedArray.getColor(R.styleable.ScaleSelectView_textDefaultColor, Color.rgb(151, 151, 151));
            mUnit = typedArray.getDimension(R.styleable.ScaleSelectView_unitSize, 50.f);
            mUnitLongLine = typedArray.getInteger(R.styleable.ScaleSelectView_unitLongLine, 5);
            textSelectColor = typedArray.getColor(R.styleable.ScaleSelectView_textSelectColor, Color.rgb(70, 113, 207));
            typedArray.recycle();
        }
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dip2px(1));

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(textColor);
        mPaintText.setTextSize(mTextSize);
        mPaintText.setStyle(Paint.Style.FILL);

        mPaintRecommend = new Paint();
        mPaintRecommend.setAntiAlias(true);
        //mPaintRecommend.setColor(textColor);
        //mPaintRecommend.setTextSize(dip2px(8));
        //mPaintRecommend.setStyle(Paint.Style.FILL);

        mPaintRed = new Paint();
        mPaintRed.setAntiAlias(true);
        mPaintRed.setColor(Color.rgb(70, 113, 207));
        mPaintRed.setStrokeWidth(dip2px(1) * 3 / 2);
        mPaintRed.setStyle(Paint.Style.STROKE);

        mPaintBottomLine = new Paint();
        mPaintBottomLine.setColor(bgColor);

        mPaintColorLine = new Paint();
        mPaintColorLine.setAntiAlias(true);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        //canvasBg(canvas);
        canvasLineAndText(canvas);
        canvasRedLine(canvas);
        drawBottomLine(canvas);
        drawColorLine(canvas);
    }

    private void canvasBg(Canvas canvas) {//画圆角矩形
        RectF rectF = new RectF();
        rectF.top = mPadding;
        rectF.left = mPadding;
        rectF.bottom = getMeasuredHeight() - mPadding;
        rectF.right = getMeasuredWidth() - mPadding;
        canvas.drawRoundRect(rectF, dip2px(2), dip2px(2), mPaint);
    }

    private void canvasRedLine(Canvas canvas) {//中间红色刻度
        Path pathRed = new Path();
        pathRed.moveTo(getMeasuredWidth() / 2, getBottomY() - dip2px(40));
        pathRed.lineTo(getMeasuredWidth() / 2, getBottomY() - dip2px(2));
        canvas.drawPath(pathRed, mPaintRed);
    }

    private void drawBottomLine(Canvas canvas) {//底部灰色线
        canvas.drawLine(getMeasuredWidth(), getBottomY() - dip2px(5), -5 * mUnit, getBottomY() - dip2px(5), mPaintBottomLine);
    }

    public void drawColorLine(Canvas canvas) {//底部彩线
        Bitmap bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_grade_color);
        canvas.drawBitmap(getBitmap(bitMap, 288, 1), getMeasuredWidth() / 2 - (mMiddleValue - mMinValue) * mUnit + mPointX,
                getMeasuredHeight() - dip2px(35),
                mPaintColorLine);
    }

    //对bitmap进行自定义
    public Bitmap getBitmap(Bitmap bitmap, int needWidth, int needHeight) {
        //设置原来的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置需要的宽高
        int newWidth = dip2px(needWidth);
        int newHeight = dip2px(needHeight);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
        return bitmap;
    }

    //设置默认刻度值
    public void setSelected(int selected, int recommend) {
        this.selected = selected;
        this.recommend = recommend;
        if (selected > 5) {
            mPointX = -(selected - 5) * mUnit;
        } else if (selected < 5) {
            mPointX = (5 - selected) * mUnit;
        } else {
            mPointX = 0f;
        }
        invalidate();
    }

    //绘制数字和短线
    private void canvasLineAndText(Canvas canvas) {
        //int current = (int) (Math.rint(mPointX / mUnit));
        for (int i = mMinValue; i <= mMaxValue; i++) {
            int space = mMiddleValue - i;
            float x = getMeasuredWidth() / 2 - space * mUnit + mPointX;
            if (x > mPadding && x < getMeasuredWidth() - mPadding) {//判断x轴在视图范围内

                //float y = getMeasuredHeight() / 2;
                if (i % mUnitLongLine == 0) {//画长刻度线 默认每5格一个

                    mPaintText.setColor(textColor);
                    /*if (Math.abs(mMiddleValue - current - i) < (mUnitLongLine / 2 + 1)) {//计算绝对值在某一区间内文字显示高亮
                        mPaintText.setColor(textSelectColor);//这是滑到哪里哪里的数字变色
                    }*/
                    if (i == selected) {
                        mPaintText.setColor(textSelectColor);
                    }
                    String text = listValue.get(i - 1);

                    canvas.drawText(text,
                            x - getFontlength(mPaintText, text) / 2,
                            getMeasuredHeight() - dip2px(18),
                            mPaintText);
                    //y = getMeasuredHeight() * 2 / 3;

                    if (i == recommend) {
                        Bitmap bitMap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_recommend_rectangle);
                        canvas.drawBitmap(bitMap, x - getFontlength(mPaintRecommend, text) / 2 - dip2px(9),
                                getMeasuredHeight() - dip2px(15),
                                mPaintRecommend);
                    }

                } else {
                    //y = y + y * 2 / 3;
                }
                if (isCanvasLine) {//画短刻度线
                    canvas.drawLine(x, getBottomY() - dip2px(15), x, getBottomY() - dip2px(5), mPaint);
                }
            }
        }
    }

    private int getBottomY() {
        return getMeasuredHeight() - getPaddingBottom() - dip2px(30);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float xPosition = event.getX();

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mScrollAnim != null)
                    clearAnimation();
                isActionUp = false;
                scroller.forceFinished(true);
                break;
            case MotionEvent.ACTION_MOVE:
                isActionUp = false;
                float mPointXOff = xPosition - mLastX;
                mPointX = mPointX + mPointXOff;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isActionUp = true;
                countVelocityTracker(event);//控制快速滑动
                startAnim();
                return false;
            default:
                break;
        }
        mLastX = xPosition;
        return true;
    }


    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            float mPointXoff = (scroller.getFinalX() - scroller.getCurrX());
            mPointX = mPointX + mPointXoff * functionSpeed();
            float absmPointX = Math.abs(mPointX);
            float mmPointX = (mMaxValue - mMinValue) * mUnit / 2;
            if (absmPointX < mmPointX) {//在视图范围内
                startAnim();
            }
        }
        super.computeScroll();
    }

    /**
     * 控制滑动速度
     *
     * @return
     */
    private float functionSpeed() {
        return 0.5f;
    }

    private void countVelocityTracker(MotionEvent event) {
        velocityTracker.computeCurrentVelocity(800, 800);
        float xVelocity = velocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > minvelocity) {
            scroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE,
                    Integer.MAX_VALUE, 0, 0);
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    public float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    private class ScrollAnim extends Animation {

        float fromX = 0f;
        float desX = 0f;

        public ScrollAnim(float d, float f) {
            fromX = f;
            desX = d;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mPointX = fromX + (desX - fromX) * interpolatedTime;//计算动画每贞滑动的距离
            invalidate();
        }
    }

    private void startAnim() {
        float absmPointX = Math.abs(mPointX);
        float mmPointX = (mMaxValue - mMinValue) * mUnit / 2;
        if (absmPointX > mmPointX) {//超出视图范围
            if (mPointX > 0) {//最左
                moveToX(mMiddleValue - mMinValue, 300);
            } else {//最右
                moveToX(mMiddleValue - mMaxValue, 300);
            }
        } else {
            int space = (int) (Math.rint(mPointX / mUnit));//四舍五入计算出往左还是往右滑动
            moveToX(space, 200);
        }
    }

    private void moveToX(int distance, int time) {
        if (mScrollAnim != null)
            clearAnimation();
        mScrollAnim = new ScrollAnim((distance * mUnit), mPointX);
        mScrollAnim.setDuration(time);
        startAnimation(mScrollAnim);
        if (mOnSelect != null)
            mOnSelect.onSelectItem(listValue.get(mMiddleValue - distance - 1));
    }

    public void showValue(List<String> list, onSelect monSelect) {
        mOnSelect = monSelect;
        listValue.clear();
        listValue.addAll(list);
        mMaxValue = listValue.size();
        mMinValue = 1;
        mMiddleValue = (mMaxValue + mMinValue) / 2;
    }

    public interface onSelect {
        void onSelectItem(String value);
    }

}
