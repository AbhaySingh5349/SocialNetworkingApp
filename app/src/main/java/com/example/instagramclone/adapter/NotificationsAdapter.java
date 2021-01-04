package com.example.instagramclone.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.fragments.ProfilePostDetailsFragment;
import com.example.instagramclone.model.NotificationModelClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {

    private Context context;
    private List<NotificationModelClass> notificationModelClassList;

    public NotificationsAdapter(Context context, List<NotificationModelClass> notificationModelClassList) {
        this.context = context;
        this.notificationModelClassList = notificationModelClassList;
    }

    private DatabaseReference databaseReference, postsDatabaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String currentUserId;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    @NonNull
    @Override
    public NotificationsAdapter.NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item_layout,parent,false); // attaching layout to Recycler View to display posts uploaded
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.NotificationsViewHolder holder, int position) {

        NotificationModelClass notificationModelClass = notificationModelClassList.get(position); // getting reference for particular item of Recycler View

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = context.getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // reference to nodes of database

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // retrieving notifications info from database

        String publisherName = notificationModelClass.getNotificationSenderProfileName();
        holder.notificationProfileNameTextView.setText(publisherName);

        String publisherId = notificationModelClass.getNotificationSenderId();
        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(publisherId);
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.notificationProfileImageView);
            }
        });

        String notificationMessage = notificationModelClass.getNotificationText();
        holder.notificationTextView.setText(notificationMessage);

        String isPost = notificationModelClass.getIsPost();

        // checking if notification is of post or about request

        if(isPost.equals("true")){
            holder.postImageView.setVisibility(View.VISIBLE);

            String postType = notificationModelClass.getNotificationPostType();
            String postId = notificationModelClass.getNotificationPostId();

            if(postType.equals(Constants.POSTIMAGE)){
                StorageReference postImage = storageReference.child(Constants.POSTIMAGESFOLDER).child(postId).child(currentUserId);
                postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.postImageView); // loading post photo in image view
                    }
                });
            }else{
                StorageReference postVideo = storageReference.child(Constants.POSTVIDEOSFOLDER).child(postId).child(currentUserId);
                postVideo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.postImageView); // loading post video in image view
                    }
                });
            }

            // passing intent to ProfilePostDetailsFragment

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferences.edit().putString("profile post Id",postId).apply();

                    ProfilePostDetailsFragment profilePostDetailsFragment = new ProfilePostDetailsFragment();
                    FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frameLayout, profilePostDetailsFragment).commit();
                }
            });
        }else{
            holder.postImageView.setVisibility(View.GONE);

            // passing intent to ProfileFragment

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferences.edit().putString("profile Id",publisherId).apply();

                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frameLayout, profileFragment).commit();
                }
            });
        }

        String notificationDate = notificationModelClass.getNotificationDate();
        holder.dateTextView.setText(notificationDate);

        String notificationTime = notificationModelClass.getNotificationTime();
        holder.timeTextView.setText(notificationTime);
    }

    @Override
    public int getItemCount() {
        return notificationModelClassList.size();
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView notificationProfileImageView;
        private TextView notificationProfileNameTextView, notificationTextView, dateTextView, timeTextView;
        private ImageView postImageView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationProfileImageView = itemView.findViewById(R.id.notificationProfileImageView);
            notificationProfileNameTextView = itemView.findViewById(R.id.notificationProfileNameTextView);
            notificationTextView = itemView.findViewById(R.id.notificationTextView);
            postImageView= itemView.findViewById(R.id.postImageView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
