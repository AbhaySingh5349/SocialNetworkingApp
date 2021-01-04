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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
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

public class ProfileActivity extends AppCompatActivity {

    /* to add profile picture, Bio, profile name */

    @BindView(R.id.editProfileImageView)
    CircleImageView editProfileImageView;
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
    @BindView(R.id.saveInfoTextView)
    TextView saveInfoTextView;

    FirebaseAuth firebaseAuth; // to create object of Firebase Auth class to fetch currently loged in user
    FirebaseUser firebaseUser; // to create object of Firebase User class to get current user to store currently loged in user
    DatabaseReference userDatabaseReference;
    HashMap<String,Object> usersNodeHashMap;
    StorageReference storageReference, profileImageStorageReference; // to upload profile image to firebase
    Uri serverImageUri, selectedImageUri;

    UserProfileChangeRequest userProfileChangeRequest;

    int profileImageRequestCode=101 , readExternalStorageRequestCode=102;
    Intent profileImageIntent;

    String profileName, userName, bio, mobileNumber, userFirebaseId;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userFirebaseId = Objects.requireNonNull(firebaseUser).getUid();

        mobileNumber = getIntent().getStringExtra("mobile number"); // receiving intent from Registration Activity

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        storageReference = FirebaseStorage.getInstance().getReference(); // give reference to root folder of file storage

        progressDialog = new ProgressDialog(this); // instantiating Progress Dialog

        if(firebaseUser!=null){

            editProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // checking permission to access photo and video gallery of device
                    if(ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                        profileImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(profileImageIntent,profileImageRequestCode);
                    }else {
                        ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},readExternalStorageRequestCode);
                    }
                }
            });

            saveInfoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkProfileCredentials();
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==readExternalStorageRequestCode){
            if(ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent profileImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(profileImageIntent,profileImageRequestCode);
                }else {
                    Toast.makeText(ProfileActivity.this,"Access Gallery Permission Required",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // validating all required fields

    private boolean validateProfileName(){
        profileName = Objects.requireNonNull(profileNameTextInputEditText.getText()).toString().trim();
        if(profileName.isEmpty()){
            profileNameTextInputLayout.setErrorEnabled(true);
            profileNameTextInputLayout.setError("Enter Profile Name");
            return false;
        }else{
            profileNameTextInputLayout.setErrorEnabled(false);
            profileNameTextInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateUserName(){
        userName = Objects.requireNonNull(userNameTextInputEditText.getText()).toString().trim();
        if(userName.isEmpty()){
            userNameTextInputLayout.setErrorEnabled(true);
            userNameTextInputLayout.setError("Enter User Name");
            return false;
        }else{
            userNameTextInputLayout.setErrorEnabled(false);
            userNameTextInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateBio(){
        bio = Objects.requireNonNull(bioTextInputEditText.getText()).toString().trim();
        if(bio.isEmpty()){
            bioTextInputLayout.setErrorEnabled(true);
            bioTextInputLayout.setError("Enter Bio");
            return false;
        }else{
            bioTextInputLayout.setErrorEnabled(false);
            bioTextInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateProfileImage(){
        if(selectedImageUri==null){
            if(serverImageUri==null){
                Toast.makeText(ProfileActivity.this,"Add Profile Image",Toast.LENGTH_SHORT).show();
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    private void checkProfileCredentials() {
        if(!validateProfileName() | !validateUserName() | !validateBio() | !validateProfileImage()){
            validateProfileName();
            validateUserName();
            validateBio();
            validateProfileImage();
        }else {
            updateUserProfile();
        }
    }

    // updating user profile

    private void updateUserProfile() {
        progressDialog.setTitle("Updating Profile");
        progressDialog.setMessage("Please wait while we are updating your profile");
        progressDialog.show();

        if(validateProfileName() && validateUserName() && validateBio()){

            if(selectedImageUri!=null){
                profileImageStorageReference = storageReference.child(Constants.IMAGESFOLDER).child(userFirebaseId);
                profileImageStorageReference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            profileImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    selectedImageUri=uri;

                                    userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(userName).setPhotoUri(selectedImageUri).build();
                                    firebaseUser.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                usersNodeHashMap = new HashMap<>();
                                                usersNodeHashMap.put(NodeNames.PROFILENAME,profileName.toLowerCase());
                                                usersNodeHashMap.put(NodeNames.USERNAME,userName);
                                                usersNodeHashMap.put(NodeNames.BIO,bio);
                                                usersNodeHashMap.put(NodeNames.MOBILENUMBER,mobileNumber);
                                                usersNodeHashMap.put(NodeNames.PHOTOURL,selectedImageUri.getPath());
                                                usersNodeHashMap.put(NodeNames.USERID,userFirebaseId);

                                                userDatabaseReference.child(userFirebaseId).setValue(usersNodeHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            progressDialog.dismiss();
                                                            Toast.makeText(ProfileActivity.this,"Details Added Successfully",Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                                                            finish();
                                                        }else {
                                                            Toast.makeText(ProfileActivity.this,"Failed to add details: " + task.getException(),Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }else{
            Toast.makeText(ProfileActivity.this,"Add Details",Toast.LENGTH_SHORT).show();
        }
    }
}