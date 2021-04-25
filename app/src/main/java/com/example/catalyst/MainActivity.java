package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       try{
           File songFile = new File(this.getFilesDir(),"SongsList.txt");

           songFile.createNewFile();
       }
       catch(IOException e){
           e.printStackTrace();
       }
        Log.d("ERRORS???","why");
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        Log.d("ERRORS???","why");
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                1337
        );
        Log.d("ERRORS???","why");


    }

    protected void onActivityResult(int rC, int resC, Intent data){
        super.onActivityResult(rC,resC,data);
        if(rC==1337){
            IdpResponse res = IdpResponse.fromResultIntent(data);
            if(resC==RESULT_OK){
                if(res.isNewUser()){
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference dr = db.collection("userinfo").document("master");
                    Map<String,String> m = new HashMap<>();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String USER_UID = user.getUid();
                    m.put(USER_UID,user.getDisplayName());
                    dr.set(m, SetOptions.merge());
                }
                Intent i = new Intent(this,DisplaySongs.class);
                startActivity(i);
            }
            else{
                Log.d("LOGIN_ERROR",res.getError().getMessage());
            }
        }
    }
}
