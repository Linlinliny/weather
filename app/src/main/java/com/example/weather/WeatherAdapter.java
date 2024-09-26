package com.example.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private JSONArray apparentTemperatureList;
    private JSONArray skyconList;
    private String currentHour;

    // 构造方法，传入体感温度和skycon数据列表
    public WeatherAdapter(JSONArray apparentTemperatureList, JSONArray skyconList) {
        this.apparentTemperatureList = apparentTemperatureList;
        this.skyconList = skyconList;

        // 获取当前小时
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH", Locale.getDefault());
        currentHour = dateFormat.format(calendar.getTime());
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建每个天气项的视图
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_weather, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        try {
            // 从JSONArray中获取JSONObject
            JSONObject temperatureObject = apparentTemperatureList.getJSONObject(position);
            JSONObject skyconObject = skyconList.getJSONObject(position);

            // 提取时间、体感温度和skycon
            String datetime = temperatureObject.getString("datetime");
            double apparentTemperature = temperatureObject.getDouble("value");
            String skycon = skyconObject.getString("value");

            // 只保留小时部分
            String hour = datetime.substring(11, 13);

            // 设置时间显示
            if (hour.equals(currentHour)) {
                holder.tvTime.setText("现在");
            } else {
                holder.tvTime.setText(hour + ":00");
            }

            // 设置体感温度显示
            holder.tvTemperature.setText(String.valueOf(apparentTemperature) + "°C");

            // 根据skycon设置图像资源
            int weatherIcon = getWeatherIcon(skycon);
            holder.ivWeather.setImageResource(weatherIcon);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        // 返回天气数据项的数量
        return apparentTemperatureList.length();
    }

    // 定义ViewHolder，持有每个天气项的视图
    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemperature;
        ImageView ivWeather;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            ivWeather = itemView.findViewById(R.id.iv_weather);
        }
    }

    // 根据skycon返回相应的天气图标资源ID
    private int getWeatherIcon(String skycon) {
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
