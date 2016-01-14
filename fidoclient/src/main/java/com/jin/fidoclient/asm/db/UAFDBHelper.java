package com.jin.fidoclient.asm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 雅麟 on 2015/4/8.
 */
public class UAFDBHelper extends SQLiteOpenHelper {
    private static final String TAG = UAFDBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "uaf_database";
    private static final String DATABASE_USER_KEY_PAIR_TABLE = "user_reg";

    public static final String KEY_ID = "_id";
    public static final String KEY_AUTH_TYPE = "type";
    public static final String KEY_TOUCH_ID = "touch_id";
    public static final String KEY_KEY_ID = "key_id";
    public static final String KEY_APP_ID = "app_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_PRIVATE_KEY = "private_key";
    public static final String KEY_USER_PUBLIC_KEY = "public_key";


    public static final int CURRENT_DATABASE_VERSION = 2;
    private static UAFDBHelper mInstance = null;

    public synchronized static UAFDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UAFDBHelper(context, UAFDBHelper.DATABASE_NAME, null,
                    UAFDBHelper.CURRENT_DATABASE_VERSION);
        }
        return mInstance;
    }

    private UAFDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public UAFDBHelper(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_USER_KEY_PAIR_TABLE
                    + " (" + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_AUTH_TYPE + " VARCHAR,"
                    + KEY_TOUCH_ID + " VARCHAR,"
                    + KEY_KEY_ID + " VARCHAR,"
                    + KEY_APP_ID + " VARCHAR,"
                    + KEY_USERNAME + " VARCHAR,"
                    + KEY_USER_PRIVATE_KEY + " VARCHAR,"
                    + KEY_USER_PUBLIC_KEY + " VARCHAR)");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean registered(SQLiteDatabase db, String touchId) {
        return getUserRecord(db, touchId) != null;
    }

    public RegRecord getUserRecord(SQLiteDatabase db, String touchId) {
        RegRecord regRecord = null;
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DATABASE_USER_KEY_PAIR_TABLE, new String[]{
                    KEY_ID,
                    KEY_AUTH_TYPE,
                    KEY_KEY_ID,
                    KEY_APP_ID,
                    KEY_USERNAME,
                    KEY_USER_PRIVATE_KEY,
                    KEY_USER_PUBLIC_KEY,
            }, KEY_TOUCH_ID + "='" + touchId + "'", null, null, null, KEY_ID + " ASC");
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String type = cursor.getString(cursor.getColumnIndex(KEY_AUTH_TYPE));
                String keyId = cursor.getString(cursor.getColumnIndex(KEY_KEY_ID));
                String appId = cursor.getString(cursor.getColumnIndex(KEY_APP_ID));
                String username = cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
                String publicKeyBase64 = cursor.getString(cursor.getColumnIndex(KEY_USER_PUBLIC_KEY));
                String privateKeyBase64 = cursor.getString(cursor.getColumnIndex(KEY_USER_PRIVATE_KEY));
                regRecord = new RegRecord()
                        .id(id)
                        .type(type)
                        .touchId(touchId)
                        .keyId(keyId)
                        .appId(appId)
                        .username(username)
                        .userPrivateKey(privateKeyBase64)
                        .userPublicKey(publicKeyBase64);

            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return regRecord;
    }

    public List<RegRecord> getUserRecords(SQLiteDatabase db, String username) {
        List<RegRecord> regRecords = null;
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DATABASE_USER_KEY_PAIR_TABLE, new String[]{
                    KEY_ID,
                    KEY_AUTH_TYPE,
                    KEY_TOUCH_ID,
                    KEY_KEY_ID,
                    KEY_APP_ID,
                    KEY_USER_PRIVATE_KEY,
                    KEY_USER_PUBLIC_KEY,
            }, KEY_USERNAME + "='" + username + "'", null, null, null, KEY_ID + " ASC");
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (regRecords == null) {
                    regRecords = new ArrayList<>();
                }
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String touchId = cursor.getString(cursor.getColumnIndex(KEY_TOUCH_ID));
                String type = cursor.getString(cursor.getColumnIndex(KEY_AUTH_TYPE));
                String keyId = cursor.getString(cursor.getColumnIndex(KEY_KEY_ID));
                String appId = cursor.getString(cursor.getColumnIndex(KEY_APP_ID));
                String publicKeyBase64 = cursor.getString(cursor.getColumnIndex(KEY_USER_PUBLIC_KEY));
                String privateKeyBase64 = cursor.getString(cursor.getColumnIndex(KEY_USER_PRIVATE_KEY));
                RegRecord regRecord = new RegRecord()
                        .id(id)
                        .type(type)
                        .touchId(touchId)
                        .keyId(keyId)
                        .appId(appId)
                        .username(username)
                        .userPrivateKey(privateKeyBase64)
                        .userPublicKey(publicKeyBase64);

                regRecords.add(regRecord);
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return regRecords;
    }

    public boolean addRecord(SQLiteDatabase db, RegRecord regRecord) {
        if (registered(db, regRecord.touchId)) {
            return false;
        }
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_AUTH_TYPE, regRecord.type);
            values.put(KEY_TOUCH_ID, regRecord.touchId);
            values.put(KEY_KEY_ID, regRecord.keyId);
            values.put(KEY_APP_ID, regRecord.appId);
            values.put(KEY_USERNAME, regRecord.username);
            values.put(KEY_USER_PRIVATE_KEY, regRecord.userPrivateKey);
            values.put(KEY_USER_PUBLIC_KEY, regRecord.userPublicKey);
            db.insert(DATABASE_USER_KEY_PAIR_TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return true;
    }

}
