package com.AhmedSheir.online;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetteingAcivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userStatus, userName, userFullname, userCountry, userGender, userRelationship, userDob;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfileImage;
    private DatabaseReference SettingUserRef;
    private FirebaseAuth mAuth;
    private String current_user_id;
    final static int gallery_pick = 1 ;
    private ProgressDialog loadingbar;
    private StorageReference UserProfileImage ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setteing_acivity);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        SettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        UserProfileImage = FirebaseStorage.getInstance().getReference().child("ProfileImage");

        mToolbar = (Toolbar) findViewById(R.id.settinges_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingbar = new ProgressDialog(this);

        userProfileImage = (CircleImageView) findViewById(R.id.setting_profile_image);

        userStatus = (EditText) findViewById(R.id.setting_status);
        userName = (EditText) findViewById(R.id.setting_username);
        userFullname = (EditText) findViewById(R.id.setting_profile_fullname);
        userCountry = (EditText) findViewById(R.id.setting_profile_country);
        userGender = (EditText) findViewById(R.id.setting_profile_gender);
        userRelationship = (EditText) findViewById(R.id.setting_profile_relationship);
        userDob = (EditText) findViewById(R.id.setting_profile_date);

        updateAccountSettingsButton = (Button) findViewById(R.id.setting_profile_button);


        SettingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("Username").getValue().toString();
                    String myFullName = dataSnapshot.child("FullName").getValue().toString();
                    String myStatus = dataSnapshot.child("Status").getValue().toString();
                    String myCountry = dataSnapshot.child("Country").getValue().toString();
                    String myrelationshipstatus = dataSnapshot.child("relationship status").getValue().toString();
                    String mygender = dataSnapshot.child("gender").getValue().toString();
                    String mydob = dataSnapshot.child("dob").getValue().toString();

                    Picasso.with(SetteingAcivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText(myUsername);
                    userFullname.setText(myFullName);
                    userCountry.setText(myCountry);
                    userRelationship.setText(myrelationshipstatus);
                    userStatus.setText(myStatus);
                    userGender.setText(mygender);
                    userDob.setText(mydob);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ValidateAccountInfo();

            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent IntentGallery = new Intent();
                IntentGallery.setAction(Intent.ACTION_GET_CONTENT);
                IntentGallery.setType("image/*");
                startActivityForResult(IntentGallery, gallery_pick);

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
                loadingbar.setMessage("please wait , while we are updated your  Profile image... ");
                loadingbar.setCanceledOnTouchOutside(true);
                loadingbar.show();



                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImage.child(current_user_id + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetteingAcivity.this, "profile image is successfully save to Firebase Storage ", Toast.LENGTH_SHORT).show();

                            final String downlaodUrl = task.getResult().getDownloadUrl().toString();
                            SettingUserRef.child("profileimage").setValue(downlaodUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Intent selfintent = new Intent(SetteingAcivity.this , SetteingAcivity.class);
                                                startActivity(selfintent);

                                                Toast.makeText(SetteingAcivity.this, "profile image is successfully save to Firebase Database", Toast.LENGTH_SHORT).show();
                                                loadingbar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetteingAcivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
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

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String fullname = userFullname.getText().toString();
        String country = userCountry.getText().toString();
        String status = userStatus.getText().toString();
        String gender = userGender.getText().toString();
        String dob = userDob.getText().toString();
        String relationship = userRelationship.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "please write your username...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "please write your fullname...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "please write your country name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "please write your status...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "please write your gender...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "please write your date of birth...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(relationship)) {
            Toast.makeText(this, "please write your relationship...", Toast.LENGTH_SHORT).show();
        } else {
            loadingbar.setTitle("Save information");
            loadingbar.setMessage("please wait , while we are updated your  Profile image... ");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            UpdateAccountInfo(username, fullname, country, status, gender, dob, relationship);
        }

    }

    private void UpdateAccountInfo(String username, String fullname, String country, String status, String gender, String dob, String relationship) {

        HashMap userMap = new HashMap();
        userMap.put("Username", username);
        userMap.put("FullName", fullname);
        userMap.put("Country", country);
        userMap.put("Status", status);
        userMap.put("relationship status", relationship);
        userMap.put("gender", gender);
        userMap.put("dob", dob);

        SettingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    SendUserrToMainActivity();
                    Toast.makeText(SetteingAcivity.this, "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
                else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetteingAcivity.this, "Error occured while updated account settings info" + message, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

            }
        });


    }

    private void SendUserrToMainActivity() {

        Intent intentmainactvity = new Intent(SetteingAcivity.this, MainActivity.class);
        intentmainactvity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentmainactvity);
        finish();


    }
}
