package com.example.brebner.spaceinvaders;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;


public class SpaceInvadersView extends SurfaceView implements Runnable {

    private static final String TAG = "SpaceInvadersView";

    private final int BACKGROUND = Color.argb(255, 26, 128, 182);
    private final int FOREGROUND = Color.argb(255, 255, 255, 255);
    private final int IBCOLOUR = Color.argb(255, 255, 0, 255);
    private final int PBCOLOUR = Color.argb(255, 0, 255, 0);
    private final int HUD_COLOUR = Color.argb(255, 249, 129, 0);
    private final int HUD_TEXT_SIZE = 40;
    public static final int MENANCE_INTERVAL = 1000;
    public final int NCOLUMNS = 6;
    public final int NROWS = 5;

    Context context;

    private Thread gameThread = null;

    private SurfaceHolder surfaceHolder;

    private volatile boolean playing;

    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    private long fps;

    private long timeThisFrame;

    private int screenX;
    private int screenY;

    private PlayerShip playerShip;
    private Bullet bullet;
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;

    // sound
    private SoundPool soundPool;
    private SoundPool.Builder spb;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    int score = 0;
    private int lives = 3;

    private long menanceInterval = MENANCE_INTERVAL;
    private boolean uhOrOh;
    private long lastMenaceTime = System.currentTimeMillis();

    public SpaceInvadersView(Context context, int x, int y) {
        super(context);
        this.context = context;
        surfaceHolder = getHolder();
        paint = new Paint();
        screenX = x;
        screenY = y;
        loadSounds(context);
        prepareLevel();
    }

    private void prepareLevel() {
        // init games objects
        menanceInterval = 1000;

        // make new player space ship
        playerShip = new PlayerShip(context, screenX, screenY);

        // prepare players bullet
        bullet = new Bullet(screenY);

        // init invaders bullet array
        for (int i = 0; i < invadersBullets.length; i++) {
            invadersBullets[i] = new Bullet(screenY);
        }

        // build army of Invaders
        numInvaders = 0;
        for (int column = 0; column < NCOLUMNS; column++) {
            for (int row = 0; row < NROWS; row++) {
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders++;
            }
        }

        // build shelters
        numBricks = 0;
        for (int s = 0; s < 4; s++) {
            for (int column = 0; column < 10; column++) {
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, s, screenX, screenY);
                    numBricks++;
                }
            }
        }
    }

    private void loadSounds(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spb = new SoundPool.Builder();
            spb.setMaxStreams(4);
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            spb.setAudioAttributes(aa);
            soundPool = spb.build();
        }
        else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            Log.d(TAG, "loadSounds: OK");
        }
        catch(IOException e) {
            // Print an error message to the console
            Log.e(TAG, "loadSounds: FAILED to load", e);
        }
    }

    @Override
    public void run() {
        while (playing) {
            long startFrameTime = System.currentTimeMillis();
            if (! paused) {
                update();
            }
            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
            if (! paused) {
                if (startFrameTime - lastMenaceTime > menanceInterval) {
                    if (uhOrOh) {
                        soundPool.play(uhID, 1, 1, 0, 0, 1);
                    }
                    else {
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }
                    lastMenaceTime = System.currentTimeMillis();
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    private void update() {
        boolean bumped = false;
        boolean lost = false;

        // move ship
        playerShip.update(fps);

        // update invaders
        for (int i = 0; i < numInvaders; i++) {
            if (invaders[i].isVisible()) {
                invaders[i].update(fps);
                if (invaders[i].takeAim(playerShip.getX(), playerShip.getLength())) {
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {
                        nextBullet++;
                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0;
                        }
                    }
                }
                if (invaders[i].getX() > screenX - invaders[i].getLength() || invaders[i].getX() < 0) {
                    bumped = true;
                }
            }
        }

        // update invader bullets

        // did invader bump edge of screen
        if (bumped) {
            for (int i = 0; i < numInvaders; i++) {
                invaders[i].dropDownAndReverse();
                if (invaders[i].getY() > screenY - screenY / 10) {
                    lost = true;
                }
            }
            menanceInterval = menanceInterval - 80;
        }

        if (lost) {
            prepareLevel();
        }

        // Update the players bullet
        if(bullet.getStatus()){
            bullet.update(fps);
        }
        if (bullet.getImpactPointY() < 0) {
            bullet.setInactive();
        }
        // has player bullet hit an invader
        if (bullet.getStatus()) {
            for (int i =0; i < numInvaders; i++) {
                if (invaders[i].isVisible()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRectF())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score += 10;
                        if (score == numInvaders * 10) {
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }
        // has player bullet hit a shelter brick
        if (bullet.getStatus()) {
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].isVisible()) {
                    if (RectF.intersects(bullet.getRect(), bricks[i].getRect())) {
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // Update all the invaders bullets if active
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // has an invader bullet hit the bottom of the screen
        for (int i = 0; i < numInvaders; i++) {
            if (invadersBullets[i].getImpactPointY() > screenY) {
                invadersBullets[i].setInactive();
            }
        }
        // has alien bullet hit stuff
        for (int i =0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                for (int j = 0; j < numBricks; j++) {
                    if (bricks[j].isVisible()) {
                        if (RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            // collision
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }
        // has invader hit player
        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus()) {
                if (RectF.intersects(playerShip.getRectF(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    // game over?
                    if (lives <= 0) {
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();
                    }
                }
            }
        }


    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(BACKGROUND);
            // paint.setColor(FOREGROUND);
            // draw spaceship
            playerShip.draw(canvas);

            // draw invaders
            for (int i =0; i < numInvaders; i++) {
                if (invaders[i].isVisible()) {
                    invaders[i].draw(canvas, uhOrOh);
                }
            }

            // draw bricks
            //paint.setColor(getResources().getColor(R.color.white));
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].isVisible()) {
                    bricks[i].draw(canvas);
                }
            }

            // draw player bullet
            paint.setColor(PBCOLOUR);
            if(bullet.getStatus()){
                canvas.drawRect(bullet.getRect(), paint);
            }

            // Draw all the invader's bullets if active
            paint.setColor(IBCOLOUR);
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // draw score / lives
            paint.setColor(HUD_COLOUR);
            paint.setTextSize(HUD_TEXT_SIZE);
            canvas.drawText("TOTO Score: " + score + "  Lives : " + lives, 10, 50, paint);

            // done
            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "pause: ERROR", e);
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                paused = false;
                if(motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }
                if(motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                    if(bullet.shoot(playerShip.getX()+
                            playerShip.getLength()/2,screenY,bullet.UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_BUTTON_RELEASE:
                this.performClick();
                break;
            default:
                Log.e(TAG, "onTouchEvent: something unexpectedi happened", null);
        }
        return true;
    }

    @Override
    public boolean performClick() {
        Log.d(TAG, "performClick!");
        return super.performClick();
    }

}
