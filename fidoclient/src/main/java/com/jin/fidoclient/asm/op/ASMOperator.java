package com.jin.fidoclient.asm.op;

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
    public static final String TYPE_KEY = "requestType";
    public static final String ARGS_KEY = "args";

    protected Gson gson = new Gson();

    public static ASMOperator parseMessage(int touchId, String asmMessage) {
        try {
            ASMRequest asmRequest;
            JSONObject jsonObject = new JSONObject(asmMessage);
            String requestType = jsonObject.getString(TYPE_KEY);
            if (requestType.equals(Request.Register.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, RegisterIn.class);
                return new Reg(touchId, asmRequest);
            } else if (requestType.equals(Request.Authenticate.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, AuthenticateIn.class);
                return new Auth(touchId, asmRequest);
            } else if (requestType.equals(Request.Deregister.name())) {
                asmRequest = ASMRequest.fromJson(asmMessage, DeregisterIn.class);
                return new Dereg(asmRequest);
            }
        } catch (JSONException e) {
        }
        return new ASMOperator() {
            @Override
            public String handle() {
                return null;
            }
        };
    }

    public abstract String handle();
}
