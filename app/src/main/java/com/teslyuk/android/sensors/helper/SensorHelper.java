package com.teslyuk.android.sensors.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

/**
 * Created by teslyuk.taras on 2/7/18.
 */

public class SensorHelper implements SensorEventListener {

    private SensorHelperListener listener;
    private SensorManager mgr;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    public SensorHelper(SensorHelperListener listener) {
        this.listener = listener;
    }

    public void onResume(Context context) {
        //виведемо список доступних сенсорів
        mgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
        }

        boolean accelerometerExist = mgr.registerListener(this,
                mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        if (!accelerometerExist) {
            listener.showSensorMissedAlert();
        }
    }

    public void onPause() {
        if (mgr != null) {
            mgr.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            //пройшло принаймні 40 мс
            if ((curTime - lastUpdate) > 40) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                last_x = -x;
                last_y = y;
                last_z = z;

                listener.onAccelerometerUpdate(last_x, last_y);
//                Log.d("ACCELEROMETER", "OX: " + last_x + " OY: " + last_y + " OZ: " + last_z);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public interface SensorHelperListener {
        void showSensorMissedAlert();

        void onAccelerometerUpdate(float x, float y);
    }
}
