package com.example.susmitha.gym_pilot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login_signup extends AppCompatActivity {

    private ImageButton mCloseButton;
    private ImageButton mForgotCloseButton;
    private RelativeLayout mLoginDetailsLayout;
    private RelativeLayout mForgotPwdLayout;
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPwdEditText;
    private TextInputEditText mForgotPwdEmail;
    private MaterialButton mLoginButton;
    private MaterialButton mSignupButton;
    private MaterialButton mForgotPwdButton;
    private MaterialButton mForgotPwdResetButton;
    private View.OnClickListener mLoginButtonClickListener;
    private View.OnClickListener mSignupButtonClickListener;
    private FirebaseAuth mAuth;

    private void init(){

        //Firebase variables
        mAuth = FirebaseAuth.getInstance();

        //initialize UI elements
        mLoginDetailsLayout = findViewById(R.id.login_details_layout);
        mForgotPwdLayout = findViewById(R.id.forgot_pwd_layout);
        mCloseButton = findViewById(R.id.close_btn);
        mForgotCloseButton = findViewById(R.id.forgot_close_btn);
        mEmailEditText = findViewById(R.id.email_edit_text);
        mPwdEditText = findViewById(R.id.pswd_edit_text);
        mLoginButton = findViewById(R.id.login_btn);
        mSignupButton = findViewById(R.id.sign_up_btn);
        mForgotPwdButton = findViewById(R.id.forgot_pwd_btn);
        mForgotPwdResetButton = findViewById(R.id.send_pwd_reset_button);
        mForgotPwdEmail = findViewById(R.id.forgot_email_edit_text);


        mLoginButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString();
                String password = mPwdEditText.getText().toString();
                Log.d("sathu_login"," "+ checkifNull(email,password));
                if(!checkifNull(email,password)) {
                    signInWithEmailAndPassword(email, password);
                }else{
                    Log.d("sathu_login"," "+ "it was null");
                    /*show something for null enetered email & pswd*/
                }
            }
        };

        mSignupButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login_signup.this,Registration.class);
                startActivity(intent);
            }
        };

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLoginScreen();
            }
        });

        mForgotPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginDetailsLayout.setVisibility(View.INVISIBLE);
                mForgotPwdLayout.setVisibility(View.VISIBLE);
            }
        });

        mForgotPwdResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.sendPasswordResetEmail(mForgotPwdEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),"Reset Mail Sent",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                gotoLoginScreen();
            }
        });

        mForgotCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mForgotPwdLayout.setVisibility(View.INVISIBLE);
                mLoginDetailsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean checkifNull(String email, String password) {
        return ((email.isEmpty()) || (password.isEmpty())) ? true:false;
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(login_signup.this, "Authentication Success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            gotoHomePage(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(login_signup.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        init();
        mLoginButton.setOnClickListener(mLoginButtonClickListener);
        mSignupButton.setOnClickListener(mSignupButtonClickListener);
    }

    private void gotoHomePage(FirebaseUser user) {
        Intent intent = new Intent(this,home.class);
        startActivity(intent);
    }

    private void gotoLoginScreen() {
        Intent intent = new Intent(this,LoginScreen.class);
        startActivity(intent);
    }

}
