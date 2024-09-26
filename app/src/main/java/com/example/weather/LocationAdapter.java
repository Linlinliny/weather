package com.example.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    // 定义数据列表和监听器
    private List<Location> locationList;
    private OnItemDeleteListener onItemDeleteListener;
    private OnItemClickListener onItemClickListener;

    // 删除项监听器接口
    public interface OnItemDeleteListener {
        void onItemDelete(Location location);
    }

    // 点击项监听器接口
    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    // 自定义回调接口
    public interface ConfirmationCallback {
        void onResult(boolean confirmed);
    }

    // 构造函数，初始化数据和监听器
    public LocationAdapter(List<Location> locationList, OnItemDeleteListener onItemDeleteListener, OnItemClickListener onItemClickListener) {
        this.locationList = locationList;
        this.onItemDeleteListener = onItemDeleteListener;
        this.onItemClickListener = onItemClickListener;
    }

    // 创建ViewHolder
    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.district, parent, false);
        return new LocationViewHolder(view);
    }

    // 绑定数据到ViewHolder
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        holder.textView.setText(location.getDistrict());
        // 设置其他视图的内容，例如图像等

        // 设置更多按钮的点击事件，弹出菜单
        holder.ivMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.menu_item_options);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    showConfirmationDialog(holder.itemView.getContext(), new ConfirmationCallback() {
                        @Override
                        public void onResult(boolean confirmed) {
                            if (confirmed) {
                                // 用户点击了确定按钮
                                onItemDeleteListener.onItemDelete(location);
                            }
                        }
                    });
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // 设置整个项的点击事件
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(location));
    }

    private void showConfirmationDialog(Context context, final ConfirmationCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认操作");
        builder.setMessage("您确定要执行此操作吗？");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了确定按钮，返回true
                callback.onResult(true);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击了取消按钮，返回false
                callback.onResult(false);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 返回数据项的数量
    @Override
    public int getItemCount() {
        return locationList.size();
    }

    // 更新数据列表并刷新显示
    public void updateData(List<Location> newLocationList) {
        this.locationList = newLocationList;
        notifyDataSetChanged();
    }

    // 定义ViewHolder类
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        ImageView ivMore;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            imageView = itemView.findViewById(R.id.imageView3);
            ivMore = itemView.findViewById(R.id.iv_more);
        }
    }
}