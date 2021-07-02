package com.microfountain.contentproviderapplication;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Copyright (C), 2015-2021
 * FileName: MyContentProvider
 * Author: hujian
 * Date: 2021/7/1 9:30
 * History:
 * <author> <time> <version> <desc>
 */
public class MyContentProvider extends ContentProvider {

    private static final String TAG = "MyContentProvider";

    private Context mContext;
    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;

    public static final String AUTHORITY = "com.example.MyContentProvider";

    public static final int User_Code = 1;

    public static final int User_Code_LIMIT = 2;

    public static final int Job_Code = 3;

    public static final int Job_Code_LIMIT = 4;


    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mMatcher.addURI(AUTHORITY, "user", User_Code);
        mMatcher.addURI(AUTHORITY, "user" + "/limit/#", User_Code_LIMIT);

        mMatcher.addURI(AUTHORITY, "job", Job_Code);
        mMatcher.addURI(AUTHORITY, "job" + "/limit/#", Job_Code_LIMIT);
    }


    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new DBHelper(mContext);
        db = mDbHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int code = mMatcher.match(uri);
        switch (code) {
            case User_Code:
            case User_Code_LIMIT:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + DBHelper.USER_TABLE_NAME;
            case Job_Code:
            case Job_Code_LIMIT:
                return "vnd.android.cursor.dir/" + AUTHORITY + "." + DBHelper.JOB_TABLE_NAME;
        }
        return null;
    }

    private String getTableName(int code) {
        switch (code) {
            case User_Code:
            case User_Code_LIMIT:
                return DBHelper.USER_TABLE_NAME;
            case Job_Code:
            case Job_Code_LIMIT:
                return DBHelper.JOB_TABLE_NAME;
            default:
                return "";
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int code = mMatcher.match(uri);
        String tableName = getTableName(code);
        Cursor query = null;
        if (code == User_Code_LIMIT || code == Job_Code_LIMIT) {
            String segment = uri.getLastPathSegment();
            query = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder, segment);
        } else {
            query = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values != null) {
            Set<Map.Entry<String, Object>> valueSet = values.valueSet();
            for (Map.Entry<String, Object> value : valueSet) {
                Log.v(TAG, "value: " + value.getKey() + "/" + value.getValue());
            }
            int code = mMatcher.match(uri);
            String tableName = getTableName(code);
            long insert = db.insert(tableName, null, values);
            if (insert != -1) {
                ContentResolver contentResolver = mContext.getContentResolver();
                if (contentResolver != null) {

                    contentResolver.notifyChange(uri, null);
                }
                return ContentUris.withAppendedId(uri, insert);
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
