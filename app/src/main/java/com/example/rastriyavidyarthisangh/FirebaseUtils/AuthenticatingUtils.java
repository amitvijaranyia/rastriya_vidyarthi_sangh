package com.example.rastriyavidyarthisangh.FirebaseUtils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.rastriyavidyarthisangh.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AuthenticatingUtils {
    private static boolean hasAdminAccess = false;

    public static boolean isUserLoggedIn(FirebaseUser currentUser){
        if(currentUser == null) return false;
        return true;
    }


}
