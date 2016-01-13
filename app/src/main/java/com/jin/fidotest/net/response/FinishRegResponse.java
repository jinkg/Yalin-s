package com.jin.fidotest.net.response;

import com.google.gson.Gson;
import com.jin.fidotest.data.RegistrationRecord;

/**
 * Created by YaLin on 2016/1/12.
 */
public class FinishRegResponse extends BaseResponse {
    public RegistrationRecord[] data;

    public String toJson() {
        return new Gson().toJson(data);
    }
}
