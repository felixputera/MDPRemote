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
    public static final int STATUS_ROBOT_ORIENTATION = 5;

    public static final int ORIENTATION_UP = 0;
    public static final int ORIENTATION_RIGHT = 1;
    public static final int ORIENTATION_DOWN = 2;
    public static final int ORIENTATION_LEFT = 3;

    private static final int TILE_MARGIN = 5;
    private int[][] tileStatus;
    private Paint mUnexploredPaint, mEmptyPaint, mObstaclePaint, mSelectedPaint, mRobotPaint, mOrientationPaint;
    private float xSize, ySize;
    private GestureDetectorCompat detector;
    private boolean touchEnabled = false;
    private boolean touchRobot = false;
    private boolean touchWaypoint = false;
    private int[] robotPos = {1, 1};
    private int robotOrientation = ORIENTATION_UP;


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

        mOrientationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOrientationPaint.setColor(ContextCompat.getColor(context, R.color.mapRobotOrientation));
        mOrientationPaint.setStyle(Paint.Style.FILL);

        detector = new GestureDetectorCompat(getContext(), new GestureTap());
    }

    public void setTileStatus(int[][] status) {
        tileStatus = status;
    }

    public void enableTouchRobot() {
        touchEnabled = true;
        touchRobot = true;
    }

    public void disableTouchRobot() {
        touchEnabled = false;
        touchRobot = false;
    }

    public void rotateRobotAnti() {
        robotOrientation = (robotOrientation + 3) % 4;
        MapView.this.invalidate();
    }

    public void rotateRobotClock() {
        robotOrientation = (robotOrientation + 1) % 4;
        MapView.this.invalidate();
    }

    public void enableTouchWaypoint() {
        touchEnabled = true;
        touchWaypoint = true;
    }

    public void disableTouchWaypoint() {
        touchEnabled = false;
        touchWaypoint = false;
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
        setRobotTile();
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 20; y++) {
                float left = x * (xSize + TILE_MARGIN);
                float top = y * (ySize + TILE_MARGIN);
                float right = left + xSize;
                float bottom = top + ySize;
                Paint tilePaint;
                switch (tileStatus[x][19 - y]) {
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
                    case STATUS_ROBOT_ORIENTATION:
                        tilePaint = mOrientationPaint;
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
        if (touchEnabled) {
            detector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void setRobotTile() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 20; j++) {
                if (i >= robotPos[0] - 1 && i <= robotPos[0] + 1 && j <= robotPos[1] + 1 && j >= robotPos[1] - 1) {
                    tileStatus[i][j] = STATUS_ROBOT;
                    switch (robotOrientation) {
                        case ORIENTATION_UP:
                            if (i == robotPos[0] && j == robotPos[1] + 1) {
                                tileStatus[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_RIGHT:
                            if (i == robotPos[0] + 1 && j == robotPos[1]) {
                                tileStatus[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_DOWN:
                            if (i == robotPos[0] && j == robotPos[1] - 1) {
                                tileStatus[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_LEFT:
                            if (i == robotPos[0] - 1 && j == robotPos[1]) {
                                tileStatus[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                    }
                } else if (tileStatus[i][j] == STATUS_ROBOT || tileStatus[i][j] == STATUS_ROBOT_ORIENTATION) {
                    tileStatus[i][j] = STATUS_UNEXPLORED;
                }
            }
        }
    }

    public void setMapDescriptor(String obstacleMapHex, String explorationMapHex) {
        String explorationMapBin = String.format("%300s",
                Integer.toBinaryString(Integer.parseInt(explorationMapHex, 16)).replace(' ', '0'));
        if (explorationMapBin.length() == 304) {
            explorationMapBin = explorationMapBin.substring(2, 302);
        }

        int obstacleMapHexLen = obstacleMapHex.length();
        String obstacleMapBin = Integer.toBinaryString(Integer.parseInt(obstacleMapHex, 16));

        int obstacleMapBinLen = obstacleMapBin.length();
        if (obstacleMapBinLen < obstacleMapHexLen * 4) {
            for (int i = 0; i < obstacleMapHexLen * 4 - obstacleMapBinLen; i++) {
                obstacleMapBin = "0" + obstacleMapBin;
            }
        }

        int obstacleIndex = 0;
        for (int i = 0; i < 300; i++) {
            if (explorationMapBin.charAt(i) == 1) {
//                if () {
//
//                }

            }
        }
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

            if (touchRobot) {
                if (y <= 1) {
                    y = 1;
                } else if (y >= 13) {
                    y = 13;
                }
                if (x <= 1) {
                    x = 1;
                } else if (x >= 13) {
                    x = 13;
                }
                robotPos[0] = x;
                robotPos[1] = 19 - y;

                MapView.this.invalidate();
            } else if (touchWaypoint) {
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 20; j++) {
                        if (i == x && j == 19 - y && tileStatus[i][j] == STATUS_EMPTY) {
                            tileStatus[i][j] = STATUS_SELECTED;
                        } else if (tileStatus[i][j] == STATUS_SELECTED) {
                            tileStatus[i][j] = STATUS_EMPTY;
                        }
                    }
                }

                MapView.this.invalidate();
            }
            return true;
        }
    }
}
