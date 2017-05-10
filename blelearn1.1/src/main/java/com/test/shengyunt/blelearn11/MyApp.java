package com.test.shengyunt.blelearn11;

import android.app.Application;
import android.os.Handler;

/**
 * Created by snowlive on 17-5-9.
 */

public class MyApp extends Application {
    private Handler myHandler ;//消息传输

    public Handler getMyHandler() {
        return myHandler;
    }

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }
}
