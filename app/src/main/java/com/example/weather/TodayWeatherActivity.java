package com.example.weather;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jayway.jsonpath.JsonPath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodayWeatherActivity extends AppCompatActivity {
    public TextView textViewRealtimeSkycon;
    public TextView textViewDate;
    public TextView textViewDistrict;
    public ImageView imageView;
    private RecyclerView recyclerViewHourly;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // 启用边缘到边缘显示，使内容可以展示到屏幕边缘，包括状态栏和导航栏下方
        setContentView(R.layout.activity_today_weather);

        String weatherData = getIntent().getStringExtra("WEATHER_DATA");
        String district = getIntent().getStringExtra("DISTRICT");
        textViewRealtimeSkycon = findViewById(R.id.textView_realtime_skycon);
        textViewDate = findViewById(R.id.date);
        textViewDistrict = findViewById(R.id.district);
        recyclerViewHourly = findViewById(R.id.recyclerView_hourly);
        imageView = findViewById(R.id.imageView2);
        textViewDistrict.setText(district);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodayWeatherActivity.this, DistrictActivity.class);
                startActivity(intent);
            }
        });

        // 获取今天的日期
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        // 显示日期
        textViewDate.setText(currentDate);
        textViewDate.setGravity(Gravity.CENTER);
        textViewDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        if (weatherData != null) {
            try {
                //解析天气数据
                JSONObject weatherDataJSON = new JSONObject(weatherData);
                String realtimeSkycon = JsonPath.read(weatherData, "$.result.realtime.skycon");
                WeatherReferenceTable weatherReferenceTable = new WeatherReferenceTable();
                String skycon = weatherReferenceTable.getWeatherConditions(realtimeSkycon);
                textViewRealtimeSkycon.setText(skycon);

                // 动态设置背景图像
                switch (realtimeSkycon) {
                    case "CLEAR_DAY":
                    case "CLEAR_NIGHT":
                        getWindow().setBackgroundDrawableResource(R.drawable.qing);
                        break;
                    case "PARTLY_CLOUDY_DAY":
                    case "PARTLY_CLOUDY_NIGHT":
                        getWindow().setBackgroundDrawableResource(R.drawable.yun);
                        break;
                    case "CLOUDY":
                        getWindow().setBackgroundDrawableResource(R.drawable.yin);
                        break;
                    case "MODERATE_RAIN":
                    case "HEAVY_RAIN":
                    case "LIGHT_RAIN":
                    case "STORM_RAIN":
                        getWindow().setBackgroundDrawableResource(R.drawable.yu);
                        break;
                    default:
                        // 设置一个默认的背景图像（如果找不到对应的背景图像）
                        getWindow().setBackgroundDrawableResource(R.drawable.defaultbackground);
                        break;
                }
                int weatherIcon = getimageView(realtimeSkycon);
                imageView.setImageResource(weatherIcon);
               // 获取 hourly 数据并设置 RecyclerView
                JSONArray hourlyData = weatherDataJSON.getJSONObject("result").getJSONObject("hourly").getJSONArray("apparent_temperature");
                JSONArray skyconData = weatherDataJSON.getJSONObject("result").getJSONObject("hourly").getJSONArray("skycon");
                WeatherAdapter adapter = new WeatherAdapter(hourlyData,skyconData);
                recyclerViewHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                recyclerViewHourly.setAdapter(adapter);


            } catch (JSONException e) {
                e.printStackTrace();
                // 处理错误
            }
        }
    }

    private int getimageView(String skycon) {
        switch (skycon) {
            case "CLEAR_DAY":
            case "CLEAR_NIGHT":
                return R.drawable.weatherclear;
            case "PARTLY_CLOUDY_DAY":
            case "PARTLY_CLOUDY_NIGHT":
                return R.drawable.weatherpartlycloudy;
            case "CLOUDY":
                return R.drawable.weathercloudy;
            case "MODERATE_RAIN":
            case "HEAVY_RAIN":
            case "LIGHT_RAIN":
            case "STORM_RAIN":
                return R.drawable.weatherrain;
            case "LIGHT_SNOW":
            case "MODERATE_SNOW":
            case "HEAVY_SNOW":
            case "STORM_SNOW":
                return R.drawable.weathersnow;
            case "WIND":
            case "DUST":
            case "SAND":
                return R.drawable.weatherwind;
            case "FOG":
            case "LIGHT_HAZE":
            case "MODERATE_HAZE":
            case "HEAVY_HAZE":
                return R.drawable.weatherfog;
            default:
                return R.drawable.weather;
        }
    }
}