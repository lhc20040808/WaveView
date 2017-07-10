package com.lhc.wave;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：lhc
 * 时间：2017/7/10.
 */

public class WaveView extends View {
    private static final int WAVE_PAINT_COLOR = 0x880000aa;//波纹颜色
    private static final int WAVE_COUNT = 2;//波纹数量
    private static final int WAVE_HEIGHT = 70;//波峰高度
    private static final int FIRST_WAVE_SPEED = 5;//第一个水波的移动速度
    private static final int SECOND_WAVE_SPEED = 6;//第二个水波的移动速度
    private static final int CIRCLE_RADIUS = 170;//圆的半径
    private int waveWidth;
    private int centerY;
    private int centerX;
    private Path firstWavePath;
    private Path secondWavePath;
    private Paint paint;
    private int firstWavePosX;
    private int secondWavePosX;
    private Bitmap waveBitmap;
    private Bitmap circleBitmap;
    private Matrix matrix;
    private Bitmap shipBitmap;
    private PathMeasure pathMeasure;
    private float lengthOfWave;

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        firstWavePath = new Path();
        secondWavePath = new Path();
        matrix = new Matrix();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        pathMeasure = new PathMeasure();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        waveWidth = w / WAVE_COUNT / 2;
        centerY = h / 2;
        centerX = w / 2;
        firstWavePosX = -(waveWidth * 2);
        secondWavePosX = -(waveWidth * 2);
        waveBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        circleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        shipBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ship);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setXfermode(null);
        generateDesWaveBitmap();
        generateSrcCircleBitmap();

        canvas.drawBitmap(waveBitmap, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));//在两者相交的地方绘制源图像
        canvas.drawBitmap(circleBitmap, 0, 0, paint);

        drawStrokeCircle(canvas);

        canvas.drawBitmap(shipBitmap, matrix, null);

        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);
        canvas.drawLine(0f, (float) centerY, (float) getWidth(), (float) centerY, paint);
        canvas.drawLine(centerX, 0, centerX, getHeight(), paint);

//        postInvalidate();
    }

    private void drawStrokeCircle(Canvas canvas) {
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#20B2AA"));
        canvas.drawCircle(centerX, centerY, CIRCLE_RADIUS, paint);
    }

    private void generateSrcCircleBitmap() {
        Canvas c = new Canvas(circleBitmap);
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        initCirclePaint();
        drawCircle(c);
    }

    private void generateDesWaveBitmap() {
        Canvas c = new Canvas(waveBitmap);
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        initWavePaint();
        drawFirstWave(c);
        drawSecondWave(c);
    }

    private void initWavePaint() {
        paint.setColor(WAVE_PAINT_COLOR);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(4);
    }

    private void initCirclePaint() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(8);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, CIRCLE_RADIUS, paint);
    }

    private void drawFirstWave(Canvas canvas) {
        firstWavePath.reset();
        firstWavePath.moveTo(firstWavePosX, centerY);
        for (int i = 0; i < (WAVE_COUNT + 2) * 2; i++) {
            if (i % 2 == 0) {
                firstWavePath.rQuadTo(waveWidth / 2, -WAVE_HEIGHT, waveWidth, 0);
            } else {
                firstWavePath.rQuadTo(waveWidth / 2, WAVE_HEIGHT, waveWidth, 0);
            }
        }
        firstWavePath.rLineTo(0, getHeight());
        firstWavePath.lineTo(firstWavePosX, getHeight());
        firstWavePath.close();
        canvas.drawPath(firstWavePath, paint);
        firstWavePosX += FIRST_WAVE_SPEED;
        if (firstWavePosX > 0) {
            firstWavePosX = -(waveWidth * 2);
        }
    }

    private void drawSecondWave(Canvas canvas) {
        secondWavePath.reset();
        secondWavePath.moveTo(secondWavePosX, centerY);
        for (int i = 0; i < (WAVE_COUNT + 2) * 2; i++) {
            if (i % 2 == 0) {
                secondWavePath.rQuadTo(waveWidth / 2, -WAVE_HEIGHT, waveWidth, 0);
            } else {
                secondWavePath.rQuadTo(waveWidth / 2, WAVE_HEIGHT, waveWidth, 0);
            }
        }

        pathMeasure.setPath(secondWavePath, false);
        matrix.reset();
        //TODO 此处有个计算错误尚未解决，想要保持小船在圆心，需要减去这段移动距离的曲线长度而不是减去移动距离
        pathMeasure.getMatrix(pathMeasure.getLength() / 2 - (secondWavePosX + 2 * waveWidth), matrix, PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);
        matrix.preTranslate(-shipBitmap.getWidth() / 2, -shipBitmap.getHeight());

        secondWavePath.rLineTo(0, getHeight());
        secondWavePath.lineTo(secondWavePosX, getHeight());
        secondWavePath.close();

        canvas.drawPath(secondWavePath, paint);
        secondWavePosX += SECOND_WAVE_SPEED;
        if (secondWavePosX > 0) {
            secondWavePosX = -(waveWidth * 2);
        }
    }

}
