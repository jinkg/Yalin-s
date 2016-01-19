/*
 * Copyright 2015 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jin.fidoclient.asm.op;

import android.database.sqlite.SQLiteDatabase;

import com.jin.fidoclient.api.UAFClientApi;
import com.jin.fidoclient.asm.db.UAFDBHelper;
import com.jin.fidoclient.asm.msg.ASMRequest;
import com.jin.fidoclient.asm.msg.obj.DeregisterIn;
import com.jin.fidoclient.utils.StatLog;

import java.util.logging.Logger;

public class Dereg extends ASMOperator {
    private static final String TAG = Dereg.class.getSimpleName();
    private final ASMRequest request;
    private final HandleResultCallback callback;

    public Dereg(ASMRequest request, HandleResultCallback callback) {
        if (!(request.args instanceof DeregisterIn)) {
            throw new IllegalStateException("asm request must has a DeregisterIn object");
        }
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void handle() {
        StatLog.printLog(TAG, "asm dereg delete record");
        String keyId = ((DeregisterIn) (request.args)).keyID;

        UAFDBHelper dbHelper = UAFDBHelper.getInstance(UAFClientApi.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.delete(db, keyId);
        if (callback != null) {
            callback.onHandleResult(null);
        }
    }
}
