package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dochat.Adapter.UserAdapter;
import com.example.dochat.Adapter.findFriendsAdapter;
import com.example.dochat.Model.Group;
import com.example.dochat.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class group_info extends AppCompatActivity {

    private static int globalforOwner=0;
    private String groupname,GroupOwner;
    private CircleImageView GroupIcon;
    private RecyclerView recyclerView;
    private static int flag=0;
    private List<User> groupMembers;
    private UserAdapter userAdapter;
    private FirebaseStorage storage;
    private StorageReference storageReference;
//    private int GALLERY_REQUEST_CODE=123,PERMISSION_REQUEST=10;
    private ProgressDialog progressDialog;
    public Uri filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        recyclerView=(RecyclerView)findViewById(R.id.recycleView_for_group_members);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        GroupIcon=(CircleImageView)findViewById(R.id.group_icon);

        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference().child("profile_images_for_group");

        Intent i=getIntent();
        groupname=i.getStringExtra("groupName");
        GroupOwner=i.getStringExtra("groupowner");

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupname);

        if(globalforOwner == 0 ){

            AddGroupOwner();

        }

        ReadGroupMembers(groupname);

        GroupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetPermissionForGalleryAccess();

            }
        });

        DatabaseReference referenceForProfilePic=FirebaseDatabase.getInstance().getReference().child("groups").child(GroupOwner).child("groups for the user "+GroupOwner).child(groupname);
        referenceForProfilePic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Group group=dataSnapshot.getValue(Group.class);

                if(group.getImageurl().equals("default")){

                    GroupIcon.setImageResource(R.drawable.profile_image);

                }
                else{

                    Glide.with(group_info.this).load(group.getImageurl()).into(GroupIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void AddGroupOwner() {

        DatabaseReference ref_for_group_owner=FirebaseDatabase.getInstance().getReference().child("group_members_in_groups").child(groupname).child(GroupOwner);
        HashMap<String,Object> hashMap1=new HashMap<>();
        hashMap1.put("userid",GroupOwner);
        hashMap1.put("username",GroupOwner);
        hashMap1.put("group_owner",GroupOwner);
        hashMap1.put("imageurl","default");
        ref_for_group_owner.setValue(hashMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == SettingsActivity.PERMISSION_REQUEST){

            if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){

                AccessGallery();

            }
            else{
                Toast.makeText(group_info.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void GetPermissionForGalleryAccess() {

        if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){

            AccessGallery();

        }
        else{

            if(ActivityCompat.shouldShowRequestPermissionRationale(group_info.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                new AlertDialog.Builder(group_info.this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed for access your gallery")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(group_info.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        }).create().show();
            }
            else{
                ActivityCompat.requestPermissions(group_info.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);

            }
        }


    }

    private void AccessGallery() {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"please select an image"),SettingsActivity.GALLERY_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_user_menu_option,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SettingsActivity.GALLERY_REQUEST_CODE && data!= null && resultCode == RESULT_OK){

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(group_info.this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            assert result != null;
            filepath=result.getUri();

            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),filepath);
                GroupIcon.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToFirebaseStorage() {

        if(filepath != null){

//            Toast.makeText(group_info.this, "stored in 1", Toast.LENGTH_SHORT).show();
            progressDialog=new ProgressDialog(group_info.this);
            progressDialog.setTitle("Uploading...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            StorageReference ref=storageReference.child(groupname);

            ref.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(group_info.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    UpdateImageUrl();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(group_info.this, "Image not uploaded successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress=(100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Image uploaded "+(int)progress+" % completed");
                        }
                    });
        }
    }

    private void UpdateImageUrl() {

        final DatabaseReference ref1=FirebaseDatabase.getInstance().getReference().child("groups");
        final DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("group_members_in_groups").child(groupname);

        ref2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Iterator iterator=dataSnapshot.getChildren().iterator();

                while(iterator.hasNext()){

                    String group_owner=(String)((DataSnapshot)iterator.next()).getValue();
                    String imageurl=(String)((DataSnapshot)iterator.next()).getValue();
                    String userid=(String)((DataSnapshot)iterator.next()).getValue();
                    String username=(String)((DataSnapshot)iterator.next()).getValue();

                    assert userid != null;
                    DatabaseReference reference1=ref1.child(userid).child("groups for the user "+userid).child(groupname);
                    DatabaseReference reference2=ref2.child(userid);

                    HashMap<String,Object> hashMap2=new HashMap<>();
                    hashMap2.put("imageurl",filepath.toString());

                    globalforOwner=1;

                    reference1.updateChildren(hashMap2);
                    reference2.updateChildren(hashMap2);

                }

                Toast.makeText(group_info.this, "Image updated successfully", Toast.LENGTH_SHORT).show();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.add_user:
                AddUser();
                return true;
            case R.id.remove_user:
                RemoveUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void RemoveUser() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_user_id=firebaseUser.getUid();
        if(current_user_id.equals(GroupOwner)){

            final View view= getLayoutInflater().inflate(R.layout.cutom_layout_for_alertdialog_edittext,null);

            new AlertDialog.Builder(group_info.this)
                    .setTitle("Enter username")
                    .setView(view)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final EditText user_name=view.findViewById(R.id.group_name);
                            final String UserName=user_name.getText().toString();
                            if(TextUtils.isEmpty(UserName)){
                                Toast.makeText(getApplicationContext(),"Please fill the Username",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                checkForUserExistence(UserName,groupname,GroupOwner,true);
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
        else{
            Toast.makeText(group_info.this, "You are not a admin !", Toast.LENGTH_SHORT).show();
        }

    }

    private void AddUser() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_user_id=firebaseUser.getUid();

        if(GroupOwner.equals(current_user_id)){
            final View view= getLayoutInflater().inflate(R.layout.cutom_layout_for_alertdialog_edittext,null);

            new AlertDialog.Builder(group_info.this)
                    .setTitle("Enter username")
                    .setView(view)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final EditText user_name=view.findViewById(R.id.group_name);
                            final String UserName=user_name.getText().toString();
                            if(TextUtils.isEmpty(UserName)){
                                Toast.makeText(getApplicationContext(),"Please fill the Username",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                checkForUserExistence(UserName,groupname,GroupOwner,false);
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
        else{
            Toast.makeText(group_info.this, "you are not a admin !", Toast.LENGTH_SHORT).show();
        }


    }

    private void checkForUserExistence(final String UserName,final String groupname,final String groupowner,final boolean isRemove) {


        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                        User user=snapshot.getValue(User.class);
                        assert user != null;
                        String username=user.getUsername();
                        String uid=user.getUid();
                        if(username.equals(UserName)){
                            flag=1;
                            if(isRemove){
                                RemoveUserFromGroup(uid,groupname,groupowner,username);
                            }
                            else{
                                AddUserToGroups(uid,groupname,groupowner,username);
                            }
                        }
                        if(flag==0){
                            Toast.makeText(group_info.this, "Sorry the user "+UserName+" is not the user of our app", Toast.LENGTH_SHORT).show();
                        }
                        else if(flag==1){

                            break;

                        }


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void RemoveUserFromGroup(String uid, String groupname, String groupowner, String username) {

        DatabaseReference RemoveRef=FirebaseDatabase.getInstance().getReference().child("group_members_in_groups").child(groupname).child(uid);
        RemoveRef.removeValue();
        DatabaseReference RemoveGroupRef=FirebaseDatabase.getInstance().getReference().child("groups").child(uid)
                .child("groups for the user "+uid).child(groupname);
        RemoveGroupRef.removeValue();
        Toast.makeText(group_info.this, "User ' "+username+" ' removed from group ' "+groupname+" ' successfully! ", Toast.LENGTH_SHORT).show();
    }

    private void AddUserToGroups(final String userId, final String groupname, String groupowner, final String username) {


        DatabaseReference groupRef=FirebaseDatabase.getInstance().getReference().child("groups").child(userId).child("groups for the user "+userId).child(groupname);

        final HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("created_by",groupowner);
        hashMap.put("group_name",groupname);
        hashMap.put("imageurl","default");


        groupRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(group_info.this,"User "+username+" added successfully",Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(group_info.this,"User "+username+" not added successfully",Toast.LENGTH_SHORT).show();
                }

            }
        });



        DatabaseReference ref_for_group_members=FirebaseDatabase.getInstance().getReference().child("group_members_in_groups").child(groupname).child(userId);
        HashMap<String,Object> hashMap1=new HashMap<>();
        hashMap1.put("userid",userId);
        hashMap1.put("username",username);
        hashMap1.put("group_owner",groupowner);
        hashMap1.put("imageurl","default");
        ref_for_group_members.setValue(hashMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(group_info.this,"User "+username+" added successfully",Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void ReadGroupMembers(final String groupname){

        groupMembers=new ArrayList<>();;


        final DatabaseReference referenceForGroupMembers=FirebaseDatabase.getInstance().getReference().child("group_members_in_groups").child(groupname);
        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users");

        referenceForGroupMembers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Iterator iterator=dataSnapshot.getChildren().iterator();


                while(iterator.hasNext()){

                    String group_owner=(String)((DataSnapshot)iterator.next()).getValue();
                    String imageurl=(String)((DataSnapshot)iterator.next()).getValue();
                    String userid=(String)((DataSnapshot)iterator.next()).getValue();
                    String username=(String)((DataSnapshot)iterator.next()).getValue();

                    addUserTogroupMembers(userid);
                }

            }

            private void addUserTogroupMembers(String userid) {

                assert userid != null;
                databaseReference.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user=dataSnapshot.getValue(User.class);

                        groupMembers.add(user);
                        assert user != null;

//                        Toast.makeText(group_info.this, groupMembers.size()+" members in "+groupname, Toast.LENGTH_SHORT).show();

                       findFriendsAdapter FindFriendsAdapter=new findFriendsAdapter(group_info.this,groupMembers,true,groupname,GroupOwner);
                       recyclerView.setAdapter(FindFriendsAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


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
}