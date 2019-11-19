package com.example.helloworld2;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.util.TimeUtils;
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
    List<Float> historyGravity = new ArrayList<Float>();
    List<Float> landscapeControl = new ArrayList<Float>();
    long lastTime = 0; // 上次发生大幅度重力变化的时间

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

                float historyMean = calculateAverage(historyGravity);
                historyGravity.add(X_lateral);
                if(historyGravity.size() > historyNum){
                    historyGravity.remove(0);
                    if(X_lateral < historyMean-deltaBoundary || X_lateral > historyMean+deltaBoundary){
                        long currTime = System.currentTimeMillis();
                        if(currTime - lastTime > millisBoundary) landscapeControl.clear();
                        lastTime = currTime;

                        float delta = X_lateral-historyMean;
                        Log.i("sensor","\n heading "+delta);
                        landscapeControl.add(delta);

                        if(landscapeControl.size() == controlNum){
                            if(landscapeControl.get(0) < 0 && landscapeControl.get(1) > 0 && landscapeControl.get(2) < 0) ;
                            else if(landscapeControl.get(0) > 0 && landscapeControl.get(1) < 0 && landscapeControl.get(2) > 0);
                            landscapeControl.clear();
                        }
                    }
                }
                else Log.i("sensor", "no enough history");
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
