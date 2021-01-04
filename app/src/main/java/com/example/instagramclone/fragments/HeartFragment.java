package com.example.instagramclone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instagramclone.R;
import com.example.instagramclone.adapter.NotificationsAdapter;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.NotificationModelClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HeartFragment extends Fragment {

    /* displaying all the notifications like:
     friend request received, friend request accepted, post liked
     */

    private RecyclerView notificationsRecyclerView;
    private NotificationsAdapter notificationsAdapter;
    private List<NotificationModelClass> notificationModelClassList;

    private DatabaseReference databaseReference, notificationDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String currentUserId;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HeartFragment() {
        // Required empty public constructor
    }

    public static HeartFragment newInstance(String param1, String param2) {
        HeartFragment fragment = new HeartFragment();
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
        View view= inflater.inflate(R.layout.fragment_heart, container, false);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // reference to nodes of database

        databaseReference = FirebaseDatabase.getInstance().getReference();
        notificationDatabaseReference = databaseReference.child(NodeNames.NOTIFICATIONS);

        notificationModelClassList = new ArrayList<>(); // creating Array List consisting Notification Information
        notificationsAdapter = new NotificationsAdapter(getContext(),notificationModelClassList);

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(notificationsAdapter); // attaching adapter to Recycler View
        
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        notificationDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    notificationModelClassList.clear(); // removing previously stored data

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        NotificationModelClass notificationModelClass = dataSnapshot.getValue(NotificationModelClass.class); // storing all info from database about notification
                        notificationModelClassList.add(notificationModelClass); // adding notifications info
                    }
                    Collections.reverse(notificationModelClassList); // loading notifications in reverse order
                    notificationsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}