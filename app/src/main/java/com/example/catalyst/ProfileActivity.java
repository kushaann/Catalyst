package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {
    private String USER_UID, name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        USER_UID = user.getUid();
        name = user.getDisplayName();
        buildUI();

    }
    public void buildUI(){
        TextView nameField = (TextView)findViewById(R.id.nameProfileField);
        TextView sharedOut = (TextView)findViewById(R.id.songsSharedField);
        TextView recieved = (TextView)findViewById(R.id.songsRecievedField);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(USER_UID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snap = task.getResult();
                    nameField.setText(name);
                    sharedOut.setText(Long.toString((Long)snap.get("songs_shared_out")));
                    recieved.setText(Long.toString((Long)snap.get("songs_received")));
                }
            }
        });
    }
}