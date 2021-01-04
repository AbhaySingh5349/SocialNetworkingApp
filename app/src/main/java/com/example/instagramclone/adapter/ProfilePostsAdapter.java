package com.example.instagramclone.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.instagramclone.fragments.ProfilePostDetailsFragment;
import com.example.instagramclone.model.PostsInfoModelClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.MyPostsViewHolder> {

    private Context context;
    private List<PostsInfoModelClass> myPostsInfoModelClassList;

    public ProfilePostsAdapter(Context context, List<PostsInfoModelClass> myPostsInfoModelClassList) {
        this.context = context;
        this.myPostsInfoModelClassList = myPostsInfoModelClassList;
    }

    private StorageReference storageReference;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    @NonNull
    @Override
    public ProfilePostsAdapter.MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_posts_layout,parent,false); // attaching layout to Recycler View to display posts uploaded
        return new MyPostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostsAdapter.MyPostsViewHolder holder, int position) {

        PostsInfoModelClass postsInfoModelClass = myPostsInfoModelClassList.get(position); // getting reference for particular item of Recycler View

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = context.getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);

        storageReference = FirebaseStorage.getInstance().getReference(); // getting reference for particular item of Recycler View

        // retrieving info about post

        String postId = postsInfoModelClass.getPostId();
        String postType = postsInfoModelClass.getPostType();
        String publisherId = postsInfoModelClass.getPostPublisherId();

        if(postType.equals(Constants.POSTIMAGE)){
            StorageReference postImage = storageReference.child(Constants.POSTIMAGESFOLDER).child(postId).child(publisherId);
            postImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.profilePostsImageView);
                    holder.profilePostTypeTextView.setText("(" + Constants.POSTIMAGE + ")");
                }
            });
        }else{
            StorageReference postVideo = storageReference.child(Constants.POSTVIDEOSFOLDER).child(postId).child(publisherId);
            postVideo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).placeholder(R.drawable.add_image_icon).into(holder.profilePostsImageView);
                    holder.profilePostTypeTextView.setText("(" + Constants.POSTVIDEO + ")");
                }
            });
        }

        // passing intent to ProfilePostDetailsFragment

        holder.profilePostsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putString("profile post Id",postId).apply();

                ProfilePostDetailsFragment profilePostDetailsFragment = new ProfilePostDetailsFragment();
                FragmentManager fragmentManager = ((FragmentActivity)context).getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frameLayout, profilePostDetailsFragment).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return myPostsInfoModelClassList.size();
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePostsImageView;
        TextView profilePostTypeTextView;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePostsImageView = itemView.findViewById(R.id.profilePostsImageView);
            profilePostTypeTextView = itemView.findViewById(R.id.profilePostTypeTextView);
        }
    }
}
