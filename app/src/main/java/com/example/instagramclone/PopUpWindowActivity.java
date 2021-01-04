package com.example.instagramclone;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PopUpWindowActivity extends AppCompatActivity {

    @BindView(R.id.userProfileImageView)
    CircleImageView userProfileImageView;

    String userFirebaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_window);
        ButterKnife.bind(this);

        DisplayMetrics displayMetrics = new DisplayMetrics(); // structure describing general information about a display, such as its size, density, and font scaling.
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.6),(int)(height*0.3)); // setting dimensions for pop up window

        userFirebaseId = getIntent().getStringExtra("firebase id"); // intent received from Search Fragment upon clicking profile image

        StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + userFirebaseId); // accessing profile picture of user in Storage Reference
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(getApplicationContext()==null){
                    return;
                }
                Glide.with(PopUpWindowActivity.this).load(uri).placeholder(R.drawable.profile).into(userProfileImageView); // loading image into User profile image view
            }
        });
    }
}