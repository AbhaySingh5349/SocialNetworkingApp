package com.example.instagramclone.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.EditProfileActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.ShowUsersActivity;
import com.example.instagramclone.adapter.ProfilePostsAdapter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    /* displaying user details like : profile image, user bio, displaying post uploaded, post saved, post count, friends count
     handling friend requests */

    private TextView profileNameTextView, postsNumberTextView,followersTextView, followersNumberTextView, editProfileTextView, userNameTextView, bioTextView;
    private CircleImageView profileImageView;
    private ImageView gridImageView, savedImageView;

    private DatabaseReference databaseReference,userDatabaseReference, friendRequestDatabaseReference, contactsDatabaseReference, postsDatabaseReference, savedPostsDatabaseReference, notificationDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String currentUserId, profileId, currentUserName;

    SharedPreferences sharedPreferences;
    HashMap<String,Object> notificationsHashMap;

    private List<PostsInfoModelClass> myPostsInfoModelClassList, savedPostsInfoModelClassList;
    private RecyclerView myPostsRecyclerView, savedPostsRecyclerView;
    private ProfilePostsAdapter myPostsAdapter, savedPostAdapter;

    ArrayList<String> savedPostsArrayList;

    int myPostsCount = 0;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = getContext().getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);
        profileId = sharedPreferences.getString("profile Id","none"); // retrieving data from shared preferences

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        // accessing database nodes

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseReference = databaseReference.child(NodeNames.USERS);
        friendRequestDatabaseReference = databaseReference.child(NodeNames.FRIENDREQUESTS);
        contactsDatabaseReference = databaseReference.child(NodeNames.CONTACTS);
        postsDatabaseReference = databaseReference.child(NodeNames.POSTS);
        savedPostsDatabaseReference = databaseReference.child(NodeNames.SAVEDPOSTS);
        notificationDatabaseReference = databaseReference.child(NodeNames.NOTIFICATIONS);

        profileNameTextView = view.findViewById(R.id.profileNameTextView);
        postsNumberTextView = view.findViewById(R.id.postsNumberTextView);
        followersTextView = view.findViewById(R.id.followersTextView);
        followersNumberTextView = view.findViewById(R.id.followersNumberTextView);
        editProfileTextView = view.findViewById(R.id.editProfileTextView);
        userNameTextView = view.findViewById(R.id.userNameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        gridImageView = view.findViewById(R.id.gridImageView);
        savedImageView = view.findViewById(R.id.savedImageView);

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

        getFollowersCount();
        getProfileInfo();

        // checking if the user is viewing its own profile or some other user and handling friend requests

        if(profileId.equals(currentUserId)){
            editProfileTextView.setText("Edit Profile");
            editProfileTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
            });
            followersTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ShowUsersActivity.class);
                    intent.putExtra("category title","Friends");
                    intent.putExtra("category id",currentUserId);
                    startActivity(intent);
                }
            });
        }else{

            // managing different states of Friend type

            friendRequestDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(snapshot.hasChild(profileId)){
                            String requestType = Objects.requireNonNull(snapshot.child(profileId).child(NodeNames.REQUESTTYPE).getValue()).toString();

                            if(requestType.equals(Constants.FRIENDREQUESTSENT)){
                                editProfileTextView.setText("Cancel Request");
                                editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getContext(),"Request Cancelled Successfully",Toast.LENGTH_SHORT).show();
                                                                editProfileTextView.setText("Follow");
                                                            }else {
                                                                Toast.makeText(getContext(),"Failed to Cancel Request",Toast.LENGTH_SHORT).show();
                                                                editProfileTextView.setText("Cancel Request");
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                editProfileTextView.setText("Respond to request");
                                editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        View alertView = LayoutInflater.from(getContext()).inflate(R.layout.response_request_profile_fragment,null);

                                        CircleImageView respondProfileImageView = alertView.findViewById(R.id.respondProfileImageView);
                                        TextView requestUserNameTextView = alertView.findViewById(R.id.requestUserNameTextView);
                                        TextView acceptTextView = alertView.findViewById(R.id.acceptTextView);
                                        TextView declineTextView = alertView.findViewById(R.id.declineTextView);

                                        StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + profileId);
                                        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                if(getContext()==null){
                                                    return;
                                                }
                                                Glide.with(getContext()).load(uri).placeholder(R.drawable.profile).into(respondProfileImageView);
                                            }
                                        });

                                        userDatabaseReference.child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild(NodeNames.PROFILENAME)){
                                                    requestUserNameTextView.setText(Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setView(alertView).create();
                                        alertDialog.show();

                                        acceptTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                contactsDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.CONTACTSTATUS).setValue(Constants.CONTACTSAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            contactsDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.CONTACTSTATUS).setValue(Constants.CONTACTSAVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                Toast.makeText(getContext(),"Request Accepted",Toast.LENGTH_SHORT).show();
                                                                                                requestAcceptedNotification();
                                                                                                editProfileTextView.setText("Unfollow");
                                                                                                alertDialog.dismiss();
                                                                                            }else {
                                                                                                Toast.makeText(getContext(),"Failed to Accept Request",Toast.LENGTH_SHORT).show();
                                                                                                editProfileTextView.setText("Respond to request");
                                                                                                alertDialog.dismiss();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        declineTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Toast.makeText(getContext(),"Request Cancelled Successfully",Toast.LENGTH_SHORT).show();
                                                                        editProfileTextView.setText("Follow");
                                                                        alertDialog.dismiss();
                                                                    }else {
                                                                        Toast.makeText(getContext(),"Failed to Decline Request",Toast.LENGTH_SHORT).show();
                                                                        editProfileTextView.setText("Respond to request");
                                                                        alertDialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }else{
                            contactsDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        if(snapshot.hasChild(profileId)){
                                            editProfileTextView.setText("Unfollow");
                                            editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    View alertView = LayoutInflater.from(getContext()).inflate(R.layout.unfollow_alert_dialogue,null);

                                                    CircleImageView unfollowProfileImageView = alertView.findViewById(R.id.unfollowProfileImageView);
                                                    TextView unfollowedNameTextView = alertView.findViewById(R.id.unfollowedNameTextView);
                                                    TextView yesTextView = alertView.findViewById(R.id.yesTextView);
                                                    TextView noTextView = alertView.findViewById(R.id.noTextView);

                                                    StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + profileId);
                                                    profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            if(getContext()==null){
                                                                return;
                                                            }
                                                            Glide.with(getContext()).load(uri).placeholder(R.drawable.profile).into(unfollowProfileImageView);
                                                        }
                                                    });

                                                    userDatabaseReference.child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.hasChild(NodeNames.PROFILENAME)){
                                                                unfollowedNameTextView.setText(Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString());
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setView(alertView).create();
                                                    alertDialog.show();

                                                    yesTextView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            contactsDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.CONTACTSTATUS).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        contactsDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.CONTACTSTATUS).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(),"Unfollowed succesfully",Toast.LENGTH_SHORT).show();
                                                                                    editProfileTextView.setText("Follow");
                                                                                    alertDialog.dismiss();
                                                                                }else {
                                                                                    Toast.makeText(getContext(),"Failed to delete contact",Toast.LENGTH_SHORT).show();
                                                                                    editProfileTextView.setText("Following");
                                                                                    alertDialog.dismiss();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });

                                                    noTextView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            alertDialog.dismiss();
                                                        }
                                                    });
                                                }
                                            });
                                        }else{
                                            editProfileTextView.setText("Follow");
                                            editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTSENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTRECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Toast.makeText(getContext(),"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                            requestReceivedNotification();
                                                                            editProfileTextView.setText("Cancel Request");
                                                                        }else {
                                                                            Toast.makeText(getContext(),"Failed to Send Request",Toast.LENGTH_SHORT).show();
                                                                            editProfileTextView.setText("Follow");
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }else{
                                        editProfileTextView.setText("Follow");
                                        editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTSENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTRECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Toast.makeText(getContext(),"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                        requestReceivedNotification();
                                                                        editProfileTextView.setText("Cancel Request");
                                                                    }else {
                                                                        Toast.makeText(getContext(),"Failed to Send Request",Toast.LENGTH_SHORT).show();
                                                                        editProfileTextView.setText("Follow");
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }else {
                        contactsDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(snapshot.hasChild(profileId)){
                                        editProfileTextView.setText("Unfollow");
                                        editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                View alertView = LayoutInflater.from(getContext()).inflate(R.layout.unfollow_alert_dialogue,null);

                                                CircleImageView unfollowProfileImageView = alertView.findViewById(R.id.unfollowProfileImageView);
                                                TextView unfollowedNameTextView = alertView.findViewById(R.id.unfollowedNameTextView);
                                                TextView yesTextView = alertView.findViewById(R.id.yesTextView);
                                                TextView noTextView = alertView.findViewById(R.id.noTextView);

                                                StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + profileId);
                                                profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        if(getContext()==null){
                                                            return;
                                                        }
                                                        Glide.with(getContext()).load(uri).placeholder(R.drawable.profile).into(unfollowProfileImageView);
                                                    }
                                                });

                                                userDatabaseReference.child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.hasChild(NodeNames.PROFILENAME)){
                                                            unfollowedNameTextView.setText(Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setView(alertView).create();
                                                alertDialog.show();

                                                yesTextView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        contactsDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.CONTACTSTATUS).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    contactsDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.CONTACTSTATUS).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(getContext(),"Unfollowed succesfully",Toast.LENGTH_SHORT).show();
                                                                                editProfileTextView.setText("Follow");
                                                                                alertDialog.dismiss();
                                                                            }else {
                                                                                Toast.makeText(getContext(),"Failed to delete contact",Toast.LENGTH_SHORT).show();
                                                                                editProfileTextView.setText("Following");
                                                                                alertDialog.dismiss();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                                noTextView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        alertDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }else{
                                        editProfileTextView.setText("Follow");
                                        editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTSENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTRECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        Toast.makeText(getContext(),"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                        requestReceivedNotification();
                                                                        editProfileTextView.setText("Cancel Request");
                                                                    }else {
                                                                        Toast.makeText(getContext(),"Failed to Send Request",Toast.LENGTH_SHORT).show();
                                                                        editProfileTextView.setText("Follow");
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }else{
                                    editProfileTextView.setText("Follow");
                                    editProfileTextView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            friendRequestDatabaseReference.child(currentUserId).child(profileId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTSENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        friendRequestDatabaseReference.child(profileId).child(currentUserId).child(NodeNames.REQUESTTYPE).setValue(Constants.FRIENDREQUESTRECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getContext(),"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                    requestReceivedNotification();
                                                                    editProfileTextView.setText("Cancel Request");
                                                                }else {
                                                                    Toast.makeText(getContext(),"Failed to Send Request",Toast.LENGTH_SHORT).show();
                                                                    editProfileTextView.setText("Follow");
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        // displaying post in grid layout having 3 posts in a row

        LinearLayoutManager myPostsLinearLayoutManager = new GridLayoutManager(getContext(),3);
        LinearLayoutManager savedPostsLinearLayoutManager = new GridLayoutManager(getContext(),3);

        myPostsRecyclerView = view.findViewById(R.id.myPostsRecyclerView);
        myPostsRecyclerView.setLayoutManager(myPostsLinearLayoutManager);

        myPostsInfoModelClassList = new ArrayList<>();
        myPostsAdapter = new ProfilePostsAdapter(getContext(),myPostsInfoModelClassList);
        myPostsRecyclerView.setAdapter(myPostsAdapter);

        myPosts();

        savedPostsArrayList = new ArrayList<>();

        savedPostsRecyclerView = view.findViewById(R.id.savedPostsRecyclerView);
        savedPostsRecyclerView.setLayoutManager(savedPostsLinearLayoutManager);

        savedPostsInfoModelClassList = new ArrayList<>(); // creating Array List consisting Post Information
        savedPostAdapter = new ProfilePostsAdapter(getContext(),savedPostsInfoModelClassList);
        savedPostsRecyclerView.setAdapter(savedPostAdapter); // attaching adapter to Recycler View

        myPostsRecyclerView.setVisibility(View.VISIBLE);
        savedPostsRecyclerView.setVisibility(View.GONE);

        // retrieving ids of Post Saved by user and storing the ids in array list

        savedPostsDatabaseReference.child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    savedPostsArrayList.clear(); // removing preciously stored post ids
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        savedPostsArrayList.add(dataSnapshot.getKey()); // add post ids
                    }
                    retrieveSavedPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        gridImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPostsRecyclerView.setVisibility(View.VISIBLE);
                savedPostsRecyclerView.setVisibility(View.GONE);
            }
        });

        savedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPostsRecyclerView.setVisibility(View.GONE);
                savedPostsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    // retrieving number of friend of particular user

    private void getFollowersCount() {
        contactsDatabaseReference.child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int count = (int) snapshot.getChildrenCount();
                    followersNumberTextView.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving user profile info from database

    private void getProfileInfo() {
        userDatabaseReference.child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String profileName = Objects.requireNonNull(snapshot.child(NodeNames.PROFILENAME).getValue()).toString();
                    String userName = Objects.requireNonNull(snapshot.child(NodeNames.USERNAME).getValue()).toString();
                    String bio = Objects.requireNonNull(snapshot.child(NodeNames.BIO).getValue()).toString();

                    StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + profileId);
                    profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(getContext()==null){
                                return;
                            }
                            Glide.with(getContext()).load(uri).placeholder(R.drawable.profile).into(profileImageView);
                        }
                    });

                    profileNameTextView.setText(profileName);
                    userNameTextView.setText(userName);
                    bioTextView.setText(bio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving user uploaded posts from database

    private void myPosts(){

        postsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    myPostsInfoModelClassList.clear(); // removing previously stored data
                    myPostsCount = 0;

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                        PostsInfoModelClass postsInfoModelClass = dataSnapshot.getValue(PostsInfoModelClass.class); // storing all info from database about post
                        String postId = dataSnapshot.getKey(); // retrieving post id
                        String publisherId = Objects.requireNonNull(snapshot.child(Objects.requireNonNull(postId)).child(NodeNames.POSTPUBLISHERID).getValue()).toString(); // retrieving post publisher id

                        if(publisherId.equals(profileId)){
                            myPostsInfoModelClassList.add(postsInfoModelClass); // adding post info if it belongs to users post
                            myPostsCount++;
                        }
                        Collections.reverse(myPostsInfoModelClassList);
                        myPostsAdapter.notifyDataSetChanged();
                    }
                    postsNumberTextView.setText(String.valueOf(myPostsCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving user saved posts from database

    private void retrieveSavedPosts() {

        postsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    savedPostsInfoModelClassList.clear(); // removing previously stored data

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                        PostsInfoModelClass postsInfoModelClass = dataSnapshot.getValue(PostsInfoModelClass.class); // storing all info from database about post
                        String postId = dataSnapshot.getKey(); // retrieving post id

                        for (String savedId : savedPostsArrayList){
                            if(postId.equals(savedId)){
                                savedPostsInfoModelClassList.add(postsInfoModelClass); // adding post info if it belongs to users saved post
                            }
                        }
                    }
                    Collections.reverse(savedPostsInfoModelClassList);
                    savedPostAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // adding Request Received notification to database

    private void requestReceivedNotification() {
        String notificationId = notificationDatabaseReference.child(profileId).push().getKey();

        notificationsHashMap = new HashMap<>();
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERID,currentUserId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONTEXT,"sent you friend request");
        notificationsHashMap.put(NodeNames.NOTIFICATIONID,notificationId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERPROFILENAME, currentUserName);
        notificationsHashMap.put("isPost","false");

        // Calender is an abstract class that provides methods for converting between a specific instant in time and a set of calendar fields such as YEAR, MONTH, DAY_OF_MONTH, HOUR

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

        String notificationReference = NodeNames.NOTIFICATIONS + "/" + profileId + "/" + notificationId;

        HashMap<String,Object> notificationNodeHashMap = new HashMap<>();
        notificationNodeHashMap.put(notificationReference,notificationsHashMap);

        databaseReference.updateChildren(notificationNodeHashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null){
                    Toast.makeText(getContext(),"Failed to push notification",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // adding Request Accepted notification to database

    private void requestAcceptedNotification() {
        String notificationId = notificationDatabaseReference.child(profileId).push().getKey();

        notificationsHashMap = new HashMap<>();
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERID,currentUserId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONTEXT,"accepted your friend request");
        notificationsHashMap.put(NodeNames.NOTIFICATIONID,notificationId);
        notificationsHashMap.put(NodeNames.NOTIFICATIONSENDERPROFILENAME, currentUserName);
        notificationsHashMap.put("isPost","false");

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

        String notificationReference = NodeNames.NOTIFICATIONS + "/" + profileId + "/" + notificationId;

        HashMap<String,Object> notificationNodeHashMap = new HashMap<>();
        notificationNodeHashMap.put(notificationReference,notificationsHashMap);

        databaseReference.updateChildren(notificationNodeHashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error!=null){
                    Toast.makeText(getContext(),"Failed to push notification",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}