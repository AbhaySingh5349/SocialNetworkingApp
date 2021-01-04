package com.example.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.fragments.HeartFragment;
import com.example.instagramclone.fragments.HomeFragment;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.frameLayout)
    FrameLayout frameLayout;

    public static BottomNavigationView bottomNavigation; // It is an implementation of material design bottom navigation used to make easy switch between top-level views in single tap


    Fragment fragment; // a fragment is a kind of sub-activity having its own layout and its own behaviour with its own life cycle callbacks
    FragmentManager fragmentManager; // A FragmentManager manages Fragments in Android, specifically it handles transactions between fragments

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference contactsDatabaseReference;

    String currentUserId;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = this.getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid(); // storing currently active user Id

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnNavigationItemSelectedListener(this);

        fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameLayout,new HomeFragment()).commit(); // setting Home Fragment as default fragment when application starts

        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.CONTACTS); // accessing users Contacts Saved
        contactsDatabaseReference.child(currentUserId).child(currentUserId).child(NodeNames.CONTACTSTATUS).setValue(Constants.CONTACTSAVED); // to follow ourselves
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.navHome:
                 fragment= new HomeFragment();
                 Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                 fragmentManager.beginTransaction().replace(R.id.frameLayout,fragment).commit(); // switching to Home Fragment
                 return true;

            case R.id.navSearch:
                 fragment= new SearchFragment();
                 Toast.makeText(this,"search",Toast.LENGTH_SHORT).show();
                 fragmentManager.beginTransaction().replace(R.id.frameLayout,fragment).commit(); // switching to User Search Fragment
                 return true;

            case R.id.navAddPost:
                 startActivity(new Intent(this,AddPostActivity.class));
                 Toast.makeText(this,"add",Toast.LENGTH_SHORT).show(); // switching to Add Post Fragment
                 return true;

            case R.id.navHeart:
                 fragment= new HeartFragment();
                 fragmentManager.beginTransaction().replace(R.id.frameLayout,fragment).commit(); // switching to Notifications Fragment
                 Toast.makeText(this,"activity",Toast.LENGTH_SHORT).show();
                 return true;

            case R.id.navProfile:
                 fragment= new ProfileFragment();
                 sharedPreferences.edit().putString("profile Id",currentUserId).apply(); // passing data to Profile Fragment
                 fragmentManager.beginTransaction().replace(R.id.frameLayout,fragment).commit(); // switching to User Profile Fragment
                 Toast.makeText(this,"profile",Toast.LENGTH_SHORT).show();
                 return true;

            default:
                 return false;

        }
    }
}