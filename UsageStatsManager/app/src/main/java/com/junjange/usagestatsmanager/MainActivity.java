package com.junjange.usagestatsmanager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    CheckPackageNameThread checkPackageNameThread;

    boolean operation = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // 버튼 정의
        Button start_button = findViewById(R.id.start_button);
        Button end_button = findViewById(R.id.end_button);


        // 시작 버튼 이벤트
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 권환 허용이 안되어 있으면 권환 설정창으로 이동
                if(!checkPermission()) {
                    Intent PermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivity(PermissionIntent);
                }
                // 권환 허용 되어 있으면 현재 포그라운드 앱 패키지 로그로 띄운다.
                else{

                    operation = true;
                    checkPackageNameThread = new CheckPackageNameThread();
                    checkPackageNameThread.start();
                }

            }
        });

        // 종료 버튼 이벤트
        end_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                operation = false;
            }

        });

    }

    // 현재 포그라운드 앱 패키지 로그로 띄우는 함수
    private class CheckPackageNameThread extends Thread{

        public void run(){
            // operation == true 일때만 실행
            while(operation){
                if(!checkPermission())
                    continue;


                // 현재 포그라운드 앱 패키지 이름 가져오기
                System.out.println(getPackageName(getApplicationContext()));
                try {
                    // 2초마다 패키치 이름을 로그창에 출력
                    sleep(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 권환 체크
    private boolean checkPermission(){

        boolean granted = false;

        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (getApplicationContext().checkCallingOrSelfPermission(
                    android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        }
        else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        return granted;
    }


    // 자신의 앱의 최소 타겟을 롤리팝 이전으로 설정
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    // 현재 포그라운드 앱 패키지를 가져오는 함수
    public static String getPackageName(@NonNull Context context) {

        // UsageStatsManager 선언
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // 마지막 실행 앱 타임스탬프
        long lastRunAppTimeStamp = 0L;

        // 얼마만큼의 시간동안 수집한 앱의 이름을 가져오는지 정하기 (begin ~ end 까지의 앱 이름을 수집한다)
        final long INTERVAL = 1000 * 60 * 5;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL; // 5분전


        LongSparseArray packageNameMap = new LongSparseArray<>();

        // 수집한 이벤트들을 담기 위한 UsageEvents
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);

        // 이벤트가 여러개 있을 경우 (최소 존재는 해야 hasNextEvent가 null이 아니니까)
        while (usageEvents.hasNextEvent()) {

            // 현재 이벤트를 가져오기
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            // 현재 이벤트가 포그라운드 상태라면(현재 화면에 보이는 앱이라면)
            if(isForeGroundEvent(event)) {

                // 해당 앱 이름을 packageNameMap에 넣는다.
                packageNameMap.put(event.getTimeStamp(), event.getPackageName());

                // 가장 최근에 실행 된 이벤트에 대한 타임스탬프를 업데이트 해준다.
                if(event.getTimeStamp() > lastRunAppTimeStamp) {
                    lastRunAppTimeStamp = event.getTimeStamp();
                }
            }
        }
        // 가장 마지막까지 있는 앱의 이름을 리턴해준다.
        return packageNameMap.get(lastRunAppTimeStamp, "").toString();
    }

    // 앱이 포그라운드 상태인지 체크
    private static boolean isForeGroundEvent(UsageEvents.Event event) {

        // 이벤트가 없으면 false 반환
        if(event == null)
            return false;

        // 이벤트가 포그라운드 상태라면 true 반환
        if(BuildConfig.VERSION_CODE >= 29)
            return event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED;

        return event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND;
    }

}