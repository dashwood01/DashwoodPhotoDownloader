package com.dashwood.photodownloader.listener;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

public class RetryPolicy implements com.android.volley.RetryPolicy {

    private DefaultRetryPolicy defaultRetryPolicy;

    public RetryPolicy(int timeout, int numRetry, float backOff) {
        defaultRetryPolicy = new DefaultRetryPolicy(timeout, numRetry, backOff);
    }


    @Override
    public int getCurrentTimeout() {
        return defaultRetryPolicy.getCurrentTimeout();
    }

    @Override
    public int getCurrentRetryCount() {
        return defaultRetryPolicy.getCurrentRetryCount();
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse == null) {
            defaultRetryPolicy.retry(error);
            return;
        }
        if (networkResponse.statusCode == 401) {
            throw error;
        } else if (networkResponse.statusCode == 403) {
            throw error;
        } else {
            defaultRetryPolicy.retry(error);
        }
    }
}
