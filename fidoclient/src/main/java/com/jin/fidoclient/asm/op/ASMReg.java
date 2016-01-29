package com.jin.fidoclient.asm.op;

import android.app.Activity;

import com.jin.fidoclient.asm.api.StatusCode;
import com.jin.fidoclient.asm.authenticator.Simulator;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.ASMResponse;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterOut;
import com.jin.fidoclient.utils.StatLog;


public class ASMReg extends ASMOperator implements Simulator.BiometricsAuthResultCallback {
    public static final String TAG = ASMReg.class.getSimpleName();
    private final ASMRequest request;
    private final Activity activity;
    private final HandleResultCallback callback;

    public ASMReg(Activity activity, ASMRequest request, HandleResultCallback callback) {
        if (!(request.args instanceof RegisterIn)) {
            throw new IllegalStateException("asm request must has a RegisterIn object");
        }
        this.activity = activity;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm reg show biometrics auth");
        Simulator.getInstance(request.authenticatorIndex).showBiometricsAuth(activity, this);
    }

    @Override
    public void onAuthSuccess(Simulator simulator, String biometricsId) {
        StatLog.printLog(TAG, "asm reg biometrics auth success,prepare asmRequest");
        ASMResponse<RegisterOut> response = new ASMResponse<>();
        try {
            RegisterOut registerOut = Simulator.register(biometricsId, (RegisterIn) request.args, request.authenticatorIndex);
            if (registerOut != null) {
                response.responseData = registerOut;
                response.statusCode = StatusCode.UAF_ASM_STATUS_OK;
            } else {
                response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
            }
        } catch (Exception e) {
            response.statusCode = StatusCode.UAF_ASM_STATUS_ERROR;
        }
        StatLog.printLog(TAG, "asm response is:" + gson.toJson(response));
        if (callback != null) {
            callback.onHandleResult(gson.toJson(response));
        }
    }

    @Override
    public void onAuthFailed(Simulator simulator) {
        StatLog.printLog(TAG, "asm reg biometrics auth failed");
    }
}
