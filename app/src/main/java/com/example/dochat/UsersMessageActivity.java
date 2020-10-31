package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dochat.Adapter.GroupChatAdapter;
import com.example.dochat.Adapter.PrivateChatAdapter;
import com.example.dochat.Fragments.ChatFragment;
import com.example.dochat.Model.GroupMessage;
import com.example.dochat.Model.PrivateMessage;
import com.example.dochat.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersMessageActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST= 143;
    private String checker;
    private Toolbar toolbar;
    private TextView UserName,Last_Seen_Date,Last_Seen_Time;
    private EditText SearchBar;
    private ImageButton Send,SendImage;
    private CircleImageView UserIcon;
    private List<PrivateMessage> Chats;
    private Uri image;
    private GroupChatAdapter groupChatAdapter;
    private RecyclerView recyclerView;
    private String Username,Usericon,About,Userid,Last_Seen_date,Last_Seen_time,Userstatus;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_message);

        InitializeFields();
        GetValues();
        SetValues();

        recyclerView=findViewById(R.id.recycleview_for_usermsg);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ReadChatsForTheUser();

        Send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                String msg=SearchBar.getText().toString();

                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(UsersMessageActivity.this, "Please type something", Toast.LENGTH_SHORT).show();
                }
                else{
                    Calendar calForDate=Calendar.getInstance();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDateFormat=new SimpleDateFormat("MM:dd:yyyy");
                    String currentDate=currentDateFormat.format(calForDate.getTime());

                    Calendar calForTime=Calendar.getInstance();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
                    String currentTime=currentTimeFormat.format(calForTime.getTime());

                    StoreMessageInDb(msg,currentDate,currentTime);
                }

                SearchBar.setText("");

            }
        });

        final CharSequence []options=new CharSequence[]{
                "Image",
                "Pdf files",
                "docx"
        };

        SendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder=new AlertDialog.Builder(UsersMessageActivity.this);
                builder.setTitle("choose an otption");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){
                            checker="image";
                            AskPermissionForGallery();
                        }
                        if(which ==1){
                            checker="pdf";

                        }
                        if(which == 2){
                            checker="docx";

                        }

                    }
                });
                builder.show();

            }
        });

        seenMessage(Userid);

    }

    private void StoreMessageInDb(String msg, String currentDate, String currentTime) {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId=firebaseUser.getUid();
        DatabaseReference msgReference= FirebaseDatabase.getInstance().getReference().child("private_messages");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("message",msg);
        hashMap.put("msg_type","text");
        hashMap.put("sender",currentUserId);
        hashMap.put("receiver",Userid);
        hashMap.put("Time",currentTime);
        hashMap.put("Date",currentDate);
        hashMap.put("isseen",false);

        msgReference.push().setValue(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_for_user_info,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){

            case R.id.user_info:

                Intent UserInfoIntent = new Intent(UsersMessageActivity.this, ProfileActivity.class);
                UserInfoIntent.putExtra("UserName", Username);
                UserInfoIntent.putExtra("About", About);
                UserInfoIntent.putExtra("userid", Userid);
                UserInfoIntent.putExtra("ProfileImage", Usericon);
                startActivity(UserInfoIntent);
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    private void ReadChatsForTheUser() {

        Chats=new ArrayList<>();
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserId=firebaseUser.getUid();

        DatabaseReference msgReference=FirebaseDatabase.getInstance().getReference().child("private_messages");

        msgReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Chats.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    PrivateMessage privateMessage=snapshot.getValue(PrivateMessage.class);

                    assert privateMessage != null;
                    if(privateMessage.getSender().equals(currentUserId) && privateMessage.getReceiver().equals(Userid)
                    || privateMessage.getSender().equals(Userid) && privateMessage.getReceiver().equals(currentUserId)){

                        Chats.add(privateMessage);

                    }
                }


                PrivateChatAdapter privateChatAdapter=new PrivateChatAdapter(getApplicationContext(),Chats);
                recyclerView.setAdapter(privateChatAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SetValues() {

        UserName.setText(Username);
        DatabaseReference UserRef=FirebaseDatabase.getInstance().getReference().child("users").child(Userid);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);

                if(user.getStatus().equals("online")){
                    Last_Seen_Time.setText("online");
                    Last_Seen_Date.setText("");
                }
                else{
                    Last_Seen_Time.setText("Last seen at "+Last_Seen_time);
                    Last_Seen_Date.setText(Last_Seen_date);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(Usericon.equals("default")){
            UserIcon.setImageResource(R.drawable.profile_image);
        }
        else{
            Glide.with(UsersMessageActivity.this).load(Usericon).into(UserIcon);
        }

    }

    private void seenMessage(final String userid){

        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("private_messages");

        seenListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){

                    PrivateMessage privateMessage=dataSnapshot.getValue(PrivateMessage.class);

                    assert firebaseUser != null;
                    assert privateMessage != null;
                    if((privateMessage.getReceiver().equals(firebaseUser.getUid())) && (privateMessage.getSender().equals(userid))){

                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetValues() {

        Intent i=getIntent();

        Username=i.getStringExtra("username");
        Usericon=i.getStringExtra("usericon");
        About=i.getStringExtra("about");
        Userid=i.getStringExtra("userid");
        Last_Seen_date=i.getStringExtra("Last_seen_date");
        Last_Seen_time=i.getStringExtra("Last_seen_time");


    }

    private void InitializeFields() {
        UserName=(TextView)findViewById(R.id.user_name);
        UserIcon=(CircleImageView)findViewById(R.id.user_icon);
        SearchBar=(EditText)findViewById(R.id.search_bar);
        Send=(ImageButton)findViewById(R.id.btn_send);
        SendImage=(ImageButton)findViewById(R.id.send_image);
        Last_Seen_Date=(TextView)findViewById(R.id.last_seen_date);
        Last_Seen_Time=(TextView)findViewById(R.id.last_seen_time);
    }

    public void AskPermissionForGallery() {

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            AccessGallery();
        }
        else{
//            Toast.makeText(SettingsActivity.this, "AddImage2", Toast.LENGTH_SHORT).show();


            if(ActivityCompat.shouldShowRequestPermissionRationale(UsersMessageActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed for accessing you gallery and internal storage only...")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(UsersMessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);
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
                ActivityCompat.requestPermissions(UsersMessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == SettingsActivity.PERMISSION_REQUEST){

            if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
//                Toast.makeText(SettingsActivity.this, "AddImage3", Toast.LENGTH_SHORT).show();

                AccessGallery();

            }
            else{
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void AccessGallery() {

//        Toast.makeText(SettingsActivity.this, "AddImage4", Toast.LENGTH_SHORT).show();


        Intent GalleryIntent=new Intent();
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(GalleryIntent,"Pick an image "),GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null){

            if(checker.equals("image")){
                image=data.getData();
//            Toast.makeText(getApplicationContext(), "after image stored in uri", Toast.LENGTH_SHORT).show();
                UploadImageToFirebaseStorage(image);
            }

        }
    }

    private void UploadImageToFirebaseStorage(final Uri image) {

        if(image != null){

            final ProgressDialog progressDialog=new ProgressDialog(UsersMessageActivity.this);
            progressDialog.setTitle("sending...");
            progressDialog.show();

            FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
            String current_user_id=firebaseUser.getUid();


            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("images_files_for_private_chat").child(current_user_id).child(Userid);
            final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(current_user_id);


            storageReference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(UsersMessageActivity.this, "File sent !", Toast.LENGTH_SHORT).show();
                    UpdateImageInPrivateMessages(image);

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UsersMessageActivity.this, "File not sent !", Toast.LENGTH_SHORT).show();
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress=(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("sent "+(int)progress+"% completed !");

                        }
                    });

        }



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UpdateImageInPrivateMessages(Uri image) {

        DatabaseReference msgRef=FirebaseDatabase.getInstance().getReference().child("private_messages");
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId=firebaseUser.getUid();

        Calendar calForDate=Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDateFormat=new SimpleDateFormat("MM:dd:yyyy");
        String currentDate=currentDateFormat.format(calForDate.getTime());

        Calendar calForTime=Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
        String currentTime=currentTimeFormat.format(calForTime.getTime());

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("message",image.toString());
        hashMap.put("msg_type","image");
        hashMap.put("sender",currentUserId);
        hashMap.put("receiver",Userid);
        hashMap.put("Time",currentTime);
        hashMap.put("Date",currentDate);
        hashMap.put("isseen",false);

        msgRef.push().setValue(hashMap);

    }

}