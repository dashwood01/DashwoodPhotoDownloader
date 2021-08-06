package com.dashwood.photodownloader.service;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DownloadHttpService extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private RetryPolicy retryPolicy;
    private Map<String, String> headers = new HashMap<>();

    public DownloadHttpService(String mUrl, Response.Listener<byte[]> listener,
                               Response.ErrorListener errorListener) {
        super(Method.GET, mUrl, errorListener);
        setShouldCache(false);
        mListener = listener;
    }

    public DownloadHttpService setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response.data,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return new com.dashwood.photodownloader.listener.RetryPolicy(600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        return super.setRetryPolicy(getRetryPolicy());
    }
}
