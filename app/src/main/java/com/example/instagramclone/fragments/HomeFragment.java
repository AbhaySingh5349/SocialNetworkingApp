package com.example.instagramclone.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.EditProfileActivity;
import com.example.instagramclone.HashTagsListActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.adapter.ContactsPostAdapter;
import com.example.instagramclone.adapter.StoryAdapter;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.PostsInfoModelClass;
import com.example.instagramclone.model.StoryModelClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    /* displaying all the post uploaded by user and its friends */

    private RecyclerView storyRecyclerView, postsRecyclerView;
    private ImageView hashTagFragmentImageView;

    private DatabaseReference storiesDatabaseReference, contactsDatabaseReference, postsDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String currentUserId;

    ArrayList<String> contactsArrayList;

    private StoryAdapter storyAdapter;
    private List<StoryModelClass> storyModelClassList;

    private ContactsPostAdapter contactsPostAdapter;
    private List<PostsInfoModelClass> postsInfoModelClassList;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        storyModelClassList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(),storyModelClassList);

        postsInfoModelClassList = new ArrayList<>(); // creating Array List consisting Post Information
        contactsPostAdapter = new ContactsPostAdapter(getContext(),postsInfoModelClassList);

        storyRecyclerView = view.findViewById(R.id.storyRecyclerView);
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        hashTagFragmentImageView = view.findViewById(R.id.hashTagFragmentImageView);

        hashTagFragmentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HashTagsListActivity.class)); // starting HashTagsList Activity
            }
        });

        LinearLayoutManager storyLinearLayoutManager = new LinearLayoutManager(getContext());
        storyLinearLayoutManager.setReverseLayout(true); // to show new story in beginning
        storyLinearLayoutManager.setStackFromEnd(true);
        storyRecyclerView.setLayoutManager(storyLinearLayoutManager);

        storyRecyclerView.setAdapter(storyAdapter); // attaching adapter to Recycler View

        LinearLayoutManager postsLinearLayoutManager = new LinearLayoutManager(getContext());
        postsLinearLayoutManager.setReverseLayout(true); // to show new post at top
        postsLinearLayoutManager.setStackFromEnd(true);
        postsRecyclerView.setLayoutManager(postsLinearLayoutManager);

        postsRecyclerView.setAdapter(contactsPostAdapter); // attaching adapter to Recycler View

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // reference to nodes of database

        storiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.STORIES); // accessing Story node in database
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.CONTACTS); // accessing User Contacts node in database
        postsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.POSTS); // accessing Posts node in database

        contactsArrayList = new ArrayList<>(); // creating array list consisting of current users contacts

        // storing current users friend ids in array list

        contactsDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    contactsArrayList.clear(); // removing preciously stored ids
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        contactsArrayList.add(dataSnapshot.getKey()); // adding contacts ids to array list
                    }
                    retrieveContactsPosts();
                    retrieveContactsStories();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return view;
    }

    private void retrieveContactsStories() {

        storiesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    storyModelClassList.clear();

                    long currentTime = System.currentTimeMillis();

                    storyModelClassList.add(new StoryModelClass(currentUserId,"","","","","","",0,0,0));
                    storyAdapter.notifyDataSetChanged();

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        StoryModelClass storyModelClass = dataSnapshot.getValue(StoryModelClass.class);
                        String storyId = dataSnapshot.getKey();
                        String publisherId = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(storyId)).child(NodeNames.STORYPUBLISHERID).getValue()).toString();

                        for (String contactsId : contactsArrayList){

                            if(publisherId.equals(contactsId)){
                                if(currentTime> Objects.requireNonNull(storyModelClass).getStoryTimeStart() && currentTime<storyModelClass.getStoryTimeEnd()){
                                    storyModelClassList.add(storyModelClass);
                                    storyAdapter.notifyDataSetChanged();
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

    private void retrieveContactsPosts() {

        postsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postsInfoModelClassList.clear(); // removing previously stored data

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        PostsInfoModelClass postsInfoModelClass = dataSnapshot.getValue(PostsInfoModelClass.class); // storing all info from database about post
                        String postId = dataSnapshot.getKey(); // retrieving post id
                        String publisherId = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(postId)).child(NodeNames.POSTPUBLISHERID).getValue()).toString(); // retrieving post publisher id

                        for (String contactsId : contactsArrayList){
                            if(publisherId.equals(contactsId)){
                                postsInfoModelClassList.add(postsInfoModelClass); // adding post info if it belongs to users contacts post
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