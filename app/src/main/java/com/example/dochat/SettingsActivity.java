package com.example.dochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.dochat.Model.User;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
//import io.grpc.Context;

public class SettingsActivity extends AppCompatActivity {

    public static int GALLERY_REQUEST_CODE = 123 ,PERMISSION_REQUEST=10;
    private CircleImageView ProfileImage;
    private EditText Username;
    private EditText About;
    private Button Update;
    private static int test=0;
    private static int test1=0;
    private ImageView AddImage;
    private static int flag1=0;
    private Uri imagepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        Toolbar toolbar=(Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chatty");

        AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Toast.makeText(SettingsActivity.this, "AddImage1", Toast.LENGTH_SHORT).show();
                AskPermissionForGallery();

            }
        });

        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateUserInformations();
            }
        });

        retrieveUserInfo();

        DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);

                assert user != null;
                if(user.getImageurl().equals("default")){
                    ProfileImage.setImageResource(R.drawable.profile_image);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(ProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void AskPermissionForGallery() {

        if(ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            AccessGallery();
        }
        else{
//            Toast.makeText(SettingsActivity.this, "AddImage2", Toast.LENGTH_SHORT).show();


            if(ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed for accessing you gallery only")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST);
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
                ActivityCompat.requestPermissions(SettingsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SettingsActivity.PERMISSION_REQUEST);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST){

            if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
//                Toast.makeText(SettingsActivity.this, "AddImage3", Toast.LENGTH_SHORT).show();

                AccessGallery();

            }
            else{
                Toast.makeText(SettingsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void AccessGallery() {

//        Toast.makeText(SettingsActivity.this, "AddImage4", Toast.LENGTH_SHORT).show();


        Intent GalleryIntent=new Intent();
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(GalleryIntent,"Pick an image "),GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){

//            Toast.makeText(SettingsActivity.this, "AddImage5", Toast.LENGTH_SHORT).show();


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imagepath=result.getUri();

            try{

                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
                ProfileImage.setImageBitmap(bitmap);
                UploadImageToFirebaseStorage();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void UploadImageToFirebaseStorage() {

        if(imagepath != null){

            flag1=1;

            final ProgressDialog progressDialog=new ProgressDialog(SettingsActivity.this);
            progressDialog.setTitle("uploading...");
            progressDialog.show();

            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("profile_images_for_users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


            storageReference.putFile(imagepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Image not uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress=(100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded"+(int)progress+"%");

                        }
                    });

        }



    }


    private void retrieveUserInfo() {

        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user=dataSnapshot.getValue(User.class);

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("about"))){

                    Username.setText(user.getUsername());
                    About.setText(user.getAbout());

                }
                else{

                    Toast.makeText(SettingsActivity.this, "welcome", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void UpdateUserInformations() {

        String username=Username.getText().toString();
        String about=About.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(),"Please fill the username",Toast.LENGTH_SHORT).show();
        }
        else{

            final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            assert firebaseUser != null;
            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());

            if(imagepath != null ){
                HashMap<String,Object> hashMap1=new HashMap<>();
                hashMap1.put("imageurl",imagepath.toString());
                databaseReference.updateChildren(hashMap1);
                test1=1;
            }
            if(!(about.equals("i am using chatty"))){
                HashMap<String,Object> hashMap2=new HashMap<>();
                hashMap2.put("about",about);
                databaseReference.updateChildren(hashMap2);
                test1=1;
            }
            if(!(TextUtils.isEmpty(username))){
                checkUserNameExistence(username);
            }

        }
    }

    private void checkUserNameExistence(final String username) {

        DatabaseReference usersReference=FirebaseDatabase.getInstance().getReference().child("users");
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                assert firebaseUser != null;
                int flag=0;

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    if(dataSnapshot.exists()){

                        User user=snapshot.getValue(User.class);

                        if((username.equals(user.getUsername())) && !(firebaseUser.getUid().equals(user.getUid()))){

                            flag=1;
                            Toast.makeText(SettingsActivity.this, username+" already exists", Toast.LENGTH_SHORT).show();
                            break;
                        }

                    }

                }
                if(flag == 0){

                    test=1;

                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
                    HashMap<String,Object> hashMap3=new HashMap<>();
                    hashMap3.put("username",username);
                    hashMap3.put("uid",firebaseUser.getUid());
                    databaseReference.updateChildren(hashMap3);

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(test == 1){
            SendUserToMainActivity();
        }

    }

    private void SendUserToMainActivity() {
//        Toast.makeText(SettingsActivity.this, "in checkusernameexistence for main", Toast.LENGTH_SHORT).show();

        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeFields() {

        ProfileImage=(CircleImageView)findViewById(R.id.profileImage);
        Username=(EditText)findViewById(R.id.username);
        About=(EditText)findViewById(R.id.about);
        Update=(Button)findViewById(R.id.update);
        AddImage=(ImageView)findViewById(R.id.add_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.for_settings_activity,menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){

            case R.id.signOut_option:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    updateUserState("offline");
                }
                FirebaseUser firebaseuser=FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId=firebaseuser.getUid();
                DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                HashMap<String,Object> hashmap=new HashMap<>();
                hashmap.put("login_status","loggedout");
                userRef.updateChildren(hashmap);
                FirebaseAuth.getInstance().signOut();
                SendUserToLoginActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(getApplicationContext(),LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}