package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.rastriyavidyarthisangh.Adapters.AdapterGroupMembersList;
import com.example.rastriyavidyarthisangh.POJO.SingleGroupMember;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GroupMembersActivity extends AppCompatActivity {
    private static final String TAG = "tag g";

    RecyclerView rvGroupMembersList;
    AdapterGroupMembersList groupMembersListAdapter;
    ProgressBar pbLoadingIndicator;

    FirebaseDatabase mRdb;
    DatabaseReference mRdbReference;
    ChildEventListener mGroupMemberChildListener;

    DatabaseReference groupMembersReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();

        groupMembersReference = mRdbReference.child(getString(R.string.json_object_group_members));

        rvGroupMembersList = findViewById(R.id.rvGroupMembersList);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);

        groupMembersListAdapter = new AdapterGroupMembersList(this, new ArrayList<SingleGroupMember>());
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvGroupMembersList.setLayoutManager(lm);
        rvGroupMembersList.setAdapter(groupMembersListAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGroupMemberChildListener == null){
            mGroupMemberChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    SingleGroupMember singleGroupMember = dataSnapshot.getValue(SingleGroupMember.class);
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    groupMembersListAdapter.addSingleGroupMember(singleGroupMember);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            groupMembersReference.addChildEventListener(mGroupMemberChildListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
