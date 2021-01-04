package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class EditProfileActivity extends AppCompatActivity {

    /* to change profile picture, add Bio , change profile name or log out from profile */

    @BindView(R.id.closeImageView)
    ImageView closeImageView;
    @BindView(R.id.saveImageView)
    ImageView saveImageView;
    @BindView(R.id.editProfileImageView)
    CircleImageView editProfileImageView;
    @BindView(R.id.changeProfileTextView)
    TextView changeProfileTextView;
    @BindView(R.id.profileNameTextInputLayout)
    TextInputLayout profileNameTextInputLayout;
    @BindView(R.id.profileNameTextInputEditText)
    TextInputEditText profileNameTextInputEditText;
    @BindView(R.id.userNameTextInputLayout)
    TextInputLayout userNameTextInputLayout;
    @BindView(R.id.userNameTextInputEditText)
    TextInputEditText userNameTextInputEditText;
    @BindView(R.id.bioTextInputLayout)
    TextInputLayout bioTextInputLayout;
    @BindView(R.id.bioTextInputEditText)
    TextInputEditText bioTextInputEditText;
    @BindView(R.id.logOutTextView)
    TextView logOutTextView;

    FirebaseAuth firebaseAuth; // to create object of Firebase Auth class to fetch currently logged in user
    FirebaseUser firebaseUser; // to create object of Firebase User class to get current user to store currently logged in user
    DatabaseReference userDatabaseReference;
    HashMap<String,Object> usersNodeHashMap;
    StorageReference storageReference, profileImageStorageReference; // to upload profile image to firebase
    Uri serverImageUri, selectedImageUri;

    int profileImageRequestCode=101 , readExternalStorageRequestCode=102;
    Intent profileImageIntent;

    String profileName, userName, bio, mobileNumber, currentUserId;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        mobileNumber = getIntent().getStringExtra("mobile number"); // receiving intent from Registration Activity

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS); // accessing Users node in database
        storageReference = FirebaseStorage.getInstance().getReference(); // give reference to root folder of file storage

        progressDialog = new ProgressDialog(this); // instantiating Spinner Progress Dialog

        if(firebaseUser!=null){
            retrieveUserProfile();

            // retrieving info from edit texts

            profileName = Objects.requireNonNull(profileNameTextInputEditText.getText()).toString().trim();
            userName = Objects.requireNonNull(userNameTextInputEditText.getText()).toString().trim();
            bio = Objects.requireNonNull(bioTextInputEditText.getText()).toString().trim();

            changeProfileTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // checking permission to access photo and video gallery of device
                    if(ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                        profileImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // intent to open Photo Gallery
                        startActivityForResult(profileImageIntent,profileImageRequestCode);
                    }else {
                        ActivityCompat.requestPermissions(EditProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},readExternalStorageRequestCode);
                    }
                }
            });

            saveImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateProfileInfo();
                }
            });

            logOutTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseAuth.signOut(); // signing out from application
                    startActivity(new Intent(EditProfileActivity.this,RegistrationActivity.class));
                }
            });
        }
    }

    // retrieving user profile info from database

    private void retrieveUserProfile(){
        userDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    StorageReference profileImageDB = storageReference.child(Constants.IMAGESFOLDER + "/" + currentUserId);
                    profileImageDB.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverImageUri = uri;
                            Glide.with(EditProfileActivity.this).load(serverImageUri).placeholder(R.drawable.profile).into(editProfileImageView); // loading image to user profile image
                        }
                    });

                    String profileNameDB = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                    String userNameDB = Objects.requireNonNull(snapshot.child(NodeNames.USERNAME).getValue()).toString();
                    String bioDB = Objects.requireNonNull(snapshot.child(NodeNames.BIO).getValue()).toString();

                    profileNameTextInputEditText.setText(profileNameDB);
                    userNameTextInputEditText.setText(userNameDB);
                    bioTextInputEditText.setText(bioDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this,"Failed to fetch details: " + error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK ) {
                    if (requestCode == profileImageRequestCode) {
                        // Get the url from data
                        selectedImageUri = Objects.requireNonNull(data).getData();

                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            // Set the image in ImageView
                            editProfileImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    editProfileImageView.setImageURI(selectedImageUri);
                                }
                            });
                        }
                    }
                }
            }
        }).start();
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (Objects.requireNonNull(cursor).moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    // updating user profile info

    private void updateProfileInfo() {
        progressDialog.setTitle("Updating Profile");
        progressDialog.setMessage("Please wait while we are updating your profile");
        progressDialog.show();

        // retrieving info from edit texts

        profileName = Objects.requireNonNull(profileNameTextInputEditText.getText()).toString().trim();
        userName = Objects.requireNonNull(userNameTextInputEditText.getText()).toString().trim();
        bio = Objects.requireNonNull(bioTextInputEditText.getText()).toString().trim();

        usersNodeHashMap = new HashMap<>();

        if(!profileName.isEmpty()){
            usersNodeHashMap.put(NodeNames.PROFILENAME,profileName);
        }
        if(!userName.isEmpty()){
            usersNodeHashMap.put(NodeNames.USERNAME,userName);
        }
        if(!bio.isEmpty()){
            usersNodeHashMap.put(NodeNames.BIO,bio);
        }

        usersNodeHashMap.put(NodeNames.USERID,currentUserId);

        if(selectedImageUri!=null){
            profileImageStorageReference = storageReference.child(Constants.IMAGESFOLDER).child(currentUserId);

            profileImageStorageReference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        profileImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                selectedImageUri = uri;
                                usersNodeHashMap.put(NodeNames.PHOTOURL,selectedImageUri.getPath());
                            }
                        });
                    }
                }
            });
        }

        // updating nodes

        userDatabaseReference.child(currentUserId).updateChildren(usersNodeHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditProfileActivity.this,"Profile updated Successfully",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(EditProfileActivity.this,"Failed to update profile: " + task.getException(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
}


