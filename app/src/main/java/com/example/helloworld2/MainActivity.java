package com.example.helloworld2;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    /**
     * 操作的是SurfaceHolder，所以定义全局变量
     */
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);

        // 获取在布局文件中定义的SurfaceView
        SurfaceView surfaceView = findViewById(R.id.surface_view);

        // 不能直接操作SurfaceView，需要通过SurfaceView拿到SurfaceHolder
        surfaceHolder = surfaceView.getHolder();

//        // 使用SurfaceHolder设置屏幕高亮，注意：所有的View都可以设置 设置屏幕高亮
//        surfaceHolder.setKeepScreenOn(true);
//
//        // 使用SurfaceHolder设置把画面或缓存 直接显示出来
//        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /**
         * 由于要观察SurfaceView生命周期，所以需要设置监听
         * 此监听不一样，是addCallback
         *
         */
        surfaceHolder.addCallback(callback);
    }

    /**
     * 定义相机对象
     */
    private Camera camera;

    /**
     * 定义SurfaceView监听回调
     */
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        /**
         * SurfaceView被创建了
         * @param holder
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /**
             * 只有在SurfaceView生命周期方法-->SurfaceView被创建后在打开相机
             * 以前我在 onResume 之前去打开相机，结果报错了，所以只有在这里打开相机，才是安全🔐
             */
            // Camera要选择hardware.Camera，因为Camera属于硬件hardware
            // Camera.open(1); // 这了传入的值，可以指定为：前置摄像头/后置摄像头
            camera = Camera.open(1);

            /**
             * 设置Camera与SurfaceHolder关联，Camera的数据让SurfaceView显示
             */
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "相机设置预览失败");
            }

            camera.setFaceDetectionListener(new FaceDetectorListener());

            /**
             * 开始显示
             */
            camera.startPreview();
        }

        /**
         * SurfaceView发生改变了
         * @param holder
         * @param format
         * @param width
         * @param height
         */
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

        /**
         * SurfaceView被销毁了
         * @param holder
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            /**
             * SurfaceView被销毁后，一定要释放硬件资源，Camera是硬件
             */
            camera.stopPreview(); // (一定要有，不然只release也可能出问题)
            camera.release();
            camera = null;
            System.gc();
        }
    };

    /**
     * 拍照
     */
    public void takepicture(View view) {
        if (null != camera) {
            /**
             * 参数一：相机的快门，咔嚓，这里用不到
             * 参数二：相机原始的数据
             * 参数三：相机处理后的数据
             */
            camera.takePicture(null, null, pictureCallback);
        }
    }

    /**
     * 相机处理后的数据回调
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        /**
         * 拍照完成，可以拿到数据了
         * @param data 数据
         * @param camera
         */
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            // 写在指定目录 sdcard 中去
//            File file = new File("/storage/Pictures/Screenshots", "pictureCallback.jpg");
//            try {
//                // 字节文件输出流，把byte[]数据写入到文件里面去
//                OutputStream os = new FileOutputStream(file);
//                os.write(data);
//
//                // 关闭字节文件输出流
//                os.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e(TAG, "保存失败");
//                Toast.makeText(MainActivity.this, "照片保存失败", Toast.LENGTH_SHORT).show();
//            }
            Bitmap bitmap0 = BitmapFactory.decodeByteArray(data,0,data.length);
            bitmap0 = bitmap0.copy(Bitmap.Config.RGB_565, true);
            System.out.println("onPictureTaken");
            System.out.println("width: " + bitmap0.getWidth());
            System.out.println("height: " + bitmap0.getHeight());
            for (int i = 0; i < 4; i++) {
                Bitmap bitmap = rotateBitmap(bitmap0, 90 * i);
                FaceDetector detector = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(), 1);
                FaceDetector.Face[] faces = new FaceDetector.Face[1];
                detector.findFaces(bitmap, faces);
                if (faces[0] != null) {
                    System.out.println(i + ": not null!");
                    PointF p = new PointF();
                    faces[0].getMidPoint(p);
                    System.out.println(p.x + " " + p.y);
                    System.out.println("x: " + faces[0].pose(FaceDetector.Face.EULER_X));
                    System.out.println("y: " + faces[0].pose(FaceDetector.Face.EULER_Y));
                    System.out.println("z: " + faces[0].pose(FaceDetector.Face.EULER_Z));
//                    Settings.System.putInt(getContentResolver(),Settings.System. USER_ROTATION, i);
//                    break;
                }
            }

        }
    };

    /**
     * 对焦
     */
    public void focus(View view) {
        // 对焦都是硬件设备来完成的
        camera.autoFocus(null);
    }

    /**
     * 当Activity被销毁的时候，一定要移除监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ">>>> onDestroy()");
        if (null != callback) surfaceHolder.removeCallback(callback);
    }


    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
//        origin.recycle();
        return newBM;
    }

    /**
     * 启动脸部检测，如果getMaxNumDetectedFaces()!=0说明不支持脸部检测
     */
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

    /**
     * 脸部检测接口
     */
    private class FaceDetectorListener implements Camera.FaceDetectionListener {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Camera.Face face = faces[0];
                Rect rect = face.rect;
                Log.d("FaceDetection", "可信度：" + face.score + "face detected: " + faces.length +
                        " Face 1 Location X: " + rect.centerX() +
                        "Y: " + rect.centerY() + "   " + rect.left + " " + rect.top + " " + rect.right + " " + rect.bottom);
                Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: ");
            } else {
                // 只会执行一次
                Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: " + "没有脸部");
            }
        }
    }
}