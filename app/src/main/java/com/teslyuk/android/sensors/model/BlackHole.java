package com.teslyuk.android.sensors.model;

import com.teslyuk.android.sensors.C;

/**
 * Created by taras.teslyuk on 2/20/16.
 */
public class BlackHole {
    public static float MIN_RADIUS = 8f;
    public static float FRICTION_RADIUS = MIN_RADIUS * 5f;

    public float x, y;
    public float mass;
    public float radius;

    public static int MAX_ID = 0;
    public int ID;

    public BlackHole(float x, float y, float mass) {
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.radius = MIN_RADIUS * 2;
        this.ID = MAX_ID;
        MAX_ID++;
    }

    public void gravityForBall(float dt, Ball ball) {
        float dx = x - ball.x;
        float dy = y - ball.y;
        float dl = (float) Math.sqrt(dx * dx + dy * dy);
        if (dl > FRICTION_RADIUS) {
            float impulse = getImpulse(dl);
            ball.vx += dt * (dx / dl) * impulse;
            ball.vy += dt * (dy / dl) * impulse;
        } else {
            //додати тертя
            ball.vx *= (1f - dt * C.FRICTION_COEF);
            ball.vy *= (1f - dt * C.FRICTION_COEF);
        }
    }

    private float getImpulse(float dl) {
        if (dl < MIN_RADIUS) {
            return C.GRAVITY_COEF * mass / MIN_RADIUS / MIN_RADIUS;
        } else {
            return C.GRAVITY_COEF * mass / dl / dl;
        }
    }
}
