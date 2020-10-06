package com.example.dochat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dochat.Fragments.ContactFragment;
import com.example.dochat.Model.PrivateMessage;
import com.example.dochat.Model.User;
import com.example.dochat.R;
import com.example.dochat.UsersMessageActivity;
import com.example.dochat.group_info;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserHolder> {


    private Context context;
    private List<User> users;
    private Boolean ischat;
    private String the_last_message,the_image_type;


    public UserAdapter(Context context, List<User> users,Boolean ischat) {
        this.context = context;
        this.users = users;
        this.ischat=ischat;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.user_card_view,parent,false);
        return new UserHolder(view);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, final int position) {

        if(users.size() == 0){
            String s="k";
        }
        else{
            String usrname=users.get(position).getUsername();
            holder.UserName.setText(usrname);

            if(users.get(position).getImageurl().equals("default")){

                holder.UserIcon.setImageResource(R.drawable.profile_image);
            }
            else{

                Glide.with(context).load(users.get(position).getImageurl()).into(holder.UserIcon);

            }

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent userMessageActivityIntent=new Intent(context, UsersMessageActivity.class);
                    userMessageActivityIntent.putExtra("username",users.get(position).getUsername())
                            .putExtra("usericon",users.get(position).getImageurl())
                            .putExtra("about",users.get(position).getAbout())
                            .putExtra("userid",users.get(position).getUid())
                            .putExtra("Last_seen_date",users.get(position).getLast_seen_date())
                            .putExtra("Last_seen_time",users.get(position).getLast_seen_time());

                    context.startActivity(userMessageActivityIntent);

                }
            });



            if(ischat){

                UpdateUserStatus(position,holder);
                ReadLastMessage(holder.LastMsg,position);

            }

        }

    }

    private void UpdateUserStatus(int position,@NonNull final UserHolder holder) {

        DatabaseReference UserRef=FirebaseDatabase.getInstance().getReference().child("users").child(users.get(position).getUid());

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);

                assert user != null;
                String status=user.getStatus();

                if(status.equals("online")){

                    holder.Status.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ReadLastMessage(final TextView LastMsg, final int position) {

        the_last_message="default";
        the_image_type="";
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        final String current_user_id=firebaseUser.getUid();
        final String sender_user_id=users.get(position).getUid();

        DatabaseReference LastMsgRef=FirebaseDatabase.getInstance().getReference().child("private_messages");

        LastMsgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        PrivateMessage privateMessage = snapshot.getValue(PrivateMessage.class);

                        if ((privateMessage.getSender().equals(current_user_id) && privateMessage.getReceiver().equals(sender_user_id))
                                || (privateMessage.getSender().equals(sender_user_id) && privateMessage.getReceiver().equals(current_user_id))){

                            the_last_message = privateMessage.getMessage();
                            the_image_type=privateMessage.getMsg_type();

                        }
                    }

                    if (the_last_message.equals("default")) {
                        LastMsg.setText("");
                    } else {
                        if(the_image_type.equals("text")){
                            LastMsg.setText(the_last_message);

                        }
                        if(the_image_type.equals("image")){
                            LastMsg.setText("photo");
                        }

                    }

                }

            }
            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){

            }


        });


//        the_last_message="default";

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
class UserHolder extends RecyclerView.ViewHolder{

    public CircleImageView UserIcon;
    public TextView UserName;
    public CardView cardView;
    public TextView LastMsg;
    public CircleImageView Status;

    public UserHolder(@NonNull View itemView) {
        super(itemView);

        UserIcon=itemView.findViewById(R.id.profile_image_for_user);
        UserName=itemView.findViewById(R.id.name_user);
        cardView=itemView.findViewById(R.id.card_view_for_user);
        LastMsg=itemView.findViewById(R.id.last_message);
        Status=itemView.findViewById(R.id.online);


    }
}
