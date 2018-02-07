package com.teslyuk.android.sensors;

/**
 * Created by taras.teslyuk on 2/20/16.
 */
public class C {
    public static final boolean MICRO_WORLD = false;
    public static final boolean ENABLE_BLACKHOLES = true;
    public static final boolean SOUND_EFFECTS_ENABLE = false;

    public static final float GRAVITY_COEF = 10f;//10f;//10f
    public static final float FRICTION_COEF = 0.05f;//0.05f

    //ball controller constants
    public static final int BALLS_COUNT = C.MICRO_WORLD ? 300 : 30;//30
    public static final int BALLS_RADIUS = C.MICRO_WORLD ? 10 : 40;//40
    public static final int BALLS_COLLISION_TIME = 36;//36

    public static final float ADDITIONAL_IMPULSE = 1f;//1f

    //Touch
    public static final int TOUCH_RADIUS = 200;//200
    public static final int TOUCH_ACCELERATION = 200;//200
    public static final int BALLS_TOUCH_TIME = 36;//36
}
