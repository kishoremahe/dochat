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
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dochat.Adapter.GroupAdapter;
import com.example.dochat.Adapter.GroupChatAdapter;
import com.example.dochat.Model.Group;
import com.example.dochat.Model.GroupMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private static final int GALLERY_REQUEST= 143;
    private TextView Groupname;
    private EditText SearchBar;
    private String checker;
    private ImageButton Send,SendImage;
    private CircleImageView GroupIcon;
    private List<GroupMessage> GroupChats;
    private GroupChatAdapter groupChatAdapter;
    private RecyclerView recyclerView;
    private String groupname;
    private Uri image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        InitializeFields();


        recyclerView=findViewById(R.id.recycleview_for_groupmsg);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        Intent i=getIntent();
        groupname=i.getStringExtra("GroupName");

        Groupname.setText(groupname);


        DatabaseReference referenceForProfilePic=FirebaseDatabase.getInstance().getReference().child("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("groups for the user "+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupname);
        referenceForProfilePic.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Group group=dataSnapshot.getValue(Group.class);

                if(group.getImageurl().equals("default")){

                    GroupIcon.setImageResource(R.drawable.profile_image);

                }
                else{

                    Glide.with(GroupMessageActivity.this).load(group.getImageurl()).into(GroupIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        readGroupChats(groupname);

        Send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                String msg=SearchBar.getText().toString();

                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(GroupMessageActivity.this, "Please type something", Toast.LENGTH_SHORT).show();
                }
                else{
                    Calendar calForDate=Calendar.getInstance();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDateFormat=new SimpleDateFormat("MM:dd:yyyy");
                    String currentDate=currentDateFormat.format(calForDate.getTime());

                    Calendar calForTime=Calendar.getInstance();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
                    String currentTime=currentTimeFormat.format(calForTime.getTime());

                    StoreMessageInDb(msg,groupname,currentDate,currentTime);
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

                AlertDialog.Builder builder=new AlertDialog.Builder(GroupMessageActivity.this);
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
    }

    private void AskPermissionForGallery() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            AccessGallery();
        }
        else{
//            Toast.makeText(SettingsActivity.this, "AddImage2", Toast.LENGTH_SHORT).show();


            if(ActivityCompat.shouldShowRequestPermissionRationale(GroupMessageActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed for accessing you gallery and internal storage only...")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(GroupMessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);
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
                ActivityCompat.requestPermissions(GroupMessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);

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

            final ProgressDialog progressDialog=new ProgressDialog(GroupMessageActivity.this);
            progressDialog.setTitle("sending...");
            progressDialog.show();

            FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
            String current_user_id=firebaseUser.getUid();


            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("images_files_for_group_chat").child(groupname).child(current_user_id);
            final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(current_user_id);


            storageReference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(GroupMessageActivity.this, "File sent !", Toast.LENGTH_SHORT).show();
                    UpdateImageInGroupMessages(image);

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(GroupMessageActivity.this, "File not sent !", Toast.LENGTH_SHORT).show();
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
    private void UpdateImageInGroupMessages(Uri image) {

        DatabaseReference msgRef=FirebaseDatabase.getInstance().getReference().child("group_messages").child(groupname);
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
        hashMap.put("Time",currentTime);
        hashMap.put("Date",currentDate);

        msgRef.push().setValue(hashMap);
    }

    private void readGroupChats(String groupname) {


        GroupChats=new ArrayList<>();

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference msgReference=FirebaseDatabase.getInstance().getReference().child("group_messages").child(groupname);

        msgReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GroupChats.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    GroupMessage groupMessage=snapshot.getValue(GroupMessage.class);
                    GroupChats.add(groupMessage);
                }

//                Toast.makeText(GroupMessageActivity.this, GroupChats.size()+" messages in groupMessages", Toast.LENGTH_SHORT).show();

                groupChatAdapter=new GroupChatAdapter(getApplicationContext(),GroupChats);
                recyclerView.setAdapter(groupChatAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void StoreMessageInDb(String msg, String groupname,String currentDate,String currentTime) {

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId=firebaseUser.getUid();
        DatabaseReference msgReference=FirebaseDatabase.getInstance().getReference().child("group_messages").child(groupname);


        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("message",msg);
        hashMap.put("sender",firebaseUser.getUid());
        hashMap.put("Time",currentTime);
        hashMap.put("Date",currentDate);
        hashMap.put("msg_type","text");

        msgReference.push().setValue(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_for_group_msg_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


         switch(item.getItemId()){

             case R.id.group_info:

                 FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

                 DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("groups").child(firebaseUser.getUid()).child("groups for the user "+firebaseUser.getUid()).child(groupname);
                 ref.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                         Group group=dataSnapshot.getValue(Group.class);
                         startActivity(new Intent(GroupMessageActivity.this,group_info.class).putExtra("groupName",groupname).putExtra("groupowner",group.getCreated_by()));
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
    }

    private void InitializeFields() {

        Groupname=(TextView)findViewById(R.id.group_name);
        GroupIcon=(CircleImageView)findViewById(R.id.group_icon);
        SearchBar=(EditText)findViewById(R.id.search_bar);
        Send=(ImageButton)findViewById(R.id.btn_send);
        SendImage=(ImageButton)findViewById(R.id.send_image);


    }
}