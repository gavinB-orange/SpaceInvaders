/*
 * Copyright (c) 2018. G&GApps
 */

package com.example.brebner.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

public class Invader {

    private static final String TAG = "Invader";

    public final int SHIPSPEED = 40;
    public final int PROPORTION = 20;
    public final int PADDING_SCALE = 25;

    RectF rectF;
    Random random = new Random();

    private Bitmap[] bitmaps = new Bitmap[2];

    private float height;
    private float length;

    private float x;
    private float y;

    private float shipSpeed;

    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = RIGHT;

    private boolean isVisible;

    private Paint paint;

    public Invader(Context context, int row, int column, int screenX, int screenY) {

        rectF = new RectF();
        length = screenX / PROPORTION;
        height = screenY / PROPORTION;

        isVisible = true;
        int padding = screenX / PADDING_SCALE;

        x = column * (length + padding);
        y = row * (length + padding / 4);

        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);
        for (int i=0; i<2; i++) {
            bitmaps[i] = Bitmap.createScaledBitmap(bitmaps[i], (int)length, (int)height, false);
        }
        shipSpeed = SHIPSPEED;
        paint = new Paint();
    }

    public RectF getRectF() {
        return rectF;
    }

    public float getLength() {
        return length;
    }

    public float getHeight() {
        return height;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setInvisible() {
        this.setVisible(false);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Bitmap[] getBitmaps() {
        return bitmaps;
    }

    public Bitmap getBitmap1() {
        return bitmaps[0];
    }

    public Bitmap getBitmap2() {
        return bitmaps[1];
    }

    public void update(long fps) {
        switch (shipMoving) {
            case LEFT:
                x = x - shipSpeed / fps;
                break;
            case RIGHT:
                x = x + shipSpeed / fps;
                break;
            default:
                Log.e(TAG, "update: UNKNOWN STATE", null);
        }
        rectF.top = y;
        rectF.bottom = y + height;
        rectF.left = x;
        rectF.right = x + length;
    }

    public void dropDownAndReverse() {
        if (shipMoving == LEFT) {
            shipMoving = RIGHT;
        }
        else {
            shipMoving = LEFT;
        }
        y = y + height;
        shipSpeed = shipSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength) {
        int rnumb;
        if ((playerShipX + playerShipLength > x && playerShipX + playerShipLength < x + length) ||
            (playerShipX > x && playerShipX < x + length)) {
            rnumb = random.nextInt(150);
            return (rnumb == 0);
        }
        rnumb = random.nextInt(2000);
        return (rnumb == 0);
    }

    public void draw(Canvas canvas, boolean w) {
        if (w) {
            canvas.drawBitmap(bitmaps[0], x, y, paint);
        }
        else {
            canvas.drawBitmap(bitmaps[1], x, y, paint);
        }
    }

}
