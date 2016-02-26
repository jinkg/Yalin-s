package com.jin.fidoclient.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.jin.fidoclient.R;
import com.jin.fidoclient.api.UAFClientError;
import com.jin.fidoclient.api.UAFIntent;
import com.jin.fidoclient.asm.api.ASMIntent;
import com.jin.fidoclient.msg.AsmInfo;
import com.jin.fidoclient.ui.fragment.AsmListFragment;
import com.jin.fidoclient.ui.fragment.AuthenticatorListFragment;
import com.jin.fidoclient.utils.StatLog;

import java.util.List;


/**
 * Created by YaLin on 2015/10/21.
 */
public class UAFClientActivity extends AppCompatActivity implements AsmListFragment.AsmItemPickListener {
    private static final String TAG = UAFClientActivity.class.getSimpleName();

    private static final String ASM_INFO_SP = "asm_pack";
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
    public void onAsmItemPick(AsmInfo info) {
        setAsmInfo(getApplicationContext(), info);
        showAuthenticator();
    }

    public static String getAsmPack(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                ASM_INFO_SP, Context.MODE_PRIVATE);

        return sp.getString(ASM_PACK_KEY, null);
    }

    public static AsmInfo getAsmInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                ASM_INFO_SP, Context.MODE_PRIVATE);
        String name = sp.getString(ASM_APP_NAME_KEY, null);
        String pack = sp.getString(ASM_PACK_KEY, null);
        ResolveInfo resolveInfo = resolve(context, pack);
        AsmInfo info = new AsmInfo();
        info.appName(name)
                .pack(pack);
        if (resolveInfo != null) {
            info.icon = resolveInfo.loadIcon(context.getPackageManager());
        }

        return info;
    }

    public static void setAsmInfo(Context context, AsmInfo info) {
        if (info == null) {
            info = new AsmInfo();
        }
        SharedPreferences sp = context.getSharedPreferences(
                ASM_INFO_SP, Context.MODE_PRIVATE);
        sp.edit().putString(ASM_PACK_KEY, info.pack)
                .putString(ASM_APP_NAME_KEY, info.appName)
                .apply();
    }

    private static ResolveInfo resolve(Context context, String pack) {
        if (TextUtils.isEmpty(pack)) {
            return null;
        }
        Intent intent = ASMIntent.getASMIntent();
        intent.setPackage(pack);

        List<android.content.pm.ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
        if (infos != null && infos.size() > 0) {
            return infos.get(0);
        } else {
            return null;
        }
    }
}
