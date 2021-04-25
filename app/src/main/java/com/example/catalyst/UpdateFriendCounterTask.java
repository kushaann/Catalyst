package com.example.catalyst;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

class UpdateFriendCounterTask extends AsyncTask<String,Void,Void> {
    private String me;
    public UpdateFriendCounterTask(String m){
        me = m;
    }
    @Override
    public Void doInBackground(String... params){
        String other = params[0];
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(other);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snap = task.getResult();
                    Map<String,Long> temp = (Map)snap.get("friend_counter");
                    temp = temp != null ? temp : new HashMap<>();
                    if(temp.containsKey(me))temp.put(me,temp.get(me)+1);
                    else temp.put(me,Integer.toUnsignedLong(1));
                    docRef.update("friend_counter",temp);
                }
            }
        });
        return null;
    }
}
