package com.pawras.selfi.networks;


import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pawras.selfi.listners.DataCallListener;


import java.util.Map;

/**
 * Created by Saif on 8/18/2016.
 */
public class Network {
    // declare volley variables
    StringRequest mStringRequest;
    RequestQueue mRequestQueue;
    Context mContext;
    DataCallListener mDataCallListener;


    public Network(Context con, DataCallListener mDataCallListener) {
        this.mContext = con;
        this.mDataCallListener = mDataCallListener;
        mRequestQueue = Volley.newRequestQueue(con);
    }

    // this function will fetch user data from table volleyupload
    public void getUserData(String url, final Map<String,String> params) {

        mStringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mDataCallListener.onRequestCompleted(1,response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mDataCallListener.onRequestError(volleyError);

                        //Showing toast
                        //Toast.makeText(Splash.this, "server error"+volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

//                Map<String,String> params = new Hashtable<String, String>();
                //Adding parameters
                //param=params;
//                params.put(SSID,ssid);
//                params.put(BSSID,bssid);
                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        // RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        mRequestQueue.add(mStringRequest);
    }
}
