package com.example.dochat.Fragments;

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
import com.example.dochat.Model.User;
import com.example.dochat.R;
import com.google.firebase.auth.FirebaseAuth;
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


public class ContactFragment extends Fragment {

    private RecyclerView recyclerViewContact;
    private List<User> usersForContact=new ArrayList<>();
    private DatabaseReference usersReference,contactsReference;
    private String current_user_id;
    private List<String> tempContactId=new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tempContactId.clear();

        View view=inflater.inflate(R.layout.fragment_contact, container, false);

        recyclerViewContact=view.findViewById(R.id.recycleViewForContact);
        recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        tempContactId.clear();
        current_user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();

        contactsReference= FirebaseDatabase.getInstance().getReference().child("contacts").child(current_user_id);
        usersReference=FirebaseDatabase.getInstance().getReference().child("users");


        contactsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Iterator iterator=dataSnapshot.getChildren().iterator();

                while(iterator.hasNext()){

                    String request_type=(String)((DataSnapshot)iterator.next()).getValue();
                    String userid=(String)((DataSnapshot)iterator.next()).getValue();

                    tempContactId.add(userid);

                }

//                Toast.makeText(getContext(), tempContactIds.size()+" contact ids", Toast.LENGTH_SHORT).show();

                ReadUsers();


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void ReadUsers() {

//        Toast.makeText(getContext(), tempContactIds.size()+" contact ids", Toast.LENGTH_SHORT).show();

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersForContact.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    User user=snapshot.getValue(User.class);

                    for(String id:tempContactId){

                        if(id.equals(user.getUid())){

                            usersForContact.add(user);
                        }
                    }

                }

                UserAdapter userAdapter=new UserAdapter(getContext(),usersForContact,false);
                recyclerViewContact.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}