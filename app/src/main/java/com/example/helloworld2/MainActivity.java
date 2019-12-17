package com.example.helloworld2;

import android.Manifest;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private long last_check_time;
    private long fuck_time = 2000;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private SurfaceView myview;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},1);

        last_check_time = 0;

        setContentView(R.layout.activity_main);

        SurfaceView myview = new SurfaceView(this);
        surfaceHolder = myview.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(callback);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = layoutParams.height = 1;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.gravity = Gravity.RIGHT | Gravity.TOP ;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;

        windowManager.addView(myview, layoutParams);
    }

    private Camera camera;

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // Camera要选择hardware.Camera，因为Camera属于硬件hardware
            // Camera.open(1); // 这了传入的值，可以指定为：前置摄像头/后置摄像头
            camera = Camera.open(1);

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "相机设置预览失败");
            }
            camera.setFaceDetectionListener(new FaceDetectorListener());
            camera.startPreview();
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // 以后做具体业务功能的时候，才会用到改变的值来处理
            // ......
            System.out.println("surfaceChanged");
            if (surfaceHolder.getSurface() == null) {
                // preview surface does not exist
                Log.e(TAG, "mHolder.getSurface() == null");
                return;
            }

            try {
                camera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.e(TAG, "Error stopping camera preview: " + e.getMessage());
            }

            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                startFaceDetection(); // re-start face detection feature
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview(); // (一定要有，不然只release也可能出问题)
            camera.release();
            camera = null;
            System.gc();
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ">>>> onDestroy()");
        if (null != callback) surfaceHolder.removeCallback(callback);
    }


    public void startFaceDetection() {
        // Try starting Face Detection
        Camera.Parameters params = camera.getParameters();
        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0) {
            // mCamera supports face detection, so can start it:
            camera.startFaceDetection();
        } else {
            Log.e("tag", "【FaceDetectorActivity】类的方法：【startFaceDetection】: " + "不支持");
        }
    }


    private class FaceDetectorListener implements Camera.FaceDetectionListener {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                long current_time = System.currentTimeMillis();
                if (current_time - last_check_time > fuck_time) {
                    Camera.Face face = faces[0];
                    Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: 检测到人脸");
                    Log.d("tag", "Left eye: (" + face.leftEye.x + ", " + face.leftEye.y + ")");
                    Log.d("tag", "Right eye: (" + face.rightEye.x + ", " + face.rightEye.y + ")");
                    Log.d("tag", "Mouth: (" + face.mouth.x + ", " + face.mouth.y + ")");
                    Point mid = new Point((face.leftEye.x + face.rightEye.x) / 2, (face.leftEye.y + face.rightEye.y) / 2);
                    Point v = new Point(face.mouth.x - mid.x, face.mouth.y - mid.y);
                    int angle;
                    if (Math.abs(v.x) > Math.abs(v.y)) {
                        if (v.x > 0) angle = 2;
                        else angle = 0;
                    }
                    else {
                        if (v.y > 0) angle = 1;
                        else angle = 3;
                    }
                    Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION, angle);
                    last_check_time = current_time;
                }
            } else {
                Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: 没有人脸");
            }
        }
    }

    public void Fuck500(View view) {
        fuck_time = 500;
        Log.d(TAG, "Fuck500");
    }

    public void Fuck1000(View view) {
        fuck_time = 1000;
        Log.d(TAG, "Fuck1000");
    }

    public void Fuck2000(View view) {
        fuck_time = 2000;
        Log.d(TAG, "Fuck2000");
    }
}