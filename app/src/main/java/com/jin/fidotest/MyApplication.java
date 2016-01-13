package com.jin.fidotest;

import android.app.Application;
import android.content.Context;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.utils.StatLog;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by YaLin on 2016/1/11.
 */
public class MyApplication extends Application {
    private static Context mContext;
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable ex) {
                StringWriter wr = new StringWriter();
                PrintWriter err = new PrintWriter(wr);
                ex.printStackTrace(err);

                StatLog.printLog("app exception", wr.toString() + defaultUncaughtExceptionHandler.getClass().getName());

                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
        UAFClientApi.init(mContext);
    }

    public static Context getContext() {
        return mContext;
    }
}
