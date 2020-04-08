package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rastriyavidyarthisangh.POJO.SingleEventPojo;
import com.example.rastriyavidyarthisangh.Utils.DateTimeUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddNewEventActivity extends AppCompatActivity {
    private static final String TAG = "tag ae";

    private FirebaseDatabase mRdb;
    private DatabaseReference mRdbReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    EditText etName, etDescription;
    ImageView ivImageToBeUploaded;
    Button btnPickPhoto, btnUpload;
    ProgressBar pbLoadingIndicator;

    private String mPhoneNumber;
    private Uri imageUri;
    private static final int RC_PICK_PHOTO = 0;

    private String idOfEvent;
    private long timePosted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        Intent intentThatStartedThis = getIntent();
        if(intentThatStartedThis.hasExtra(getString(R.string.intent_current_user_phone_number))){
            mPhoneNumber = intentThatStartedThis.getStringExtra(getString(R.string.intent_current_user_phone_number));
        }

        etName = findViewById(R.id.etNameWhoPosted);
        etDescription = findViewById(R.id.etDescription);
        ivImageToBeUploaded = findViewById(R.id.ivImageToBeUploaded);
        btnPickPhoto = findViewById(R.id.btnPickImage);
        btnUpload = findViewById(R.id.btnUpload);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);

        btnPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PICK_PHOTO);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etName.getText().toString().trim())) {
                    timePosted = DateTimeUtils.getCurrentTimeInMilliSeconds();
                    idOfEvent = timePosted + mPhoneNumber;
                    btnUpload.setText(getString(R.string.btn_text_uploading_event));
                    pbLoadingIndicator.setVisibility(View.VISIBLE);
                    uploadEvent();
                }
                else {
                    timePosted = DateTimeUtils.getCurrentTimeInMilliSeconds();
                    idOfEvent = timePosted + mPhoneNumber;
                    btnUpload.setText(getString(R.string.btn_text_uploading_event));
                    pbLoadingIndicator.setVisibility(View.VISIBLE);
                    uploadEventHelper(etName.getText().toString().trim());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PICK_PHOTO && resultCode == RESULT_OK){
            if(data != null){
                imageUri = data.getData();
                ivImageToBeUploaded.setImageURI(imageUri);
            }
        }
    }

    private void uploadEvent(){
        DatabaseReference cwc_members_ref = mRdbReference
                .child(getString(R.string.json_object_cwc_members))
                .child(mPhoneNumber)
                .child("name");
        cwc_members_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String whoPosted = dataSnapshot.getValue(String.class);
                if(TextUtils.isEmpty(whoPosted)){
                    handleDetailsNotFound();
//                    whoPosted = etName.getText().toString().trim();
//                    if(!TextUtils.isEmpty(whoPosted)) {
//                        uploadEventHelper(whoPosted);
//                    }
                }else {
                    uploadEventHelper(whoPosted);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDetailsNotFound();
                onEventUploadFailure();
            }
        });
    }

    private void uploadEventHelper(String whoPosted){
        etName.setVisibility(View.VISIBLE);
        etName.setText(whoPosted);
        if (imageUri != null) {
            uploadImageOfEvent(whoPosted);
        } else {
            uploadEventIntoRdb(whoPosted, null);
        }
    }

    private void uploadImageOfEvent(final String whoPosted){
        final StorageReference eventPhotoRef = mStorageReference
                .child(getString(R.string.json_object_events_photos))
                .child(idOfEvent);
        UploadTask task = eventPhotoRef.putFile(imageUri);
        Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    if(task.getException() != null){
                        throw task.getException();
                    }
                }
                return eventPhotoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri event_photo_url = task.getResult();
                    uploadEventIntoRdb(whoPosted, event_photo_url.toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onEventUploadFailure();
            }
        });
    }

    private void uploadEventIntoRdb(String whoPosted, String event_photo_url){
        String description = etDescription.getText().toString().trim();
        SingleEventPojo singleEventPojo = new SingleEventPojo(
                whoPosted,
                timePosted,
                mPhoneNumber,
                description,
                event_photo_url
        );

        DatabaseReference specificEventRef = mRdbReference
                .child(getString(R.string.json_object_events))
                .child(idOfEvent);
        specificEventRef.setValue(singleEventPojo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AddNewEventActivity.this, "New Event Uploaded",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onEventUploadFailure();
            }
        });
    }

    private void onEventUploadFailure(){
        Toast.makeText(
                AddNewEventActivity.this,
                "Some error happened while uploading the event",
                Toast.LENGTH_LONG
        ).show();
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        btnUpload.setText(getString(R.string.btn_text_upload_event));
    }

    private void handleDetailsNotFound(){
        Toast.makeText(
                AddNewEventActivity.this,
                "Couldn't Get your Details. Please Enter your Name",
                Toast.LENGTH_LONG
        ).show();
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        btnUpload.setText(getString(R.string.btn_text_upload_event));
        etName.setVisibility(View.VISIBLE);
    }
}
