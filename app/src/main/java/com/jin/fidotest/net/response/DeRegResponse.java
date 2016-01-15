package com.jin.fidotest.net.response;

import com.google.gson.Gson;
import com.jin.fidoclient.msg.DeregistrationRequest;

import java.util.List;

/**
 * Created by YaLin on 2016/1/14.
 */
public class DeRegResponse extends BaseResponse {
    public List<DeregistrationRequest> data;

    public String toJson() {
        return new Gson().toJson(data);
    }
}
