package com.delivery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.delivery.site.fragment.entity.SiteEntity;

import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.DATABASE_NAME;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_BASE_PRICE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_DESCRIPTION;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_DISPLAY_TYPE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_FACING_DIRECTION;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_ID;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_IMAGE_URL;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_LATITUDE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_LOCATION_TYPE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_LONGITUDE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_NAME;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_TABLE;
import static com.delivery.utils.jkeys.Keys.BookmarkDatabase.SITE_TYPE;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "CREATE TABLE "
            + SITE_TABLE + "(" + SITE_ID + " INTEGER PRIMARY KEY,"
            + SITE_NAME + " TEXT,"
            + SITE_BASE_PRICE + " INTEGER,"
            + SITE_DESCRIPTION + " TEXT,"
            + SITE_TYPE + " TEXT,"
            + SITE_LOCATION_TYPE + " TEXT,"
            + SITE_FACING_DIRECTION + " TEXT,"
            + SITE_IMAGE_URL + " TEXT,"
            + SITE_LATITUDE + " TEXT,"
            + SITE_LONGITUDE + " TEXT,"
            + SITE_DISPLAY_TYPE + " TEXT" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SKOUT", "DATABASE CREATED");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SITE_TABLE);
        onCreate(db);
    }

    public boolean insertData(SiteEntity siteEntity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SITE_ID, siteEntity.getSiteId());
        contentValues.put(SITE_NAME, siteEntity.getName());
        contentValues.put(SITE_BASE_PRICE, siteEntity.getBasePrice());
        contentValues.put(SITE_DESCRIPTION, siteEntity.getDescription());
        contentValues.put(SITE_TYPE, siteEntity.getSiteType());
        contentValues.put(SITE_LOCATION_TYPE, siteEntity.getSiteLocationType());
        contentValues.put(SITE_FACING_DIRECTION, siteEntity.getFacingDirection());
        contentValues.put(SITE_IMAGE_URL, siteEntity.getPhotosUrl());
        contentValues.put(SITE_LATITUDE, siteEntity.getLatitude());
        contentValues.put(SITE_LONGITUDE, siteEntity.getLongitude());
        contentValues.put(SITE_DISPLAY_TYPE, siteEntity.getSiteDisplayType());

        long result = db.insert(SITE_TABLE, null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + SITE_TABLE, null);
        return res;
    }

    public Cursor getAllDataWithId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + SITE_TABLE + " WHERE  " + SITE_ID + "= " + id;
        Cursor res = db.rawQuery(query, null);
        return res;
    }

    public boolean deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(SITE_TABLE, null, null);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteDataId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(SITE_TABLE, SITE_ID + " = " + id, null);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
}
