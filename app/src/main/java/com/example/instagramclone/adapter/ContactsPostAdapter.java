package com.example.instagramclone.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.MainActivity;
import com.example.instagramclone.PostsCommentsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.ShowUsersActivity;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.PostsInfoModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Node;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsPostAdapter extends RecyclerView.Adapter<ContactsPostAdapter.ContactsPostViewHolder> {

    private Context context;
    private List<PostsInfoModelClass> postsInfoModelClassList;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userDatabaseReference, postsDatabaseReference, postLikesDatabaseReference, postReportsDatabaseReference, commentsDatabaseReference, savedPostsDatabaseReference, notificationDatabaseReference, databaseReference;
    private StorageReference storageReference;

    HashMap<String,Object> notificationsHashMap;
    private String currentUserId, currentUserName;

    public ContactsPostAdapter(Context context, List<PostsInfoModelClass> postsInfoModelClassList) {
        this.context = context;
        this.postsInfoModelClassList = postsInfoModelClassList;
    }

    @NonNull
    @Override
    public ContactsPostAdapter.ContactsPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_posts_display_layout,parent,false); // attaching layout to Recycler View to display posts uploaded
        return new ContactsPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsPostAdapter.ContactsPostViewHolder holder, int position) {

        PostsInfoModelClass postsInfoModelClass = postsInfoModelClassList.get(position); // getting reference for particular item of Recycler View

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // reference to nodes of database

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseReference = databaseReference.child(NodeNames.USERS);
        postsDatabaseReference = databaseReference.child(NodeNames.POSTS);
        postLikesDatabaseReference = databaseReference.child(NodeNames.POSTLIKES);
        postReportsDatabaseReference = databaseReference.child(NodeNames.POSTREPORTS);
        commentsDatabaseReference = databaseReference.child(NodeNames.POSTCOMMENTS);
        savedPostsDatabaseReference = databaseReference.child(NodeNames.SAVEDPOSTS);
        notificationDatabaseReference = databaseReference.child(NodeNames.NOTIFICATIONS);

        // retrieving current user name from database

        userDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // retrieving post info from model class

        String postId = postsInfoModelClass.getPostId();
        String publisherId = postsInfoModelClass.getPostPublisherId();
        String postType = postsInfoModelClass.getPostType();

        if(postType.equals(Constants.POSTIMAGE)){
            StorageReference postImage = storageReference.child(Constants.POSTIMAGESFOLDER).child(postId).child(publisherId); // retrieving post image from database
            postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.postImageView); // loading image to icon image
                    holder.postTypeTextView.setText("(" + Constants.POSTIMAGE + ")");
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            intent.setDataAndType(uri,"image/jpg"); // allowing user to view image
                            context.startActivity(intent);
                        }
                    });
                }
            });
        }else{
            StorageReference postVideo = storageReference.child(Constants.POSTVIDEOSFOLDER).child(postId).child(publisherId); // retrieving post video from database
            postVideo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.postImageView); // loading video to icon image
                    holder.postTypeTextView.setText("(" + Constants.POSTVIDEO + ")");
                    holder.postImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                            intent.setDataAndType(uri,"video/mp4"); // allowing user to view video
                            context.startActivity(intent);
                        }
                    });
                }
            });
        }

        // retrieving info of Post Publisher from database

        userDatabaseReference.child(publisherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild(NodeNames.PROFILENAME)){
                        String profileName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                        holder.postPublisherNameTextView.setText(profileName);
                    }
                    StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(publisherId);
                    profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.postPublisherImageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // retrieving info of Post from database

        postsDatabaseReference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild(NodeNames.POSTDESCRIPTION)){
                        String description = Objects.requireNonNull(snapshot.child(NodeNames.POSTDESCRIPTION).getValue()).toString();
                        holder.descriptionTextView.setText(description);
                    }
                    if(snapshot.hasChild(NodeNames.POSTTIME)){
                        String postTime = Objects.requireNonNull(snapshot.child(NodeNames.POSTTIME).getValue()).toString();
                        holder.timeTextView.setText(postTime);
                    }
                    if(snapshot.hasChild(NodeNames.POSTDATE)){
                        String postDate = Objects.requireNonNull(snapshot.child(NodeNames.POSTDATE).getValue()).toString();
                        holder.dateTextView.setText(postDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postLikeStatus(postId,holder.heartNotClickedImageView);
        numberOfPostLikes(postId,holder.numberOfLikesTextView);

        // liking or disliking a post

        holder.heartNotClickedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.heartNotClickedImageView.getTag() == Constants.POSTNOTLIKED){
                    postLikesDatabaseReference.child(postId).child(currentUserId).setValue(Constants.POSTLIKED); // updating in database that post has been liked
                    addLikedNotification(postId,publisherId,postType);
                }else{
                    postLikesDatabaseReference.child(postId).child(currentUserId).setValue(null); // updating in database that post has been disliked
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }
        });

        numberOfPostComments(postId,holder.numberOfCommentsTextView);

        // passing intent to add comment on post

        holder.commentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostsCommentsActivity.class);
                intent.putExtra("Post Id",postId);
                intent.putExtra("Post Type",postType);
                intent.putExtra("Post Publisher Id",publisherId);
                context.startActivity(intent);
            }
        });

        checkSavedPostStatus(postId, holder.unsavedPostImageView);

        // adding or removing post from saved list

        holder.unsavedPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.unsavedPostImageView.getTag() == Constants.POSTNOTSAVED){
                    savedPostsDatabaseReference.child(currentUserId).child(postId).setValue(Constants.POSTSAVED);
                }else{
                    savedPostsDatabaseReference.child(currentUserId).child(postId).setValue(null);
                }
                Toast.makeText(context,"clicked",Toast.LENGTH_SHORT).show();
            }
        });

        // passing intent to show users who liked the post

        holder.numberOfLikesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowUsersActivity.class);
                intent.putExtra("category title","Likes");
                intent.putExtra("category id",postId);
                context.startActivity(intent);
            }
        });

        // reporting post for inappropriate content on Long click

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View alertView = LayoutInflater.from(context).inflate(R.layout.report_post_alert_dialogue,null);

                TextView reportTextView = alertView.findViewById(R.id.reportTextView);
                TextView discardTextView = alertView.findViewById(R.id.discardTextView);

                AlertDialog alertDialog = new AlertDialog.Builder(context).setView(alertView).setCancelable(false).create(); // custom alert dialog box
                alertDialog.show();

                reportTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postReportsDatabaseReference.child(postId).child(currentUserId).setValue(Constants.POSTREPORTED).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,"Post Reported",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(context,"Failed to Report: " + task.getException(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        alertDialog.dismiss();
                    }
                });

                discardTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsInfoModelClassList.size();
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class ContactsPostViewHolder extends RecyclerView.ViewHolder{

        CircleImageView postPublisherImageView;
        TextView postPublisherNameTextView, numberOfLikesTextView, numberOfCommentsTextView, descriptionTextView, commentsTextView, dateTextView, timeTextView, postTypeTextView;
        ImageView postImageView, heartNotClickedImageView, commentImageView, unsavedPostImageView;

        public ContactsPostViewHolder(@NonNull View itemView) {
            super(itemView);

            postPublisherImageView = itemView.findViewById(R.id.postPublisherImageView);
            postPublisherNameTextView = itemView.findViewById(R.id.postPublisherNameTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            heartNotClickedImageView = itemView.findViewById(R.id.heartNotClickedImageView);
            commentImageView = itemView.findViewById(R.id.commentImageView);
            unsavedPostImageView = itemView.findViewById(R.id.unsavedPostImageView);
            numberOfLikesTextView = itemView.findViewById(R.id.numberOfLikesTextView);
            numberOfCommentsTextView = itemView.findViewById(R.id.numberOfCommentsTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            commentsTextView = itemView.findViewById(R.id.commentsTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            postTypeTextView = itemView.findViewById(R.id.postTypeTextView);
        }
    }

    // checking liked status of particular post by current user and setting Tag accordingly

    private void postLikeStatus(String postId, ImageView heartNotClickedImageView) {

        postLikesDatabaseReference.child(postId).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    heartNotClickedImageView.setImageResource(R.drawable.heart_clicked);
                    heartNotClickedImageView.setTag(Constants.POSTLIKED);
                }else {
                    heartNotClickedImageView.setImageResource(R.drawable.heart_not_clicked);
                    heartNotClickedImageView.setTag(Constants.POSTNOTLIKED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving number of post likes from database

    private void numberOfPostLikes(String postId, TextView numberOfLikesTextView) {

        postLikesDatabaseReference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    numberOfLikesTextView.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving number of post comments from database

    private void numberOfPostComments(String postId, TextView numberOfCommentsTextView) {
        commentsDatabaseReference.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfCommentsTextView.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // checking saved post status and setting Tag accordingly

    private void checkSavedPostStatus(String postId, ImageView unsavedPostImageView) {

        savedPostsDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(postId)){
                    unsavedPostImageView.setImageResource(R.drawable.save_large_icon);
                    unsavedPostImageView.setTag(Constants.POSTSAVED);
                }else{
                    unsavedPostImageView.setImageResource(R.drawable.save_unfilled_large_icon);
                    unsavedPostImageView.setTag(Constants.POSTNOTSAVED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // updating post liked notification info in database

    private void addLikedNotification(String postId, String publisherId, String postType) {
        String notificationId = notificationDatabaseReference.child(publisherId).push().getKey();

        notificationsHashMap = new HashMap<>();
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERID,currentUserId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONTEXT,"liked your post");
        notificationsHashMap.put(NodeNames.NOTIFICATIONPOSTID,postId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONID,notificationId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONPOSTTYPE,postType);
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERPROFILENAME,currentUserName);
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

        String notificationReference = NodeNames.NOTIFICATIONS + "/" + publisherId + "/" + notificationId; // reference path for notifications in database

        HashMap<String,Object> notificationNodeHashMap = new HashMap<>();
        notificationNodeHashMap.put(notificationReference,notificationsHashMap);

        databaseReference.updateChildren(notificationNodeHashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null){
                    Toast.makeText(context,"Failed to push notification",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
