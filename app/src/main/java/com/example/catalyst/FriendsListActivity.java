package com.example.catalyst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity {

    private String USER_UID;
    private String myName;
    final Map<String,Boolean> sendList = new HashMap<>();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        Log.d("FRIENDSLIST","why");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        USER_UID = user.getUid();
        myName = user.getDisplayName();
        getFriendsList();
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
                        for(String x : friendsList)sendList.put(x,false);
                        Log.d("LIST_FRIENDS",friendsList.toString());
                        buildUI(friendsList);
                    }
                }
            }
        });


    }

    public View.OnClickListener getTogglers(String str){
        return new View.OnClickListener(){
            @Override
            public void onClick(View view){

                TextView tv = view.findViewById(R.id.nameField);
                CompoundButton btn = view.findViewById(R.id.friendSelectedButton);
                String name = str;

                Log.d("FRIENDS_LIST_TOGGLING",sendList.toString());
                Log.d("FRIENDS_LIST_TOGGLING",name);

                sendList.put(name,!sendList.get(name));
                btn.toggle();
                view.setOnClickListener(getTogglers(str));
                Log.d("FRIENDS_LIST_ACTIVE_LIST",sendList.toString());
            }
        };
    }


    public void finalClick(View view){
        Intent intent = getIntent();
        String tURI = intent.getStringExtra("uri");
        String msg = intent.getStringExtra("msg").replaceAll("\n","");
        Map<String,String> sendObj = new HashMap<>();
        sendObj.put("uri",tURI);
        sendObj.put("msg",msg);
        sendObj.put("sender",myName);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for(Map.Entry<String,Boolean> pair : sendList.entrySet()){
            Boolean toSend = pair.getValue();
            if(toSend){
                db.collection("users").document(pair.getKey()).update("songs2",FieldValue.arrayUnion(sendObj));
            }
        }
        finish();

    }


    public void buildUI(List<String> friends){
        Intent intent = getIntent();
        final String tURI = intent.getStringExtra("URI");
        final String msg = intent.getStringExtra("msg");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("userinfo").document("master");
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    ViewGroup scrollwrap = (ViewGroup) findViewById(R.id.friendsscroller);
                    Log.d("FRIENDS_LIST",friends.toString());
                    for(String friend : friends){
                        Log.d("FRIENDS_LIST_UI",friend);
                         ConstraintLayout templayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.friendblock,null);
                         TextView tv1 = (TextView)templayout.findViewById(R.id.nameField);
                         CompoundButton toggler = (CompoundButton)templayout.findViewById(R.id.friendSelectedButton);
                         String curF = friend;
                        tv1.setText((String)doc.get(friend));
                        Log.d("FRIENDS_LIST_UI",friend+"|"+((String)doc.get(friend)));
                        templayout.setOnClickListener(getTogglers(curF));
                        scrollwrap.addView(templayout);
                        Log.d("FRIENDS_LIST_UI","added "+friend);

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
//                if(task.isSuccessful()){
//                    DocumentSnapshot doc = task.getResult();
//                    if(doc.exists()){
//                        Intent intent = getIntent();
//                        String tURI = intent.getStringExtra("URI");
//                        String msg = intent.getStringExtra("msg");
//
//                    }
//                }
//
//            }
//        })
//
//    }
}
