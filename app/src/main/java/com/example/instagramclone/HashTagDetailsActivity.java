package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.instagramclone.adapter.ContactsPostAdapter;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.PostsInfoModelClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HashTagDetailsActivity extends AppCompatActivity {

    /* retrieving post belonging to particular HashTags */

    @BindView(R.id.hashTagNameTextView)
    TextView hashTagNameTextView;
    @BindView(R.id.postCountTextView)
    TextView postCountTextView;
    @BindView(R.id.hashTagRecyclerView)
    RecyclerView hashTagRecyclerView;

    private DatabaseReference hashTagsDatabaseReference, postsDatabaseReference;

    private ContactsPostAdapter contactsPostAdapter;
    private List<PostsInfoModelClass> postsInfoModelClassList;

    ArrayList<String> postsArrayList;

    String hashTagName, hashTagCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tag_details);
        ButterKnife.bind(this);

        postsInfoModelClassList = new ArrayList<>();
        contactsPostAdapter = new ContactsPostAdapter(HashTagDetailsActivity.this,postsInfoModelClassList);

        hashTagName = getIntent().getStringExtra("Hash Tag Name"); // receiving intent from HashTagList Activity
        hashTagCount = getIntent().getStringExtra("Hash Tag Posts Count"); // receiving intent from HashTagList Activity

        hashTagNameTextView.setText(hashTagName);
        postCountTextView.setText(hashTagCount);

        hashTagsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.HASHTAGS); // accessing HashTag node in database
        postsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.POSTS); // accessing Post node in database

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); // to get latest hashTag post at top
        linearLayoutManager.setStackFromEnd(true);
        hashTagRecyclerView.setLayoutManager(linearLayoutManager);

        hashTagRecyclerView.setAdapter(contactsPostAdapter); // attaching adapter to Recycler View

        postsArrayList = new ArrayList<>();

        // storing post ids belonging to particular HashTag

        hashTagsDatabaseReference.child(hashTagName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postsArrayList.clear(); // removing preciously stored post ids
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        postsArrayList.add(dataSnapshot.getKey()); // add post ids
                    }
                    retrieveHashTagPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving HashTag posts details

    private void retrieveHashTagPosts() {
        postsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postsInfoModelClassList.clear(); // removing previously stored data

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        PostsInfoModelClass postsInfoModelClass = dataSnapshot.getValue(PostsInfoModelClass.class); // storing all info from database about post
                        String postId = dataSnapshot.getKey(); // retrieving post id

                        for (String hashTagId : postsArrayList){
                            if(Objects.requireNonNull(postId).equals(hashTagId)){
                                postsInfoModelClassList.add(postsInfoModelClass); // adding post info if it belongs to users post
                                contactsPostAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}