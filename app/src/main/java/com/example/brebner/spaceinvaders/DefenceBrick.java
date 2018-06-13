package com.example.brebner.spaceinvaders;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class DefenceBrick {

    public final int BRICKCOLOUR = Color.argb(255, 128, 255, 128);

    private RectF rect;

    private boolean isVisible;

    private Paint paint;

    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY){

        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        // Sometimes a bullet slips through this padding.
        // Set padding to zero if this annoys you
        int brickPadding = 1;

        // The number of shelters
        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY /8 * 2);

        rect = new RectF(column * width + brickPadding +
                (shelterPadding * shelterNumber) +
                shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (shelterPadding * shelterNumber) +
                        shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
        paint = new Paint();
        paint.setColor(BRICKCOLOUR);
    }

    public RectF getRect() {
        return rect;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setInvisible() {
        isVisible = false;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

}
