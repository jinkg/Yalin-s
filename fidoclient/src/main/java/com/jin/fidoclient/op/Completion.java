package com.jin.fidoclient.op;

import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.ui.UAFClientActivity;

/**
 * Created by YaLin on 2016/1/22.
 */
public class Completion extends ASMMessageHandler {
    public Completion(UAFClientActivity activity) {
        super(activity);
    }

    @Override
    public boolean startTraffic() {
        activity.finish();
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) throws ASMException {
        return true;
    }
}
