package com.example.mausam;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private FirebaseFirestore db;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    // CREATE
    public void addFavorite(FavoriteCity city,
                            OnSuccessListener<DocumentReference> onSuccess,
                            OnFailureListener onFailure) {

        db.collection("favorites")
                .add(city)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // READ - real-time updates
    public void getFavorites(EventListener<QuerySnapshot> listener) {

        db.collection("favorites")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    // DELETE
    public void deleteFavorite(String documentId,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure) {

        db.collection("favorites")
                .document(documentId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // SEARCH HISTORY
    public void addSearchHistory(String city) {

        Map<String, Object> entry = new HashMap<>();

        entry.put("city", city);
        entry.put("timestamp", System.currentTimeMillis());

        db.collection("searchHistory")
                .add(entry);
    }
}