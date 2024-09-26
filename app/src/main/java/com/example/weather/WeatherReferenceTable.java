package com.example.weather;
import java.util.HashMap;
import java.util.Map;
public class WeatherReferenceTable {
    private Map<String, String> weatherConditions;
    public WeatherReferenceTable() {
        weatherConditions = new HashMap<>();
        initializeWeatherConditions();
    }
    private void initializeWeatherConditions() {
        weatherConditions.put("CLEAR_DAY", "晴");
        weatherConditions.put("CLEAR_NIGHT", "晴");
        weatherConditions.put("PARTLY_CLOUDY_DAY", "多云");
        weatherConditions.put("PARTLY_CLOUDY_NIGHT", "多云");
        weatherConditions.put("CLOUDY", "阴");
        weatherConditions.put("LIGHT_HAZE", "轻度霾");
        weatherConditions.put("MODERATE_HAZE", "中度霾");
        weatherConditions.put("HEAVY_HAZE", "重度霾");
        weatherConditions.put("LIGHT_RAIN", "小雨");
        weatherConditions.put("MODERATE_RAIN", "中雨");
        weatherConditions.put("HEAVY_RAIN", "大雨");
        weatherConditions.put("STORM_RAIN", "暴雨");
        weatherConditions.put("FOG", "雾");
        weatherConditions.put("LIGHT_SNOW", "小雪");
        weatherConditions.put("MODERATE_SNOW", "中雪");
        weatherConditions.put("HEAVY_SNOW", "大雪");
        weatherConditions.put("STORM_SNOW", "暴雪");
        weatherConditions.put("DUST", "浮尘");
        weatherConditions.put("SAND", "沙尘");
        weatherConditions.put("WIND", "大风");
    }
    public String getWeatherConditions(String skycon) {
        String description = weatherConditions.getOrDefault(skycon, "错误");
        return description;
    }
}