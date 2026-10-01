package com.example.mausam;

import androidx.annotation.NonNull;

import com.example.mausam.models.Plan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final CollectionReference plansCollection;
    private final CollectionReference favoritesCollection;

    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        plansCollection = db.collection("plans");
        favoritesCollection = db.collection("favorites");
    }

    // ==========================================
    // 1. AUTHENTICATION (PERSON 3)
    // ==========================================

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void signUp(String email, String password,
                       OnSuccessListener<AuthResult> onSuccess,
                       OnFailureListener onFailure) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Email and password cannot be empty."));
            }
            return;
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void signIn(String email, String password,
                       OnSuccessListener<AuthResult> onSuccess,
                       OnFailureListener onFailure) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Email and password cannot be empty."));
            }
            return;
        }

        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void signOut() {
        auth.signOut();
    }

    // ==========================================
    // 2. PLANS CRUD (PERSON 3)
    // ==========================================

    /**
     * Adds a new plan tied to the current logged-in user.
     */
    public void addPlan(Plan plan,
                        OnSuccessListener<DocumentReference> onSuccess,
                        OnFailureListener onFailure) {
        String uid = getCurrentUserId();
        if (uid == null) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalStateException("User must be signed in to add a plan."));
            }
            return;
        }

        plan.setUserId(uid);
        if (plan.getCreatedAt() == 0) {
            plan.setCreatedAt(System.currentTimeMillis());
        }

        plansCollection.add(plan)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Real-time listener for current user's plans, ordered by dateTime.
     */
    public ListenerRegistration getPlans(EventListener<QuerySnapshot> listener) {
        String uid = getCurrentUserId();
        if (uid == null) {
            return null;
        }

        return plansCollection.whereEqualTo("userId", uid)
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * One-time fetch of current user's upcoming plans.
     */
    public void getUpcomingPlans(OnSuccessListener<QuerySnapshot> onSuccess,
                                 OnFailureListener onFailure) {
        String uid = getCurrentUserId();
        if (uid == null) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalStateException("User must be signed in to view plans."));
            }
            return;
        }

        long now = System.currentTimeMillis();
        plansCollection.whereEqualTo("userId", uid)
                .whereGreaterThanOrEqualTo("dateTime", now)
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete a plan by its document ID.
     */
    public void deletePlan(String planId,
                           OnSuccessListener<Void> onSuccess,
                           OnFailureListener onFailure) {
        if (planId == null || planId.trim().isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Plan ID cannot be empty."));
            }
            return;
        }

        plansCollection.document(planId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Update a plan's status ("pending", "safe", "risky").
     */
    public void updatePlanStatus(String planId, String status,
                                 OnSuccessListener<Void> onSuccess,
                                 OnFailureListener onFailure) {
        if (planId == null || planId.trim().isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new IllegalArgumentException("Plan ID cannot be empty."));
            }
            return;
        }

        plansCollection.document(planId)
                .update("status", status)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // ==========================================
    // 3. EXISTING FAVORITES & SEARCH HISTORY
    // ==========================================

    // CREATE
    public void addFavorite(FavoriteCity city,
                            OnSuccessListener<DocumentReference> onSuccess,
                            OnFailureListener onFailure) {
        favoritesCollection.add(city)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // READ - real-time updates
    public void getFavorites(EventListener<QuerySnapshot> listener) {
        favoritesCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    // DELETE
    public void deleteFavorite(String documentId,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure) {
        favoritesCollection.document(documentId)
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