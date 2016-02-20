package com.jin.fidoclient.asm.op;

import android.app.Activity;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.AuthenticateOut;
import com.jin.fidoclient.utils.StatLog;

public class ASMAuth extends ASMOperator implements Simulator.BiometricsAuthResultCallback {
    private static final String TAG = ASMAuth.class.getSimpleName();
    private final ASMRequest request;
    private final Activity activity;
    private final HandleResultCallback callback;

    public ASMAuth(Activity activity, ASMRequest request, HandleResultCallback callback) {
        if (!(request.args instanceof AuthenticateIn)) {
            throw new IllegalStateException("asm request must has a AuthenticateIn object");
        }
        this.activity = activity;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm auth show biometrics auth");
        Simulator.getInstance(request.authenticatorIndex).showBiometricsAuth(activity, this);
    }

    @Override
    public void onAuthSuccess(Simulator simulator, String biometricsId) {
        StatLog.printLog(TAG, "asm auth biometrics auth success,prepare asmRequest");
        ASMResponse<AuthenticateOut> response = new ASMResponse<>();
        try {
            AuthenticateOut authenticateOut = Simulator.authenticate(biometricsId, (AuthenticateIn) request.args, request.authenticatorIndex);
            response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            response.responseData = authenticateOut;
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ACCESS_DENIED;
        }
        if (callback != null) {
            callback.onHandleResult(gson.toJson(response));
        }
    }

    @Override
    public void onAuthFailed(Simulator simulator) {
        StatLog.printLog(TAG, "asm auth biometrics auth failed");
    }
}
