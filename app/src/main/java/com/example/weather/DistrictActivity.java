package com.example.weather;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DistrictActivity extends AppCompatActivity {

    // 请求码，用于启动添加位置的Activity
    private static final int REQUEST_CODE_ADD_LOCATION = 1;
    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private LocationDatabaseHelper dbHelper;
    private List<Location> locationList;
    private Button buttonAdd;
    private String weatherData; // 添加一个变量来存储天气数据

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_district);

        // 初始化数据库帮助类、RecyclerView和按钮
        dbHelper = new LocationDatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonAdd = findViewById(R.id.tianjia);
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DistrictActivity.this, AddLocationActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_LOCATION);
        });

        // 设置适配器及其删除监听器和点击监听器
        adapter = new LocationAdapter(locationList, location -> {
            // 删除位置的监听器
            if (!location.getDistrict().equals("当前位置")) {
                dbHelper.deleteLocation(location.getLatitude(), location.getLongitude());
                readAndDisplayLocations();  // 重新读取数据并刷新RecyclerView
            }
        }, location -> {
            // 点击位置项的监听器
            fetchWeatherData(location.getLatitude(), location.getLongitude(), () -> {
                Intent intent = new Intent(DistrictActivity.this, TodayWeatherActivity.class);
                intent.putExtra("WEATHER_DATA", weatherData);
                intent.putExtra("LATITUDE", location.getLatitude());
                intent.putExtra("LONGITUDE", location.getLongitude());
                intent.putExtra("CITY", location.getCity());
                intent.putExtra("DISTRICT", location.getDistrict());
                startActivity(intent);
            });
        });
        recyclerView.setAdapter(adapter);

        // 读取数据库并设置适配器
        readAndDisplayLocations();
    }

    // 读取数据库中的位置数据并显示在RecyclerView中
    private void readAndDisplayLocations() {
        locationList = new ArrayList<>();
        // 添加“当前位置”项
        locationList.add(new Location(0, 0, "", "当前位置"));

        Cursor cursor = dbHelper.getAllLocations();
        if (cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_LONGITUDE));
                String city = cursor.getString(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_CITY));
                String district = cursor.getString(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_DISTRICT));
                locationList.add(new Location(latitude, longitude, city, district));
                Log.i("Database", "Latitude: " + latitude + ", Longitude: " + longitude + ", City: " + city + ", District: " + district);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // 刷新适配器的数据
        if (adapter != null) {
            adapter.updateData(locationList);
        }
    }

    // 获取天气数据
    private void fetchWeatherData(double latitude, double longitude, Runnable onFinish) {
        OkHttpClient client = new OkHttpClient();  // 创建HTTP客户端
        String url = String.format(Locale.US, "https://api.caiyunapp.com/v2.6/vvBPP5xDr9lY5FFb/%.4f,%.4f/weather?dailysteps=3&hourlysteps=48", longitude, latitude);
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

    // 处理添加位置Activity的返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_LOCATION && resultCode == RESULT_OK) {
            // 重新读取数据并刷新RecyclerView
            readAndDisplayLocations();
        }
    }
}