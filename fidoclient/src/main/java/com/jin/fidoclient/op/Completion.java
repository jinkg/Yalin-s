package com.jin.fidoclient.op;


import com.jin.fidoclient.asm.exceptions.ASMException;
import com.jin.fidoclient.ui.fragment.AuthenticatorListFragment;

/**
 * Created by YaLin on 2016/1/22.
 */
public class Completion extends ASMMessageHandler {
    public Completion(AuthenticatorListFragment fragment) {
        super(fragment);
    }

    @Override
    public boolean startTraffic() {
        fragment.getActivity().finish();
        return true;
    }

    @Override
    public boolean traffic(String asmResponseMsg) throws ASMException {
        return true;
    }
}
