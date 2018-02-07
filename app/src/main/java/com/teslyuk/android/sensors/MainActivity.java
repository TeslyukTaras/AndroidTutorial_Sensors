package com.teslyuk.android.sensors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.teslyuk.android.sensors.controller.BallController;
import com.teslyuk.android.sensors.helper.SensorHelper;
import com.teslyuk.android.sensors.helper.SoundEffectHelper;
import com.teslyuk.android.sensors.model.Ball;
import com.teslyuk.android.sensors.model.BlackHole;

import java.util.Calendar;

public class MainActivity extends Activity implements SensorHelper.SensorHelperListener {

    private ImageView imageBottom, imageTop;
    private Bitmap topBitmap;

    private long currentTime = 0, previousTime = 0;

    private int screenWidthPixels, screenHeightPixels;

    private float previousX, previousY;
    private boolean hasMove = false;

    private SensorHelper sensorHelper;
    private SoundEffectHelper soundEffectHelper;
    private float ax, ay; //останні значення прискорення вільного падіння
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        initView();

        handler = new Handler();
        sensorHelper = new SensorHelper(this);
        soundEffectHelper = new SoundEffectHelper(this);
    }

    private void initView() {
        imageBottom = findViewById(R.id.bottom_image);
        imageTop = findViewById(R.id.main_image);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getScreenSize();
//        Log.d("getScreenSize", "screenWidthPixels: " + screenWidthPixels + " screenHeightPixels: " + screenHeightPixels);

        BallController.initSystemInitialState(screenWidthPixels, screenHeightPixels);
        sensorHelper.onResume(this);
        handler.postDelayed(calculationLoop, 40);
        handler.postDelayed(uiUpdateLoop, 40);
    }

    protected void onPause() {
        sensorHelper.onPause();

        handler.removeCallbacks(calculationLoop);
        handler.removeCallbacks(uiUpdateLoop);
        super.onPause();
    }

    // періодичне завдання по переобчисленню координат кульок
    private Runnable calculationLoop = new Runnable() {
        @Override
        public void run() {
            updateBalls(ax, ay);
            handler.postDelayed(this, 40);
        }
    };

    // періодичне завдання по відображенню кульок
    private Runnable uiUpdateLoop = new Runnable() {
        @Override
        public void run() {
            displayBalls();
            handler.postDelayed(this, 40);
        }
    };

    private void getScreenSize() {
        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        screenWidthPixels = metrics.widthPixels;
        screenHeightPixels = metrics.heightPixels;
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                screenWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                screenHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                screenWidthPixels = realSize.x;
                screenHeightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }

        screenHeightPixels -= navigationBarHeight();
    }

    private int navigationBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public void showSensorMissedAlert() {
        //показати діалог, що відсутній акселерометр
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Відсутній сенсор!")
                .setMessage("Акселерометр не знайдено")
                .setIcon(R.drawable.orb_blue_128)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAccelerometerUpdate(float x, float y) {
        ax = x;
        ay = y;
    }

    private void updateBalls(float ax, float ay) {
        currentTime = Calendar.getInstance().getTimeInMillis();
        //перерахувати швидкість
        //поправка на прискорення
        if (currentTime != 0 && previousTime != 0 && BallController.balls != null) {//перевірка, чи то не є перший запуск і чи є кулька проініціалізована
            float dt = (currentTime - previousTime) / 1000f;
            boolean hasCollision = BallController.controllBalls(dt, screenWidthPixels, screenHeightPixels, ax, ay);
            if (hasCollision) {
                soundEffectHelper.playSoundEffect(this);
            }
        }

        previousTime = currentTime;
    }

    private void displayBalls() {
//        Log.d("display balls","ball==null: "+(ball==null));
        imageTop.invalidate();
        imageTop.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageTop.getViewTreeObserver().removeOnPreDrawListener(this);
                imageTop.buildDrawingCache();

                Bitmap bmp = imageTop.getDrawingCache();
                //standart bitmap
                topBitmap = bmp;
                Canvas canvas = new Canvas(topBitmap);

                Paint paint = new Paint();
                paint.setFlags(Paint.FILTER_BITMAP_FLAG);

                Drawable dblue, dgreen, dred, dblack;

                if (Build.VERSION.SDK_INT >= 21) {
                    dblue = getResources().getDrawable(R.drawable.orb_blue_128, null);
                    dgreen = getResources().getDrawable(R.drawable.orb_green_128, null);
                    dred = getResources().getDrawable(R.drawable.orb_red_128, null);
                    dblack = getResources().getDrawable(R.drawable.orb_black_128, null);
                } else {
                    dblue = getResources().getDrawable(R.drawable.orb_blue_128);
                    dgreen = getResources().getDrawable(R.drawable.orb_green_128);
                    dred = getResources().getDrawable(R.drawable.orb_red_128);
                    dblack = getResources().getDrawable(R.drawable.orb_black_128);
                }

                //намалювати чорні дири
                if (BallController.blackHoles != null) {
                    for (BlackHole blackHole : BallController.blackHoles) {
                        dblack.setBounds((int) (blackHole.x - blackHole.radius), (int) (blackHole.y - blackHole.radius),
                                (int) (blackHole.x + blackHole.radius), (int) (blackHole.y + blackHole.radius));
                        dblack.draw(canvas);
                    }
                }
                //намалювати кульку
                if (BallController.balls != null) {
                    for (int i = 0; i < BallController.balls.size(); i++) {
                        Ball ball = BallController.balls.get(i);
                        if (ball != null) {
                            if (ball.touch) {
                                dred.setBounds((int) (ball.x - ball.radius), (int) (ball.y - ball.radius),
                                        (int) (ball.x + ball.radius), (int) (ball.y + ball.radius));
                                dred.draw(canvas);
                            } else if (ball.collision) {
                                dgreen.setBounds((int) (ball.x - ball.radius), (int) (ball.y - ball.radius),
                                        (int) (ball.x + ball.radius), (int) (ball.y + ball.radius));
                                dgreen.draw(canvas);
                            } else {
                                dblue.setBounds((int) (ball.x - ball.radius), (int) (ball.y - ball.radius),
                                        (int) (ball.x + ball.radius), (int) (ball.y + ball.radius));
                                dblue.draw(canvas);
                            }

//                    Log.d("display balls", "left: " + (int) (ball.x - ball.radius) +
//                                    "top: " + (int) (ball.y - ball.radius) +
//                                    "right: " + (int) (ball.x + ball.radius) +
//                                    "bottom: " + (int) (ball.y + ball.radius) );

                        }
                    }
                }


                if (Build.VERSION.SDK_INT >= 16) {
                    imageTop.setBackground(new BitmapDrawable(getResources(), topBitmap));
                } else {
                    imageTop.setBackgroundDrawable(new BitmapDrawable(getResources(), topBitmap));
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float initialX, initialY;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();
                previousX = initialX;
                previousY = initialY;
                BallController.explosionBalls(initialX, initialY);
                Log.d("TOUCH", "Action DOWN");
                return true;

            case MotionEvent.ACTION_UP:
                initialX = event.getX();
                initialY = event.getY();
                previousX = -1;
                previousY = -1;
                if (!hasMove && C.ENABLE_BLACKHOLES) {
                    BallController.addBlackHole(initialX, initialY);
                }
                hasMove = false;
                Log.d("TOUCH", "Action UP");
                return true;

            case MotionEvent.ACTION_MOVE:
                initialX = event.getX();
                initialY = event.getY();
                if (Math.abs(initialX - previousX) > 20 || Math.abs(initialY - previousY) > 20) {
                    if (C.ENABLE_BLACKHOLES) {
                        BallController.clearBlackHoles();
                    }
                    previousX = initialX;
                    previousY = initialY;
                    hasMove = true;
                    Log.d("TOUCH", "Action MOVE");
                }

                return true;

        }
        return super.onTouchEvent(event);
    }
}