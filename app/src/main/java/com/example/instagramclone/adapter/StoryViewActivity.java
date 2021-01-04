package com.example.instagramclone.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    @BindView(R.id.storyProfileImageView)
    CircleImageView storyProfileImageView;
    @BindView(R.id.userNameTextView)
    TextView userNameTextView;
    @BindView(R.id.storiesProgressView)
    StoriesProgressView storiesProgressView;
    @BindView(R.id.seenTextView)
    TextView seenTextView;
    @BindView(R.id.storyImageView)
    ImageView storyImageView;
    @BindView(R.id.videoStoryViewer)
    VideoView videoStoryViewer;
    @BindView(R.id.deleteTextView)
    TextView deleteTextView;
    @BindView(R.id.previousStoryView)
    View previousStoryView;
    @BindView(R.id.nextStoryView)
    View nextStoryView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userDatabaseReference, storiesDatabaseReference;
    private StorageReference storageReference;

    MediaController mediaController;

    private String currentUserId, storyPublisherId;

    private List<String> storyIdList;
    private List<Uri> storyValue;

    private int counter;
    private long pressTime = 0L, limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                pressTime = System.currentTimeMillis();
                storiesProgressView.pause();
                return false;
            }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                long now = System.currentTimeMillis();
                storiesProgressView.resume();
                return limit<now-pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);
        ButterKnife.bind(this);

        seenTextView.setVisibility(View.INVISIBLE);
        deleteTextView.setVisibility(View.INVISIBLE);

        storyIdList = new ArrayList<>();
        storyValue = new ArrayList<>();

        counter=0;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserId = Objects.requireNonNull(firebaseUser).getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        storiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.STORIES);

        storyPublisherId = getIntent().getStringExtra("story publisher Id");

        if(storyPublisherId.equals(currentUserId)){
            seenTextView.setVisibility(View.VISIBLE);
            deleteTextView.setVisibility(View.VISIBLE);
        }

        retrieveStories(storyPublisherId);

        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(storyPublisherId);
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(StoryViewActivity.this).load(uri).placeholder(R.drawable.profile).into(storyProfileImageView);
            }
        });

        userDatabaseReference.child(storyPublisherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(NodeNames.PROFILENAME)){
                    userNameTextView.setText(NodeNames.PROFILENAME);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        previousStoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        previousStoryView.setOnTouchListener(onTouchListener);

        nextStoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        nextStoryView.setOnTouchListener(onTouchListener);
    }

    private void addViewsToStory(String storyId){
        storiesDatabaseReference.child(storyPublisherId).child(storyId).child(NodeNames.STORYVIEWS).child(currentUserId).setValue(Constants.STORYVIEWD);
    }

    private  void retrieveSeenStoryNumber(String storyId){
        storiesDatabaseReference.child(storyPublisherId).child(storyId).child(NodeNames.STORYVIEWS).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seenTextView.setText("Seen by: " + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveStories(String storyPublisherId){
        storiesDatabaseReference.child(storyPublisherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyIdList.clear();
                storyValue.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StoryModelClass storyModelClass = dataSnapshot.getValue(StoryModelClass.class);
                    long currentTime = System.currentTimeMillis();
                    String storyType = Objects.requireNonNull(storyModelClass).getStoryType();
                    String publisherId = storyModelClass.getStoryPublisherId();
                    String storyId = storyModelClass.getStoryId();

                    if(currentTime> Objects.requireNonNull(storyModelClass).getStoryTimeStart() && currentTime<storyModelClass.getStoryTimeEnd()){

                        if(storyType.equals(Constants.STORYIMAGE)){
                            StorageReference storyUri = storageReference.child(Constants.STORYIMAGE).child(storyId).child(publisherId);
                            storyUri.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    storyValue.add(uri);
                                    storyIdList.add(storyModelClass.getStoryId());
                                }
                            });
                        }else{
                            StorageReference storyUri = storageReference.child(Constants.STORYVIDEO).child(storyId).child(publisherId);
                            storyUri.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    storyValue.add(uri);
                                    storyIdList.add(storyModelClass.getStoryId());
                                }
                            });
                        }
                        storiesProgressView.setStoriesCount(storyValue.size()); // number of stories by particular user
                        storiesProgressView.setStoryDuration(5000L);
                        storiesProgressView.setStoriesListener(StoryViewActivity.this);
                        storiesProgressView.startStories(counter);

                        if(storyType.equals(Constants.STORYIMAGE)){
                            storyImageView.setVisibility(View.VISIBLE);
                            videoStoryViewer.setVisibility(View.INVISIBLE);
                            Glide.with(StoryViewActivity.this).load(storyValue.get(counter)).placeholder(R.drawable.add_image_icon).into(storyImageView);
                        }else{
                            storyImageView.setVisibility(View.INVISIBLE);
                            videoStoryViewer.setVisibility(View.VISIBLE);
                            mediaController = new MediaController(StoryViewActivity.this);
                            videoStoryViewer.setMediaController(mediaController);
                            videoStoryViewer.setVideoURI(storyValue.get(counter));
                            videoStoryViewer.start();
                        }
                        addViewsToStory(storyIdList.get(counter));
                        retrieveSeenStoryNumber(storyIdList.get(counter));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNext() {
        storyValue.get(++counter);
        addViewsToStory(storyIdList.get(counter));
        retrieveSeenStoryNumber(storyIdList.get(counter));
    }

    @Override
    public void onPrev() {
        storyValue.get(--counter);
        retrieveSeenStoryNumber(storyIdList.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        storiesProgressView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        storiesProgressView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        storiesProgressView.resume();
    }
}