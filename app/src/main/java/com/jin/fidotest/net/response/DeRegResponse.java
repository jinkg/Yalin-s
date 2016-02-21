package com.jin.fidotest.net.response;

import com.google.gson.Gson;
import com.jin.fidoclient.msg.DeRegistrationRequest;

import java.util.List;

/**
 * Created by YaLin on 2016/1/14.
 */
public class DeRegResponse extends BaseResponse {
    public List<DeRegistrationRequest> data;

    public String toJson() {
        return new Gson().toJson(data);
    }
}
