package com.example.instagramclone.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instagramclone.R;
import com.example.instagramclone.adapter.ContactsPostAdapter;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.PostsInfoModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilePostDetailsFragment extends Fragment {

    /* displaying details of particular post */

    private RecyclerView postDetailsRecyclerView;

    private ContactsPostAdapter myContactsPostAdapter;
    private List<PostsInfoModelClass> myPostsInfoModelClassList;

    private DatabaseReference postsDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String currentUserId, profilePostId;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfilePostDetailsFragment() {
        // Required empty public constructor
    }

    public static ProfilePostDetailsFragment newInstance(String param1, String param2) {
        ProfilePostDetailsFragment fragment = new ProfilePostDetailsFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile_post_details, container, false);

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = getContext().getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);
        profilePostId = sharedPreferences.getString("profile post Id",null);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();
        
        postsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.POSTS); // reference to posts node of database

        postDetailsRecyclerView = view.findViewById(R.id.postDetailsRecyclerView);
        postDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myPostsInfoModelClassList = new ArrayList<>();
        myContactsPostAdapter = new ContactsPostAdapter(getContext(),myPostsInfoModelClassList);
        postDetailsRecyclerView.setAdapter(myContactsPostAdapter); // attaching adapter to Recycler View

        retrieveMyPost();

        return view;
    }

    // retrieving post information

    private void retrieveMyPost() {

        postsDatabaseReference.child(profilePostId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    myPostsInfoModelClassList.clear(); // clearing previously stored data
                    PostsInfoModelClass postsInfoModelClass = snapshot.getValue(PostsInfoModelClass.class); // storing info about the post
                    myPostsInfoModelClassList.add(postsInfoModelClass);
                    myContactsPostAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}