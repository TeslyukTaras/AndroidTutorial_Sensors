package com.teslyuk.android.sensors.model;

/**
 * Created by taras.teslyuk on 10/7/15.
 */
public class Ball {
    public static int MAX_ID = 0;
    public int ID;
    public float x, y;
    public float vx, vy;
    public boolean collision = false;
    public int lastCollisionTime = 0;

    public boolean touch = false;
    public int lastTouchTime = 0;
    public int radius;

    public Ball(float x, float y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;

        this.ID = MAX_ID;
        MAX_ID++;
    }

    public Ball(float x, float y, float vx, float vy, int radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;

        this.ID = MAX_ID;
        MAX_ID++;
    }

    public void setCollision() {
        collision = true;
        lastCollisionTime = 0;
    }

    public void setTouch() {
        touch = true;
        lastTouchTime = 0;
    }
}
