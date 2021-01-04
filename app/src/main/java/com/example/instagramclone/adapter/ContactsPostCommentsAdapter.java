package com.example.instagramclone.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.CommentsInfoModelClass;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsPostCommentsAdapter extends RecyclerView.Adapter<ContactsPostCommentsAdapter.ContactsPostCommentsViewHolder> {

    private Context context;
    private List<CommentsInfoModelClass> commentsInfoModelClassList;

    public ContactsPostCommentsAdapter(Context context, List<CommentsInfoModelClass> commentsInfoModelClassList) {
        this.context = context;
        this.commentsInfoModelClassList = commentsInfoModelClassList;
    }

    private StorageReference storageReference;

    @NonNull
    @Override
    public ContactsPostCommentsAdapter.ContactsPostCommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comments_published_layout,parent,false); // attaching layout to Recycler View to display posts uploaded
        return new ContactsPostCommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsPostCommentsAdapter.ContactsPostCommentsViewHolder holder, int position) {
        CommentsInfoModelClass commentsInfoModelClass = commentsInfoModelClassList.get(position);

        storageReference = FirebaseStorage.getInstance().getReference(); // reference to storage of database

        // retrieving comments info from database

        String commentPublisherId = commentsInfoModelClass.getCommentPublisherId();

        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(commentPublisherId);
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.commentPublisherImageView); // loading profile image
            }
        });
        String commentPublisherName = commentsInfoModelClass.getCommentPublisherName();
        holder.commentPublisherProfileNameTextView.setText(commentPublisherName);

        String comment = commentsInfoModelClass.getCommentValue();
        holder.commentTextView.setText(comment);

        // getting full comment on clicking comment

        holder.commentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("InflateParams")
                View alertView = LayoutInflater.from(context).inflate(R.layout.full_comment_alert_dialogue,null);

                AlertDialog alertDialog = new AlertDialog.Builder(context).setView(alertView).create();
                alertDialog.show();

                CircleImageView publisherProfileImageView = alertView.findViewById(R.id.publisherProfileImageView);
                TextView publisherName = alertView.findViewById(R.id.publisherName);
                TextView fullCommentTextView = alertView.findViewById(R.id.fullCommentTextView);

                StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(commentPublisherId);
                profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).placeholder(R.drawable.profile).into(publisherProfileImageView);
                    }
                });

                publisherName.setText(commentPublisherName);
                fullCommentTextView.setText(comment);
            }
        });

        String date = commentsInfoModelClass.getCommentDate();
        holder.commentDateTextView.setText(date);

        String time = commentsInfoModelClass.getCommentTime();
        holder.commentTimeTextView.setText(time);

    }

    @Override
    public int getItemCount() {
        return commentsInfoModelClassList.size();
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class ContactsPostCommentsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView commentPublisherImageView;
        TextView commentPublisherProfileNameTextView, commentTextView, commentDateTextView, commentTimeTextView;

        public ContactsPostCommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            commentPublisherImageView = itemView.findViewById(R.id.commentPublisherImageView);
            commentPublisherProfileNameTextView = itemView.findViewById(R.id.commentPublisherProfileNameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            commentDateTextView = itemView.findViewById(R.id.commentDateTextView);
            commentTimeTextView = itemView.findViewById(R.id.commentTimeTextView);
        }
    }
}
