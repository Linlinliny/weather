package com.example.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    // 数据库名称和版本号常量
    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 1;

    // 数据库表和列的名称常量
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_DISTRICT = "district";

    // 创建表的SQL语句
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_CITY + " TEXT, " +
                    COLUMN_DISTRICT + " TEXT);";

    // 构造函数，创建数据库
    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 创建数据库表
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    // 升级数据库时调用，删除旧表并创建新表
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 插入新的位置信息到数据库
    public void insertLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_CITY, location.getCity());
        values.put(COLUMN_DISTRICT, location.getDistrict());
        db.insert(TABLE_NAME, null, values);  // 插入数据
        db.close();
    }

    // 获取所有位置信息的游标
    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);  // 查询所有数据
    }

    // 删除指定位置的数据
    public void deleteLocation(double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_LATITUDE + "=? AND " + COLUMN_LONGITUDE + "=?",
                new String[]{String.valueOf(latitude), String.valueOf(longitude)});  // 根据经纬度删除数据
        db.close();
    }
}