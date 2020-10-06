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
import com.example.dochat.Model.PrivateMessage;
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

public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatHolder> {

    private Context context;
    private List<PrivateMessage> Chats;
    private static final int MSG_LEFT_CHAT=0;
    private static final int MSG_RIGHT_CHAT=1;

    public PrivateChatAdapter(Context context, List<PrivateMessage> Chats) {
        this.context = context;
        this.Chats = Chats;
    }

    @NonNull
    @Override
    public PrivateChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_RIGHT_CHAT){
            View view= LayoutInflater.from(context).inflate(R.layout.group_chat_right_card_view,parent,false);
            return new PrivateChatHolder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.group_chat_left_card_view,parent,false);
            return new PrivateChatHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final PrivateChatHolder holder, int position) {


        if(Chats.size() !=0 ){

            if(Chats.get(position).getMsg_type().equals("image")){

                holder.RefForUser1.setVisibility(View.VISIBLE);
                holder.image.setVisibility(View.VISIBLE);
//                Glide.with(context).load(Chats.get(position).getMessage()).into(holder.image);
                Picasso.get().load(Chats.get(position).getMessage()).resize(250,250).into(holder.image);
                holder.Date1.setText(Chats.get(position).getDate());
                holder.Time1.setText(Chats.get(position).getTime());

                final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("users");
                final String sender=Chats.get(position).getSender();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                            User user=snapshot.getValue(User.class);

                            assert user != null;
                            String uid=user.getUid().toString();
                            String username=user.getUsername().toString();
                            String imageurl=user.getImageurl().toString();

                            if (sender.equals(uid)) {

                                holder.Username1.setText(username);
                                if (imageurl.equals("default")) {
                                    holder.UserIcon1.setImageResource(R.drawable.profile_image);
                                }
                                else {
                                    Glide.with(context).load(imageurl).into(holder.UserIcon1);
                                }

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
            if(Chats.get(position).getMsg_type().equals("text")){

                holder.RefForUser.setVisibility(View.VISIBLE);
                holder.Message.setVisibility(View.VISIBLE);

                holder.Message.setText(Chats.get(position).getMessage());
                holder.Date.setText(Chats.get(position).getDate());
                holder.Time.setText(Chats.get(position).getTime());
                final DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("users");
                final String sender=Chats.get(position).getSender();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                            User user=snapshot.getValue(User.class);

                            assert user != null;
                            String uid=user.getUid().toString();
                            String username=user.getUsername().toString();
                            String imageurl=user.getImageurl().toString();

                            if (sender.equals(uid)) {

                                holder.Username.setText(username);
                                if (imageurl.equals("default")) {
                                    holder.UserIcon.setImageResource(R.drawable.profile_image);
                                }
                                else {
                                    Glide.with(context).load(imageurl).into(holder.UserIcon);
                                }

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
        if(Chats.size()>0){
            return Chats.size();
        }
        else{
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {

        super.getItemViewType(position);

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if(Chats.size()==0){
            return MSG_RIGHT_CHAT;
        }

        else if(Chats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_RIGHT_CHAT;
        }
        else{
            return MSG_LEFT_CHAT;
        }

    }
}
class PrivateChatHolder extends RecyclerView.ViewHolder{

    public TextView Username,Username1;
    public TextView Message,Date,Time,Date1,Time1;
    public CircleImageView UserIcon,UserIcon1;
    public RelativeLayout RefForUser,RefForUser1;
    public ImageView image;

    public PrivateChatHolder(@NonNull View itemView) {
        super(itemView);

        Username=itemView.findViewById(R.id.username);
        Username1=itemView.findViewById(R.id.username1);
        Message=itemView.findViewById(R.id.msg);
        UserIcon=itemView.findViewById(R.id.usericon);
        UserIcon1=itemView.findViewById(R.id.usericon1);
        Date=itemView.findViewById(R.id.date);
        Time=itemView.findViewById(R.id.time);
        RefForUser=itemView.findViewById(R.id.cardView1);
        RefForUser1=itemView.findViewById(R.id.cardView);
        image=itemView.findViewById(R.id.image);
        Date1=itemView.findViewById(R.id.date1);
        Time1=itemView.findViewById(R.id.time1);
    }
}
