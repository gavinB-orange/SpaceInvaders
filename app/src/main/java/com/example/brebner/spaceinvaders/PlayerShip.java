package com.example.brebner.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.util.Log;

public class PlayerShip {

    public final static int SHIPSPEED = 350;
    private static final String TAG = "PlayerShip";

    RectF rectF;

    Bitmap bitmap;

    private float length;
    private float height;

    private float x;
    private float y;

    private float shipSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY) {
        rectF = new RectF();
        length = screenX / 10;
        height = screenY / 10;

        x = screenX / 2;
        y = screenY - 20;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) length,
                (int) height,
                false);
        shipSpeed = SHIPSPEED;
    }

    public RectF getRectF() {
        return rectF;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getLength() {
        return length;
    }

    public float getHeight() {
        return height;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setMovementState(int state) {
        shipMoving = state;
    }

    public void update(long fps) {
        switch (shipMoving) {
            case LEFT:
                x = x - shipSpeed / fps;
                break;
            case RIGHT:
                x = x + shipSpeed / fps;
                break;
            case STOPPED:
                break;
            default:
                Log.e(TAG, "update: UNKNOWN SHIP STATE", null);
        }
        rectF.top = y;
        rectF.bottom = y + height;
        rectF.left = x;
        rectF.right = x + length;
    }
}
