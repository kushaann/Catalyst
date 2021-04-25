package com.example.catalyst;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

class AddFriendTask extends AsyncTask<String, Void, Void> {
    String me, fr;
    public AddFriendTask(String f, String m){
        fr = f;
        me = m;
    }
    @Override
    protected Void doInBackground(String... params){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection("users").document(fr);
        dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snap = task.getResult();
                    if(!isInList((List)snap.get("friends"),(List)snap.get("pending_friends"),me)){
                        dr.update("pending_friends",FieldValue.arrayUnion(me));
                    }
                }
            }
        });
//
        return null;
    }

    private boolean isInList(List<String> f, List<String> s, String x){
        for(String str : f)if(str.equals(x))return true;
        for(String str : s)if(str.equals(x))return true;
        return false;
    }

}
