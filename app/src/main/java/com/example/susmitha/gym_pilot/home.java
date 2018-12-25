package com.example.susmitha.gym_pilot;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.github.clans.fab.*;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FirebaseAuth.AuthStateListener {

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDataRef;
    private Context mContext;
    private ImageView mProfilePicture;
    private TextView mDisplayName;
    private TextView mEmailId;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String dbUsername;
    private boolean rotateFlag;

    //FAB and Menu
    private com.github.clans.fab.FloatingActionButton mFab;
    private FloatingActionMenu mFabMenu;
    private View.OnClickListener mFabButtonClickListener;

    //RecylerView for GymCards
    private RecyclerView mGymCardRecyclerView;
    private LinearLayoutManager mGymCardLayoutManager;
    private GymCardDataAdapter mGymCardDataAdapter;

    private void init(){

        //initialising context
        mContext = getApplicationContext();

        //initialising Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mDataRef = mDatabase.getReference();


        //initialising UI elements
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        mProfilePicture = headerView.findViewById(R.id.profile_pic);
        mDisplayName = headerView.findViewById(R.id.display_name);
        mEmailId = headerView.findViewById(R.id.display_email);
        mFab = findViewById(R.id.fab);
        mFabMenu = findViewById(R.id.fab_menu);
        mGymCardRecyclerView=findViewById(R.id.gym_card_recycler_view);
        rotateFlag = true;
        setSupportActionBar(toolbar);

        //gymCard RecyclerView Variables
        mGymCardLayoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false);
        mGymCardDataAdapter = new GymCardDataAdapter();
        mGymCardRecyclerView.setLayoutManager(mGymCardLayoutManager);
        mGymCardRecyclerView.setAdapter(mGymCardDataAdapter);


        //other conditions
        mFabMenu.setClosedOnTouchOutside(true);
        mFabMenu.hideMenuButton(false);

        //onClickListers
        //other Variables
        mFabButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateFabAnimationtoggle(rotateFlag);
                mFabMenu.toggle(true);

            }
        };

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();

        mFab.setOnClickListener(mFabButtonClickListener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //initialising username,mail,profilePic from FireBase
        String usernameFromDatabase = getDisplayNamefromFirebaseDB(mFirebaseUser);

        mEmailId.setText(mFirebaseUser.getEmail());
        if(mFirebaseUser.getPhotoUrl()!=null)
        Glide.with(this).load(mFirebaseUser.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(mProfilePicture);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    private String getDisplayNamefromFirebaseDB(FirebaseUser mFirebaseUser) {

        //Initialise DB Displayname string
        Log.d("sathu_home", "getting displayname");
        final Query mDataRefDBName=mDataRef.child("userInfo").orderByKey().equalTo(encodeAsFirebaseKey(mFirebaseUser.getEmail()));
        // Attach a listener to read the data at our posts reference
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("sathu_home", "getting count: "+ dataSnapshot.getChildrenCount());

                ArrayList<Users> user = new ArrayList<>();
                for(DataSnapshot dbUser : dataSnapshot.getChildren()){
                    Log.d("sathu_home", "adding the child: "+ dataSnapshot.getChildrenCount());
                    user.add(dbUser.getValue(Users.class));
                }
                Log.d("sathu_home", "added child is:  "+user.get(0).userName);

                dbUsername = user.get(0).userName;
                mDisplayName.setText(dbUsername);
                //email fetched from database
                //Log.d("sathu_home", demo[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDataRefDBName.addValueEventListener(listener);
         return dbUsername;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_location) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_near_me) {
            // Handle the camera action
        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_payment) {

        } else if (id == R.id.nav_offers) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
           mAuth.signOut();
           gotoLoginScreen();
        }

        DrawerLayout drawer =findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void gotoLoginScreen() {
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }

    private void rotateFabAnimationtoggle(boolean flag){
        //TODO: even on menuTouchOutside we need to call this
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        if(flag==true) {
            ViewCompat.animate(mFab).
                    rotation(135f).
                    withLayer().
                    setDuration(300).
                    setInterpolator(interpolator).
                    start();
            rotateFlag=false;
        }else{
            ViewCompat.animate(mFab).
                    rotation(0f).
                    withLayer().
                    setDuration(300).
                    setInterpolator(interpolator).
                    start();
            rotateFlag=true;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mFirebaseUser = firebaseAuth.getCurrentUser();
        Log.d("sathu_home", "AuthStateListener" + mFirebaseUser == null ? "user is null" : mFirebaseUser.getDisplayName());
    }

    private class GymCardDataAdapter extends RecyclerView.Adapter<GymCardDataAdapter.ViewHolder>{

        @Override
        public int getItemCount() {
            return 5;
        }

        @Override
        public void onBindViewHolder(@NonNull GymCardDataAdapter.ViewHolder viewHolder, int i) {

        }

        @NonNull
        @Override
        public GymCardDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gym_card,viewGroup,false);
            return new ViewHolder(view);
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            private CardView mGymCard;
            private View holderView;


            public ViewHolder(@NonNull View view) {
                super(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        need to implement onClick of CardView
                         */
                    }
                });

                holderView = view;
                mGymCard = holderView.findViewById(R.id.gymcardView);
            }
        }

    }

    private String encodeAsFirebaseKey(String email) {
        return email.replace(".", "%2E");
    }
}
