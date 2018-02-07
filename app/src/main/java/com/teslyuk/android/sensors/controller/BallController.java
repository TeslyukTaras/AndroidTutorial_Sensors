package com.teslyuk.android.sensors.controller;

import com.teslyuk.android.sensors.C;
import com.teslyuk.android.sensors.model.Ball;
import com.teslyuk.android.sensors.model.BlackHole;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by taras.teslyuk on 10/7/15.
 */
public class BallController {
    public static boolean HAS_COLLISION_AT_CURRENT_TICK = false;

    public static List<BlackHole> blackHoles = new ArrayList<>();
    public static List<Ball> balls;

    public static void initSystemInitialState(int screenWidthPixels, int screenHeightPixels) {
        Random random = new Random();
        balls = new ArrayList<Ball>();
        //згенерувати випадково розташовані кульки
        float x, y, vx, vy;
        Ball ball;
        for (int i = 0; i < C.BALLS_COUNT; i++) {
            x = random.nextInt(screenWidthPixels);
            y = random.nextInt(screenHeightPixels);
            vx = 0;//(random.nextFloat()-0.5f) * (screenWidthPixels/10f);
            vy = 0;//(random.nextFloat()-0.5f) * (screenWidthPixels/10f);
            ball = new Ball(x, y, vx, vy, C.BALLS_RADIUS);
            balls.add(ball);
        }
    }

    public static void controllBall(Ball ball, float dt, int screenWidth, int screenHeight, float ax, float ay) {
        //перерахувати швидкість
        ball.vx += ax * dt * C.GRAVITY_COEF;
        ball.vy += ay * dt * C.GRAVITY_COEF;

        //додати тертя
        ball.vx *= (1f - dt * C.FRICTION_COEF);
        ball.vy *= (1f - dt * C.FRICTION_COEF);

        //перерахувати координати
        ball.x += ball.vx * dt;
        ball.y += ball.vy * dt;

        //виявлення колізій із стінками
        if (ball.x < ball.radius) {
            ball.vx = Math.abs(ball.vx) + C.ADDITIONAL_IMPULSE;
            setBallsCollision(ball);
        }
        if (ball.x > screenWidth - ball.radius) {
            ball.vx = -Math.abs(ball.vx) - C.ADDITIONAL_IMPULSE;
            setBallsCollision(ball);
        }
        if (ball.y < ball.radius) {
            ball.vy = Math.abs(ball.vy) + C.ADDITIONAL_IMPULSE;
            setBallsCollision(ball);
        }
        if (ball.y > screenHeight - ball.radius) {
            ball.vy = -Math.abs(ball.vy) - C.ADDITIONAL_IMPULSE;
            setBallsCollision(ball);
        }

        for (BlackHole blackHole : blackHoles) {
            blackHole.gravityForBall(dt, ball);
        }
    }

    public static boolean controllBalls(float dt, int screenWidth, int screenHeight, float ax, float ay) {
        HAS_COLLISION_AT_CURRENT_TICK = false;
        for (Ball ball : balls) {
            if (ball.touch) {
                ball.lastTouchTime++;
                if (ball.lastTouchTime > C.BALLS_TOUCH_TIME) {
                    ball.touch = false;
                }
            }
            if (ball.collision) {
                ball.lastCollisionTime++;
                if (ball.lastCollisionTime > C.BALLS_COLLISION_TIME) {
                    ball.collision = false;
                }
            }
        }

        Ball ball = null;
        for (int i = 0; i < balls.size(); i++) {
            ball = balls.get(i);
            //перерахувати швидкість і зіткнення із стінками
            controllBall(ball, dt, screenWidth, screenHeight, ax, ay);

            //перевірити, чи кульки не зіткнулися
            for (int j = i + 1; j < balls.size(); j++) {
                //вістань між центрами кульок
                float l = (float) Math.sqrt((ball.x - balls.get(j).x) * (ball.x - balls.get(j).x) +
                        (ball.y - balls.get(j).y) * (ball.y - balls.get(j).y));
                if (l < ball.radius * 2) {
                    //відбулося зіткнення
                    //нехай їх відносить в протилежні боки
                    float vcx = (ball.vx + balls.get(j).vx) / 2f;
                    float vcy = (ball.vy + balls.get(j).vy) / 2f;
                    float dvx = ball.vx - vcx;
                    float dvy = ball.vy - vcy;
                    //перевірити, чи вони наближаються, чи розлітаються
                    if (dvx * (balls.get(j).x - ball.x) + dvy * (balls.get(j).y - ball.y) > 0) {
                        ball.vx -= 2 * dvx;
                        ball.vy -= 2 * dvy;
                        balls.get(j).vx += 2 * dvx;
                        balls.get(j).vy += 2 * dvy;
                        setBallsCollision(ball);
                        setBallsCollision(balls.get(j));
                    }
                }
            }
        }

        return HAS_COLLISION_AT_CURRENT_TICK;
    }

    public static void explosionBalls(float touchX, float touchY) {
        float dx, dy;

        for (Ball ball : balls) {
            float l = (float) Math.sqrt((ball.x - touchX) * (ball.x - touchX) +
                    (ball.y - touchY) * (ball.y - touchY));

            if (l < 1) l = 1;
            //кулька має відлетіти
            if (l < C.TOUCH_RADIUS) {
                dx = ball.x - touchX;
                dy = ball.y - touchY;

                ball.vx += C.TOUCH_ACCELERATION * dx / l;
                ball.vy += C.TOUCH_ACCELERATION * dy / l;

                ball.setTouch();
            }
        }
    }

    private static void setBallsCollision(Ball ball) {
        ball.setCollision();
        HAS_COLLISION_AT_CURRENT_TICK = true;
    }

    public static void addBlackHole(float touchX, float touchY) {
        BlackHole blackHole = new BlackHole(touchX, touchY, 100000);
        blackHoles.add(blackHole);
    }

    public static void clearBlackHoles() {
        blackHoles.clear();
    }
}
