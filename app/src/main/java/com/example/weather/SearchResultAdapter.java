package com.example.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.geocoder.GeocodeAddress;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    // 定义搜索结果列表和监听器
    private List<GeocodeAddress> searchResults;
    private OnItemClickListener onItemClickListener;

    // 定义点击事件监听器接口
    public interface OnItemClickListener {
        void onItemClick(GeocodeAddress result);
    }

    // 构造函数，初始化搜索结果和监听器
    public SearchResultAdapter(List<GeocodeAddress> searchResults, OnItemClickListener onItemClickListener) {
        this.searchResults = searchResults;
        this.onItemClickListener = onItemClickListener;
    }

    // 创建ViewHolder
    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 将视图绑定到ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.district, parent, false);
        return new SearchResultViewHolder(view);
    }

    // 绑定数据到ViewHolder
    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        // 获取当前位置的搜索结果
        GeocodeAddress result = searchResults.get(position);
        // 设置TextView显示城市和区信息
        holder.textView.setText(result.getCity() + " " + result.getDistrict());
        // 设置项的点击事件
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(result));
    }

    // 返回数据项的数量
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // 定义ViewHolder类
    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}