package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rastriyavidyarthisangh.Adapters.AdapterCwcMember;
import com.example.rastriyavidyarthisangh.FirebaseUtils.AuthenticatingUtils;
import com.example.rastriyavidyarthisangh.POJO.SingleCwcMember;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CwcMembersActivity extends AppCompatActivity {
    private static final String TAG = "tag cwc";

    RecyclerView rvCwcMembersList;
    ProgressBar pbLoadingIndicator;
    AdapterCwcMember cwcMemberListAdapter;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    FirebaseDatabase mRdb;
    DatabaseReference mRdbReference;
    ChildEventListener mCwcMemberChildListener;

    DatabaseReference cwcMembersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cwc_members);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();

        cwcMembersReference = mRdbReference.child(getString(R.string.json_object_cwc_members));

        rvCwcMembersList = findViewById(R.id.rvCwcMembersList);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);

        cwcMemberListAdapter = new AdapterCwcMember(this, new ArrayList<SingleCwcMember>());

        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvCwcMembersList.setLayoutManager(lm);
        rvCwcMembersList.setAdapter(cwcMemberListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentUser = mAuth.getCurrentUser();
        if(mCwcMemberChildListener == null){
            mCwcMemberChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    SingleCwcMember singleCwcMember = dataSnapshot.getValue(SingleCwcMember.class);
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    cwcMemberListAdapter.addSingleCwcMember(singleCwcMember);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            cwcMembersReference.orderByChild("priority").addChildEventListener(mCwcMemberChildListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cwc_members_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add_new_cwc_member :{
                handleAddNewCwcMemberClick();
                return true;
            }
            case android.R.id.home :{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleAddNewCwcMemberClick(){
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        if(AuthenticatingUtils.isUserLoggedIn(mCurrentUser)) {
            mRdbReference.child(getString(R.string.json_object_users))
                    .child(mCurrentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        boolean admin_access = dataSnapshot.child("admin_access").getValue(Boolean.class);
                        if(admin_access){
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Intent i = new Intent(CwcMembersActivity.this, AddCwcMemberActivity.class);
                            startActivity(i);
                        }else {
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(
                                    CwcMembersActivity.this,
                                    "Uh oh! Only few member have access to this.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(
                            CwcMembersActivity.this,
                            "Some Error happened",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
        else {
            pbLoadingIndicator.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    CwcMembersActivity.this,
                    "This action requires you to login",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

}
