package com.junjange.floatingwidget2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int SYSTEM_ALERT_WINDOW_PERMISSION = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 버튼 정의
        Button button ;
        button = (Button)findViewById(R.id.buttonShow);


        // 권한을 확인한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            RuntimePermissionForUser();
        }

        // 버튼 클릭
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // 권한이 설정돼있으면 위젯 액티비티로 연결
                    startService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    finish();
                }
                else if (Settings.canDrawOverlays(MainActivity.this)) {
                    // 권한이 설정돼있으면 위젯 액티비티로 연결
                    startService(new Intent(MainActivity.this, FloatingWidgetShowService.class));
                    finish();
                }
                else {
                    RuntimePermissionForUser();

                    Toast.makeText(MainActivity.this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    // M 버전(안드로이드 6.0 마시멜로우 버전) 보다 같거나 큰 API에서만 설정창 이동 가능
    public void RuntimePermissionForUser() {
        Intent PermissionIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(PermissionIntent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }
}