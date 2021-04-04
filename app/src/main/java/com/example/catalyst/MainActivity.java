package com.example.catalyst;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent i = new Intent(this,DisplaySongs.class);
                startActivity(i);
            }
            else{
                Log.d("LOGIN_ERROR",res.getError().getMessage());
            }
        }
    }
}
