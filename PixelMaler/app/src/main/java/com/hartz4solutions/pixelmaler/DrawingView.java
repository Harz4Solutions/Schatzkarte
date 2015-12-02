package com.hartz4solutions.pixelmaler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * Die DrawingView ist für die Darstellung und Verwaltung der Zeichenfläche
 * zuständig.
 */
public class DrawingView extends View {

    private static final int GRID_SIZE = 11;

    private Path drawPath = new Path();
    private Paint drawPaint = new Paint();
    private Paint linePaint = new Paint();
    private boolean isErasing = false;
    private ArrayList<MyPoint> touchedPoints = new ArrayList<>();
    int maxX;
    int maxY;
    int stepSizeX;
    int stepSizeY;
    int[][] paintPixels;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        linePaint.setColor(0xFF666666);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(1.0f);
        linePaint.setStyle(Paint.Style.STROKE);

        paintPixels = new int[GRID_SIZE][GRID_SIZE];
    }

    @Override
    protected void onDraw(Canvas canvas) {

        maxX = canvas.getWidth();
        maxY = canvas.getHeight();

        stepSizeX = (int) Math.ceil((double) maxX / GRID_SIZE);
        stepSizeY = (int) Math.ceil((double) maxY / GRID_SIZE);

        // TODO Zeichne das Gitter

        for (int i = 0; i < paintPixels.length; i++) {
            for (int j = 0; j < paintPixels.length; j++) {
                int color = paintPixels[i][j];
                if(color!=0){
                    Paint p = new Paint();
                    p.setColor(color);
                    canvas.drawRect(i * stepSizeX, j * stepSizeY, i * stepSizeX + stepSizeX, j * stepSizeY + stepSizeY, p);
                }
            }
        }
        Paint line = new Paint();
        line.setColor(Color.GRAY);
        for (int i = 1; i < GRID_SIZE; i++) {
            canvas.drawLine(i * stepSizeX, 0, i * stepSizeX, maxY, line);
            canvas.drawLine(0, i * stepSizeY, maxX, i * stepSizeY, line);
        }

        // Zeichnet einen Pfad der dem Finger folgt
        canvas.drawPath(drawPath, drawPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if(touchX>0 && touchY>0){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);

                    // TODO wir müssen uns die berührten Punkte zwischenspeichern
                    touchedPoints.add(new MyPoint(touchX,touchY));

                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);

                    // TODO wir müssen uns die berührten Punkte zwischenspeichern
                    touchedPoints.add(new MyPoint(touchX,touchY));

                    break;
                case MotionEvent.ACTION_UP:

                    // TODO Jetzt können wir die zwischengespeicherten Punkte auf das
                    // Gitter umrechnen und zeichnen, bzw. löschen, falls wir isErasing
                    // true ist (optional)
                    for (int i = 0; i < touchedPoints.size(); i++) {
                        MyPoint p = touchedPoints.get(i);
                        if(isErasing){
                            paintPixels[(int)(p.getX()/stepSizeX)][(int)(p.getY()/stepSizeY)] = 0;
                        }else {
                            paintPixels[(int)(p.getX()/stepSizeX)][(int)(p.getY()/stepSizeY)] = drawPaint.getColor();
                        }
                    }
                    touchedPoints.clear();
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
        }

        invalidate();
        return true;
    }

    public void startNew() {

        // TODO Gitter löschen
        paintPixels = new int[GRID_SIZE][GRID_SIZE];
        invalidate();
    }

    public void setErase(boolean isErase) {
        isErasing = isErase;
           if (isErasing) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setColor(String color) {
        invalidate();
        drawPaint.setColor(Color.parseColor(color));
    }

    public int[][] getPaintPixels() {
        return paintPixels;
    }

    class MyPoint{
        float x;
        float y;
        public MyPoint(float x, float y){
            this.x =x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
