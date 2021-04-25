package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AddNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_name);
    }

    public void onClick(View view){
        String name = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();
        if(name.equals("")){
            Toast err = Toast.makeText(this,"Input a valid name",Toast.LENGTH_SHORT);
            err.show();
        }else{
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference dr = db.collection("userinfo").document("master");
            Map<String,String> m = new HashMap<>();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String USER_UID = user.getUid();
            m.put(USER_UID,name);
            dr.set(m, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent i = new Intent(AddNameActivity.this,DisplaySongs.class);
                    startActivity(i);
                }
            });
        }
    }
}