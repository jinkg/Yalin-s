package com.jin.fidotest.net.response;

import com.google.gson.Gson;
import com.jin.fidoclient.msg.RegistrationRequest;

import java.util.List;

/**
 * Created by YaLin on 2016/1/11.
 */
public class StartRegResponse extends BaseResponse {
    public List<RegistrationRequest> data;

    public String toJson() {
        return new Gson().toJson(data);
    }
}
