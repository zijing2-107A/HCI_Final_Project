package com.example.helloworld2;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    SensorManager sensorManager;

    // 策略：采用过去n次重力值平均值作为稳定值，随后如果重力值偏离稳定值超过k，说明需要进行操作
    final int historyNum = 10;
    final int deltaBoundary = 5;
    final int controlNum = 3; // 使用3次重力变化控制屏幕方向
    final int millisBoundary = 500; // 两次大幅度重力变化的时间差
    List<Float> xHistoryGravity = new ArrayList<Float>();
    List<Float> xControl = new ArrayList<Float>();
    List<Float> yHistoryGravity = new ArrayList<Float>();
    List<Float> yControl = new ArrayList<Float>();
    long xLastTime = 0; // 上次发生大幅度重力变化的时间
    long yLastTime = 0; // 上次发生大幅度重力变化的时间

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getBaseContext(),"MyService onCreate", Toast.LENGTH_LONG).show();
        // 重力感应
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getBaseContext(),"MyService onDestroy", Toast.LENGTH_LONG).show();
    }

    final SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//                Log.i("sensor","onSensorChanged");
                //图解中已经解释三个值的含义
                float X_lateral = event.values[0];
                float Y_longitudinal = event.values[1];
                float Z_vertical = event.values[2];

                float xHistoryMean = calculateAverage(xHistoryGravity);
                xHistoryGravity.add(X_lateral);
                if(xHistoryGravity.size() > historyNum){
                    xHistoryGravity.remove(0);
                    if(X_lateral < xHistoryMean-deltaBoundary || X_lateral > xHistoryMean+deltaBoundary){
                        long currTime = System.currentTimeMillis();
                        if(currTime - xLastTime > millisBoundary) xControl.clear();
                        xLastTime = currTime;

                        float delta = X_lateral-xHistoryMean;
                        Log.i("sensor","\n x "+delta);
                        xControl.add(delta);

                        if(xControl.size() == controlNum){
                            if(xControl.get(0) < 0 && xControl.get(1) > 0 && xControl.get(2) < 0)
                                Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, 3);
                            else if(xControl.get(0) > 0 && xControl.get(1) < 0 && xControl.get(2) > 0)
                                Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, 1);
                            xControl.clear();
                        }
                    }
                }
                else Log.i("sensor", "x no enough history");

                float yHistoryMean = calculateAverage(yHistoryGravity);
                yHistoryGravity.add(Y_longitudinal);
                if(yHistoryGravity.size() > historyNum){
                    yHistoryGravity.remove(0);
                    if(Y_longitudinal < yHistoryMean-deltaBoundary || Y_longitudinal > yHistoryMean+deltaBoundary){
                        long currTime = System.currentTimeMillis();
                        if(currTime - yLastTime > millisBoundary) yControl.clear();
                        yLastTime = currTime;

                        float delta = Y_longitudinal-yHistoryMean;
                        Log.i("sensor","\n x "+delta);
                        yControl.add(delta);

                        if(yControl.size() == controlNum){
                            if(yControl.get(0) < 0 && yControl.get(1) > 0 && yControl.get(2) < 0)
                                Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, 2);
                            else if(yControl.get(0) > 0 && yControl.get(1) < 0 && yControl.get(2) > 0)
                                Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, 0);
                            yControl.clear();
                        }
                    }
                }
                else Log.i("sensor", "x no enough history");
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            Log.i("sensor", "onAccuracyChanged");
        }

        private float calculateAverage(List<Float> array){
            float sum = 0;
            for(Float num : array){
                sum += num;
            }
            if(array.isEmpty()) return 0;
            else return sum/array.size();
        }
    };
}
