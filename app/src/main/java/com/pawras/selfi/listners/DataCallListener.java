package com.pawras.selfi.listners;

import com.android.volley.VolleyError;

/**
 * Created by Saif on 8/18/2016.
 */
public interface DataCallListener {
    public void onRequestCompleted(int REQUEST_TYPE, String response);
    public void onRequestError(VolleyError volleyError);
}
