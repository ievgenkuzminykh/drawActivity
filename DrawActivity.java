package com.example.geka.permittowork;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrawActivity extends Activity implements View.OnClickListener {
    DrawingView dv;
    private Paint mPaint;
    Button btnClear, btnSave,btnExit;
    LinearLayout layMain;
    String fileSTR;
    Animation anim;


    //=======================================================================
    // :::::::::::::::::::::::: onCreate :::::::::::::::::::::::::::::::
    //=======================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        layMain = findViewById(R.id.draw_lay);
        btnClear = findViewById(R.id.draw_btn_clear);
        btnSave = findViewById(R.id.draw_btn_save);
        btnExit = findViewById(R.id.draw_btn_exit);
        btnClear.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        dv = new DrawingView(this);
        layMain.addView(dv);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);

        anim = AnimationUtils.loadAnimation(this, R.anim.btn_press_anim);


        if (ContextCompat.checkSelfPermission(DrawActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            SDwrPermission();
        }

    }



    //===================================================================================
    //::::::::::::::::::::::::::::::::  SDwrPermission  :::::::::::::::::::::::::::::::
    //===================================================================================
    public void SDwrPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 23);
    }




    //=======================================================================
    // :::::::::::::::::::::::: clearDraw  :::::::::::::::::::::::::::
    //=======================================================================
    public void clearDraw() {
        layMain.removeAllViews();
        dv = new DrawingView(this);
        layMain.addView(dv);
    }




    //=======================================================================
    // :::::::::::::::::::::::: saveDraw  :::::::::::::::::::::::::::
    //=======================================================================
    public void saveDraw() {
        Bitmap bm = dv.getBitmap();
        try {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PtwPhoto");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Toast.makeText(DrawActivity.this, "ERROR - can not make photo dir", Toast.LENGTH_SHORT).show();
                }
            }

            String serialNum = Build.SERIAL;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            fileSTR = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + serialNum + ".png").toString();

            FileOutputStream fileOutputStream = null;

            fileOutputStream = new FileOutputStream(fileSTR);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            Toast.makeText(DrawActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }



    //=======================================================================
    // :::::::::::::::::::::::: onClick  :::::::::::::::::::::::::::
    //=======================================================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.draw_btn_clear:
                btnClear.startAnimation(anim);
                clearDraw();
                break;

            case R.id.draw_btn_save:
                btnSave.startAnimation(anim);
                saveDraw();
                Intent intent = new Intent(DrawActivity.this,MainActivity.class);
                intent.putExtra("sign",fileSTR);
                setResult(RESULT_OK,intent);
                finish();
                break;

            case R.id.draw_btn_exit:
                btnExit.startAnimation(anim);
                finish();
                break;
        }

    }


    //=======================================================================
    // :::::::::::::::::::::::: class DrawingView :::::::::::::::::::::::::::
    //=======================================================================
    public class DrawingView extends View {
        public int width;
        public int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }


        public Bitmap getBitmap() {
            return mBitmap;
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(circlePath, circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;


        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }


        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }



    //==========================================================
    //::::::::::::::::::::::  onBackPressed ::::::::::
    //==========================================================
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    //==========================================================
    //::::::::::::::::::::::  onDestroy ::::::::::
    //==========================================================
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(MainActivity.this,"Db closed", Toast.LENGTH_SHORT).show();

    }


}
