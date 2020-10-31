package com.example.dochat;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.example.dochat.Adapter.ViewPagerAdapter;
import com.example.dochat.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
//import com.google.api.OAuthRequirementsOrBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.FirebaseAppHelper;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar,toolBar;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigateView;
    private ActionBarDrawerToggle mToggle;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private CircleImageView UserIcon;
    private TextView Username;
    private String current_user_id;




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        assert firebaseUser != null;


        toolbar=(Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Chatty");

        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        mToggle=new ActionBarDrawerToggle(MainActivity.this,mDrawerLayout,toolbar,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();



        navigateView=(NavigationView)findViewById(R.id.navigate_view);
        View headerView=navigateView.getHeaderView(0);
        UserIcon=(CircleImageView)headerView.findViewById(R.id.profileImageForNavigation);
        Username=(TextView)headerView.findViewById(R.id.usernameForNavigation);


        toolBar=(Toolbar)headerView.findViewById(R.id.appbarlayout);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Dochat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigateView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId()) {

                    case R.id.find_friends_option:
                        startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
                        return true;
                    case R.id.logout_option:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            updateUserState("offline");
                        }
                        FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = firebaseuser.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                        HashMap<String, Object> hashmap = new HashMap<>();
                        hashmap.put("login_status", "loggedout");
                        userRef.updateChildren(hashmap);
                        firebaseAuth.signOut();
                        SendUserToLoginActivity();
                        return true;
                    case R.id.settings_option:
                        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(settingsIntent);
                        return true;
                    case R.id.create_group_option:
                        GroupCreation();
                        return true;
                    default:
                        return true;

                }
            }
        });

    }

    private void setValues() {
        FirebaseAuth fAuth=FirebaseAuth.getInstance();
        FirebaseUser fUser=fAuth.getCurrentUser();
        assert fUser != null;
        String Current_user_id=fUser.getUid();

        DatabaseReference UserRef= FirebaseDatabase.getInstance().getReference().child("users").child(Current_user_id);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);
                assert user != null;
                Username.setText(user.getUsername());
                if(user.getImageurl().equals("default")){
                    Glide.with(getApplicationContext()).load(R.drawable.profile_image).into(UserIcon);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(UserIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser1=FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser1 == null) {

            SendUserToLoginActivity();
        }
        else{

           CheckForUsernameExistence();

        }
        
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUserState(String state){

        FirebaseUser firebaseuser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId=firebaseuser.getUid();

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDateFormat=new SimpleDateFormat("MM dd,yyyy");
        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");

        String currentDate=currentDateFormat.format(calendar.getTime());
        String currentTime=currentTimeFormat.format(calendar.getTime());

        HashMap<String,Object> hashMap1=new HashMap<>();
        hashMap1.put("status",state);
        hashMap1.put("last_seen_date",currentDate);
        hashMap1.put("last_seen_time",currentTime);

        DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userRef.updateChildren(hashMap1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        if(firebaseUser != null){
            updateUserState("online");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseUser != null){
            updateUserState("offline");
        }
    }

    private void CheckForUsernameExistence() {

        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    User user=dataSnapshot.getValue(User.class);

                    if(user.getUsername().equals("")){
                        SendUserToSettingsActivity();
                    }
                    else{
                        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());

                        viewPager=findViewById(R.id.viewpager);
                        viewPager.setAdapter(viewPagerAdapter);

                        tabLayout=findViewById(R.id.tablayout);
                        tabLayout.setupWithViewPager(viewPager);

                        setValues();


                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return  super.onOptionsItemSelected(item);
    }

    private void GroupCreation() {

        final View view= getLayoutInflater().inflate(R.layout.cutom_layout_for_alertdialog_edittext,null);

        new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog)
                .setTitle("Enter Group name:")
                .setView(view)
                .setPositiveButton("create", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(final DialogInterface dialog, int which) {
                     final EditText grp_name=view.findViewById(R.id.group_name);
                     final String GroupName=grp_name.getText().toString();
                     if(TextUtils.isEmpty(GroupName)){
                          Toast.makeText(getApplicationContext(),"Please fill the group name",Toast.LENGTH_SHORT).show();
                     }
                     else{
                         createGroup(GroupName);
                     }
                   }
                 })
                 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                    }
                  }).create().show();
    }

    private void createGroup(final String GroupName) {

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference Reference=FirebaseDatabase.getInstance().getReference().child("groups").child(firebaseUser.getUid()).child("groups for the user "+firebaseUser.getUid()).child(GroupName);

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("created_by",firebaseUser.getUid());
        hashMap.put("group_name",GroupName);
        hashMap.put("imageurl","default");

        Reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),GroupName+" group is created successfully",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),GroupName+" group is not created successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void SendUserToSettingsActivity() {

        Intent settingsIntent=new Intent(getApplicationContext(),SettingsActivity.class);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


}