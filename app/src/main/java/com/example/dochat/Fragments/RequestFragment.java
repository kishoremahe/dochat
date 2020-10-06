package com.example.dochat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dochat.Adapter.RequestAdapter;
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


public class RequestFragment extends Fragment {

    private String CurrentUserId;
    private List<String> requests;
    private List<User> users;
    private RecyclerView recyclerViewForRequest;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_request, container, false);
        recyclerViewForRequest=view.findViewById(R.id.recycleview_for_requests);
        recyclerViewForRequest.setLayoutManager(new LinearLayoutManager(getContext()));


        return view ;
    }

    @Override
    public void onStart() {
        super.onStart();

        requests=new ArrayList<>();
        users=new ArrayList<>();
        CurrentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference requestsReference= FirebaseDatabase.getInstance().getReference().child("chat_requests").child(CurrentUserId);

        requestsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){

                    Iterator iterator=dataSnapshot.getChildren().iterator();

                    while(iterator.hasNext()){

                        String request_type=(String)((DataSnapshot)iterator.next()).getValue();
                        String user_id=(String)((DataSnapshot)iterator.next()).getValue();

                        if(request_type.equals("received")){

                            requests.add(user_id);

                        }
                    }
                }
//                Toast.makeText(getContext(), requests.size()+" requests", Toast.LENGTH_SHORT).show();

                ReadRequests();

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

    private void ReadRequests() {

        DatabaseReference userReference=FirebaseDatabase.getInstance().getReference().child("users");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                users.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    User user=snapshot.getValue(User.class);

                    for(String id:requests){

                        if(user.getUid().equals(id)){

                            users.add(user);
                        }
                    }

                }
//                Toast.makeText(getContext(), users.size()+" requests", Toast.LENGTH_SHORT).show();

                RequestAdapter requestAdapter=new RequestAdapter(getContext(),users);
                recyclerViewForRequest.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
