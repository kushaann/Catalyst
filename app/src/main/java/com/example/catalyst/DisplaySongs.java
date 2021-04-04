package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplaySongs extends AppCompatActivity {
    public String token;
    private static final int reqC = 1337;
    private static final String redirect = "http://com.example.catalyst/callback";
    private static final String CLIENT_ID = "eaf16244a7d0462c8c3a92856324fd1f";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_songs);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, redirect);
        AuthenticationRequest req = builder.build();
        AuthenticationClient.openLoginActivity(this,reqC,req);

    }

    protected void onActivityResult(int reqCode, int resCode, Intent i){
        super.onActivityResult(reqCode,resCode,i);
        if(reqCode==reqC){
            AuthenticationResponse res = AuthenticationClient.getResponse(resCode,i);
            switch(res.getType()){
                case TOKEN:
                    Log.d("SPOTIFY_AUTH_SUCCESS",res.getAccessToken());
                    token = res.getAccessToken();
                    loadSongs();
                    break;
                case ERROR:
                    Log.d("SPOTIFY_AUTH_ERROR",res.getError());

                    break;
                default:
                    Log.d("SPOTIFY_AUTH","???");

            }
        }
    }


    public void loadSongs(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("users").document(Constants.USER_ID);
        final List<String> ret = new ArrayList<>();
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        List<String> x = (List<String>)doc.get("songs");
                        buildUI(x);
                    }
                }
            }
        });
    }

    public void buildUI(List<String> songs){
        ViewGroup scrollwrap = (ViewGroup) findViewById(R.id.scroller);
        RequestQueue rq = Volley.newRequestQueue(this);
        int i = 0;
        for(String song : songs){
            final ConstraintLayout templayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.songblock,null);
            templayout.setTag(i);
            final TextView tv1 = templayout.findViewById(R.id.SongTitle);
            final TextView tv2 = templayout.findViewById(R.id.ArtistName);
            String url = Constants.SPOTIFY_API_URL+song;
            JsonObjectRequest JOR = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String artist = response.getJSONArray("artists").getJSONObject(0).getString("name");
                        String songName = response.getString("name");
                        tv1.setText(songName);
                        tv2.setText(artist);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("GET_TRACK_INFO_FAILURE", token + " wtf");
                    Log.d("GET_TRACK_INFO_FAILURE", error.toString());
                    Log.d("GET_TRACK_INFO_FAILURE", String.valueOf(error.networkResponse.statusCode));
                    Log.d("GET_TRACK_INFO_FAILURE_id", url);
                }
            }){
                @Override
                public Map<String,String> getHeaders() throws AuthFailureError{
                    Log.d("SPOTIFY_AUTH_TOKEN", token);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + token);
                    params.put("Accept", "application/json");
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };
            rq.add(JOR);
            scrollwrap.addView(templayout);
        }

    }

}