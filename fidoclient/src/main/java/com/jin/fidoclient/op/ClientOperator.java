package com.jin.fidoclient.op;

import android.app.Activity;

import com.google.gson.Gson;
import com.jin.fidoclient.asm.exceptions.ASMException;

/**
 * Created by YaLin on 2016/1/11.
 */
public abstract class ClientOperator {
    public static final String REG_TAG = "\"Reg\"";
    public static final String AUTH_TAG = "\"Auth\"";
    public static final String DEREG_TAG = "\"Dereg\"";

    public static final int REQUEST_ASM_OPERATION = 1;

    protected final Gson gson = new Gson();

    public static ClientOperator parseMessage(Activity activity, String uafMessage, String channelBinding) {
        if (uafMessage.contains(REG_TAG)) {
            return new Reg(activity, uafMessage, channelBinding);
        } else if (uafMessage.contains(AUTH_TAG)) {
            return new Auth(activity, uafMessage, channelBinding);
        } else if (uafMessage.contains(DEREG_TAG)) {
            return new Dereg(uafMessage);
        }
        return new ClientOperator() {
            @Override
            public void handle() {
            }

            @Override
            public String assemble(String result) {
                return null;
            }
        };
    }

    public abstract void handle();

    public abstract String assemble(String result) throws ASMException;
}
