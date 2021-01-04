package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddStoryActivity extends AppCompatActivity {

    @BindView(R.id.cancelStoryImageView)
    ImageView cancelStoryImageView;
    @BindView(R.id.uploadStoryImageView)
    ImageView uploadStoryImageView;
    @BindView(R.id.addStoryImageView)
    ImageView addStoryImageView;
    @BindView(R.id.videoStoryViewer)
    VideoView videoStoryViewer;
    @BindView(R.id.storyDescriptionTextInputLayout)
    TextInputLayout storyDescriptionTextInputLayout;
    @BindView(R.id.storyDescriptionTextInputEditText)
    TextInputEditText storyDescriptionTextInputEditText;

    FirebaseAuth firebaseAuth; // to create object of Firebase Auth class to fetch currently loged in user
    FirebaseUser firebaseUser; // to create object of Firebase User class to get current user to store currently loged in user
    MediaController mediaController;

    DatabaseReference databaseReference, storyDatabaseReference;
    StorageReference storageReference, storyStorageReference;
    Uri selectedImageUri, selectedVideoUri, selectedUri;

    HashMap<String,Object> storyHashMap;
    String currentUserId, storyDescription, storyType;

    int readExternalStorageRequestCode=102, photoPickRequestCode = 1005, videoPickRequestCode = 1006;

    private ProgressDialog progressDialog;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.upload_file_bottom_sheet_layout,null);
        bottomSheetDialog.setContentView(bottomSheetDialogView);
        bottomSheetDialog.setCancelable(false);

        if(firebaseUser!=null){
            addStoryImageView.setVisibility(View.VISIBLE);
            videoStoryViewer.setVisibility(View.INVISIBLE);

            addStoryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ActivityCompat.checkSelfPermission(AddStoryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        if(bottomSheetDialog != null){
                            bottomSheetDialog.show();
                        }
                    }else {
                        ActivityCompat.requestPermissions(AddStoryActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},readExternalStorageRequestCode);
                    }

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                }
            });

            bottomSheetDialogView.findViewById(R.id.photoImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,photoPickRequestCode);
                }
            });

            bottomSheetDialogView.findViewById(R.id.videoImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,videoPickRequestCode);
                }
            });

            bottomSheetDialogView.findViewById(R.id.closeImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetDialog.dismiss();
                }
            });

            uploadStoryImageView.setOnClickListener(new View.OnClickListener() {
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
                if(ContextCompat.checkSelfPermission(AddStoryActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                    if(bottomSheetDialog != null){
                        bottomSheetDialog.show();
                    }
                }else {
                    Toast.makeText(AddStoryActivity.this,"Permissions required to access files",Toast.LENGTH_SHORT).show();
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
                storyType = Constants.STORYIMAGE;
                addStoryImageView.setVisibility(View.VISIBLE);
                videoStoryViewer.setVisibility(View.INVISIBLE);
                addStoryImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        addStoryImageView.setImageURI(selectedImageUri);
                    }
                });

            }else if(requestCode == videoPickRequestCode){
                Uri uri = Objects.requireNonNull(data).getData();
                selectedVideoUri = uri;
                selectedImageUri = null;
                selectedUri = selectedVideoUri;
                storyType = Constants.STORYVIDEO;
                addStoryImageView.setVisibility(View.INVISIBLE);
                videoStoryViewer.setVisibility(View.VISIBLE);
                mediaController = new MediaController(this);
                videoStoryViewer.setMediaController(mediaController);
                videoStoryViewer.setVideoURI(selectedVideoUri);
                videoStoryViewer.start();
            }
        }
    }

    private boolean validateStoryDescription(){
        storyDescription = Objects.requireNonNull(storyDescriptionTextInputEditText.getText()).toString().trim();
        if(storyDescription.isEmpty()){
            storyDescriptionTextInputLayout.setErrorEnabled(true);
            storyDescriptionTextInputLayout.setError("Enter Story Description");
            return false;
        }else{
            storyDescriptionTextInputLayout.setErrorEnabled(false);
            storyDescriptionTextInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateStoryFile(){
        if(selectedImageUri!=null && selectedVideoUri==null){
            selectedUri = selectedImageUri;
            return true;
        }else if(selectedVideoUri!=null && selectedImageUri==null){
            selectedUri = selectedVideoUri;
            return true;
        } else{
            Toast.makeText(AddStoryActivity.this,"Add File",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void checkPostCredentials() {
        if(!validateStoryFile() | !validateStoryDescription()){
            validateStoryFile();
            validateStoryDescription();
        }else{
            progressDialog.setTitle("Uploading New Story");
            progressDialog.setMessage("Please wait while story is being uploaded");
            progressDialog.show();

            storyHashMap = new HashMap<>();

            storyDatabaseReference = databaseReference.child(NodeNames.STORIES).child(currentUserId).push();
            String storyId = storyDatabaseReference.getKey();

            databaseReference.child(NodeNames.USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(NodeNames.PROFILENAME)){
                        String profileName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                        storyHashMap.put(NodeNames.PROFILENAME,profileName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if(selectedImageUri!=null && selectedVideoUri==null){
                storyStorageReference = storageReference.child(Constants.STORYIMAGESFOLDER).child(Objects.requireNonNull(storyId)).child(currentUserId);
                selectedUri = selectedImageUri;
                storyType = Constants.STORYIMAGE;
            }else if(selectedVideoUri!=null && selectedImageUri==null){
                storyStorageReference = storageReference.child(Constants.STORYVIDEOSFOLDER).child(Objects.requireNonNull(storyId)).child(currentUserId);
                selectedUri = selectedVideoUri;
                storyType = Constants.STORYVIDEO;
            }

            long timeEnd = System.currentTimeMillis() + 86400000;

            storyStorageReference.putFile(selectedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        storyStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                selectedUri = uri;
                            }
                        });
                        storyHashMap.put(NodeNames.STORYVALUE,selectedUri.getPath());
                        storyHashMap.put(NodeNames.STORYDESCRIPTION,storyDescription);
                        storyHashMap.put(NodeNames.STORYTIMESTAMP, ServerValue.TIMESTAMP);
                        storyHashMap.put(NodeNames.STORYID,storyId);
                        storyHashMap.put(NodeNames.STORYPUBLISHERID,currentUserId);
                        storyHashMap.put(NodeNames.STORYTYPE,storyType);
                        storyHashMap.put(NodeNames.STORYTIMESTART,ServerValue.TIMESTAMP);
                        storyHashMap.put(NodeNames.STORYTIMEEND,timeEnd);

                        Calendar date = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
                        String currentDate = currentDateFormat.format(date.getTime());
                        storyHashMap.put(NodeNames.STORYDATE,currentDate);

                        Calendar time = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                        String currentTime = currentTimeFormat.format(time.getTime());
                        storyHashMap.put(NodeNames.STORYTIME,currentTime);

                        String storyReference = NodeNames.STORIES + "/" + storyId;

                        HashMap<String,Object> storyNodeHashMap = new HashMap<>();
                        storyNodeHashMap.put(storyReference,storyHashMap);

                        databaseReference.updateChildren(storyNodeHashMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if(error==null){
                                    progressDialog.dismiss();
                                    Toast.makeText(AddStoryActivity.this,"Story Uploaded Successfully",Toast.LENGTH_SHORT).show();
                                    storyDescriptionTextInputEditText.setText(null);
                                    addStoryImageView.setImageURI(null);
                                    addStoryImageView.setImageResource(R.drawable.add_image_icon);
                                    videoStoryViewer.setVideoURI(null);
                                    videoStoryViewer.setVisibility(View.INVISIBLE);
                                    addStoryImageView.setVisibility(View.VISIBLE);
                                }else{
                                    progressDialog.dismiss();
                                    Toast.makeText(AddStoryActivity.this,"Failed to upload story",Toast.LENGTH_SHORT).show();
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
        startActivity(new Intent(AddStoryActivity.this,MainActivity.class));
    }
}