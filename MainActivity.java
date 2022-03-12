package com.shadan.mcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    //constant variable
    private static final int PERMISSION_REQUEST_CODE=100;

    //Main Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handlePermission();
    }

    //EncodeActivity
    public void Encode_Activity_btn(View view) {
        //MainActivity to EncodeActivity
        Intent encodingActivity = new Intent(MainActivity.this, EncodeActivity.class);
        startActivity(encodingActivity);
    }

    //DecodeActivity
    public void Decode_Activity_btn(View view) {
        //MainActivity to DecodeActivity
        Intent encodingActivity = new Intent(MainActivity.this, DecodeActivity.class);
        startActivity(encodingActivity);
    }

    //Asking_For permissions
    private void handlePermission() {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},  PERMISSION_REQUEST_CODE);
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},  PERMISSION_REQUEST_CODE);
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},  PERMISSION_REQUEST_CODE);
    }



    //checking permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case  PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                            Toast.makeText(this,"we need this Permission", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,permissions[i]+" Permission Deny", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
