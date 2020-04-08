package com.example.rastriyavidyarthisangh;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rastriyavidyarthisangh.Adapters.AdapterEvents;
import com.example.rastriyavidyarthisangh.FirebaseUtils.AuthenticatingUtils;
import com.example.rastriyavidyarthisangh.POJO.SingleEventPojo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterEvents.AdapterEventsOnClickHandler {
    private static final String TAG = "tag m";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private RecyclerView rvEvents;
    private ProgressBar pbLoadingIndicator;
    private AdapterEvents mEventsAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mRdb;
    private DatabaseReference mRdbReference;
    private DatabaseReference mEventsReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialising FireBase related stuffs
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mEventsReference = mRdbReference.child(getString(R.string.json_object_events));

        drawerLayout = findViewById(R.id.activity_main);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvEvents = findViewById(R.id.rvEvents);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);
        navigationView = findViewById(R.id.navigation_view);

        mEventsAdapter = new AdapterEvents(this, new ArrayList<SingleEventPojo>(), mRdbReference, this);
        setUpEventsView();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
                switch (menuItemId) {
                    case R.id.navAboutUs: {
                        handleNotProvidedFeatures();
                        break;
                    }
                    case R.id.navPhotos: {
                        handlePhotosClick();
                        break;
                    }
                    case R.id.navRulesAndRegulations: {
                        int a = 2;
                        handleNotProvidedFeatures();
                        break;
                    }
                    case R.id.navCwcMembers :{
                        Intent i = new Intent(MainActivity.this, CwcMembersActivity.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.navGroupMembers :{
                        Intent i = new Intent(MainActivity.this, GroupMembersActivity.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.navWhatsAppJoin: {
                        joinRVSWhatsAppGroup();
                        break;
                    }
                    case R.id.navFaceBookPage: {
                        openRVSFacebookPage();
                        break;
                    }
                    case R.id.navMapUs: {
                        openRVSLocation();
                        break;
                    }
                    case R.id.navEmailUs: {
                        sentMailToRSV();
                        break;
                    }
                    case R.id.navCallUs: {
                        contactRSV();
                        break;
                    }
                    case R.id.navJoinRvs :{
                        handleJoinRvsClick();
                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentUser = mAuth.getCurrentUser();
        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, @Nullable String s) {
                    SingleEventPojo singleEvent = dataSnapshot.getValue(SingleEventPojo.class);
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    mEventsAdapter.addSingleEvent(singleEvent);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, @Nullable String s) {

                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    SingleEventPojo singleEvent = dataSnapshot.getValue(SingleEventPojo.class);
                    String key = dataSnapshot.getKey();
                    Log.d(TAG, "onChildRemoved: " + "key = " + key);
                    mEventsAdapter.removeSingleEvent(singleEvent);
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, @Nullable String s) {

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mEventsReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_sign_in :{
                handleSignInClick();
                return true;
            }
            case R.id.menu_sign_out :{
                handleSignOutClick();
                return true;
            }
            case R.id.menu_add_new_event :{
                handleAddNewEventClick();
                return true;
            }
        }
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //fires an intent to join whatsApp group of rastriya vidyarthi sangh
    private void joinRVSWhatsAppGroup() {
        Intent intentWhatsAppGroupJoin = new Intent(Intent.ACTION_VIEW);
        String url = "https://chat.whatsapp.com/FhTvBqHZyiW4IgOFJoUyN2";
        intentWhatsAppGroupJoin.setData(Uri.parse(url));
        intentWhatsAppGroupJoin.setPackage("com.whatsapp");
        startActivity(intentWhatsAppGroupJoin);
    }

    //fires an intent to join facebook page of rastriya vidyarthi sangh
    private void openRVSFacebookPage() {
        String facebookPageID = "Rastriya-Janta-Party-Nepal-Student-Organisation-india-401532800308917/";

        // URL
        String facebookUrl = "https://www.facebook.com/" + facebookPageID;

        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.facebook.katana", 0);

            if (info.enabled) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
            else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

    //fires an intent to open location of rastriya vidyarthi sangh
    private void openRVSLocation(){
        String address = "A-32 street no 2, Laxminagar New Delhi 110091, India";
        String map = "http://maps.google.co.in/maps?q=" + address;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(intent);
    }

    //fires an intent to email to rastriya vidyarthi sangh
    private void sentMailToRSV(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("mailto:rvsangh2017@gmail.com");
        i.setData(uri);
        startActivity(i);
    }

    //fires an intent to contact rastriya vidyarthi sangh
    private void contactRSV(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.drawable.ic_nav_call);
        builder.setTitle(R.string.nav_call_us);

        String[] contacts = new String[]{"Avinash Jha", "Dhiraj Mahato"};

        builder.setItems(contacts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0 :{
                        intentToContactRSV(getString(R.string.call_avinash_jha));
                        break;
                    }
                    case 1 :{
                        intentToContactRSV(getString(R.string.call_dhiraj_mahato));
                        break;
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //contactRSV helper
    private void intentToContactRSV(String contactNumber){
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("tel:" + contactNumber);
        i.setData(uri);
        startActivity(i);
    }

    //handling Join Rvs Click
    private void handleJoinRvsClick(){
        boolean isUserLoggedIn = AuthenticatingUtils.isUserLoggedIn(mCurrentUser);
        if(isUserLoggedIn){
            Snackbar.make(
                    navigationView,
                    "Hurrah!! You're already RVS's member.",
                    Snackbar.LENGTH_SHORT
            ).show();
        }
        else{
            Intent i = new Intent(MainActivity.this, JoinRvsActivity.class);
            startActivity(i);
        }
    }

    //handling Sign in Click
    private void handleSignInClick(){
        boolean isUserLoggedIn = AuthenticatingUtils.isUserLoggedIn(mCurrentUser);
        if(isUserLoggedIn){
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }
    }

    //handling sign out click
    private void handleSignOutClick(){
        mAuth.signOut();
        mCurrentUser = null;
        Toast.makeText(MainActivity.this, "Successfully signed out", Toast.LENGTH_SHORT).show();
    }

    private void setUpEventsView(){
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvEvents.setLayoutManager(lm);
        rvEvents.setAdapter(mEventsAdapter);
    }

    private void handleAddNewEventClick(){
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        if(AuthenticatingUtils.isUserLoggedIn(mCurrentUser)) {
            mRdbReference.child(getString(R.string.json_object_users))
                    .child(mCurrentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        boolean admin_access = dataSnapshot.child("admin_access").getValue(Boolean.class);
                        if(admin_access){
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Intent i = new Intent(MainActivity.this, AddNewEventActivity.class);
                            i.putExtra(getString(R.string.intent_current_user_phone_number), mCurrentUser.getPhoneNumber());
                            startActivity(i);
                        }else {
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(
                                    MainActivity.this,
                                    "Uh oh! Only few member have access to this.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(
                            MainActivity.this,
                            "Some Error happened",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
        else {
            pbLoadingIndicator.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    this,
                    "This action requires you to be signed in.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onClick(final String idOfEvent) {
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        if(AuthenticatingUtils.isUserLoggedIn(mCurrentUser) && mCurrentUser.getPhoneNumber() != null) {
            mRdbReference.child(getString(R.string.json_object_users))
                    .child(mCurrentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        boolean admin_access = dataSnapshot.child("admin_access").getValue(Boolean.class);
                        if(admin_access){
                            deleteEventFromRdb(idOfEvent);
                            deleteEventPhotoFromStorage(idOfEvent);
                        }else {
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(
                                    MainActivity.this,
                                    "Uh oh! Only few member have access to this feature.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(
                            MainActivity.this,
                            "Some Error happened",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
        else {
            pbLoadingIndicator.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    this,
                    "This action requires you to be signed in.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void deleteEventFromRdb(String idOfEvent){
        DatabaseReference referenceToSpecificEvent = mRdbReference
                .child(getString(R.string.json_object_events))
                .child(idOfEvent);
        referenceToSpecificEvent.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        MainActivity.this,
                        "Event Deleted Successfully",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Error while deleting the event",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void deleteEventPhotoFromStorage(String idOfEvent){
        StorageReference referenceToSpecificEventPhoto = mStorageReference
                .child(getString(R.string.json_object_events_photos))
                .child(idOfEvent);
        referenceToSpecificEventPhoto.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        MainActivity.this,
                        "Event Photo Deleted Successfully",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Error while deleting Event Photo",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void handlePhotosClick(){
        Intent i = new Intent(MainActivity.this, PhotosActivity.class);
        startActivity(i);
    }

    private void handleNotProvidedFeatures(){
        Snackbar.make(
                navigationView,
                "This information is not provided by the app owner yet.",
                Snackbar.LENGTH_LONG
        ).show();
    }
}
