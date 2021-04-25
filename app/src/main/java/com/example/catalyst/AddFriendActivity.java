package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {

    private String USER_UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        USER_UID = user.getUid();
        startBuild();

    }

    public void startBuild() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("users").document(USER_UID);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                List<String> pending = new ArrayList();
                if(task.isSuccessful()){
                    DocumentSnapshot ds = task.getResult();
                    pending = (List)ds.get("pending_friends");
                }
                buildUI(pending);
            }
        });
    }


    public void buildUI(List<String> pending){
        TextView displayID = (TextView)findViewById(R.id.idDisplayField);
        displayID.setText("Your ID: " + USER_UID);
        if(pending.size() > 0){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference dr = db.collection("userinfo").document("master");
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot ds = task.getResult();
                        ViewGroup scrollwrap = (ViewGroup) findViewById(R.id.friendaddscroller);
                        for(String pender : pending){
                            ConstraintLayout templayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.friendecisionblock,null);
                            final TextView nameField = (TextView)templayout.findViewById(R.id.addFriendName);
                            final Button yesBtn = (Button)templayout.findViewById(R.id.acceptFriend);
                            final Button noBtn = (Button)templayout.findViewById(R.id.denyFriend);
                            nameField.setText((String)ds.get(pender));
                            yesBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    db.collection("users").document(USER_UID).update("friends", FieldValue.arrayUnion(pender));
                                    //db.collection("users").document(pender).update("friends",FieldValue.arrayUnion(USER_UID));
                                    db.collection("users").document(USER_UID).update("pending_friends",FieldValue.arrayRemove(pender));
                                    yesBtn.setVisibility(View.GONE);
                                    noBtn.setVisibility(View.GONE);

                                }
                            });
                            noBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    db.collection("users").document(USER_UID).update("pending_friends",FieldValue.arrayRemove(pender));
                                    yesBtn.setVisibility(View.GONE);
                                    noBtn.setVisibility(View.GONE);
                                }
                            });
                            scrollwrap.addView(templayout);
                        }
                    }
                }
            });
        }

    }

}