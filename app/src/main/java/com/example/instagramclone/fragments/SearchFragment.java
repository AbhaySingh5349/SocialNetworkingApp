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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.EditProfileActivity;
import com.example.instagramclone.MainActivity;
import com.example.instagramclone.PopUpWindowActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.model.SearchModelClass;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFragment extends Fragment {

    /* displaying registered users on app and searching users on basis of profile name using TextWatcher*/

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;

    private DatabaseReference userDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String search, currentUserId;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = getContext().getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid(); // storing currently active user Id

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS); // accessing Users node in database

        // searching users

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // listener could be added to the EditText to execute an action whenever the text is changed in the EditText View
                search = charSequence.toString().toLowerCase();
                onStart();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<SearchModelClass> firebaseRecyclerOptions = null; // a class provide by the FirebaseUI to make a query in the database to fetch appropriate data

        if(search==null){
            firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<SearchModelClass>().setQuery(userDatabaseReference,SearchModelClass.class).build();
        }else{
            firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<SearchModelClass>().setQuery(userDatabaseReference.orderByChild(NodeNames.PROFILENAME).startAt(search).endAt(search + "\uf8ff"),SearchModelClass.class).build();
        }

        // FirebaseRecyclerAdapter binds a Query to a RecyclerView and responds to all real-time events included items being added, removed, moved, or changed

        FirebaseRecyclerAdapter<SearchModelClass,SearchViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SearchModelClass, SearchViewHolder>(firebaseRecyclerOptions) {
            @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
            @Override
            protected void onBindViewHolder(@NonNull SearchViewHolder holder, int position, @NonNull SearchModelClass model) {

                String requestReceiverUserId = getRef(position).getKey(); // get database reference key of Recycler View item

                holder.userNameTextView.setText(model.getUserName());
                holder.profileNameTextView.setText(model.getProfileName());

                StorageReference profileImage = FirebaseStorage.getInstance().getReference().child(Constants.IMAGESFOLDER + "/" + requestReceiverUserId); // accessing profile picture of user in Storage Reference
                profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(getContext()==null){
                            return;
                        }
                        Glide.with(getContext()).load(uri).placeholder(R.drawable.profile).into(holder.userProfileImageView); // loading image into User profile image view
                    }
                });

                holder.userProfileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), PopUpWindowActivity.class);
                        intent.putExtra("firebase id",requestReceiverUserId); // passing info to PopUpWindow Activity
                        startActivity(intent);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sharedPreferences.edit().putString("profile Id",requestReceiverUserId).apply(); // passing data to Profile Fragment

                        ProfileFragment profileFragment = new ProfileFragment();
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frameLayout, profileFragment).commit(); // switching to User Profile Fragment
                    }
                });
            }

            @NonNull
            @Override
            public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_display_layout,parent,false); // attaching user display layout to Recycler View
                return new SearchViewHolder(view);
            }
        };
        searchRecyclerView.setAdapter(firebaseRecyclerAdapter); // attaching adapter to Recycler View
        firebaseRecyclerAdapter.startListening(); // an event listener to monitor changes to the Firebase query
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userProfileImageView;
        private TextView userNameTextView, profileNameTextView;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            profileNameTextView = itemView.findViewById(R.id.profileNameTextView);
        }
    }
}