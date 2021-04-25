package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplaySongs extends AppCompatActivity {
    public String token;
    public RequestQueue rq;
    private static final int reqC = 1337;
    private static final String redirect = "http://com.example.catalyst/callback";
    private static final String CLIENT_ID = "eaf16244a7d0462c8c3a92856324fd1f";
    private int viewTag = 0;
    private String USER_UID;
    private SpotifyAppRemote remoteSpotifyConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_songs);
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(Constants.CLIENT_ID)
                        .setRedirectUri(Constants.redirect)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                remoteSpotifyConnection = spotifyAppRemote;
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d("SPOTIFY_REMOTE","failed");
            }
        });


        rq = Volley.newRequestQueue(this);
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getString(R.string.token_file), Context.MODE_PRIVATE);
        token = sharedPrefs.getString("token","");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        USER_UID = user.getUid();
        if (token == "") {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,redirect);
            AuthenticationRequest req = builder.build();
            AuthenticationClient.openLoginActivity(this,reqC,req);

        }
        else {
            JsonObjectRequest TokenChecker = new JsonObjectRequest(Request.Method.GET, Constants.TEST_URL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loadSongs();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,redirect);
                    AuthenticationRequest req = builder.build();
                    AuthenticationClient.openLoginActivity(DisplaySongs.this,reqC,req);
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


    public void clearFile(View view){
        try{
            File songFile = new File(getApplicationContext().getFilesDir(), "SongsList.txt");
            FileOutputStream fos = new FileOutputStream(songFile,false);
            fos.write("".getBytes());
            fos.close();
            LinearLayout toClear = (LinearLayout)findViewById(R.id.scroller);
            toClear.removeAllViews();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public View.OnClickListener getClicker(String playableURI){
        return new View.OnClickListener(){
            @Override
            public void onClick(View view){
                remoteSpotifyConnection.getPlayerApi().play(playableURI);
                view.setOnClickListener(getClicker(playableURI));
            }
        };
    }

    public void AddFriends(View view){
        Intent i = new Intent(this,AddFriendActivity.class);
        startActivity(i);
    }

    public void queryDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("users").document(USER_UID);
        final List<Map<String,String>> ret = new ArrayList<>();
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        List<Map<String, String>> x = (List)doc.get(Constants.SONG_MAP_NAME);
                        Log.d("DATABASE_READING",x.toString());
                        for(Map<String,String> y : x){
                            if(!y.containsKey("placeholder")) {
                                ret.add(y);
                                Log.d("MAP_READING", "read");
                                Log.d("MAP_OUTPUT", y.get("song") + "||" + y.get("msg"));

                                dr.update(Constants.SONG_MAP_NAME,FieldValue.arrayRemove(y));
                            }
                        }
//                        Map<String,Object> upd = new HashMap<>();
//                        upd.put(Constants.SONG_MAP_NAME,FieldValue.delete());
//                        dr.update(upd);

                        try {
                            File songFile = new File(getApplicationContext().getFilesDir(), "SongsList.txt");
                            FileOutputStream fos = new FileOutputStream(songFile,true);
                            for(Map<String,String> maps : ret){
                                String t = "";
                                t += maps.get("uri") + "," + maps.get("msg")+ "," + maps.get("sender") + "\n";
                                fos.write(t.getBytes());
                            }
                            fos.close();
                            Log.d("BUILD_LAYOUT","from db");
                            buildUIFromMaps(ret);
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    public void loadSongs(){
        try{
            File songFile = new File(this.getFilesDir(),"SongsList.txt");
            BufferedReader br = new BufferedReader(new FileReader(songFile));
            String line;
            List<Map<String,String>> loader = new ArrayList();
            while((line = br.readLine())!=null){
                Map<String,String> temp = new HashMap<>();
                String[] parts = line.split(",");
                temp.put("uri",parts[0]);
                temp.put("msg",parts[1]);
                temp.put("sender",parts[2]);
                loader.add(temp);
            }
            Log.d("BUILD_LAYOUT","from file");
            buildUIFromMaps(loader);
            queryDatabase();

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void buildUIFromMaps(List<Map<String,String>> songs){
        ViewGroup scrollwrap = (ViewGroup) findViewById(R.id.scroller);
        for(Map<String,String> songInfo : songs){
            Log.d("BUILD_LAYOUT",songs.toString());
            final ConstraintLayout templayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.songblock,null);
            //templayout.setBackgroundColor(Color.parseColor("#883FF5"));
            templayout.setTag(viewTag);
            viewTag++;
            final TextView tv1 = templayout.findViewById(R.id.SongTitle);
            final TextView tv2 = templayout.findViewById(R.id.ArtistName);
            final TextView tv3 = templayout.findViewById(R.id.Message);
            final ImageView albumView = templayout.findViewById(R.id.AlbumArt);
            final TextView tv4 = templayout.findViewById(R.id.senderField);
            final String msg = songInfo.get("msg");
            final String sender = songInfo.containsKey("sender") ? songInfo.get("sender") : "anonymous";
            String url = Constants.SPOTIFY_API_URL+songInfo.get("uri");
            JsonObjectRequest JOR = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String artist = response.getJSONArray("artists").getJSONObject(0).getString("name");
                        String songName = response.getString("name");
                        tv1.setText(songName);
                        tv2.setText(artist);
                        tv3.setText(msg);
                        tv4.setText(sender);
                        JSONObject image = response.getJSONObject("album").getJSONArray("images").getJSONObject(0);
                        new DownloadImagesTask(albumView,DisplaySongs.this).execute(image.getString("url"));
                        String playableURI = "spotify:track:"+songInfo.get("uri");
                        templayout.setOnClickListener(getClicker(playableURI));


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


    public void buildUI(List<String> songs){
        ViewGroup scrollwrap = (ViewGroup) findViewById(R.id.scroller);

        for(String song : songs){
            final ConstraintLayout templayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.songblock,null);
            templayout.setTag(viewTag);
            viewTag++;
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