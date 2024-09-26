package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.List;

public class AddLocationActivity extends AppCompatActivity {

    private EditText editTextAddress;  // 输入地址的编辑框
    private Button buttonSearch;  // 搜索按钮
    private RecyclerView recyclerViewResults;  // 显示搜索结果的RecyclerView
    private LocationDatabaseHelper dbHelper;  // 数据库帮助类实例
    private SearchResultAdapter adapter;  // 搜索结果的适配器
    private List<GeocodeAddress> searchResults;  // 存储搜索结果的列表

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // 初始化视图和变量
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSearch = findViewById(R.id.buttonSearch);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        dbHelper = new LocationDatabaseHelper(this);
        searchResults = new ArrayList<>();

        // 设置RecyclerView的布局管理器和适配器
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(searchResults, result -> {
            saveLocationAndReturn(result);
        });
        recyclerViewResults.setAdapter(adapter);

        // 为编辑框添加文本变化监听器
        editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 文本变化时进行地址搜索
                try {
                    searchLocation(charSequence.toString());
                } catch (AMapException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 为搜索按钮添加点击事件监听器
        buttonSearch.setOnClickListener(v -> {
            // 点击搜索按钮时进行地址搜索
            try {
                searchLocation(editTextAddress.getText().toString());
            } catch (AMapException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 搜索地址位置
     *
     * @param address 要搜索的地址
     * @throws AMapException 如果搜索失败抛出异常
     */
    private void searchLocation(String address) throws AMapException {
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                // 不处理逆地理编码结果
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                searchResults.clear();
                if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null) {
                    searchResults.addAll(geocodeResult.getGeocodeAddressList());
                    adapter.notifyDataSetChanged();  // 更新适配器以显示新的搜索结果
                } else {
                    // 如果没有找到结果，不显示提示
                    // Toast.makeText(AddLocationActivity.this, "未找到结果", Toast.LENGTH_SHORT).show();
                }
            }
        });

        GeocodeQuery query = new GeocodeQuery(address, null);
        geocodeSearch.getFromLocationNameAsyn(query);  // 异步进行地理编码搜索
    }

    //保存位置并返回
    private void saveLocationAndReturn(GeocodeAddress result) {
        double latitude = result.getLatLonPoint().getLatitude();
        double longitude = result.getLatLonPoint().getLongitude();
        String city = result.getCity();
        String district = result.getDistrict();

        Location location = new Location(latitude, longitude, city, district);
        dbHelper.insertLocation(location);  // 将位置插入数据库

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);  // 设置结果并返回
        finish();
    }
}