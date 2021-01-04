package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddPostActivity extends AppCompatActivity {

    /* upload Photo or Video post with captions which can also consist of HashTags */

    @BindView(R.id.cancelPostImageView)
    ImageView cancelPostImageView;
    @BindView(R.id.uploadPostImageView)
    ImageView uploadPostImageView;
    @BindView(R.id.addPostImageView)
    ImageView addPostImageView;
    @BindView(R.id.videoPostViewer)
    VideoView videoPostViewer;
    @BindView(R.id.postDescriptionTextInputLayout)
    TextInputLayout postDescriptionTextInputLayout;
    @BindView(R.id.postDescriptionTextInputEditText)
    TextInputEditText postDescriptionTextInputEditText;

    FirebaseAuth firebaseAuth; // to create object of Firebase Auth class to fetch currently logged in user
    FirebaseUser firebaseUser; // to create object of Firebase User class to get current user to store currently loged in user
    MediaController mediaController; // A view containing controls for a MediaPlayer having buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress slider

    DatabaseReference databaseReference, postDatabaseReference, hashTagsDatabaseReference;
    StorageReference storageReference, postStorageReference;
    Uri selectedImageUri, selectedVideoUri, selectedUri;

    HashMap<String,Object> postHashMap, hashTagHashMap;
    ArrayList<String> hashTagsArrayList;
    String currentUserId, postDescription, postType;

    int postImageRequestCode=101, readExternalStorageRequestCode=102, writeExternalStorageCode = 1003, photoPickRequestCode = 1005, videoPickRequestCode = 1006;

    private ProgressDialog progressDialog;
    private BottomSheetDialog bottomSheetDialog;
    private boolean postSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        ButterKnife.bind(this);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // reference to nodes of database

        databaseReference = FirebaseDatabase.getInstance().getReference(); // accessing Firebase Database
        hashTagsDatabaseReference = databaseReference.child(NodeNames.HASHTAGS); // accessing HashTag node in database
        storageReference = FirebaseStorage.getInstance().getReference(); // accessing storage reference

        progressDialog = new ProgressDialog(this); // instantiating Spinner Progress Dialog

        bottomSheetDialog = new BottomSheetDialog(this); // instantiating Bottom Sheet Dialog
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.upload_file_bottom_sheet_layout,null);
        bottomSheetDialog.setContentView(bottomSheetDialogView); // attaching layout to Bottom Sheet Dialog
        bottomSheetDialog.setCancelable(false);

        if(firebaseUser!=null){

            addPostImageView.setVisibility(View.VISIBLE);
            videoPostViewer.setVisibility(View.INVISIBLE);

            addPostImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // checking permission to access photo and video gallery of device
                    if(ActivityCompat.checkSelfPermission(AddPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        if(bottomSheetDialog != null){
                            bottomSheetDialog.show();
                        }
                    }else {
                        ActivityCompat.requestPermissions(AddPostActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},readExternalStorageRequestCode);
                    }

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0); // hiding keyboard
                }
            });

            bottomSheetDialogView.findViewById(R.id.photoImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // intent to open Photo Gallery
                    startActivityForResult(intent,photoPickRequestCode);
                }
            });

            bottomSheetDialogView.findViewById(R.id.videoImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI); // intent to open Video Gallery
                    startActivityForResult(intent,videoPickRequestCode);
                }
            });

            bottomSheetDialogView.findViewById(R.id.closeImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                }
            });

            uploadPostImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkPostCredentials();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==readExternalStorageRequestCode){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(AddPostActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    if(bottomSheetDialog != null){
                        bottomSheetDialog.show();
                    }
                }else {
                    Toast.makeText(AddPostActivity.this,"Permissions required to access files",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            // if anything is selected
            if(requestCode == photoPickRequestCode){
                Uri uri = Objects.requireNonNull(data).getData();
                selectedImageUri = uri;
                selectedVideoUri = null;
                selectedUri = selectedImageUri;
                postType = Constants.POSTIMAGE;
                addPostImageView.setVisibility(View.VISIBLE);
                videoPostViewer.setVisibility(View.INVISIBLE);
                addPostImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        addPostImageView.setImageURI(selectedImageUri); // setting selected photo from Gallery to Image View
                    }
                });

            }else if(requestCode == videoPickRequestCode){
                Uri uri = Objects.requireNonNull(data).getData();
                selectedVideoUri = uri;
                selectedImageUri = null;
                selectedUri = selectedVideoUri;
                postType = Constants.POSTVIDEO;
                addPostImageView.setVisibility(View.INVISIBLE);
                videoPostViewer.setVisibility(View.VISIBLE);
                mediaController = new MediaController(this);
                videoPostViewer.setMediaController(mediaController); // attaching Media Controller to Video View
                videoPostViewer.setVideoURI(selectedVideoUri); // setting selected video from Gallery to Image View
                videoPostViewer.start(); // starting video automatically as soon as it gets loaded in image view
            }
        }
    }

    // checking if post description is entered or not

    private boolean validatePostDescription(){
        postDescription = Objects.requireNonNull(postDescriptionTextInputEditText.getText()).toString().trim();
        if(postDescription.isEmpty()){
            postDescriptionTextInputLayout.setErrorEnabled(true);
            postDescriptionTextInputLayout.setError("Enter Post Description");
            return false;
        }else{
            hashTagsArrayList = new ArrayList<>();
            hashTagsArrayList.clear(); // removing previous hashTags from array list
            hashTagsArrayList = getHashTags(postDescription); // storing HashTags of particular post
            postDescriptionTextInputLayout.setErrorEnabled(false);
            postDescriptionTextInputLayout.setError(null);
            return true;
        }
    }

    // retrieving HashTags from description

    private ArrayList<String> getHashTags(String postDescription) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.clear(); // removing previous hashTags from array list
        String tag = postDescription + "#";

        while (tag.contains("#")){
            String hashTag = "";
            int hash = tag.indexOf("#"); // splitting "tag" on encountering "#" and storing index at beginning of hashTag
            tag = tag.substring(hash+1); // storing value of hashTag

            int firstSpace = tag.indexOf(" "); // storing index till hashTag exists before "space" occurs
            int firstHash = tag.indexOf("#"); // storing index till hashTag exists before next "#" occurs

            if(firstSpace == -1 && firstHash == -1){
                hashTag = tag.substring(0); // if there doesn't exists any space or other hashTag, so first substring is the only hashTag
            }else if(firstSpace != -1 && firstSpace < firstHash){
                hashTag = tag.substring(0,firstSpace);  // if hashTag is separated by "space"
                tag = tag.substring(firstSpace+1); // modifying to look for next hashTag if present
            }else if(firstHash != -1 && firstHash < firstSpace){
                hashTag = tag.substring(0,firstHash); // if hashTag is separated by "hashTag"
                tag = tag.substring(firstHash+1); // modifying to look for next hashTag if present
            }else {
                hashTag = tag.substring(0,firstHash);
                tag = tag.substring(firstHash);
            }

            if(hashTag.length()>0){
                if(hashTag.endsWith(",")){
                    hashTag = hashTag.substring(0,hashTag.length()-1);
                }
                arrayList.add(hashTag);
            }
        }
        return arrayList;
    }

    // checking if post image or video is added or not

    private boolean validatePostFile(){
        if(selectedImageUri!=null && selectedVideoUri==null){
            selectedUri = selectedImageUri;
            return true;
        }else if(selectedVideoUri!=null && selectedImageUri==null){
            selectedUri = selectedVideoUri;
            return true;
        } else{
            Toast.makeText(AddPostActivity.this,"Add File",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void checkPostCredentials() {
        if (!validatePostDescription() | !validatePostFile()) {
            validatePostDescription();
            validatePostFile();
        }else{

            // updating post on database

            progressDialog.setTitle("Uploading New Post");
            progressDialog.setMessage("Please wait while post is being uploaded");
            progressDialog.show();

            postHashMap = new HashMap<>();
            hashTagHashMap = new HashMap<>();

            postDatabaseReference = databaseReference.child(NodeNames.POSTS).child(currentUserId).push(); // generating unique id of post
            String postId = postDatabaseReference.getKey();

            databaseReference.child(NodeNames.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(NodeNames.PROFILENAME)){
                        String profileName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                        postHashMap.put(NodeNames.PROFILENAME,profileName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // adding Post Info to database

            if(selectedImageUri!=null && selectedVideoUri==null){
                postStorageReference = storageReference.child(Constants.POSTIMAGESFOLDER).child(Objects.requireNonNull(postId)).child(currentUserId);
                selectedUri = selectedImageUri;
                postType = Constants.POSTIMAGE;
            }else if(selectedVideoUri!=null && selectedImageUri==null){
                postStorageReference = storageReference.child(Constants.POSTVIDEOSFOLDER).child(Objects.requireNonNull(postId)).child(currentUserId);
                selectedUri = selectedVideoUri;
                postType = Constants.POSTVIDEO;
            }

            postStorageReference.putFile(selectedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        postStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                selectedUri = uri;
                            }
                        });
                        postHashMap.put(NodeNames.POSTVALUE,selectedUri.getPath());
                        postHashMap.put(NodeNames.POSTDESCRIPTION,postDescription);
                        postHashMap.put(NodeNames.POSTTIMESTAMP, ServerValue.TIMESTAMP);
                        postHashMap.put(NodeNames.POSTID,postId);
                        postHashMap.put(NodeNames.POSTPUBLISHERID,currentUserId);
                        postHashMap.put(NodeNames.POSTTYPE,postType);

                        if(hashTagsArrayList.size()>0){
                            postHashMap.put(NodeNames.POSTHASHTAGS,hashTagsArrayList);
                        }

                        // Calender is an abstract class that provides methods for converting between a specific instant in time and a set of calendar fields such as YEAR, MONTH, DAY_OF_MONTH, HOUR

                        Calendar date = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy"); // Nov 26,2020
                        String currentDate = currentDateFormat.format(date.getTime());
                        postHashMap.put(NodeNames.POSTDATE,currentDate);

                        Calendar time = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a"); // 01:42 AM
                        String currentTime = currentTimeFormat.format(time.getTime());
                        postHashMap.put(NodeNames.POSTTIME,currentTime);

                        String postReference = NodeNames.POSTS + "/" + postId;

                        HashMap<String,Object> postNodeHashMap = new HashMap<>();
                        postNodeHashMap.put(postReference,postHashMap);

                        databaseReference.updateChildren(postNodeHashMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if(error==null){

                                    for (String hashTag : hashTagsArrayList){
                                        hashTagHashMap.put(NodeNames.POSTHASHTAG,hashTag);
                                        hashTagHashMap.put(NodeNames.POSTID,postId);

                                        String hashTagReference = NodeNames.HASHTAGS + "/" + hashTag.toLowerCase() + "/" + postId;

                                        HashMap<String,Object> hashTagNodeHashMap = new HashMap<>();
                                        hashTagNodeHashMap.put(hashTagReference,hashTagHashMap);

                                        databaseReference.updateChildren(hashTagNodeHashMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                // updating HashTag nodes in database
                                            }
                                        });
                                    }

                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this,"Post Uploaded Successfully",Toast.LENGTH_SHORT).show();

                                    // resetting post activity Views

                                    postDescriptionTextInputEditText.setText(null);
                                    addPostImageView.setImageURI(null);
                                    addPostImageView.setImageResource(R.drawable.add_image_icon);
                                    videoPostViewer.setVideoURI(null);
                                    videoPostViewer.setVisibility(View.INVISIBLE);
                                    addPostImageView.setVisibility(View.VISIBLE);

                            /*        Handler handler = new Handler();
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            ProfileFragment profileFragment = new ProfileFragment();
                                            FragmentManager fragmentManager = getSupportFragmentManager();
                                            fragmentManager.beginTransaction().replace(R.id.frameLayout, profileFragment).commit();
                                            MainActivity.bottomNavigation.setSelectedItemId(R.id.navProfile);
                                        }
                                    };
                                    handler.postDelayed(runnable,5000); */

                                }else{
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this,"Failed to upload post: " + Objects.requireNonNull(error).getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddPostActivity.this,MainActivity.class)); // handling back press to reach Home Fragment
    }
}