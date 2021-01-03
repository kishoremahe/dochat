package com.example.dochat.Fragments;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dochat.Adapter.UserAdapter;
import com.example.dochat.Model.PrivateMessage;
import com.example.dochat.Model.User;
import com.example.dochat.ProfileActivity;
import com.example.dochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewForChat;
    private List<User> users=new ArrayList<>();
    private List<String> chatListId=new ArrayList<>();
    private List<String> ForChatList=new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerViewForChat = (RecyclerView) view.findViewById(R.id.recycleview_for_chat);
        recyclerViewForChat.setLayoutManager(new LinearLayoutManager(getContext()));

        chatListId.clear();
        ForChatList.clear();

        ManageChatsForTheUser();

        return view;
    }

    private void ManageChatsForTheUser() {

        final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("private_messages");
        final DatabaseReference ChatList = FirebaseDatabase.getInstance().getReference().child("chat_lists").child(current_user_id);
        DatabaseReference UserRef=FirebaseDatabase.getInstance().getReference().child("users");
        final HashMap<String, Object> ForMsg = new HashMap<>();

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        PrivateMessage privateMessage = snapshot.getValue(PrivateMessage.class);

                        if (current_user_id.equals(privateMessage.getSender())) {

                           ForChatList.add(privateMessage.getReceiver().toString());

                        }
                        else if (current_user_id.equals(privateMessage.getReceiver())) {

                           ForChatList.add(privateMessage.getSender().toString());

                        }

                    }

                }
                else{
                    Toast.makeText(getContext(), "welcome", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    User user=snapshot.getValue(User.class);

                    for(String id:ForChatList){

                        if(id.equals(user.getUid()) && !chatListId.contains(id)){

                            chatListId.add(user.getUid());
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("user_id",user.getUid());
                            ChatList.child(user.getUid()).setValue(hashMap);

                        }
                    }
                }

                ReadUsers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void ReadUsers() {

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                users.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);

                    for (String id : chatListId) {

                        if (id.equals(user.getUid())) {

                            users.add(user);

                        }
                    }
                }

                UserAdapter userAdapter = new UserAdapter(getContext(), users, true);
                recyclerViewForChat.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}