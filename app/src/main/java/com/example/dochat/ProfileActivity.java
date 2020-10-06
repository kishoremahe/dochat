package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dochat.Model.PrivateMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView ProfileImage;
    private String senderUserId,recieverUserId;
    private TextView Username;
    private TextView About;
    public static Button SendMessage,DeclineBtn;
    public static String current_state="new";



    public ProfileActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar=findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chatty");

        senderUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        InitializeFields();

        Intent i=getIntent();
        String UserName=i.getStringExtra("UserName");
        String profileImage=i.getStringExtra("ProfileImage");
        String about=i.getStringExtra("About");
        recieverUserId=i.getStringExtra("userid");

        if(recieverUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

            SendMessage.setEnabled(false);
            SendMessage.setVisibility(View.INVISIBLE);
        }

        assert profileImage != null;
        MakeProfile(UserName,profileImage,about);

        SendMessage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {


                if(current_state.equals("new")){

                    SendChatRequest();
                }
                if(current_state.equals("request_sent")){

                    cancelChatRequest();
                }
                if(current_state.equals("request_received")){

                    DeclineBtn.setEnabled(false);
                    DeclineBtn.setVisibility(View.INVISIBLE);

                    AddToContacts();
                }
                if(current_state.equals("request_friends")){

                    final DatabaseReference contactReference1=FirebaseDatabase.getInstance().getReference().child("contacts");

                    contactReference1.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                contactReference1.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            current_state="new";
                                            SendMessage.setEnabled(true);
                                            SendMessage.setText("Send message");

                                            DeclineBtn.setEnabled(false);
                                            DeclineBtn.setVisibility(View.INVISIBLE);

                                            RemoveMessages();

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

    private void MakeProfile(String userName, String profileImage, String about) {

        if(profileImage.equals("default")){
            ProfileImage.setImageResource(R.drawable.profile_image);
        }
        else{
            Glide.with(ProfileActivity.this).load(profileImage).into(ProfileImage);
        }

        About.setText(about);
        Username.setText(userName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final DatabaseReference chatRequestRef1=FirebaseDatabase.getInstance().getReference().child("chat_requests");
        final DatabaseReference contactReference=FirebaseDatabase.getInstance().getReference().child("contacts");
        chatRequestRef1.child(senderUserId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(recieverUserId)){

                    String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){

                        current_state="request_sent";
                        SendMessage.setText("cancel request");
                    }
                    if(request_type.equals("received")){

                        current_state="request_received";

                        DeclineBtn.setEnabled(true);
                        DeclineBtn.setVisibility(View.VISIBLE);
                        SendMessage.setText("Accept request");

                        DeclineBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                DeclineBtn.setVisibility(View.INVISIBLE);
                                cancelChatRequest();

                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        contactReference.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(recieverUserId)){

                    String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("friends")){

                        current_state="request_friends";

                        SendMessage.setEnabled(true);
                        SendMessage.setText("Remove this contact");

                        DeclineBtn.setEnabled(false);
                        DeclineBtn.setVisibility(View.INVISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void RemoveMessages() {

        DatabaseReference chatRef= FirebaseDatabase.getInstance().getReference().child("private_messages");
        RemoveChatList();
//        final String current_user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        PrivateMessage privateMessage = snapshot.getValue(PrivateMessage.class);

                        assert privateMessage != null;
                        if (senderUserId.equals(privateMessage.getSender()) && recieverUserId.equals(privateMessage.getReceiver())
                        || senderUserId.equals(privateMessage.getReceiver()) && recieverUserId.equals(privateMessage.getSender())){

                            snapshot.getRef().removeValue();

                        }
                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void RemoveChatList() {

        String current_user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ChatListRef1=FirebaseDatabase.getInstance().getReference().child("chat_lists").child(current_user_id).child(recieverUserId);
        DatabaseReference ChatListRef2=FirebaseDatabase.getInstance().getReference().child("chat_lists").child(recieverUserId).child(current_user_id);

       ChatListRef1.removeValue();
       ChatListRef2.removeValue();

    }

    private void AddToContacts() {

        final DatabaseReference contactRef=FirebaseDatabase.getInstance().getReference().child("contacts");
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

                                                        current_state="request_friends";

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

    private void cancelChatRequest() {

        final DatabaseReference chatRequestRef2=FirebaseDatabase.getInstance().getReference().child("chat_requests");

        chatRequestRef2.child(senderUserId).child(recieverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    chatRequestRef2.child(recieverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                SendMessage.setEnabled(true);
                                current_state="new";
                                SendMessage.setText("Send message");
                            }

                        }
                    });
                }

            }
        });


    }

    private void SendChatRequest() {

        final DatabaseReference chatRequestRef=FirebaseDatabase.getInstance().getReference().child("chat_requests");
        final DatabaseReference NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        HashMap<String,Object> hashMap1=new HashMap<>();
        hashMap1.put("request_type","sent");
        hashMap1.put("user_id",recieverUserId);

        chatRequestRef.child(senderUserId).child(recieverUserId).setValue(hashMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    HashMap<String,Object> hashMap2=new HashMap<>();
                    hashMap2.put("request_type","received");
                    hashMap2.put("user_id",senderUserId);

                    chatRequestRef.child(recieverUserId).child(senderUserId).setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                HashMap<String,Object> hashMap2=new HashMap<>();
                                hashMap2.put("type","request");
                                hashMap2.put("sender",senderUserId);

                                NotificationRef.child(recieverUserId).push().setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        SendMessage.setEnabled(true);
                                        current_state="request_sent";
                                        SendMessage.setText("Cancel request");

                                    }
                                });

                            }

                        }
                    });
                }

            }
        });


    }

    private void InitializeFields() {

        ProfileImage=findViewById(R.id.profileImage);
        Username=findViewById(R.id.username);
        About=findViewById(R.id.about);
        SendMessage=findViewById(R.id.btn_send_message);
        DeclineBtn=findViewById(R.id.btn_cancel_request);
    }
}