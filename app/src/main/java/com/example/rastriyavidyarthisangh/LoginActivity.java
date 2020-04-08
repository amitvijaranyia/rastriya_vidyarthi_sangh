package com.example.rastriyavidyarthisangh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "tag l";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mRdb;
    private DatabaseReference mRdbRefrence;
    private DatabaseReference usersReference;

    private String codeSent;

    EditText etPhoneNumber, etCodeSent;
    Button btnLogin, btnNotRegistered;
    ProgressBar pbLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRdb = FirebaseDatabase.getInstance();
        mRdbRefrence = mRdb.getReference();

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etCodeSent = findViewById(R.id.etCodeSent);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);
        btnNotRegistered = findViewById(R.id.btnNotRegistered);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogin.getText().toString().equalsIgnoreCase(getString(R.string.btn_text_login))) {
                    if (TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())
                            || etPhoneNumber.getText().toString().trim().length() != 10) {
                        etPhoneNumber.setError(getString(R.string.empty_phone_number_error));
                    } else {
                        pbLoadingIndicator.setVisibility(View.VISIBLE);
                        usersReference = mRdbRefrence.child(getString(R.string.json_object_users));
                        String currentNumber = "+91" + etPhoneNumber.getText().toString().trim();
                        usersReference.orderByKey().equalTo(currentNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    pbLoadingIndicator.setVisibility(View.VISIBLE);
                                    btnLogin.setText(getString(R.string.btn_text_verifying));
                                    sendVerificationCode(etPhoneNumber.getText().toString().trim());
                                } else {
                                    Toast.makeText(LoginActivity.this, "No account exist with this Number",
                                            Toast.LENGTH_SHORT).show();
                                    pbLoadingIndicator.setVisibility(View.INVISIBLE);
                                    btnLogin.setText(getString(R.string.btn_text_login));
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                handleOnFailureLogin();
                            }
                        });
                    }
                }
                else if(btnLogin.getText().toString().trim().equalsIgnoreCase(getString(R.string.btn_text_click_to_verify_otp))){
                    if(TextUtils.isEmpty(etCodeSent.getText().toString().trim())){
                        etCodeSent.setError("Invalid format.");
                    }else {
                        verifySignInCodeSent();
                        pbLoadingIndicator.setVisibility(View.VISIBLE);
                        btnLogin.setText(getString(R.string.btn_text_verifying));
                    }
                }
            }
        });

        btnNotRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, JoinRvsActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void sendVerificationCode(String phoneNumber){
        Log.d(TAG, "sendVerificationCode: " + "sending verification code" + phoneNumber);
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
            handleOnFailureLogin();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
            etCodeSent.setVisibility(View.VISIBLE);
            pbLoadingIndicator.setVisibility(View.INVISIBLE);
            btnLogin.setText(getString(R.string.btn_text_click_to_verify_otp));
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Log.d(TAG, "signIn:success");
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Successfully signed in",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();

                        } else {
//                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                Toast.makeText(
//                                        LoginActivity.this,
//                                        "signIn:failure",
//                                        Toast.LENGTH_SHORT
//                                ).show();
                                handleOnFailureLogin();
                            }
                        }
                    }
                });
    }

    private void handleOnFailureLogin(){
        pbLoadingIndicator.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Some error happened.", Toast.LENGTH_SHORT).show();
        btnLogin.setText(getString(R.string.btn_text_login));
    }

}
