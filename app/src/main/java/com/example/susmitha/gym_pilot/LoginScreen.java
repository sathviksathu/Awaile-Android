package com.example.susmitha.gym_pilot;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LoginScreen extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN = 1;
    private String TAG ="loginScreenActivity";
    private CallbackManager mFacebookCallbackManager;
    private FirebaseAuth mAuth;
    private static final String EMAIL = "email";
    private Button mailSignIn;
    private Button googleSignIn;
    private Button facebookMaterialButton;
    private LoginButton facebookSignIn;
    private  GoogleSignInClient mGoogleSignInClient;
    private ViewPager loginViewPager;
    private List<Integer> loginPageDrawables;
    private ProgressBar mProgressBar;
    private RelativeLayout mProgressBarLayout;
    private RelativeLayout mLoginScreenTextImageLayout;





    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            mProgressBarLayout.setVisibility(View.GONE);
            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(this,home.class);
            intent.putExtra("displayName",account.getDisplayName());
            intent.putExtra("email",account.getEmail());
            startActivity(intent);
           // updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("sathu", "signInResult:failed code=" + e.getStatusCode());
           // updateUI(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("sathu_facebook", "onActivityResult:");
        // Pass the activity result back to the Facebook SDK
        mFacebookCallbackManager.onActivityResult(requestCode,resultCode,data);


        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Log.d("sathu", "onActivityResult with request Code == RC_SIGN_IN");
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    private void init() {

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                . requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Facebook shizz
        mFacebookCallbackManager = CallbackManager.Factory.create();


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialising parameters for google and facbook signIns
        init();

        //firebase config
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        //Initialising Buttons
        googleSignIn = findViewById(R.id.google_sign_in_button);
        facebookMaterialButton = findViewById(R.id.facebook_sign_in_material_button);
        mailSignIn=findViewById(R.id.email_sign_in_button);
        facebookSignIn =  findViewById(R.id.facebook_sign_in_button);
        loginViewPager = findViewById(R.id.viewPager);
        mProgressBarLayout = findViewById(R.id.progress_box);
        mProgressBar = findViewById(R.id.indeterminate_bar);
        mProgressBar.setIndeterminate(true);
        mLoginScreenTextImageLayout = findViewById(R.id.text_and_image_layout);

        //Login Screen image slider resources
        loginPageDrawables = new ArrayList<>(4);
        loginPageDrawables.add(R.drawable.ic_dip_bar);
        loginPageDrawables.add(R.drawable.ic_bar);
        loginPageDrawables.add(R.drawable.ic_bike);
        loginPageDrawables.add(R.drawable.ic_weight);

        //temp
        loginViewPager.setAdapter(  new SliderAdapter(this, loginPageDrawables));
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 2000, 3000);


        //Mail Login or SignUp
        mailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailLogin();
            }
        });

        //google SignIn
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        //facebook SignIn
        facebookMaterialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignIn.performClick();
            }
        });


        facebookSignIn.setReadPermissions(Arrays.asList("email", "public_profile"));


        // Callback registration
        facebookSignIn.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("sathu_facebook", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("sathu_facebook", "facebook:onCancel:");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("sathu_facebook", "facebook:onError:" + exception.getMessage());

            }
        });

    }

    private class SliderTimer extends TimerTask {

        @Override
        public void run() {
            LoginScreen.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loginViewPager.getCurrentItem() < loginPageDrawables.size() - 1) {
                        loginViewPager.setCurrentItem(loginViewPager.getCurrentItem() + 1);
                    } else {
                        loginViewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d("sathu_facebook", "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sathu_facebook", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("sathu_facebook", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }
                    }
                });


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            // Snackbar.make(findViewById(R.id.ma), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                             updateUI(null);
                        }

                        // ...
                    }
                });


    }


    private void mailLogin() {
        Intent intent = new Intent(LoginScreen.this,login_signup.class);
        startActivity(intent);
        finish();
    }

    private void googleSignIn() {
        Log.d("sathu", "inSignIn");
        mProgressBarLayout.bringToFront();
        setActivityBackgroundColor();
        mProgressBarLayout.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void updateUI(FirebaseUser user){
        mProgressBarLayout.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(LoginScreen.this,home.class);
        intent.putExtra("firebaseUser",user);
        startActivity(intent);
        finish();
    }

    public void setActivityBackgroundColor() {
        mProgressBarLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        mLoginScreenTextImageLayout.setBackgroundColor(getResources().getColor(R.color.black_overlay));
    }
}
