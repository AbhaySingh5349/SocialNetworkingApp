package com.example.instagramclone;

import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import android.content.Intent;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.EditText;
        import android.widget.TextView;

        import com.example.instagramclone.firebasetree.NodeNames;
        import com.example.instagramclone.model.HashTagModelClass;
        import com.firebase.ui.database.FirebaseRecyclerAdapter;
        import com.firebase.ui.database.FirebaseRecyclerOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import java.util.Objects;

        import butterknife.BindView;
        import butterknife.ButterKnife;

public class HashTagsListActivity extends AppCompatActivity {

    /* displaying all HashTags, post count belonging to HashTags, search HashTags
     and allowing user to see all posts belonging to particular HashTag */

    @BindView(R.id.hashTagEditText)
    EditText hashTagEditText;
    @BindView(R.id.hashTagRecyclerView)
    RecyclerView hashTagRecyclerView;

    private DatabaseReference hashTagsDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String search, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_tags_list);
        ButterKnife.bind(this);

        // retrieving current user

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserId = Objects.requireNonNull(firebaseUser).getUid();

        hashTagsDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.HASHTAGS); // accessing HashTag node in database

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); // to get latest hashTag post at top
        linearLayoutManager.setStackFromEnd(true);
        hashTagRecyclerView.setLayoutManager(linearLayoutManager);

        // searching HashTags

        hashTagEditText.addTextChangedListener(new TextWatcher() {
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<HashTagModelClass> firebaseRecyclerOptions = null; // a class provide by the FirebaseUI to make a query in the database to fetch appropriate data

        if(search==null){
            firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<HashTagModelClass>().setQuery(hashTagsDatabaseReference,HashTagModelClass.class).build();
        }else {
            firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<HashTagModelClass>().setQuery(hashTagsDatabaseReference.orderByKey().startAt(search).endAt(search + "\uf8ff"), HashTagModelClass.class).build();
        }

        // FirebaseRecyclerAdapter binds a Query to a RecyclerView and responds to all real-time events included items being added, removed, moved, or changed

        FirebaseRecyclerAdapter<HashTagModelClass,HashTagViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HashTagModelClass, HashTagViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull HashTagViewHolder holder, int position, @NonNull HashTagModelClass model) {
                String hashTag = getRef(position).getKey(); // get database reference key of Recycler View item
                holder.hashTagTextView.setText(hashTag);

                hashTagsDatabaseReference.child(hashTag).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String postsCount = String.valueOf(snapshot.getChildrenCount());
                            holder.hashTagCountTextView.setText(postsCount + " posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // viewing all posts belonging to particular HashTag

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(HashTagsListActivity.this,HashTagDetailsActivity.class);
                        intent.putExtra("Hash Tag Name",hashTag);
                        intent.putExtra("Hash Tag Posts Count",holder.hashTagCountTextView.getText().toString());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public HashTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(HashTagsListActivity.this).inflate(R.layout.hash_tag_activity_item,parent,false); // attaching user display layout to Recycler View
                return new HashTagViewHolder(view);
            }
        };
        hashTagRecyclerView.setAdapter(firebaseRecyclerAdapter); // attaching adapter to Recycler View
        firebaseRecyclerAdapter.startListening(); // an event listener to monitor changes to the Firebase query
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class HashTagViewHolder extends RecyclerView.ViewHolder {

        private TextView hashTagTextView, hashTagCountTextView;

        public HashTagViewHolder(@NonNull View itemView) {
            super(itemView);

            hashTagTextView = itemView.findViewById(R.id.hashTagTextView);
            hashTagCountTextView = itemView.findViewById(R.id.hashTagCountTextView);
        }
    }
}