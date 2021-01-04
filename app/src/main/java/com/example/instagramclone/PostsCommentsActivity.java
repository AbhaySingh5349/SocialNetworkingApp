package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagramclone.adapter.ContactsPostCommentsAdapter;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.CommentsInfoModelClass;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostsCommentsActivity extends AppCompatActivity {

    @BindView(R.id.postImageView)
    ImageView postImageView;
    @BindView(R.id.commentsRecyclerView)
    RecyclerView commentsRecyclerView;
    @BindView(R.id.commentPublisherImageView)
    CircleImageView commentPublisherImageView;
    @BindView(R.id.commentEditText)
    EditText commentEditText;
    @BindView(R.id.publishCommentImageView)
    ImageView publishCommentImageView;

    private ContactsPostCommentsAdapter contactsPostCommentsAdapter;
    private List<CommentsInfoModelClass> commentsInfoModelClassList;

    FirebaseAuth firebaseAuth; // to create object of Firebase Auth class to fetch currently loged in user
    FirebaseUser firebaseUser; // to create object of Firebase User class to get current user to store currently loged in user

    DatabaseReference databaseReference, userDatabaseReference, postDatabaseReference, commentsDatabaseReference, notificationDatabaseReference;
    StorageReference storageReference;

    HashMap<String,Object> commentHashMap, notificationsHashMap;
    String currentUserId, postId, postType, postPublisherId, commentValue, currentProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_comments);
        ButterKnife.bind(this);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // accessing Database nodes

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseReference = databaseReference.child(NodeNames.USERS);
        postDatabaseReference = databaseReference.child(NodeNames.POSTS);
        commentsDatabaseReference = databaseReference.child(NodeNames.POSTCOMMENTS);
        notificationDatabaseReference = databaseReference.child(NodeNames.NOTIFICATIONS);

        // intents from Contacts post adapter

        postId = getIntent().getStringExtra("Post Id");
        postType = getIntent().getStringExtra("Post Type");
        postPublisherId = getIntent().getStringExtra("Post Publisher Id");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); // to have latest comment at top
        linearLayoutManager.setStackFromEnd(true);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);

        commentsInfoModelClassList = new ArrayList<>();
        contactsPostCommentsAdapter = new ContactsPostCommentsAdapter(this,commentsInfoModelClassList);
        commentsRecyclerView.setAdapter(contactsPostCommentsAdapter);

        // retrieving comment publisher profile image

        userDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild(NodeNames.PHOTOURL)){
                        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(currentUserId); // reference to user profile image in Storage
                        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(PostsCommentsActivity.this).load(uri).placeholder(R.drawable.profile).into(commentPublisherImageView);
                            }
                        });
                    }
                    if(snapshot.hasChild(NodeNames.PROFILENAME)){
                        currentProfileName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(postType.equals(Constants.POSTIMAGE)){
            StorageReference postImage = storageReference.child(Constants.POSTIMAGESFOLDER).child(postId).child(postPublisherId);
            postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(PostsCommentsActivity.this).load(uri).placeholder(R.drawable.add_image_icon).into(postImageView);
                    postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            intent.setDataAndType(uri,"image/jpg"); // allowing user to view image
                            startActivity(intent);
                        }
                    });
                }
            });
        }else{
            StorageReference postVideo = storageReference.child(Constants.POSTVIDEOSFOLDER).child(postId).child(postPublisherId);
            postVideo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(PostsCommentsActivity.this).load(uri).placeholder(R.drawable.add_image_icon).into(postImageView);
                    postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            intent.setDataAndType(uri,"video/mp4"); // allowing user to view video
                            startActivity(intent);
                        }
                    });
                }
            });
        }

        publishCommentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCommentCredentials();
            }
        });

        loadComments();
    }

    // retrieving comments of particular post

    private void loadComments() {
        commentsDatabaseReference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    commentsInfoModelClassList.clear(); // removing previously stored data

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        CommentsInfoModelClass commentsInfoModelClass = dataSnapshot.getValue(CommentsInfoModelClass.class); // storing all info from database about comments
                        commentsInfoModelClassList.add(commentsInfoModelClass);
                    }
                    contactsPostCommentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean validateComment(){
        commentValue = commentEditText.getText().toString().trim();
        if(commentValue.isEmpty()){
            Toast.makeText(PostsCommentsActivity.this,"Add comment to publish",Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    private void checkCommentCredentials() {
        if(!validateComment()){
            validateComment();
        }else{

            // updating comments info in database

            commentHashMap = new HashMap<>();
            commentValue = commentEditText.getText().toString().trim();

            String commentId = commentsDatabaseReference.child(currentUserId).push().getKey();

            commentHashMap.put(NodeNames.COMMENTPUBLISHERNAME,currentProfileName);
            commentHashMap.put(NodeNames.COMMENTPUBLISHERID,currentUserId);
            commentHashMap.put(NodeNames.COMMENTVALUE,commentValue);
            commentHashMap.put(NodeNames.COMMENTID,commentId);
            commentHashMap.put(NodeNames.COMMENTTIMESTAMP, ServerValue.TIMESTAMP);

            Calendar date = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy"); // Nov 26,2020
            String currentDate = currentDateFormat.format(date.getTime());
            commentHashMap.put(NodeNames.COMMENTDATE,currentDate);

            Calendar time = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a"); // 01:42 AM
            String currentTime = currentTimeFormat.format(time.getTime());
            commentHashMap.put(NodeNames.COMMENTTIME,currentTime);

            String commentReference = NodeNames.POSTCOMMENTS + "/" + postId + "/" + commentId;

            HashMap<String,Object> commentNodeHashMap = new HashMap<>();
            commentNodeHashMap.put(commentReference,commentHashMap);

            // updating comments database

            databaseReference.updateChildren(commentNodeHashMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if(error==null){
                        Toast.makeText(PostsCommentsActivity.this,"Comment published Successfully",Toast.LENGTH_SHORT).show();
                        addCommentNotification();
                        commentEditText.setText(null);
                    }else{
                        Toast.makeText(PostsCommentsActivity.this,"Failed to publish comment: " + Objects.requireNonNull(error).getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // adding notification about comment in database

    private void addCommentNotification() {

        String notificationId = notificationDatabaseReference.child(postPublisherId).push().getKey();

        notificationsHashMap = new HashMap<>();
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERID,currentUserId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONTEXT,"commented: " + commentEditText.getText().toString().trim());
        notificationsHashMap.put(NodeNames.NOTIFICATIONPOSTID,postId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONID,notificationId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERPROFILENAME,currentProfileName);
        notificationsHashMap.put(NodeNames.NOTIFICATIONPOSTTYPE,postType);
        notificationsHashMap.put("isPost","true");

        Calendar date = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy"); // Nov 26,2020
        String currentDate = currentDateFormat.format(date.getTime());
        notificationsHashMap.put(NodeNames.NOTIFICATIONDATE,currentDate);

        Calendar time = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a"); // 01:42 AM
        String currentTime = currentTimeFormat.format(time.getTime());
        notificationsHashMap.put(NodeNames.NOTIFICATIONTIME,currentTime);

        String notificationReference = NodeNames.NOTIFICATIONS + "/" + postPublisherId + "/" + notificationId;

        HashMap<String,Object> notificationNodeHashMap = new HashMap<>();
        notificationNodeHashMap.put(notificationReference,notificationsHashMap);

        // updating notifications database

        databaseReference.updateChildren(notificationNodeHashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null){
                    Toast.makeText(PostsCommentsActivity.this,"Failed to push notification",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}