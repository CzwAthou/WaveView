package waveview.athou.com.waveview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by athou on 2017/6/7.
 */

public class WaveBoatView extends View {

    private String TAG = WaveView.class.getSimpleName();

    private int waveLength = 600;
    private int waveHeight = 80;
    private float waveTotalLength = 0;
    private float waveSigalLength = 0;
    private int wave_rootBitmap;
    private int bitmapOffset;
    private int wave_color = Color.BLUE;
    private int duration = 5000;
    private int originY = 500;

    private Paint paint;
    private Path path;
    private float pathStep;
    private int width;
    private int height;
    private Bitmap mBitmap;

    //移动动画
    private ValueAnimator animator;

    public WaveBoatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveBoatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPath();
        startAnimation();
    }

    PathMeasure pathMeasure;

    private void initPath() {
        path = new Path();

        int halfWaveLength = waveLength / 2; //半个波长
        //二阶贝塞尔曲线：起始点、瞄点、终点
        path.moveTo(-waveLength, originY);
        int count = 0;
        for (int i = -waveLength; i < width + waveLength; i += waveLength) {
            path.rQuadTo(halfWaveLength / 2, -waveHeight, halfWaveLength, 0);//二阶贝塞尔曲线
            path.rQuadTo(halfWaveLength / 2, waveHeight, halfWaveLength, 0);
            count++;
        }
        //底部需要封闭
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();

        pathMeasure = new PathMeasure(path, true);
        waveTotalLength = (float) (pathMeasure.getLength() - width - Math.sqrt(originY * originY + waveLength * waveLength) * 2);
        waveSigalLength = waveTotalLength / count;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);

        int count = canvas.save();
        drawBoat(canvas);
        canvas.restoreToCount(count);
    }

    /**
     * 记录当前运动点的位置
     */
    Matrix matrix = new Matrix();

    private void drawBoat(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }
        matrix.preTranslate(-mBitmap.getWidth() / 2, -mBitmap.getHeight() / 2 + bitmapOffset);
        canvas.drawBitmap(mBitmap, matrix, paint);
    }

    public void startAnimation() {
        //属性动画控制dx的增加（控制循环）
        animator = ValueAnimator.ofFloat(waveSigalLength, waveTotalLength - waveSigalLength);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator()); //匀速前进
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pathStep = (float) animation.getAnimatedValue();
//                pathMeasure.getPosTan(pathStep, mPosition, mTan);
                pathMeasure.getMatrix(pathStep, matrix, PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);
                postInvalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
}