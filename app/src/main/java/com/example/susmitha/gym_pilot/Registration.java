package com.example.susmitha.gym_pilot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class Registration extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDataRef;
    private FirebaseUser mFirebaseUser;
    private RelativeLayout mDetailsLayout;
    private RelativeLayout mVerifyOTPLayout;
    private ImageButton mCloseButton;
    private AppCompatEditText mRegName;
    private AppCompatEditText mRegEmail;
    private AppCompatEditText mRegPassword;
    private AppCompatEditText mRegMobile;
    private AppCompatEditText mRegOTP;
    private String mOTPCode;
    private String mRegNameText;
    private String mRegEmailText;
    private String mRegPasswordText;
    private String mRegMobileText;
    private boolean isMobileVerified;
    private MaterialButton mRegButton;
    private MaterialButton mRegOTPButton;
    private View.OnClickListener mRegistrationBtnClickListener;


    private void init(){
        //Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDataRef = mDatabase.getReference();

        //initialize UI elements
        mDetailsLayout = findViewById(R.id.detailsLayout);
        mVerifyOTPLayout = findViewById(R.id.verifyOTPlayout);
        mCloseButton = findViewById(R.id.close_btn_reg);
        mRegOTPButton = findViewById(R.id.otp_btn);
        mRegName = findViewById(R.id.name_reg_edit_text);
        mRegEmail = findViewById(R.id.email_reg_edit_text);
        mRegPassword = findViewById(R.id.password_reg_edit_text);
        mRegButton = findViewById(R.id.reg_btn);
        mRegMobile = findViewById(R.id.phone_reg_edit_text);
        mRegOTP = findViewById(R.id.otp_reg_edit_text);
        isMobileVerified=false;


        mRegistrationBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: handle special characters in email since it crashes
                initStringsFromEditText();

                if (!checkifNull(mRegEmailText, mRegPasswordText)) {

                    //Verify Email Address
                    //  verifyEmailAdress(mAuth.getCurrentUser());

                    //Verify Mobile Number
                    if(!mRegMobileText.isEmpty() && mRegMobileText.length()==10)
                    verifyMobileNumber(mRegMobileText);
                    else
                        Toast.makeText(getApplicationContext(),"enter Valid mobile number",Toast.LENGTH_LONG).show();

                }else{
                    /*show something for null enetered email & pswd*/
                    }
            }
        };

        mRegOTPButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    if(mRegOTP.getText().toString().equals(mOTPCode)){
                        Toast.makeText(getApplicationContext(),"yusss",Toast.LENGTH_LONG).show();

                        //creating and adding the user into database
                        createUserWithEmailAndPassword(mRegEmailText, mRegPasswordText);
                        addUserinDatabase(mRegNameText,mRegEmailText,mRegMobileText);

                    }else{
                        Toast.makeText(getApplicationContext(),"lol ayyav",Toast.LENGTH_LONG).show();
                    }

            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSignupScreen();
            }
        });

    }

    private void signInwith(String mRegEmailText, String mRegPasswordText) {
        mAuth.signInWithEmailAndPassword(mRegEmailText,mRegPasswordText)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("sathu_reg", "signInWithEmail:success"+mAuth.getCurrentUser().getEmail());
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("sathu_reg", "signInWithEmail:failure", task.getException());
                            Toast.makeText(Registration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        init();

        mRegButton.setOnClickListener(mRegistrationBtnClickListener);

    }

    private boolean checkifNull(String email, String password) {
        return ((email.isEmpty()) || (password.isEmpty())) ? true:false;
    }

    private void verifyEmailAdress(FirebaseUser currentUser) {
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Verify email at:" + " " + mRegEmailText, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateOTPUI() {
        mDetailsLayout.setVisibility(View.INVISIBLE);
        mVerifyOTPLayout.setVisibility(View.VISIBLE);
    }

    private void verifyMobileNumber(String mRegMobileText) {
        //Formatting Mobile Number
        String mRegMobileCode = "+91";
        mRegMobileText = mRegMobileCode+mRegMobileText;

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                mOTPCode = phoneAuthCredential.getSmsCode();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(getApplicationContext(), " " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                //Animate views to enter OTP
                updateOTPUI();
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mRegMobileText,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void createUserWithEmailAndPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user
                    // Log.d(TAG, "createUserWithEmail:success");
                    Toast.makeText(Registration.this, "Registration Success.",
                            Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    signInwith(mRegEmailText,mRegPasswordText);
                    //Goto LoginPage
                    gotoHomePage();
                    //updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    //updateUI(null);
                }
            }
        });

    }

    private void addUserinDatabase(String mRegNameText, String mRegEmailText, String mRegMobileText) {

        //creating new User
        Users user = new Users(mRegNameText,mRegEmailText,mRegMobileText);

        //encode email as FirebaseKey
        String mRegEmailKey = encodeAsFirebaseKey(mRegEmailText);

        //Adding into Database
        mDataRef.child("userInfo").child(mRegEmailKey).setValue(user);

    }

    private void initStringsFromEditText(){
        mRegNameText = mRegName.getText().toString();
        mRegEmailText = mRegEmail.getText().toString();
        mRegPasswordText = mRegPassword.getText().toString();
        mRegMobileText = mRegMobile.getText().toString();
    }

    private String encodeAsFirebaseKey(String email) {
        return email.replace(".", "%2E");
    }

    private void gotoHomePage(){
        Intent intent = new Intent(this,home.class);
        startActivity(intent);
    }

    private void gotoSignupScreen(){
        Intent intent = new Intent(this,login_signup.class);
        startActivity(intent);
    }

    private void gotoLoginScreen(){
        Intent intent = new Intent(this,login_signup.class);
        startActivity(intent);
    }

}
