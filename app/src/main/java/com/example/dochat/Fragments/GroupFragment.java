package com.example.dochat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dochat.Adapter.GroupAdapter;
import com.example.dochat.Model.Group;
import com.example.dochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GroupFragment extends Fragment {

    private List<Group> groups;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view=inflater.inflate(R.layout.fragment_group, container, false);

        recyclerView=view.findViewById(R.id.recycleview_for_group);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        groups=new ArrayList<>();

        readGroups();

        return view;
    }

    private void readGroups() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("groups").child(firebaseUser.getUid()).child("groups for the user "+firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                groups.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    Group group=snapshot.getValue(Group.class);
                    groups.add(group);
                }

                GroupAdapter groupAdapter=new GroupAdapter(getActivity(),groups);
                recyclerView.setAdapter(groupAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}