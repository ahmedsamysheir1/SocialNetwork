package com.AhmedSheir.online;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.choota.dev.ctimeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar ;
    private ImageView SelectImagePost;
    private EditText PostDescription;
    private Button UpdatePostButton;
    private static final int galleryPack = 1 ;
    private Uri ImageURI ;
    private String  description ;
    private StorageReference PostImageReference;
    private String   getSaveCurrentTime , saveCurrentDate ,  postRandomName , downloadUrl , current_user_id;
    private DatabaseReference UsersRef , PostRef ;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar ;
    private TextView search_photo ;
    private TextView showNameUser ;
    private String Current_User_id;
    private CircleImageView showImageUser ;
    private long countPosts = 0 ;
    //private Date getSaveCurrentTime , saveCurrentDate ;

    //private TimeAgo agoo ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        SelectImagePost = (ImageView) findViewById(R.id.select_post_image);
        PostDescription = (EditText) findViewById(R.id.post_description);
        UpdatePostButton = (Button)findViewById(R.id.update_post);
        search_photo = (TextView)findViewById(R.id.search_post);
        showNameUser = (TextView)findViewById(R.id.show_name_post);
        showImageUser = (CircleImageView)findViewById(R.id.show_image_user);





        loadingbar = new ProgressDialog(this);


        PostImageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Post");


        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("FullName"))
                    {
                        String fullname= dataSnapshot.child("FullName").getValue().toString();
                        showNameUser.setText(fullname);
                    }

                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.hasChild("profileimage"))
                        {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.with(PostActivity.this).load(image).placeholder(R.drawable.profile).into(showImageUser);
                        }
                    }




                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });






        search_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                OpenGallery();

            }
        });



        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                validatePostInfo();
            }
        });
    }

    private void validatePostInfo()
    {
             description = PostDescription.getText().toString();

           if (ImageURI == null)
           {
               Toast.makeText(this, "Please select post image... ", Toast.LENGTH_SHORT).show();
           }

          else if (TextUtils.isEmpty(description))
           {
               Toast.makeText(this, "Please say something about your image... ", Toast.LENGTH_SHORT).show();
           }

           else
               {
                   loadingbar.setTitle("Add New Post");
                   loadingbar.setMessage("please wait , while we are updating your new Post");
                   loadingbar.show();
                   loadingbar.setCanceledOnTouchOutside(true);


                   StoringImageToFirebaseStorage();
               }





    }

    private void StoringImageToFirebaseStorage()
    {


        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd,MMM,yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        getSaveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + getSaveCurrentTime ;

        StorageReference filepath = PostImageReference.child("Post Images").child(ImageURI.getLastPathSegment()+postRandomName +".jpg");

        filepath.putFile(ImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
              if (task.isSuccessful())
              {
                  downloadUrl = task.getResult().getDownloadUrl().toString();

                  Toast.makeText(PostActivity.this, "image uploaded successfully to Storage", Toast.LENGTH_SHORT).show();

                  SavingPostInfromationToDatebase();
              }
              else
                  {
                      String message = task.getException().getMessage();
                      Toast.makeText(PostActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                  }
            }
        });
    }

    private void SavingPostInfromationToDatebase()
    {
        PostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               if(dataSnapshot.exists())
                   {
                       countPosts = dataSnapshot.getChildrenCount();

                   }
               else
                   {
                      countPosts = 0 ;
                   }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
              if (dataSnapshot.exists())
              {
                  String userFullname = dataSnapshot.child("FullName").getValue().toString();
                  String userProfileimae = dataSnapshot.child("profileimage").getValue().toString();

                  HashMap posttMap = new HashMap();
                  posttMap.put("uid" , current_user_id);
                  posttMap.put("date" , saveCurrentDate);
                  posttMap.put("time" , getSaveCurrentTime);
                  posttMap.put("description" ,description);
                  posttMap.put("postimage" , downloadUrl);
                  posttMap.put("userprofileimage" , userProfileimae);
                  posttMap.put("fullname" ,userFullname);
                  posttMap.put("counter", countPosts);

                  PostRef.child(current_user_id + postRandomName).updateChildren(posttMap).addOnCompleteListener(new OnCompleteListener() {
                      @Override
                      public void onComplete(@NonNull Task task)
                      {

                          if (task.isSuccessful())
                          {
                              SendUserToMainActivity();
                              Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();
                              loadingbar.dismiss();
                          }
                          else
                          {
                              Toast.makeText(PostActivity.this, "Error occured while updating your post", Toast.LENGTH_SHORT).show();
                              loadingbar.dismiss();
                          }
                      }
                  });



              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent intentGallery = new Intent();
        intentGallery.setAction(Intent.ACTION_GET_CONTENT);
        intentGallery.setType("image/*");
        startActivityForResult(intentGallery ,galleryPack );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPack && resultCode == RESULT_OK && data!=null)
        {
            ImageURI = data.getData() ;

            SelectImagePost.setImageURI(ImageURI);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home)
        {
          SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this , MainActivity.class);
        startActivity(mainIntent);
    }
}
