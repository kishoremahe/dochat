package com.example.dochat.Adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dochat.Model.GroupMessage;
import com.example.dochat.Model.User;
import com.example.dochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatHolder> {

    private Context context;
    private List<GroupMessage> groupChats;
    private static final int MSG_LEFT_CHAT=0;
    private static final int MSG_RIGHT_CHAT=1;

    public GroupChatAdapter(Context context, List<GroupMessage> groupChats) {
        this.context = context;
        this.groupChats = groupChats;
    }

    @NonNull
    @Override
    public GroupChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_RIGHT_CHAT){
            View view= LayoutInflater.from(context).inflate(R.layout.group_chat_right_card_view,parent,false);
            return new GroupChatHolder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.group_chat_left_card_view,parent,false);
            return new GroupChatHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final GroupChatHolder holder, int position) {

        if(groupChats.size() !=0 ){

            if(groupChats.get(position).getMsg_type().equals("image")){

                holder.Date1.setText(groupChats.get(position).getDate());
                holder.Time1.setText(groupChats.get(position).getTime());

                holder.CardView1.setVisibility(View.VISIBLE);
                holder.Image.setVisibility(View.VISIBLE);
                Picasso.get().load(groupChats.get(position).getMessage()).resize(250,250).into(holder.Image);

                final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("users");
                final String sender=groupChats.get(position).getSender();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                            User user=snapshot.getValue(User.class);
                            String uid=user.getUid();
                            String imageurl=user.getImageurl();
                            String username=user.getUsername();

                            if(sender.equals(uid)){

                                holder.Username1.setText(username);
                                if(imageurl.equals("default")){
                                    holder.UserIcon1.setImageResource(R.drawable.profile_image);
                                }
                                else{
                                    Glide.with(context).load(imageurl).into(holder.UserIcon1);
                                }
                                break;

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            if(groupChats.get(position).getMsg_type().equals("text")){

                holder.Date.setText(groupChats.get(position).getDate());
                holder.Time.setText(groupChats.get(position).getTime());

                holder.CardView.setVisibility(View.VISIBLE);
                holder.Message.setVisibility(View.VISIBLE);


                holder.Message.setText(groupChats.get(position).getMessage().toString());
                final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("users");
                final String sender=groupChats.get(position).getSender();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                            User user=snapshot.getValue(User.class);
                            String uid=user.getUid();
                            String imageurl=user.getImageurl();
                            String username=user.getUsername();

                            if(sender.equals(uid)){

                                holder.Username.setText(username);
                                if(imageurl.equals("default")){
                                    holder.UserIcon.setImageResource(R.drawable.profile_image);
                                }
                                else{
                                    Glide.with(context).load(imageurl).into(holder.UserIcon);
                                }
                                break;

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        }

    }


    @Override
    public int getItemCount() {
        if(groupChats.size()>0){

            return groupChats.size();

        }
        else{

            return 1;

        }
    }

    @Override
    public int getItemViewType(int position) {


        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(groupChats.size()==0){
            return MSG_RIGHT_CHAT;
        }

        else {
            assert firebaseUser != null;
            if(groupChats.get(position).getSender().equals(firebaseUser.getUid())){
                return MSG_RIGHT_CHAT;
            }
            else{
                return MSG_LEFT_CHAT;
            }
        }

    }
}
class GroupChatHolder extends RecyclerView.ViewHolder{

    public TextView Username,Username1;
    public TextView Message,Date,Time,Date1,Time1;
    public CircleImageView UserIcon,UserIcon1;
    public RelativeLayout CardView,CardView1;
    public ImageView Image;

    public GroupChatHolder(@NonNull View itemView) {
        super(itemView);

        Username=itemView.findViewById(R.id.username);
        Message=itemView.findViewById(R.id.msg);
        UserIcon=itemView.findViewById(R.id.usericon);
        CardView=itemView.findViewById(R.id.cardView1);
        CardView1=itemView.findViewById(R.id.cardView);
        Image=itemView.findViewById(R.id.image);
        UserIcon1=itemView.findViewById(R.id.usericon1);
        Username1=itemView.findViewById(R.id.username1);
        Date=itemView.findViewById(R.id.date);
        Time=itemView.findViewById(R.id.time);
        Date1=itemView.findViewById(R.id.date1);
        Time1=itemView.findViewById(R.id.time1);
    }
}
