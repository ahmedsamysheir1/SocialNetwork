package com.AhmedSheir.online;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.time.Instant;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName , FullName ,CountryName ;
    private Button Saveinformation ;
    private CircleImageView ProfileImage ;
    private FirebaseAuth mAuth ;
    private DatabaseReference UserRef;
    String CurrentUserID;
     private ProgressDialog loadingbar;
     private static int gallery_pick = 1 ;
     private StorageReference UserProfileImage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth =FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID);
        UserProfileImage = FirebaseStorage.getInstance().getReference().child("ProfileImage");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country);
        Saveinformation = (Button) findViewById(R.id.setup_information_profile);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);

        loadingbar = new ProgressDialog(this);


        Saveinformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                SaveAccountSetupIninFormation();
            }
        });




        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                    Intent IntentGallery = new Intent();
                    IntentGallery.setAction(Intent.ACTION_GET_CONTENT);
                    IntentGallery.setType("image/*");
                    startActivityForResult(IntentGallery, gallery_pick);
            }
        });


        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gallery_pick && resultCode == RESULT_OK && data!= null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingbar.setTitle("Profile Image");
                loadingbar.setMessage("please wait , while we are creating your  Profile image... ");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);


                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImage.child(CurrentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "profile image is successfully save to Firebase Storage ", Toast.LENGTH_SHORT).show();

                            final String downlaodUrl = task.getResult().getDownloadUrl().toString();
                            UserRef.child("profileimage").setValue(downlaodUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Intent selfintent = new Intent(SetupActivity.this , SetupActivity.class);
                                                startActivity(selfintent);

                                                Toast.makeText(SetupActivity.this, "profile image is successfully save to Firebase Database", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                            else 
                                                {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
                                                    loadingbar.dismiss();
                                                }
                                        }
                                    });
                        }
                    }
                });
            }
            else
                {
                    Toast.makeText(this, "Error Occurd: Image can be  Cropped try Again", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
        }
    }
    private void SaveAccountSetupIninFormation()
    {

        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country =  CountryName.getText().toString();
        
        
        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "please write your username...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "please write your full name...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "please write your country...", Toast.LENGTH_SHORT).show();
        }



        else
            {
                loadingbar.setTitle("Saving Information");
                loadingbar.setMessage("please wait , while we are creating your  new account... ");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);


                HashMap usermap = new HashMap();
                usermap.put("Username", username);
                usermap.put("FullName" , fullname);
                usermap.put("Country", country);
                usermap.put("Status", "hey there im using poster social network ");
                usermap.put("gender","none");
                usermap.put("dob", "none");
                usermap.put("relationship status" , "none");

                UserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                     if (task.isSuccessful())
                     {
                         SendUserToMainActivity();
                         Toast.makeText(SetupActivity.this, "your account is created successfully", Toast.LENGTH_LONG).show();
                         loadingbar.dismiss();
                     }
                     
                     else 
                         {
                             String message = task.getException().getMessage();
                             Toast.makeText(SetupActivity.this, "error occurred " + message, Toast.LENGTH_SHORT).show();
                             loadingbar.dismiss();
                         }

                    }
                });

            }



    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
