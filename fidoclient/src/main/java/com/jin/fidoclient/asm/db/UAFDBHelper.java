package com.jin.fidoclient.asm.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.jin.fidoclient.utils.StatLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 雅麟 on 2015/4/8.
 */
public class UAFDBHelper extends SQLiteOpenHelper {
    private static final String TAG = UAFDBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "uaf_database";
    private static final String DATABASE_USER_KEY_PAIR_TABLE = "user_reg";

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
                    + " (" + RegRecord.KEY_ID + " INTEGER PRIMARY KEY,"
                    + RegRecord.KEY_AUTH_TYPE + " VARCHAR,"
                    + RegRecord.KEY_BIOMETRICS_ID + " VARCHAR,"
                    + RegRecord.KEY_AAID + " VARCHAR,"
                    + RegRecord.KEY_KEY_ID + " VARCHAR,"
                    + RegRecord.KEY_APP_ID + " VARCHAR,"
                    + RegRecord.KEY_USERNAME + " VARCHAR,"
                    + RegRecord.KEY_USER_PRIVATE_KEY + " VARCHAR,"
                    + RegRecord.KEY_USER_PUBLIC_KEY + " VARCHAR)");

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

    public boolean registered(SQLiteDatabase db, String biometricsId) {
        return getUserRecord(db, biometricsId) != null;
    }

    public RegRecord getUserRecord(SQLiteDatabase db, String biometricsId) {
        StatLog.printLog(TAG, "get user record biometricsId: " + biometricsId);
        RegRecord regRecord = null;
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DATABASE_USER_KEY_PAIR_TABLE, new String[]{
                    RegRecord.KEY_ID,
                    RegRecord.KEY_AUTH_TYPE,
                    RegRecord.KEY_AAID,
                    RegRecord.KEY_KEY_ID,
                    RegRecord.KEY_APP_ID,
                    RegRecord.KEY_USERNAME,
                    RegRecord.KEY_USER_PRIVATE_KEY,
                    RegRecord.KEY_USER_PUBLIC_KEY,
            }, RegRecord.KEY_BIOMETRICS_ID + "='" + biometricsId + "'", null, null, null, RegRecord.KEY_ID + " ASC");
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(RegRecord.KEY_ID));
                String type = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_AUTH_TYPE));
                String aaid = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_AAID));
                String keyId = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_KEY_ID));
                String appId = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_APP_ID));
                String username = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_USERNAME));
                String publicKeyBase64 = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_USER_PUBLIC_KEY));
                String privateKeyBase64 = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_USER_PRIVATE_KEY));
                regRecord = new RegRecord()
                        .id(id)
                        .type(type)
                        .biometricsId(biometricsId)
                        .aaid(aaid)
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
            StatLog.printLog(TAG, "get user record exception: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return regRecord;
    }

    public List<RegRecord> getUserRecords(SQLiteDatabase db, String username) {
        StatLog.printLog(TAG, "get user records username: " + username);
        List<RegRecord> regRecords = null;
        db.beginTransaction();
        try {
            Cursor cursor = db.query(DATABASE_USER_KEY_PAIR_TABLE, new String[]{
                    RegRecord.KEY_ID,
                    RegRecord.KEY_AUTH_TYPE,
                    RegRecord.KEY_BIOMETRICS_ID,
                    RegRecord.KEY_AAID,
                    RegRecord.KEY_KEY_ID,
                    RegRecord.KEY_APP_ID,
                    RegRecord.KEY_USER_PRIVATE_KEY,
                    RegRecord.KEY_USER_PUBLIC_KEY,
            }, RegRecord.KEY_USERNAME + "='" + username + "'", null, null, null, RegRecord.KEY_ID + " ASC");
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                if (regRecords == null) {
                    regRecords = new ArrayList<>();
                }
                int id = cursor.getInt(cursor.getColumnIndex(RegRecord.KEY_ID));
                String touchId = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_BIOMETRICS_ID));
                String aaid = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_AAID));
                String type = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_AUTH_TYPE));
                String keyId = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_KEY_ID));
                String appId = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_APP_ID));
                String publicKeyBase64 = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_USER_PUBLIC_KEY));
                String privateKeyBase64 = cursor.getString(cursor.getColumnIndex(RegRecord.KEY_USER_PRIVATE_KEY));
                RegRecord regRecord = new RegRecord()
                        .id(id)
                        .type(type)
                        .biometricsId(touchId)
                        .aaid(aaid)
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
            StatLog.printLog(TAG, "get user records exception: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        StatLog.printLog(TAG, "regRecords is: " + new Gson().toJson(regRecords));
        return regRecords;
    }

    public boolean addRecord(SQLiteDatabase db, RegRecord regRecord) {
        StatLog.printLog(TAG, "add user records regRecord: " + regRecord.toString());
        if (registered(db, regRecord.biometricsId)) {
            return false;
        }
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(RegRecord.KEY_AUTH_TYPE, regRecord.type);
            values.put(RegRecord.KEY_BIOMETRICS_ID, regRecord.biometricsId);
            values.put(RegRecord.KEY_AAID, regRecord.aaid);
            values.put(RegRecord.KEY_KEY_ID, regRecord.keyId);
            values.put(RegRecord.KEY_APP_ID, regRecord.appId);
            values.put(RegRecord.KEY_USERNAME, regRecord.username);
            values.put(RegRecord.KEY_USER_PRIVATE_KEY, regRecord.userPrivateKey);
            values.put(RegRecord.KEY_USER_PUBLIC_KEY, regRecord.userPublicKey);
            db.insert(DATABASE_USER_KEY_PAIR_TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            StatLog.printLog(TAG, "add user record exception: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public void delete(SQLiteDatabase db, String keyId) {
        StatLog.printLog(TAG, "delete user records keyId: " + keyId);
        db.beginTransaction();
        try {
            db.delete(DATABASE_USER_KEY_PAIR_TABLE, RegRecord.KEY_KEY_ID + "=?", new String[]{keyId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            StatLog.printLog(TAG, "delete user records exception: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

}
