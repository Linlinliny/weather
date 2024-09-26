package com.example.weather;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.io.IOException;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long MINIMUM_DISPLAY_MS = 1500; // 设置最小停留时间为1.5秒
    private String weatherData = null;  // 添加一个变量来存储天气数据
    private double latitude;
    private double longitude;
    private String city;
    private String district;
    private boolean isWeatherDataFetched = false;
    private boolean isLocationFetched = false;
    private ProgressBar progressBar; // 声明ProgressBar
    private LocationDatabaseHelper dbHelper; // 声明数据库助手类
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private MediaPlayer mediaPlayer;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    latitude = amapLocation.getLatitude(); //获取纬度
                    longitude = amapLocation.getLongitude(); //获取经度
                    city = amapLocation.getCity(); // 获取市信息
                    district = amapLocation.getDistrict(); // 获取区信息
                    Log.i("AMap", "定位成功，纬度：" + latitude + "，经度：" + longitude + "，市：" + city + "，区：" + district);
                    isLocationFetched = true;
                    progressBar.setProgress(50);  // 更新进度条

                    // 调用 fetchWeatherData 获取天气数据
                    long startTime = System.currentTimeMillis();  // 获取当前时间（毫秒）
                    fetchWeatherData(latitude, longitude, () -> {
                        long elapsedTime = System.currentTimeMillis() - startTime;  // 计算获取数据花费的时间
                        long delayTime = Math.max(0, MINIMUM_DISPLAY_MS - elapsedTime);  // 确保至少停留1.5秒
                        handler.postDelayed(() -> {
                            isWeatherDataFetched = true;
                            progressBar.setProgress(100);  // 更新进度条
                            checkAndGoToNextActivity();
                        }, delayTime);  // 延迟执行跳转至下一个Activity
                    });
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AMapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 权限已经授予，初始化定位
            try {
                initLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initLocation() throws Exception {
        // 同意高德隐私政策
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置只定位一次
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // 启用边缘到边缘显示，使内容可以展示到屏幕边缘，包括状态栏和导航栏下方
        setContentView(R.layout.activity_main);  // 设置Activity的布局文件
        // 初始化 MediaPlayer 并播放音乐
        mediaPlayer = MediaPlayer.create(this, R.raw.tqbj);
        mediaPlayer.start(); // 开始播放
        // 初始化ProgressBar
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);  // 显示进度条

        // 初始化数据库助手类
        dbHelper = new LocationDatabaseHelper(this);

        // 检查数据库是否存在，不存在则创建并插入初始数据
        initializeDatabaseIfNeeded();

        // 设置窗口插图监听器，用于处理窗口插图，如状态栏和导航栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());  // 获取系统UI元素的插图
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);  // 设置Padding以避免内容被系统UI遮挡
            return insets;
        });

        // 检查权限并请求权限
        checkLocationPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已经授予，初始化定位
                try {
                    initLocation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 权限被拒绝，显示错误信息
                Log.e("AMapError", "定位权限被禁用，请授予应用定位权限");
            }
        }
    }

    // 定义一个方法用于从网络获取天气数据
    private void fetchWeatherData(double latitude, double longitude, Runnable onFinish) {
        OkHttpClient client = new OkHttpClient();  // 创建HTTP客户端
        String url = String.format(Locale.US, "https://api.caiyunapp.com/v2.6/vvBPP5xDr9lY5FFb/%.4f,%.4f/weather?dailysteps=3&hourlysteps=48", longitude,latitude);
        Request request = new Request.Builder()
                .url(url)
                .build();  // 构建HTTP请求

        // 异步发送HTTP请求
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();  // 打印错误信息
                onFinish.run();  // 即使失败也继续执行完成的操作
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    weatherData = response.body().string(); // 保存接收到的JSON数据
                }
                onFinish.run();
            }
        });
    }
    private void initializeDatabaseIfNeeded() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + LocationDatabaseHelper.TABLE_NAME + "'", null);
        if (cursor.getCount() == 0) {
            dbHelper.getWritableDatabase();
            insertInitialData();
        }
        cursor.close();
    }

    private void insertInitialData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocationDatabaseHelper.COLUMN_LATITUDE, 39.9042);
        values.put(LocationDatabaseHelper.COLUMN_LONGITUDE, 116.4074);
        values.put(LocationDatabaseHelper.COLUMN_CITY, "Beijing");
        values.put(LocationDatabaseHelper.COLUMN_DISTRICT, "Chaoyang");
        db.insert(LocationDatabaseHelper.TABLE_NAME, null, values);
    }
    private void checkAndGoToNextActivity() {
        if (isWeatherDataFetched && isLocationFetched) {
            goToNextActivity();
        }
    }
    //定义一个方法用于跳转到登录界面
    private void goToNextActivity() {
        Intent intent = new Intent(this, TodayWeatherActivity.class);  // 创建跳转到今日天气页面
        intent.putExtra("WEATHER_DATA", weatherData); // 将天气数据作为额外信息传递给下一个Activity
        intent.putExtra("LATITUDE", latitude); // 传递纬度信息
        intent.putExtra("LONGITUDE", longitude); // 传递经度信息
        intent.putExtra("CITY", city); // 传递市信息
        intent.putExtra("DISTRICT", district); // 传递区信息
        startActivity(intent);  // 启动目标Activity
        finish();  // 结束当前Activity
    }
}