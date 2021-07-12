package com.junjange.floatingwidget2;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatingWidgetShowService extends Service{

    WindowManager windowManager;
    WindowManager.LayoutParams params ;
    private int x_init_cord, y_init_cord ;
    private final Point szWindow = new Point();
    float height, width;
    long time_start = 0, time_end = 0;
    View floatingView, collapsedView, expandedView;
    ImageView imageClose;


    //플로팅 위젯 보기가 왼쪽에 있는지 오른쪽에 있는지 확인하는 변수
    // 처음에는 플로팅 위젯 보기를 왼쪽에 표시하므로 true로 설정합니다.
    private boolean isLeft = true;


    public FloatingWidgetShowService() {

    }
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();


        //윈도우매니저 초기화
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 우리가 만든 플로팅 뷰 레이아웃 확장
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);

        //창에 위젯 아이콘 뷰를 추가
        params = new WindowManager.LayoutParams( WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 보기 위치 지정
        // 처음에는보기가 왼쪽 중앙에 추가되며 필요에 따라 x-y 좌표를 변경
        params.gravity = Gravity.CENTER | Gravity.LEFT;
        params.x = 0;
        params.y = 100;


        //창에 제거 이미지 뷰를 추가합니다.
        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams( 140,
                140,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 보기 위치 지정
        imageParams.gravity = Gravity.BOTTOM|Gravity.CENTER;
        imageParams.y = 100;


        // 제거 이미지 정보
        imageClose = new ImageView(this);
        imageClose.setImageResource(R.drawable.close_0);

        // //초기에는 제거 이미지가 표시되지 않으므로 가시성을 GONE으로 설정
        imageClose.setVisibility(View.GONE);

        //윈도우에 뷰 추가
        windowManager.addView(imageClose, imageParams);
        windowManager.addView(floatingView, params);

        // 접힌 뷰 레이아웃의 ID 찾기
        expandedView = floatingView.findViewById(R.id.Layout_Expended);
        collapsedView = floatingView.findViewById(R.id.Layout_Collapsed);

        // 뷰 높이, 너비
        height = windowManager.getDefaultDisplay().getHeight();
        width  = windowManager.getDefaultDisplay().getWidth();


        // 확장된 위젯을 클릭할 경우
        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });


        // 사용자가 확장 아이콘을 클릭하면 시작화면으로 이동
        floatingView.findViewById(R.id.Widget_expand_Icon).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FloatingWidgetShowService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopSelf();

            }
        });

        //사용자의 터치 동작을 사용하여 플로팅 뷰를 드래그 앤 이동합니다.
        floatingView.findViewById(R.id.MainParentRelativeLayout).setOnTouchListener(new View.OnTouchListener() {

            int X_Axis, Y_Axis;
            float TouchX, TouchY;
            boolean inBounded = false; // 플로팅 뷰가 뷰를 제거할 경계인지 판단하는 변수

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // 터치 위치 좌표 가져오기(위젯 클릭 이벤트)
                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();

                // 제거 이미지 뷰 절대 좌표
                int[] imageClose_location = new int[2];
                imageClose.getLocationOnScreen(imageClose_location);
                Log.d("Tag", String.valueOf(imageClose_location[0]));
                Log.d("Tag", String.valueOf(imageClose_location[1]));

                // 위젯 뷰 절대 좌표
                int[] widget_location = new int[2];
                floatingView.getLocationOnScreen(widget_location);
                Log.d("ttt", String.valueOf(widget_location[0]));
                Log.d("ttt", String.valueOf(widget_location[1]));


                switch (event.getAction()) {


                    // ACTION_DOWN(View 를 손으로 누르기 시작하는 시점)
                    case MotionEvent.ACTION_DOWN:

                        // 제거 이미지 보임
                        imageClose.setVisibility(View.VISIBLE);


                        // 시작 시간
                        time_start = System.currentTimeMillis();

                        // 현재 좌표(위젯 클릭 이벤트)
                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        //터치 위치 좌표 가져오기(위젯 드로우)
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();

                        // 현재 좌표(위젯 드로우)
                        X_Axis = params.x;
                        Y_Axis = params.y;

                    return true;


                    // ACTION_MOVE(View 를 손으로 누르고 드래그 하는 시점)
                    case MotionEvent.ACTION_UP:

                        // 제거 이미지 숨김
                        imageClose.setVisibility(View.GONE);

                        // 초기 좌표와 현재 좌표의 차이 구하기
                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        // 위젯 클릭 이벤트
                        // 클릭하는 동안 요소가 약간 움직이기 때문에 x_diff <5 && y_diff< 5를 확인
                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {

                            // 종료 시간
                            time_end = System.currentTimeMillis();

                            // 또한 시작 시간과 종료 시간의 차이가 300ms 미만이어야 하는지 확인
                            if ((time_end - time_start) < 300)
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                        }

                        // 이미지 뷰 절대 좌표가 조건 안에 있으면 제거한다.
                        if (imageClose_location[0] - imageParams.height  <= widget_location[0] && imageClose_location[0] + imageParams.height  >= widget_location[0]
                            && imageClose_location[1] - imageParams.width  <= widget_location[1] && imageClose_location[1] + imageParams.width  >= widget_location[1]){
                            stopSelf();

                        }else {

                            // 사용자가 플로팅 뷰를 드래그하면 위치 재설정
                            resetPosition(x_cord);
                        }


                        return true;

                    // ACTION_UP(View 로부터 손을 뗀 시점)
                    case MotionEvent.ACTION_MOVE:

                        // 위젯 드로우
                        params.x = X_Axis + (int) (event.getRawX() - TouchX);
                        params.y = Y_Axis + (int) (event.getRawY() - TouchY);



                        // 새로운 X & Y 좌표로 레이아웃 업데이트
                        windowManager.updateViewLayout(floatingView, params);

                        // 제거 이미지에 가까이 가면 이미지 전환
                        if (imageClose_location[0] - imageParams.height  <= widget_location[0] && imageClose_location[0] + imageParams.height  >= widget_location[0]
                                && imageClose_location[1] - imageParams.width  <= widget_location[1] && imageClose_location[1] + imageParams.width  >= widget_location[1]){

                            imageClose.setImageResource(R.drawable.close_1);

                        }
                        else {
                            imageClose.setImageResource(R.drawable.close_0);

                        }

                    return true;
                }
                return false;
            }
        });
    }

    /* 드래깅 시 플로팅 위젯 보기의 위치 재설정 */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= width / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }

    /* 플로팅 위젯 보기를 왼쪽으로 이동하는 방법 */
    private void moveToLeft(final int current_x_cord) {
        final int x = (int) (width - current_x_cord);

        // 움직이는 횟수와 시간 0으로 초기화
        new CountDownTimer(0, 0) {

            // 플로팅 뷰 매개변수 제거 가져오기
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floatingView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = -(int) (current_x_cord * current_x_cord * step);

                // 새로운 X & Y 좌표로 레이아웃 업데이트
                windowManager.updateViewLayout(floatingView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;

                // 새로운 X & Y 좌표로 레이아웃 업데이트
                windowManager.updateViewLayout(floatingView, mParams);
            }
        }.start();
    }

    /* 플로팅 위젯 보기를 오른쪽으로 이동하는 방법 */
    private void moveToRight(final int current_x_cord) {

        // 움직이는 횟수와 시간 0으로 초기화
        new CountDownTimer(0, 0) {

            // 플로팅 뷰 매개변수 제거 가져오기
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floatingView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = (int) (width + (current_x_cord * current_x_cord * step) - floatingView.getWidth());

                // 새로운 X & Y 좌표로 레이아웃 업데이트
                windowManager.updateViewLayout(floatingView, mParams);
            }

            public void onFinish() {
                mParams.x = (int) (width- floatingView.getWidth());

                // 새로운 X & Y 좌표로 레이아웃 업데이트
                windowManager.updateViewLayout(floatingView, mParams);
            }
        }.start();
    }


    // 앱이 종료될때 실행
    @Override
    public void onDestroy(){
        super.onDestroy();

        if(floatingView != null){
            windowManager.removeView(floatingView);

        }
        if (imageClose != null){

            windowManager.removeView(imageClose);
        }

    }
}