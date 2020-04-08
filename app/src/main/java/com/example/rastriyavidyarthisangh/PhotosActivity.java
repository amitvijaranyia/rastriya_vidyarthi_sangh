package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.Adapters.AdapterPhotos;
import com.example.rastriyavidyarthisangh.FirebaseUtils.AuthenticatingUtils;
import com.example.rastriyavidyarthisangh.POJO.SinglePhotoPojo;
import com.example.rastriyavidyarthisangh.TouchUtils.OnSwipeTouchListener;
import com.example.rastriyavidyarthisangh.Utils.DateTimeUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class PhotosActivity extends AppCompatActivity implements AdapterPhotos.PhotoOnClickHandler {
    private static final String TAG = "tag ph";

    RecyclerView rvPhotosList;
    ProgressBar pbLoadingIndicator;
    PhotoView photoView;

    private String mPhoneNumber;
    private String idOfPhoto;
    private static final int RC_PHOTO_PICK = 0;
    private Uri imageUri;

    AdapterPhotos mPhotosAdapter;

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    FirebaseDatabase mRdb;
    DatabaseReference mRdbReference;
    FirebaseStorage mStorage;
    StorageReference mStorageReference;

    DatabaseReference mPhotosRdbReference;
    StorageReference mPhotoStorageReference;

    ChildEventListener mPhotosChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mPhotosRdbReference = mRdbReference.child(getString(R.string.json_object_photos));
        mPhotoStorageReference = mStorageReference.child(getString(R.string.json_object_photos_storage));

        rvPhotosList = findViewById(R.id.rvPhotoList);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);
        photoView = findViewById(R.id.photo_view);

        GridLayoutManager lm = new GridLayoutManager(this, 2);
        mPhotosAdapter = new AdapterPhotos(this, new ArrayList<SinglePhotoPojo>(), this);

        rvPhotosList.setLayoutManager(lm);
        rvPhotosList.setAdapter(mPhotosAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentUser = mAuth.getCurrentUser();
        if(mPhotosChildEventListener == null){
            mPhotosChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                    SinglePhotoPojo singlePhoto = dataSnapshot.getValue(SinglePhotoPojo.class);
                    mPhotosAdapter.addSinglePhotoPojo(singlePhoto);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    SinglePhotoPojo singlePhoto = dataSnapshot.getValue(SinglePhotoPojo.class);
                    mPhotosAdapter.removeSinglephoto(singlePhoto);
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mPhotosRdbReference.addChildEventListener(mPhotosChildEventListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_photos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add_new_photo :{
                handleOnAddPhotoClick();
                return true;
            }
            case android.R.id.home :{
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleOnAddPhotoClick(){
        pbLoadingIndicator.setVisibility(View.VISIBLE);
        if(AuthenticatingUtils.isUserLoggedIn(mCurrentUser) && mCurrentUser.getPhoneNumber() != null) {
            mPhoneNumber = mCurrentUser.getPhoneNumber();
            mRdbReference.child(getString(R.string.json_object_users))
                    .child(mCurrentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        boolean admin_access = dataSnapshot.child("admin_access").getValue(Boolean.class);
                        if(admin_access){
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/jpeg");
                            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICK);
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                        }else {
                            pbLoadingIndicator.setVisibility(View.INVISIBLE);
                            Toast.makeText(
                                    PhotosActivity.this,
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
                            PhotosActivity.this,
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICK && resultCode == RESULT_OK){
            if(data != null){
                pbLoadingIndicator.setVisibility(View.VISIBLE);
                imageUri = data.getData();
                if(imageUri != null){
                    uploadPhotoToStorage();
                }else {
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                }
            }
            else {
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "No photo selected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPhotoToStorage(){
        final long time_posted = DateTimeUtils.getCurrentTimeInMilliSeconds();
        idOfPhoto = time_posted+mPhoneNumber;
        final StorageReference referenceToSpecificPhoto = mPhotoStorageReference
                .child(idOfPhoto);
        UploadTask task = referenceToSpecificPhoto.putFile(imageUri);
        Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    if(task.getException() != null){
                        throw task.getException();
                    }
                }
                return referenceToSpecificPhoto.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri photo_url = task.getResult();
                    if(photo_url != null){
                        uploadPhotoDataIntoRdb(photo_url.toString(), time_posted);
                        Toast.makeText(
                                PhotosActivity.this,
                                "Photo successfully uploaded into storage",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                else {
                    Toast.makeText(
                            PhotosActivity.this,
                            "Some error happened.",
                            Toast.LENGTH_SHORT
                    ).show();
                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void uploadPhotoDataIntoRdb(String photo_url, long time_posted){
        DatabaseReference referenceToSpecificPhoto = mPhotosRdbReference
                .child(idOfPhoto);
        SinglePhotoPojo singlePhoto = new SinglePhotoPojo(
                photo_url,
                time_posted,
                mPhoneNumber
        );

        referenceToSpecificPhoto.setValue(singlePhoto).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Photo successfully uploaded.",
                        Toast.LENGTH_SHORT
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Some error happened.",
                        Toast.LENGTH_SHORT
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(final List<SinglePhotoPojo> photoPojoList, int position) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putInt("position", position).apply();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PhotosActivity.this);
        final int maxPhotos = photoPojoList.size();
        String photo_url = photoPojoList.get(position).getPhoto_url();
        photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(photo_url).into(photoView);

        photoView.setOnTouchListener(new OnSwipeTouchListener(PhotosActivity.this){
            @Override
            public void onSwipeRight() {
                int currentPosition = preferences.getInt("position", 0);
                int nextPosition = currentPosition-1;
                if(nextPosition >= 0 && nextPosition < maxPhotos){
                    String photo_url = photoPojoList.get(nextPosition).getPhoto_url();
                    Glide.with(PhotosActivity.this).load(photo_url).into(photoView);
                    PreferenceManager.getDefaultSharedPreferences(PhotosActivity.this)
                            .edit().putInt("position", nextPosition).apply();
                }
            }
            @Override
            public void onSwipeLeft() {
                int currentPosition = preferences.getInt("position", 0);
                int nextPosition = currentPosition+1;
                if(nextPosition >= 0 && nextPosition < maxPhotos){
                    String photo_url = photoPojoList.get(nextPosition).getPhoto_url();
                    Glide.with(PhotosActivity.this).load(photo_url).into(photoView);
                    PreferenceManager.getDefaultSharedPreferences(PhotosActivity.this)
                            .edit().putInt("position", nextPosition).apply();
                }
            }
            @Override
            public void onSwipeBottom() {
                onBackPressed();
            }

            @Override
            public void onSwipeTop() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onLongClick(final String idOfPhoto) {
        if(!TextUtils.isEmpty(idOfPhoto)){
            pbLoadingIndicator.setVisibility(View.VISIBLE);
            if(AuthenticatingUtils.isUserLoggedIn(mCurrentUser) && mCurrentUser.getPhoneNumber() != null) {
                mRdbReference.child(getString(R.string.json_object_users))
                        .child(mCurrentUser.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null){
                            boolean admin_access = dataSnapshot.child("admin_access").getValue(Boolean.class);
                            if(admin_access){
                                deleteEventFromRdb(idOfPhoto);
                                deleteEventPhotoFromStorage(idOfPhoto);
                            }else {
                                pbLoadingIndicator.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        PhotosActivity.this,
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
                                PhotosActivity.this,
                                "Some Error happened",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
            else {
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(
                        PhotosActivity.this,
                        "This action requires you to be signed in.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void deleteEventFromRdb(String idOfEvent){
        DatabaseReference referenceToSpecificPhoto = mPhotosRdbReference.child(idOfEvent);

        referenceToSpecificPhoto.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Photo Deleted Successfully",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Error while deleting the photo",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void deleteEventPhotoFromStorage(String idOfEvent){
        StorageReference referenceToSpecificPhotoStorage = mPhotoStorageReference.child(idOfEvent);

        referenceToSpecificPhotoStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Photo Deleted Successfully from storage.",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        PhotosActivity.this,
                        "Error while deleting Photo from storage.",
                        Toast.LENGTH_LONG
                ).show();
                pbLoadingIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(photoView.getVisibility() == View.GONE){
            super.onBackPressed();
        }else {
            photoView.setVisibility(View.GONE);
        }
    }
}
