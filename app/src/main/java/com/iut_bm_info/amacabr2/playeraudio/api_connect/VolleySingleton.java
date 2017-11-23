package com.iut_bm_info.amacabr2.playeraudio.api_connect;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by amacabr2 on 19/11/17.
 */

public class VolleySingleton {

    private static VolleySingleton volleySingleton;

    private static Context context;

    private RequestQueue requestQueue;

    private VolleySingleton(Context ctx) {
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context ctx) {
        if (volleySingleton == null) {
            volleySingleton = new VolleySingleton(ctx);
        }
        return volleySingleton;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }
}
