package com.jin.fidoclient.asm.op;

import android.app.Activity;

import com.google.gson.Gson;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.Request;
import com.jin.fidoclient.asm.msg.obj.AuthenticateIn;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.asm.msg.obj.RegisterIn;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YaLin on 2016/1/11.
 */
public abstract class ASMOperator {
    public interface HandleResultCallback {
        void onHandleResult(String result);
    }

    public static final String TYPE_KEY = "requestType";
    public static final String ARGS_KEY = "args";

    protected Gson gson = new Gson();

    public static ASMOperator parseMessage(Activity activity, String asmMessage, HandleResultCallback callback) {
        try {
            ASMRequest asmRequest;
            JSONObject jsonObject = new JSONObject(asmMessage);
            String requestType = jsonObject.getString(TYPE_KEY);
            if (requestType.equals(Request.Register.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, RegisterIn.class);
                return new ASMReg(activity, asmRequest, callback);
            } else if (requestType.equals(Request.Authenticate.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, AuthenticateIn.class);
                return new ASMAuth(activity, asmRequest, callback);
            } else if (requestType.equals(Request.Deregister.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, DeregisterIn.class);
                return new ASMDereg(asmRequest, callback);
            } else if (requestType.equals(Request.GetInfo.name())) {
                return new ASMGetInfo(callback);
            }
        } catch (JSONException e) {
        }
        return new ASMOperator() {
            @Override
            public void handle() {
            }
        };
    }

    public abstract void handle();
}
