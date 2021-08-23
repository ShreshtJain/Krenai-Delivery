package com.delivery.utils.database;

import android.content.Context;
import android.database.Cursor;

import com.delivery.site.fragment.entity.SiteEntity;


public class BookmarkUtil {
    DatabaseHelper db;
    Context context;

    public BookmarkUtil(Context context) {
        this.context = context;
        db = new DatabaseHelper(context);
    }

    public String recentToBookmark(SiteEntity siteEntity) {
        Cursor cursor = db.getAllDataWithId(siteEntity.getSiteId());
        if (cursor.moveToNext()) {
            db.deleteDataId(siteEntity.getSiteId());
        } else {
            db.insertData(siteEntity);
        }
        return "1";
    }


}
