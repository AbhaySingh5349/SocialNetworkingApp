package com.example.instagramclone.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.firebasetree.Constants;
import com.example.instagramclone.firebasetree.NodeNames;
import com.example.instagramclone.fragments.ProfileFragment;
import com.example.instagramclone.model.ShowUsersModelClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUsersAdapter extends RecyclerView.Adapter<ShowUsersAdapter.ShowUsersViewHolder> {

    private Context context;
    private List<ShowUsersModelClass> showUsersModelClassList;

    private StorageReference storageReference;

    SharedPreferences sharedPreferences; // it allows to save and retrieve data in the form of key,value pair

    public ShowUsersAdapter(Context context, List<ShowUsersModelClass> showUsersModelClassList) {
        this.context = context;
        this.showUsersModelClassList = showUsersModelClassList;
    }

    @NonNull
    @Override
    public ShowUsersAdapter.ShowUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_users_layout,parent,false); // attaching layout to Recycler View to display posts uploaded
        return new ShowUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowUsersAdapter.ShowUsersViewHolder holder, int position) {

        ShowUsersModelClass showUsersModelClass = showUsersModelClassList.get(position); // getting reference for particular item of Recycler View

        // getSharedPreferences() returns an instance pointing to the file that contains the values of preferences. By setting this mode, the file can only be accessed using calling application

        sharedPreferences = context.getSharedPreferences("com.example.instagramclone.fragments", Context.MODE_PRIVATE);

        storageReference = FirebaseStorage.getInstance().getReference(); // getting reference for particular item of Recycler View

        // retrieving users info using model class

        holder.profileNameTextView.setText(showUsersModelClass.getProfileName());

        String userId = showUsersModelClass.getUserId();

        StorageReference profileImage = storageReference.child(Constants.IMAGESFOLDER).child(userId);
        profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).placeholder(R.drawable.profile).into(holder.userProfileImageView); // loading profile image of user
            }
        });
    }

    @Override
    public int getItemCount() {
        return showUsersModelClassList.size();
    }

    //  ViewHolder describes an item view and metadata about its place within the RecyclerView

    public static class ShowUsersViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userProfileImageView;
        private TextView profileNameTextView;

        public ShowUsersViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            profileNameTextView = itemView.findViewById(R.id.profileNameTextView);
        }
    }
}
