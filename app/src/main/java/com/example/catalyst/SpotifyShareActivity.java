package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SpotifyShareActivity extends AppCompatActivity {
    private static final int reqC = 1337;
    private static final String redirect = "http://com.example.catalyst/callback";
    private static final String CLIENT_ID = "eaf16244a7d0462c8c3a92856324fd1f";
    private String tURI = "";
    private String token = "";
    private RequestQueue rq;
    private JsonObjectRequest JOR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_share);
        Intent intent = getIntent();
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        text = text.substring(text.indexOf("https://"));
        text = text.substring(text.indexOf("track/")+6,text.indexOf("?"));
        Log.d("spotifyshare",text);
        tURI = text;


        rq = Volley.newRequestQueue(this);

        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE);
        token = sharedPrefs.getString("token","");
        if (token == "") {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,redirect);
            AuthenticationRequest req = builder.build();
            AuthenticationClient.openLoginActivity(this,reqC,req);

        }
        else {
            JsonObjectRequest TokenChecker = new JsonObjectRequest(Request.Method.GET, Constants.TEST_URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    buildUI();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,redirect);
                    AuthenticationRequest req = builder.build();
                    AuthenticationClient.openLoginActivity(SpotifyShareActivity.this,reqC,req);
                }
            }){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Log.d("SPOTIFY_AUTH_TOKEN",token);
                    Map<String,String> params = new HashMap<String,String>();
                    params.put("Authorization","Bearer " + token);
                    params.put("Accept","application/json");
                    params.put("Content-Type","application/json");
                    return params;
                }
            };

            rq.add(TokenChecker);

        }







    }


    public void buildUI(){
        Log.d("UIBUILDER","BUILDINGUI");
        JOR = new JsonObjectRequest(Request.Method.GET, Constants.SPOTIFY_API_URL + tURI, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    TextView title = findViewById(R.id.share_songname);
                    TextView artist = findViewById(R.id.share_artistname);
                    title.setText(response.getString("name"));
                    artist.setText(response.getJSONArray("artists").getJSONObject(0).getString("name"));
                    Log.d("share_ui",response.getString("name"));
                    Log.d("share_ui",response.getJSONArray("artists").getJSONObject(0).getString("name"));
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError vError){
                Log.d("GET_TRACK_INFO_FAILURE",token + " wtf");
                Log.d("GET_TRACK_INFO_FAILURE",vError.toString());
                Log.d("GET_TRACK_INFO_FAILURE",String.valueOf(vError.networkResponse.statusCode));
                Log.d("GET_TRACK_INFO_FAILURE",vError.toString());
                Log.d("GET_TRACK_INFO_FAILURE",vError.getMessage());
            }
        }){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Log.d("SPOTIFY_AUTH_TOKEN",token);
                Map<String,String> params = new HashMap<String,String>();
                params.put("Authorization","Bearer " + token);
                params.put("Accept","application/json");
                params.put("Content-Type","application/json");
                return params;
            }
        };
        rq.add(JOR);

    }

    public void onClick(View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docref = db.collection("users").document(Constants.USER_ID);
        docref.update("songs", FieldValue.arrayUnion(tURI));
    }

    protected void onActivityResult(int reqCode, int resCode, Intent i){
        super.onActivityResult(reqCode,resCode,i);
        if(reqCode==reqC){
            AuthenticationResponse res = AuthenticationClient.getResponse(resCode,i);
            switch(res.getType()){
                case TOKEN:
                    Log.d("SPOTIFY_AUTH_SUCCESS",res.getAccessToken());
                    token = res.getAccessToken();
                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPrefs.edit();
                    edit.putString("token",token);
                    edit.apply();
                    String x = getApplicationContext().getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE).getString("token","not there");
                    Log.d("TOKEN_WRITER",x);
                    buildUI();
                    break;
                case ERROR:
                    Log.d("SPOTIFY_AUTH_ERROR",res.getError());

                    break;
                default:
                    Log.d("SPOTIFY_AUTH","???");

            }
        }
    }
}