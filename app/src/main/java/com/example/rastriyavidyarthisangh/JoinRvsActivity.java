package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rastriyavidyarthisangh.POJO.SingleGroupMember;
import com.example.rastriyavidyarthisangh.Utils.DateTimeUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinRvsActivity extends AppCompatActivity {
    private static final String TAG = "tag j";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mRdb;
    private DatabaseReference mRdbReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private DatabaseReference usersReference;
    private DatabaseReference groupMembersReference;

    private StorageReference groupMemberPhotosReference;


    EditText etName, etCollege, etPhoneNumber, etCodeSent;
    Button btnPickImage, btnJoin, btnAlreadyMember, btnVerify;
    CircleImageView ivProfilePicture;
    ProgressBar pbLoadingIndicator;

    private Uri mImageUri;
    private static final int RC_PICK_PHOTO = 0;

    private String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_rvs);

        etName = findViewById(R.id.etName);
        etCollege = findViewById(R.id.etCollege);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etCodeSent = findViewById(R.id.etCodeSent);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnJoin = findViewById(R.id.btnJoin);
        btnAlreadyMember = findViewById(R.id.btnAlreadyMember);
        btnVerify = findViewById(R.id.btnVerifyNumber);
        ivProfilePicture = findViewById(R.id.ivImage);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);

        //Instantiate FireBase variables
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRdb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mRdbReference = mRdb.getReference();
        mStorageReference = mStorage.getReference();

        if(mCurrentUser != null){
            Toast.makeText(this, "You're already logged in.", Toast.LENGTH_LONG).show();
        }
        else{
            btnPickImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PICK_PHOTO);
                }
            });

            btnJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(etName.getText().toString().trim())){
                        etName.setError(getString(R.string.empty_name_error));
                    }
                    else if(TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())
                            || etPhoneNumber.getText().toString().trim().length() != 10){
                        etPhoneNumber.setError(getString(R.string.empty_phone_number_error));
                    }

                    else{
                        pbLoadingIndicator.setVisibility(View.VISIBLE);
                        usersReference = mRdbReference.child(getString(R.string.json_object_users));
                        String currentPhoneNumber = "+91"+etPhoneNumber.getText().toString().trim();
                        usersReference.orderByKey().equalTo(currentPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null){
                                    Toast.makeText(JoinRvsActivity.this, "User with this number already exists",
                                            Toast.LENGTH_SHORT).show();
                                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                                }
                                else{
                                    pbLoadingIndicator.setVisibility(View.VISIBLE);
                                    btnJoin.setText(getString(R.string.btn_text_verifying));
                                    sendVerificationCode(etPhoneNumber.getText().toString().trim());
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                handleJoiningFailed();
//                                Log.d(TAG, "onCancelled: " + "inside order by key cancelled");
                            }
                        });
                    }
                }
            });

            btnVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifySignInCodeSent();
                }
            });

            btnAlreadyMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(JoinRvsActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PICK_PHOTO && resultCode == RESULT_OK){
            if(data != null) {
                mImageUri = data.getData();
                Glide.with(this).load(mImageUri).into(ivProfilePicture);
            }
        }
    }

    private void sendVerificationCode(String phoneNumber){
//        Log.d(TAG, "sendVerificationCode: " + "sending verification code" + phoneNumber);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void verifySignInCodeSent(){
        String code = etCodeSent.getText().toString().trim();
        if(TextUtils.isEmpty(code))
            return;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            handleJoiningFailed();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
            etCodeSent.setVisibility(View.VISIBLE);
            btnVerify.setVisibility(View.VISIBLE);
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Log.d(TAG, "signIn:success");
                            addNewUser();
                            addNewGroupMember();
                        } else {
//                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                Toast.makeText(
//                                        JoinRvsActivity.this,
//                                        "signIn:failure",
//                                        Toast.LENGTH_SHORT
//                                ).show();
                                handleJoiningFailed();
                            }
                        }
                    }
                });
    }

    private void addNewUser(){
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser != null) {
            String currentUserPhoneNumber = mCurrentUser.getPhoneNumber();
            if(currentUserPhoneNumber != null) {
                DatabaseReference specificUserRef = usersReference.child(currentUserPhoneNumber);
                specificUserRef.child(getString(R.string.json_object_admin_access)).setValue(false);
            }
        }
    }

    private void addNewGroupMember(){
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser != null) {
            final String currentUserPhoneNumber = mCurrentUser.getPhoneNumber();
            if(currentUserPhoneNumber != null) {
                if(mImageUri != null) {
                    groupMemberPhotosReference = mStorageReference.child(getString(R.string.json_object_group_members_photos));
                    final StorageReference specificMemberPhotoReference = groupMemberPhotosReference.child(currentUserPhoneNumber);
                    UploadTask task = specificMemberPhotoReference.putFile(mImageUri);
                    Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful()){
                                if(task.getException() != null)
                                    throw task.getException();
                            }
                            return specificMemberPhotoReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Uri profileImageUri = task.getResult();
                                if(profileImageUri != null) {
                                    addMemberIntoRdb(profileImageUri.toString(), currentUserPhoneNumber);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            handleJoiningFailed();
                        }
                    });
                }else {
                    addMemberIntoRdb(null, currentUserPhoneNumber);
                }
            }
        }
    }

    private void addMemberIntoRdb(String profileImageUri, String phoneNumber){
        String name = etName.getText().toString().trim();
        String college = etCollege.getText().toString().trim();
        String date_joined = DateTimeUtils.getCurrentDateAndTime();

        SingleGroupMember singleGroupMember = new SingleGroupMember(
                name,
                college,
                profileImageUri,
                phoneNumber,
                date_joined
        );

        groupMembersReference = mRdbReference.child(getString(R.string.json_object_group_members));
        DatabaseReference specificMemberReference = groupMembersReference.child(phoneNumber);

        specificMemberReference.setValue(singleGroupMember).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(
                            JoinRvsActivity.this,
                            "Congratulations! Successfully joined.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleJoiningFailed();
            }
        });
    }

    private void handleJoiningFailed(){
        Toast.makeText(
                JoinRvsActivity.this,
                "Some error happened.",
                Toast.LENGTH_LONG
        ).show();
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        btnJoin.setText(getString(R.string.btn_text_join_us));
    }
}
