package com.example.instagramclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.AddStoryActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.StoryModelClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private Context context;
    private List<StoryModelClass> storyModelClassList;

    public StoryAdapter(Context context, List<StoryModelClass> storyModelClassList) {
        this.context = context;
        this.storyModelClassList = storyModelClassList;
    }

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userDatabaseReference, storiesDatabaseReference;
    private StorageReference storageReference;

    private String currentUserId;

    @NonNull
    @Override
    public StoryAdapter.StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == 0){
            View view = LayoutInflater.from(context).inflate(R.layout.add_story_layout,parent,false);
            return new StoryViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.user_story_layout,parent,false);
            return new StoryViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.StoryViewHolder holder, int position) {
        StoryModelClass storyModelClass = storyModelClassList.get(position);

        String storyPublisherId = storyModelClass.getStoryPublisherId();
        String storyPublisherName = storyModelClass.getProfileName();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserId = Objects.requireNonNull(firebaseUser).getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        storiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.STORIES);

        userInfo(holder,storyPublisherId,position,storyPublisherName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddStoryActivity.class);
                intent.putExtra("story publisher Id",storyPublisherId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyModelClassList.size();
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        //add_story_layout
        CircleImageView storyProfileImageView, addImageView;
        TextView addStoryTextView;

        // user_story_layout
        CircleImageView userNewStoryImageView, userSeenStoryImageView;
        TextView userNameTextView;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // add_story_layout
            storyProfileImageView = itemView.findViewById(R.id.storyProfileImageView);
            addImageView = itemView.findViewById(R.id.addImageView);
            addStoryTextView = itemView.findViewById(R.id.addStoryTextView);

            // user_story_layout
            userNewStoryImageView = itemView.findViewById(R.id.userNewStoryImageView);
            userSeenStoryImageView = itemView.findViewById(R.id.userSeenStoryImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return 0; // add_story_layout
        }else{
            return 1; // user_story_layout
        }
    }

    private void userInfo(StoryViewHolder holder, String storyPublisherId, int position, String storyPublisherName) {

        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(storyPublisherId);
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(position==0){
                    Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.storyProfileImageView);
                }else{
                    Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.userSeenStoryImageView);
                    holder.userNameTextView.setText(storyPublisherName);
                }
            }
        });
    }
}
