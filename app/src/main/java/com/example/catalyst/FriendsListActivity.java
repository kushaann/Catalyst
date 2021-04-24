package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity {

    private String USER_UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        USER_UID = user.getUid();
    }

    public void getFriendsList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("users").document(USER_UID);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        List<String> friendsList = (List)doc.get("friends");

                    }
                }
            }
        });


    }

//    public  List<String> getOutList(List<String> allFriends){
//        final List<String> fL = allFriends;
//        final List<String> recommended = new ArrayList<>();
//        final List<String> rest = new ArrayList<>();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference dr = db.collection("userinfo").document("master");
//        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//
//            }
//        })
//
//    }
}
