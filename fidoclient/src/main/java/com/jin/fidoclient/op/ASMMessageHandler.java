package com.jin.fidoclient.op;

import android.content.Context;

import com.google.gson.Gson;
import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.msg.client.UAFIntentType;

/**
 * Created by YaLin on 2016/1/11.
 */
public abstract class ASMMessageHandler {
    public static final String REG_TAG = "\"Reg\"";
    public static final String AUTH_TAG = "\"Auth\"";
    public static final String DEREG_TAG = "\"Dereg\"";

    protected final Gson gson = new Gson();

    public static ASMMessageHandler parseMessage(Context context, String intentType, String uafMessage, String channelBinding) {
        if (UAFIntentType.UAF_OPERATION.name().equals(intentType)) {
            if (uafMessage.contains(REG_TAG)) {
                return new Reg(context, uafMessage, channelBinding);
            } else if (uafMessage.contains(AUTH_TAG)) {
                return new Auth(context, uafMessage, channelBinding);
            } else if (uafMessage.contains(DEREG_TAG)) {
                return new Dereg(context, uafMessage);
            }
        } else if (UAFIntentType.CHECK_POLICY.name().equals(intentType)) {
            return new CheckPolicy(context, uafMessage);
        }
        return new ASMMessageHandler() {
            @Override
            public String generateAsmRequest() {
                return null;
            }

            @Override
            public String parseAsmResponse(String asmResponseMsg) {
                return null;
            }
        };
    }

    public abstract String generateAsmRequest();

    public abstract String parseAsmResponse(String asmResponseMsg) throws ASMException;
}
