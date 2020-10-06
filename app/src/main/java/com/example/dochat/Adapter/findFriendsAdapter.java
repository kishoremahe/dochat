package com.example.dochat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dochat.Model.User;
import com.example.dochat.ProfileActivity;
import com.example.dochat.R;
import com.example.dochat.group_info;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class findFriendsAdapter extends RecyclerView.Adapter<UserHolderForFindFriends> {

    private Context context;
    private List<User> users;
    private boolean isGroupMembers;
    private String GroupOwner,GroupName;


    public findFriendsAdapter(Context context, List<User> users,boolean isGroupMembers,String GroupName,String GroupOwner) {
        this.context = context;
        this.users = users;
        this.isGroupMembers=isGroupMembers;
        this.GroupName=GroupName;
        this.GroupOwner=GroupOwner;
    }

    @NonNull
    @Override
    public UserHolderForFindFriends onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.user_card_view_for_find_friends,parent,false);
        return new UserHolderForFindFriends(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UserHolderForFindFriends holder, final int position) {

        if(users.size() == 0){
            Toast.makeText(context, "members empty", Toast.LENGTH_SHORT).show();
        }
        else{

            if(users.get(position).getUid().equals(GroupOwner)){
                holder.Admin.setVisibility(View.VISIBLE);
            }

            holder.about.setText(users.get(position).getAbout());
            holder.UserName.setText(users.get(position).getUsername());

            if(users.get(position).getImageurl().equals("default")){

                holder.UserIcon.setImageResource(R.drawable.profile_image);
            }
            else{
                Glide.with(context).load(users.get(position).getImageurl()).into(holder.UserIcon);
            }

            holder.cardViewForFindFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent profileIntent=new Intent(context, ProfileActivity.class).putExtra("UserName",users.get(position).getUsername())
                            .putExtra("ProfileImage",users.get(position).getImageurl()).putExtra("About",users.get(position).getAbout())
                            .putExtra("userid",users.get(position).getUid());
                    context.startActivity(profileIntent);

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if(users.size()>0){
            return users.size();
        }
        else{
            return 1;
        }
    }
}
class UserHolderForFindFriends extends RecyclerView.ViewHolder{

    public CircleImageView UserIcon;
    public TextView UserName;
    public TextView about,Admin;
    public CardView cardViewForFindFriends;

    public UserHolderForFindFriends(@NonNull View itemView) {
        super(itemView);

        UserIcon=itemView.findViewById(R.id.profilePic);
        UserName=itemView.findViewById(R.id.username);
        about=itemView.findViewById(R.id.about);
        cardViewForFindFriends=itemView.findViewById(R.id.card_view_for_find_friends);
        Admin=itemView.findViewById(R.id.admin);

    }
}
