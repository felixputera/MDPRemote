package com.mdpgrp4.mdpremote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
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
    public static final int STATUS_ROBOT_EMPTY = 0;
    public static final int STATUS_ROBOT = 4;
    public static final int STATUS_ROBOT_ORIENTATION = 5;

    public static final int ORIENTATION_UP = 0;
    public static final int ORIENTATION_RIGHT = 90;
    public static final int ORIENTATION_DOWN = 180;
    public static final int ORIENTATION_LEFT = 270;

    private static final int TILE_MARGIN = 5;
    private int[][] tileStatus; //x, y format
    private int[][] tileRobot; //x, y format
    private Paint mUnexploredPaint, mEmptyPaint, mObstaclePaint, mSelectedPaint, mRobotPaint,
            mOrientationPaint, mTransparentPaint;
    private float xSize, ySize;
    private GestureDetectorCompat detector;
    private boolean touchEnabled = false;
    private boolean touchRobot = false;
    private boolean touchWaypoint = false;
    private int[] waypoint = new int[2];
    private int[] robotPos = {1, 1, ORIENTATION_UP}; //y, x, orientation format (column, row, orientation)


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

    @NonNull
    private static String hexToBin(String hex) {
        StringBuilder result = new StringBuilder(hex.length() * 4);
        for (char c : hex.toUpperCase().toCharArray()) {
            switch (c) {
                case '0':
                    result.append("0000");
                    break;
                case '1':
                    result.append("0001");
                    break;
                case '2':
                    result.append("0010");
                    break;
                case '3':
                    result.append("0011");
                    break;
                case '4':
                    result.append("0100");
                    break;
                case '5':
                    result.append("0101");
                    break;
                case '6':
                    result.append("0110");
                    break;
                case '7':
                    result.append("0111");
                    break;
                case '8':
                    result.append("1000");
                    break;
                case '9':
                    result.append("1001");
                    break;
                case 'A':
                    result.append("1010");
                    break;
                case 'B':
                    result.append("1011");
                    break;
                case 'C':
                    result.append("1100");
                    break;
                case 'D':
                    result.append("1101");
                    break;
                case 'E':
                    result.append("1110");
                    break;
                case 'F':
                    result.append("1111");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid hex: '" + hex + "'");
            }
        }
        return result.toString();
    }

    private void init(Context context) {
        tileStatus = new int[15][20];
        tileRobot = new int[15][20];

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

        mTransparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTransparentPaint.setColor(Color.TRANSPARENT);

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
        robotPos[2] = (robotPos[2] + 270) % 360;
        MapView.this.invalidate();
    }

    public void rotateRobotClock() {
        robotPos[2] = (robotPos[2] + 90) % 360;
        MapView.this.invalidate();
    }

    public void enableTouchWaypoint() {
        touchEnabled = true;
        touchWaypoint = true;
    }

    public int[] disableTouchWaypoint() {
        touchEnabled = false;
        touchWaypoint = false;
        return waypoint;
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

        // draw map
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
                    default:
                        tilePaint = mUnexploredPaint;
                        break;
                }
                canvas.drawRect(left, top, right, bottom, tilePaint);
            }
        }

        // draw robot
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 20; y++) {
                if (tileRobot[x][19 - y] != STATUS_ROBOT_EMPTY) {
                    float left = x * (xSize + TILE_MARGIN);
                    float top = y * (ySize + TILE_MARGIN);
                    float right = left + xSize;
                    float bottom = top + ySize;
                    Paint tilePaint;
                    switch (tileRobot[x][19 - y]) {
                        case STATUS_ROBOT:
                            tilePaint = mRobotPaint;
                            break;
                        case STATUS_ROBOT_ORIENTATION:
                            tilePaint = mOrientationPaint;
                            break;
                        default:
                            tilePaint = mTransparentPaint;
                            break;
                    }
                    canvas.drawRect(left, top, right, bottom, tilePaint);
                }
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
                if (i >= robotPos[1] - 1 && i <= robotPos[1] + 1 && j <= robotPos[0] + 1 && j >= robotPos[0] - 1) {
                    tileRobot[i][j] = STATUS_ROBOT;
                    switch (robotPos[2]) {
                        case ORIENTATION_UP:
                            if (i == robotPos[1] && j == robotPos[0] + 1) {
                                tileRobot[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_RIGHT:
                            if (i == robotPos[1] + 1 && j == robotPos[0]) {
                                tileRobot[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_DOWN:
                            if (i == robotPos[1] && j == robotPos[0] - 1) {
                                tileRobot[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                        case ORIENTATION_LEFT:
                            if (i == robotPos[1] - 1 && j == robotPos[0]) {
                                tileRobot[i][j] = STATUS_ROBOT_ORIENTATION;
                            }
                            break;
                    }
                } else if (tileRobot[i][j] == STATUS_ROBOT || tileRobot[i][j] == STATUS_ROBOT_ORIENTATION) {
                    tileRobot[i][j] = STATUS_ROBOT_EMPTY;
                }
            }
        }
    }

    public void setMapDescriptor(String obstacleMapHex, String explorationMapHex) {
        if (obstacleMapHex == null) {
            obstacleMapHex = "000000000000000000000000000000000000000000000000000000000000000000000000000";
        }
        if (explorationMapHex == null) {
            explorationMapHex = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        }

        String explorationMapBin = hexToBin(explorationMapHex);
        String obstacleMapBin = hexToBin(obstacleMapHex);

        if (obstacleMapBin.length() == 304) {
            obstacleMapBin = obstacleMapBin.substring(2, 302);
        }

        int obstacleIndex = 0;
        for (int i = 0; i < 300; i++) {
            Log.d("MAP_ACTIVITY", "Exp map char at " + i + ":" + explorationMapBin.charAt(i));
            int curStatus = STATUS_UNEXPLORED;
            if (explorationMapBin.charAt(i) == '1') {
                if (obstacleMapBin.charAt(obstacleIndex) == '1') {
                    curStatus = STATUS_OBSTACLE;
                } else if (obstacleMapBin.charAt(obstacleIndex) == '0') {
                    curStatus = STATUS_EMPTY;
                }
                obstacleIndex++;
            }
            tileStatus[i % 15][i / 15] = curStatus;
        }

        MapView.this.invalidate();
    }

    public void setRobotPos(int[] robotPos) {
        this.robotPos[0] = robotPos[0];
        this.robotPos[1] = robotPos[1];
        this.robotPos[2] = robotPos[2];
        MapView.this.invalidate();
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
                } else if (y >= 18) {
                    y = 18;
                }
                if (x <= 1) {
                    x = 1;
                } else if (x >= 13) {
                    x = 13;
                }
                robotPos[1] = x;
                robotPos[0] = 19 - y;

                MapView.this.invalidate();
            } else if (touchWaypoint) {
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 20; j++) {
                        if (i == x && j == 19 - y && tileStatus[i][j] == STATUS_EMPTY) {
                            tileStatus[i][j] = STATUS_SELECTED;
                            waypoint[0] = j;
                            waypoint[1] = i;
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
