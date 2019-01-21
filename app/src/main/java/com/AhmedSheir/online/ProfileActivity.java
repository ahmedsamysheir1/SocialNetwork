package com.AhmedSheir.online;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userStatus, userName, userFullname, userCountry, userGender, userRelationship, userDob;
    private CircleImageView userProfileImage;

    private DatabaseReference UserProfileRef;
    private FirebaseAuth mAuth;
    private String current_user_id;

    private Button MyPosts , MyFriends ;

    private DatabaseReference FriendRef , PostRef ;

    private int countFriend = 0 ;
    private int countPost = 0 ;

    private DatabaseReference notificationRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UserProfileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        notificationRef.keepSynced(true);



        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);

        userStatus = (TextView) findViewById(R.id.my_status);
        userName = (TextView) findViewById(R.id.my_username);
        userFullname = (TextView) findViewById(R.id.my_profile_fullname);
        userCountry = (TextView) findViewById(R.id.my_country);
        userRelationship = (  TextView) findViewById(R.id.my_relationship);
        userGender = (TextView) findViewById(R.id.my_gender);
        userDob = (TextView) findViewById(R.id.my_dob);
        MyPosts = (Button) findViewById(R.id.my_post_button) ;
        MyFriends = (Button) findViewById(R.id.my_friends_button);


        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToFriendList();

            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToMyPostActivity();

            }
        });


        PostRef.orderByChild("uid")
                .startAt(current_user_id).endAt(current_user_id + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.exists())
                        {
                            countPost = (int) dataSnapshot.getChildrenCount();
                            MyPosts.setText(Integer.toString(countPost) + "  Posts");
                        }
                        else
                            {
                                MyPosts.setText("0 Posts");
                            }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        UserProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists())
                {
                    String status = dataSnapshot.child("Status").getValue().toString();
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String fullname = dataSnapshot.child("FullName").getValue().toString();
                    String country = dataSnapshot.child("Country").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String relationship = dataSnapshot.child("relationship status").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String profileimage = dataSnapshot.child("profileimage").getValue().toString();


                    userStatus.setText(""+ status);
                    userName.setText("@: " + username);
                    userFullname.setText(fullname);
                    userCountry.setText("Country: " + country);
                    userRelationship.setText("Relationship: " + relationship);
                    userDob.setText("Date of Birth: " + dob);
                    userGender.setText("Gender: " + gender);

                    Picasso.with(ProfileActivity.this).load(profileimage).placeholder(R.drawable.profile).into(userProfileImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        FriendRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {

                    countFriend = (int) dataSnapshot.getChildrenCount();
                    MyFriends.setText(Integer.toString(countFriend)+ "  Friends");

                }
                else
                    {
                        MyFriends.setText("0 Friends");
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void SendUserToFriendList()
    {
        Intent friendIntent = new Intent(ProfileActivity.this , FriendsList.class);
        startActivity(friendIntent);
    }

    private void SendUserToMyPostActivity()
    {
        Intent myPost = new Intent(ProfileActivity.this , MyPost.class);
        startActivity(myPost);
    }
}
