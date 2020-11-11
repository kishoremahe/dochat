package com.example.dochat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dochat.Fragments.ContactFragment;
import com.example.dochat.Model.User;
import com.example.dochat.ProfileActivity;
import com.example.dochat.R;
import com.example.dochat.group_info;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestHolder> {

    private Context context;
    private List<User> users;


    public RequestAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.card_view_for_request,parent,false);
        return new RequestHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, final int position) {

        if(users.size() == 0){

        }
        else{

            holder.Forrequest.setVisibility(View.VISIBLE);
            String usrname=users.get(position).getUsername();
            holder.UserName.setText(usrname);

            String ABout=users.get(position).getAbout();
            holder.About.setText(ABout);

            if(users.get(position).getImageurl().equals("default")){

                holder.UserIcon.setImageResource(R.drawable.profile_image);
            }
            else{

                Glide.with(context).load(users.get(position).getImageurl()).into(holder.UserIcon);

            }

            holder.Accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AddToContacts(position);

                }
            });

            holder.Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    cancelChatRequest(position);

                }
            });


        }

    }

    private void AddToContacts(int position) {

        final ProfileActivity profileActivity=new ProfileActivity();

        final String recieverUserId=users.get(position).getUid();
        final String senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference contactRef= FirebaseDatabase.getInstance().getReference().child("contacts");
        final DatabaseReference chatReqRef=FirebaseDatabase.getInstance().getReference().child("chat_requests");
        final HashMap<String,Object> hashMap1=new HashMap<>();
        hashMap1.put("request_type","friends");
        hashMap1.put("userid",recieverUserId);

        contactRef.child(senderUserId).child(recieverUserId).setValue(hashMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    HashMap<String,Object> hashMap2=new HashMap<>();
                    hashMap2.put("request_type","friends");
                    hashMap2.put("userid",senderUserId);

                    contactRef.child(recieverUserId).child(senderUserId).setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                chatReqRef.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            chatReqRef.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){

                                                        ProfileActivity.current_state ="request_friends";

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

    private void cancelChatRequest(int position) {

        final ProfileActivity profileActivity=new ProfileActivity();
        final String recieverUserId=users.get(position).getUid();
        final String senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference chatRequestRef2=FirebaseDatabase.getInstance().getReference().child("chat_requests");

        chatRequestRef2.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    chatRequestRef2.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                ProfileActivity.SendMessage.setEnabled(true);
                                ProfileActivity.current_state ="new";
                                ProfileActivity.SendMessage.setText("Send message");
                            }

                        }
                    });
                }

            }
        });


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
class RequestHolder extends RecyclerView.ViewHolder{

    public CircleImageView UserIcon;
    public TextView UserName;
    public TextView About;
    public Button Accept,Cancel;
    public RelativeLayout Forrequest;

    public RequestHolder(@NonNull View itemView) {
        super(itemView);

        UserIcon=itemView.findViewById(R.id.UserIcon);
        UserName=itemView.findViewById(R.id.username);
        About=itemView.findViewById(R.id.about);
        Accept=itemView.findViewById(R.id.btn_accept);
        Cancel=itemView.findViewById(R.id.btn_cancel);
        Forrequest=itemView.findViewById(R.id.ForRequest);


    }
}
