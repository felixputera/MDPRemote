package com.mdpgrp4.mdpremote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class MapView extends View {
    public static final int STATUS_UNEXPLORED = 0;
    public static final int STATUS_EMPTY = 1;
    public static final int STATUS_OBSTACLE = 2;
    public static final int STATUS_SELECTED = 3;
    public static final int STATUS_ROBOT = 4;
    private static final int TILE_MARGIN = 5;
    private int[][] tileStatus;
    private Paint mUnexploredPaint, mEmptyPaint, mObstaclePaint, mSelectedPaint, mRobotPaint;
    private float xSize, ySize;
    private GestureDetectorCompat detector;


    public MapView(Context context) {
        super(context);
        init(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        tileStatus = new int[15][20];

        mUnexploredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnexploredPaint.setColor(ContextCompat.getColor(context, R.color.mapUnexplored));
        mUnexploredPaint.setStyle(Paint.Style.FILL);

        mEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEmptyPaint.setColor(ContextCompat.getColor(context, R.color.mapEmpty));
        mEmptyPaint.setStyle(Paint.Style.FILL);

        mObstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mObstaclePaint.setColor(ContextCompat.getColor(context, R.color.mapObstacle));
        mObstaclePaint.setStyle(Paint.Style.FILL);

        mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(ContextCompat.getColor(context, R.color.mapSelected));
        mSelectedPaint.setStyle(Paint.Style.FILL);

        mRobotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRobotPaint.setColor(ContextCompat.getColor(context, R.color.mapRobot));
        mRobotPaint.setStyle(Paint.Style.FILL);

        detector = new GestureDetectorCompat(getContext(), new GestureTap());
    }

    public void setTileStatus(int[][] status) {
        tileStatus = status;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        xSize = (w - TILE_MARGIN * 14) / 15;
        ySize = (h - TILE_MARGIN * 19) / 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 20; y++) {
                float left = x * (xSize + TILE_MARGIN);
                float top = y * (ySize + TILE_MARGIN);
                float right = left + xSize;
                float bottom = top + ySize;
                Paint tilePaint;
                switch (tileStatus[x][y]) {
                    case STATUS_UNEXPLORED:
                        tilePaint = mUnexploredPaint;
                        break;
                    case STATUS_EMPTY:
                        tilePaint = mEmptyPaint;
                        break;
                    case STATUS_OBSTACLE:
                        tilePaint = mObstaclePaint;
                        break;
                    case STATUS_SELECTED:
                        tilePaint = mSelectedPaint;
                        break;
                    case STATUS_ROBOT:
                        tilePaint = mRobotPaint;
                        break;
                    default:
                        tilePaint = mUnexploredPaint;
                        break;
                }
                canvas.drawRect(left, top, right, bottom, tilePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            float touchX = e.getX();
            float touchY = e.getY();

            Log.d("touchX: ", String.valueOf(touchX));
            Log.d("touchY: ", String.valueOf(touchY));

            int x = (int) Math.round(Math.floor(touchX / (xSize + TILE_MARGIN)));
            int y = (int) Math.round(Math.floor(touchY / (ySize + TILE_MARGIN)));

            Log.d("x: ", String.valueOf(x));
            Log.d("y: ", String.valueOf(y));

            tileStatus[x][y] = STATUS_SELECTED;

            MapView.this.invalidate();

            return true;
        }
    }
}
