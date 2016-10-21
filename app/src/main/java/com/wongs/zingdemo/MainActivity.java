package com.wongs.zingdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Hashtable;

import static android.graphics.Color.BLACK;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button bt_generateQR,bt_scanQR;
    private ImageView iv_displayQR;
    private EditText et_qrContent,et_targetText;
    private static final String TAG = "MainActivity";
    private Bitmap bitmap;
    private static final int REQUEST_CAMERA = 0x123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_generateQR = (Button) findViewById(R.id.bt_generateQR);
        bt_scanQR = (Button) findViewById(R.id.bt_scanQR);
        iv_displayQR = (ImageView) findViewById(R.id.iv_displayQR);
        et_qrContent = (EditText) findViewById(R.id.et_qrContent);
        et_targetText = (EditText) findViewById(R.id.et_targeQRtext);
        bt_generateQR.setOnClickListener(this);
        bt_scanQR.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_generateQR){

            generateQR();

        }else if (v.getId() == R.id.bt_scanQR){

            if(isGrantedCameraPermission())
                scanQR();
            else{
                requestPermission();
            }
        }
    }
	//生产QR
    public void generateQR(){

        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        BitMatrix matrix = null;

        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        String text = et_targetText.getText().toString();

        if (text.length() == 0){
            Log.e(TAG,"text cannot be empty");
            return;
        }
        try {
            matrix = new MultiFormatWriter().encode(text,
                    BarcodeFormat.QR_CODE, 120, 120);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }
            }
        }
        bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        iv_displayQR.setImageBitmap(bitmap);
    }

    public boolean isGrantedCameraPermission(){

        if (Build.VERSION.SDK_INT >= 23){
            int granted = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
            return granted == PackageManager.PERMISSION_GRANTED ? true : false;
        }else{
            return true;
        }
    }

    public void requestPermission(){

        MainActivity.this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);

    }

    public void scanQR(){

        new IntentIntegrator(this).initiateScan();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                scanQR();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null){
            bitmap.recycle();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                et_qrContent.setText(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
