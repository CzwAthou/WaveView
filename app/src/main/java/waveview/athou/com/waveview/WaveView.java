package waveview.athou.com.waveview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by athou on 2017/6/5.
 */

public class WaveView extends View {

    private String TAG = WaveView.class.getSimpleName();

    private int waveLength = 400;
    private int waveHeight = 80;
    private int wave_rootBitmap;
    private int bitmapOffset;
    private int wave_color = Color.BLUE;
    private volatile int wave_rise = 0;
    private int duration = 1000;
    private int originY = 500;

    private Paint paint;
    private Path path;
    private int dx;
    private int dy;
    private int width;
    private int height;
    private Bitmap mBitmap;

    //移动动画
    private ValueAnimator animator;
    // 当前动画的播放运行时间
    private long mCurrentPlayTime = 0;

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        waveLength = typedArray.getInt(R.styleable.WaveView_waveLength, waveLength);
        waveHeight = typedArray.getInt(R.styleable.WaveView_waveHeight, waveHeight);
        wave_rootBitmap = typedArray.getResourceId(R.styleable.WaveView_bootBitmap, -1);
        bitmapOffset = typedArray.getInt(R.styleable.WaveView_bitmapOffset, 0);
        wave_color = typedArray.getColor(R.styleable.WaveView_waveColor, wave_color);
        originY = typedArray.getInt(R.styleable.WaveView_originY, originY);
        wave_rise = typedArray.getInt(R.styleable.WaveView_rise, wave_rise);
        duration = typedArray.getInt(R.styleable.WaveView_duration, duration);
        typedArray.recycle();

        if (wave_rootBitmap > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            mBitmap = BitmapFactory.decodeResource(getResources(), wave_rootBitmap, options);
        }

        paint = new Paint();
        paint.setColor(wave_color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setFilterBitmap(true);

        path = new Path();

        setOnClickListener(clickListener);
    }

    public void setWaveLength(int waveLength) {
        this.waveLength = Math.max(waveLength, 100);
        invalidate();
    }

    public void setWaveHeight(int waveHeight) {
        this.waveHeight = waveHeight;
        invalidate();
    }

    public void setRise(int rise) {
        this.wave_rise = rise;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);

        width = widthSize;
        height = heightSize;
        originY = height / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (originY + dy < -waveHeight) {
            reset();
            return;
        }
        //不断的计算这个波浪线的路线
        setPathData();
        canvas.drawPath(path, paint);
        drawBoat(canvas);
    }

    private void setPathData() {
        path.reset();
        int halfWaveLength = waveLength / 2; //半个波长
        //二阶贝塞尔曲线：起始点、瞄点、终点
        path.moveTo(-waveLength + dx, originY + dy);
        for (int i = -waveLength; i < width + waveLength; i += waveLength) {
            path.rQuadTo(halfWaveLength / 2, -waveHeight, halfWaveLength, 0);//二阶贝塞尔曲线
            path.rQuadTo(halfWaveLength / 2, waveHeight, halfWaveLength, 0);
        }
        //底部需要封闭
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
    }

    private Rect getPathBounds(float localX) {
        //矩形区域
        Region region = new Region();
        Region clip = new Region((int) (localX - 0.1f), 0, (int) localX, height * 2);
        region.setPath(path, clip); //获得相交的区域
        //得到相交区域的坐标
        return region.getBounds();
    }

    private void drawBoat(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }
        //获取中间位置的相交区域
        Rect bounds = getPathBounds(width / 2); //中间点靠前的区域
        //画出小船的位置，需要居中
        if (bounds.top > originY + dy) { //波峰
            canvas.drawBitmap(mBitmap, bounds.right - mBitmap.getWidth() / 2, bounds.top - mBitmap.getHeight() / 2 + bitmapOffset, paint);
        } else { //波谷
            canvas.drawBitmap(mBitmap, bounds.left - mBitmap.getWidth() / 2, bounds.top - mBitmap.getHeight() / 2 + bitmapOffset, paint);
        }
    }

    public void reset() {
        originY = height;
        dx = 0;
        dy = 0;
        mCurrentPlayTime = 0;
        stopAnimation();
        startAnimation();
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (animator != null) {
                if (isRunning()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        animator.pause();
                    } else {
                        mCurrentPlayTime = animator.getCurrentPlayTime();
                        animator.cancel();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        animator.resume();
                    } else {
                        animator.setCurrentPlayTime(mCurrentPlayTime);
                        animator.start();
                    }
                }
            } else {
                startAnimation();
            }
        }
    };

    public void startAnimation() {
        //属性动画控制dx的增加（控制循环）
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator()); //匀速前进
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float faction = (float) animation.getAnimatedValue();
                dx = (int) (waveLength * faction);
                dy -= wave_rise; //波浪向上涨的幅度
                postInvalidate();
            }
        });
        animator.start();
    }

    public boolean isRunning() {
        return animator != null && animator.isRunning();
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
}
