package com.jin.fidoclient.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.ui.fragment.AsmListFragment;
import com.jin.fidoclient.ui.fragment.AuthenticatorListFragment;
import com.jin.fidoclient.utils.StatLog;


/**
 * Created by YaLin on 2015/10/21.
 */
public class UAFClientActivity extends AppCompatActivity implements AsmListFragment.AsmItemPickListener {
    private static final String TAG = UAFClientActivity.class.getSimpleName();

    private static final String ASM_PACK_SP = "asm_pack";
    private static final String ASM_PACK_KEY = "asm_pack_key";
    private static final String ASM_APP_NAME_KEY = "asm_app_name_key";

    private String intentType;
    private String message;
    private String channelBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fido_client);
        Bundle extras = this.getIntent().getExtras();
        intentType = extras.getString(UAFIntent.UAF_INTENT_TYPE_KEY);
        message = extras.getString(UAFIntent.MESSAGE_KEY);
        channelBinding = extras.getString(UAFIntent.CHANNEL_BINDINGS_KEY);
        StatLog.printLog(TAG, "onCreate intentType:" + intentType + " message:" + message + " channelBinding:" + channelBinding);

        if (TextUtils.isEmpty(getAsmPack(getApplicationContext()))) {
            checkAsm();
        } else {
            showAuthenticator();
        }
    }

    private void checkAsm() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_content, AsmListFragment.getInstance(this))
                .commit();
    }

    private void showAuthenticator() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_content, AuthenticatorListFragment.getInstance(intentType, message, channelBinding))
                .commit();
    }

    @Override
    public void onBackPressed() {
        finishWithError(UAFClientError.USER_CANCELLED);
    }

    private void setFailedIntent(short errorCode) {
        Intent intent = UAFIntent.getUAFOperationErrorIntent(getComponentName().flattenToString(), errorCode);
        setResult(Activity.RESULT_CANCELED, intent);
    }

    private void finishWithError(short errorCode) {
        setFailedIntent(errorCode);
        finish();
    }

    @Override
    public void onAsmItemPick(String pack, String appName) {
        setAsmPack(getApplicationContext(), pack, appName);
        showAuthenticator();
    }

    public static String getAsmPack(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                ASM_PACK_SP, Context.MODE_PRIVATE);

        return sp.getString(ASM_PACK_KEY, null);
    }

    public static String getAsmAppName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                ASM_PACK_SP, Context.MODE_PRIVATE);

        return sp.getString(ASM_APP_NAME_KEY, null);
    }

    public static void setAsmPack(Context context, String pack, String appName) {
        SharedPreferences sp = context.getSharedPreferences(
                ASM_PACK_SP, Context.MODE_PRIVATE);
        sp.edit().putString(ASM_PACK_KEY, pack)
                .putString(ASM_APP_NAME_KEY, appName)
                .apply();
    }
}
