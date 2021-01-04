package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.instagramclone.R;
import com.example.instagramclone.adapter.ShowUsersAdapter;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.CommentsInfoModelClass;
import com.example.instagramclone.model.ShowUsersModelClass;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowUsersActivity extends AppCompatActivity {

    /* display users belonging to categories: liked the post or friends of particular user */

    @BindView(R.id.usersToolbar)
    Toolbar usersToolbar;
    @BindView(R.id.categoryTextView)
    TextView categoryTextView;
    @BindView(R.id.usersRecyclerView)
    RecyclerView usersRecyclerView;

    String categoryTitle, categoryId;

    ShowUsersAdapter showUsersAdapter;
    List<ShowUsersModelClass> showUsersModelClassList;
    List<String> userIdArrayList;

    private DatabaseReference userDatabaseReference, contactsDatabaseReference, postLikesDatabaseReference, commentsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);
        ButterKnife.bind(this);

        // accessing Database nodes

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.CONTACTS);
        postLikesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.POSTLIKES);
        commentsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.POSTCOMMENTS);

        // attaching Back arrow on Toolbar

        setSupportActionBar(usersToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        usersToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        userIdArrayList = new ArrayList<>();
        showUsersModelClassList = new ArrayList<>();
        showUsersAdapter = new ShowUsersAdapter(this,showUsersModelClassList);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(showUsersAdapter); // attaching adapter to Recycler View

        categoryTitle = getIntent().getStringExtra("category title"); // receiving intent from Contacts Post Adapter
        categoryId = getIntent().getStringExtra("category id"); // receiving intent from Contacts Post Adapter

        categoryTextView.setText(categoryTitle);

        if(categoryTitle.equals("Likes")){
            getLikesList();
        }else if(categoryTitle.equals("Friends")){
            getFriendsList();
        }
    }

    // retrieving user info who liked the particular post

    private void getLikesList() {
        postLikesDatabaseReference.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userIdArrayList.clear(); // removing preciously stored user ids

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        userIdArrayList.add(dataSnapshot.getKey()); // adding user ids
                    }
                    showUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // retrieving info of friends

    private void getFriendsList() {
        contactsDatabaseReference.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userIdArrayList.clear(); // removing preciously stored user ids

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        userIdArrayList.add(dataSnapshot.getKey()); // adding user ids
                    }
                    showUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // displaying users list

    private void showUsers() {
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    showUsersModelClassList.clear(); // removing previously stored data

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ShowUsersModelClass showUsersModelClass = dataSnapshot.getValue(ShowUsersModelClass.class); // storing all info from database about user
                        String userId = dataSnapshot.getKey();

                        for (String id : userIdArrayList){
                            if(id.equals(userId)){
                                showUsersModelClassList.add(showUsersModelClass); // adding user info
                                showUsersAdapter.notifyDataSetChanged();
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