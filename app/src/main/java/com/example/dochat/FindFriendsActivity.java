package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dochat.Adapter.findFriendsAdapter;
import com.example.dochat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private List<User> users;
    private findFriendsAdapter FindFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        recyclerView=(RecyclerView)findViewById(R.id.recycleview);
        toolbar=(Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Friends");

        recyclerView.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
        recyclerView.setHasFixedSize(true);

        ReadUsers();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.search_icon);
        SearchView searchView=(SearchView)menuItem.getActionView();
//        searchView.setQueryHint("Search Here...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                SearchUser(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void SearchUser(String s) {

        if(!s.equals("")){

            Query query=FirebaseDatabase.getInstance().getReference().child("users").orderByChild("username")
                    .startAt(s)
                    .endAt(s+"\uf8ff");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    users.clear();

                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                        User user=snapshot.getValue(User.class);

                        users.add(user);
                    }

                    FindFriendsAdapter=new findFriendsAdapter(FindFriendsActivity.this,users,false,"null","null");
                    recyclerView.setAdapter(FindFriendsAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else{
            ReadUsers();
        }

    }

    private void ReadUsers() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserId=firebaseUser.getUid();

        users=new ArrayList<>();

        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                users.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    User user=snapshot.getValue(User.class);

                    assert user != null;
                    if(!user.getUid().equals(currentUserId)){
                        users.add(user);
                    }

                }

                FindFriendsAdapter=new findFriendsAdapter(FindFriendsActivity.this,users,false,"null","null");
                recyclerView.setAdapter(FindFriendsAdapter);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}