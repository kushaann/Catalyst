package com.example.catalyst;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.rpc.context.AttributeContext;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONObject;

class Constants {
    public final static String SPOTIFY_API_URL = "https://api.spotify.com/v1/tracks/";
    public final static String USER_ID = "1";
    public static final int reqC = 1337;
    public static final String redirect = "http://com.example.catalyst/callback";
    public static final String CLIENT_ID = "eaf16244a7d0462c8c3a92856324fd1f";
    public static final String TEST_URL = SPOTIFY_API_URL + "6DCZcSspjsKoFjzjrWoCdn";

}
