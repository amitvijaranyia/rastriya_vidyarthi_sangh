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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SingleCwcMember;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddCwcMemberActivity extends AppCompatActivity {
    private static final String TAG = "tag add";

    EditText etName, etDesignation, etPhoneNumber, etEmailId, etFacebookId;
    Button btnPickPhoto, btnAddNewCwcMember;
    CircleImageView ivProfilePicture;
    ProgressBar pbLoadingIndicator;

    private Uri imageUri;
    private String mPhoneNumber, mName, mDesignation, mEmailId, mFacebookId;

    private final int RC_PICK_PHOTO = 0;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase mRdb;
    DatabaseReference mRdbReference;
    FirebaseStorage mStorage;
    StorageReference mStorageReference;

    DatabaseReference mCwcMembersReference;
    StorageReference mCwcMembersPhotosReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cwc_member);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRdb = FirebaseDatabase.getInstance();
        mRdbReference = mRdb.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mCwcMembersReference = mRdbReference.child(getString(R.string.json_object_cwc_members));
        mCwcMembersPhotosReference = mStorageReference.child(getString(R.string.json_object_cwc_members_photos));

        etName = findViewById(R.id.etName);
        etDesignation = findViewById(R.id.etDesignation);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmailId = findViewById(R.id.etEmailId);
        etFacebookId = findViewById(R.id.etFacebookId);
        btnPickPhoto = findViewById(R.id.btnPickImage);
        btnAddNewCwcMember = findViewById(R.id.btnAddNewCwcMember);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);

        btnPickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(i, "Complete Action Using"), RC_PICK_PHOTO);
            }
        });

        btnAddNewCwcMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etName.getText().toString().trim())){
                    etName.setError(getString(R.string.empty_name_error));
                }else if(TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())){
                    etPhoneNumber.setError(getString(R.string.empty_phone_number_error));
                }else {
                    mName = etName.getText().toString().trim();
                    mDesignation = etDesignation.getText().toString().trim();
                    mPhoneNumber = "+91"+etPhoneNumber.getText().toString().trim();
                    mEmailId = etEmailId.getText().toString().trim();
                    mFacebookId = etFacebookId.getText().toString().trim();
                    pbLoadingIndicator.setVisibility(View.VISIBLE);
                    btnAddNewCwcMember.setText(getString(R.string.btn_text_adding_new_member));

                    mCwcMembersReference.orderByKey().equalTo(mPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null){
                                Toast.makeText(AddCwcMemberActivity.this, "Member with this Phone Number already exists!",
                                        Toast.LENGTH_SHORT).show();
                                pbLoadingIndicator.setVisibility(View.GONE);
                                btnAddNewCwcMember.setText(getString(R.string.btn_text_adding_new_member));
                            }else {
                                addNewCwcMember();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(AddCwcMemberActivity.this, "Unknown error occurred. Please try again",
                                    Toast.LENGTH_SHORT).show();
                            pbLoadingIndicator.setVisibility(View.GONE);
                            btnAddNewCwcMember.setText(getString(R.string.btn_text_adding_new_member));
                        }
                    });
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
                Glide.with(this).load(imageUri).into(ivProfilePicture);
            }
        }
    }

    private void addNewCwcMember(){
        if(imageUri != null){
            final StorageReference specificCwcMemberReference = mCwcMembersPhotosReference.child(mPhoneNumber);
            UploadTask task = specificCwcMemberReference.putFile(imageUri);
            Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        if(task.getException() != null){
                            throw task.getException();
                        }
                    }
                    return specificCwcMemberReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri profileImageUri = task.getResult();
                        if(profileImageUri != null) {
                            addNewMemberIntoRdb(profileImageUri.toString());
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddCwcMemberActivity.this, "Failed to add new member",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }else {
            addNewMemberIntoRdb(null);
        }
    }

    private void addNewMemberIntoRdb(String profileImageUrl){
        SingleCwcMember singleCwcMember = new SingleCwcMember(
                mName,
                mDesignation,
                mPhoneNumber,
                mEmailId,
                mFacebookId,
                profileImageUrl,
                20
        );

        DatabaseReference specificCwcMemberReference = mCwcMembersReference.child(mPhoneNumber);
        specificCwcMemberReference.setValue(singleCwcMember).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(AddCwcMemberActivity.this, "New Cwc Member Successfully saved",
                            Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddCwcMemberActivity.this, "Failed to save new Cwc Member",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
