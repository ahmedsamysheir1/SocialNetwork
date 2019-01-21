package com.AhmedSheir.online;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userStatus, userName, userFullname, userCountry, userGender, userRelationship, userDob;
    private CircleImageView userProfileImage;

     private Button SendFriendReqButton , DeclineFriendReqButton ;

    private DatabaseReference FriendRequestRef , UserRef , FriendRef;
    private FirebaseAuth mAuth;
    private String sendUserId , receiverUserid , Current_State , saveCurrentDate ;
    private DatabaseReference notificationRef;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        sendUserId = mAuth.getCurrentUser().getUid();

        receiverUserid = getIntent().getExtras().get("visit_user_id").toString();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        notificationRef.keepSynced(true);

        IntializeFieleds();


        UserRef.child(receiverUserid).addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(PersonProfileActivity.this).load(profileimage).placeholder(R.drawable.profile).into(userProfileImage);

                    maintananceoOFButton();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
        DeclineFriendReqButton.setEnabled(false);

        if (!sendUserId.equals(receiverUserid))
        {
            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    SendFriendReqButton.setEnabled(false);

                    if (Current_State.equals("not_friends"))
                    {
                        SendFrinedRequestToPerson();
                    }

                    if(Current_State.equals("request_sent"))
                    {
                        CancelFriendRequest();

                    }

                    if (Current_State.equals("request_received"))
                    {

                        AcceptFriendRequest();
                    }

                    if(Current_State.equals("friends"))
                    {
                        UnfriendanExistingFriend();

                    }


                }
            });

        }
        else
            {
                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                SendFriendReqButton.setVisibility(View.INVISIBLE);

            }
    }

    private void UnfriendanExistingFriend()
    {

        FriendRef.child(sendUserId).child(receiverUserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRef.child(receiverUserid).child(sendUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {


                                                SendFriendReqButton.setEnabled(true);
                                                Current_State = "not_friends";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqButton.setEnabled(false);
                                            }

                                        }
                                    });


                        }

                    }
                });

    }

    private void AcceptFriendRequest()
    {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd,MMM,yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendRef.child(sendUserId).child(receiverUserid).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRef.child(receiverUserid).child(sendUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {

                                FriendRequestRef.child(sendUserId).child(receiverUserid)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    FriendRequestRef.child(receiverUserid).child(sendUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {


                                                                        SendFriendReqButton.setEnabled(true);
                                                                        Current_State = "Friends";
                                                                        SendFriendReqButton.setText("Unfriend This Person");

                                                                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendReqButton.setEnabled(false);
                                                                    }

                                                                }
                                                            });


                                                }

                                            }
                                        });

                            }

                        }
                    });
                }

            }
        });


    }

    private void CancelFriendRequest()
    {

        FriendRequestRef.child(sendUserId).child(receiverUserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserid).child(sendUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {


                                                SendFriendReqButton.setEnabled(true);
                                                Current_State = "not_friends";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqButton.setEnabled(false);
                                            }

                                        }
                                    });


                        }

                    }
                });
    }


    private void maintananceoOFButton()
    {

        FriendRequestRef.child(sendUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiverUserid))
                {
                    String request_type = dataSnapshot.child(receiverUserid).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {

                        Current_State = "request_sent";
                        SendFriendReqButton.setText("Cancel Friend Request");

                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                        DeclineFriendReqButton.setEnabled(false);

                    }
                    else if(request_type.equals("received"))
                    {
                          Current_State = "request_received";
                          SendFriendReqButton.setText("Accept friend Request");

                          DeclineFriendReqButton.setVisibility(View.VISIBLE);
                          DeclineFriendReqButton.setEnabled(true);

                          DeclineFriendReqButton.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View view)
                              {
                                  CancelFriendRequest();

                              }
                          });

                    }
                }

                else
                    {

                        FriendRef.child(sendUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild(receiverUserid))
                                        {
                                            Current_State = "friends";

                                            SendFriendReqButton.setText("Unfriend This Person");

                                            DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendReqButton.setEnabled(false);

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendFrinedRequestToPerson()
    {
          FriendRequestRef.child(sendUserId).child(receiverUserid)
                  .child("request_type").setValue("sent")
                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task)
                      {
                          if (task.isSuccessful())
                          {
                              FriendRequestRef.child(receiverUserid).child(sendUserId)
                                      .child("request_type").setValue("received")
                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task)
                                  {
                                      if (task.isSuccessful())
                                      {

                                          HashMap <String , String> notificationData = new HashMap<String, String>();
                                          notificationData.put("from" , sendUserId);
                                          notificationData.put("type" ,"request");

                                          notificationRef.child(receiverUserid).push().setValue(notificationData)
                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                      @Override
                                                      public void onComplete(@NonNull Task<Void> task)
                                                      {
                                                          if ((task.isSuccessful()))
                                                          {
                                                              SendFriendReqButton.setEnabled(true);
                                                              Current_State = "request_sent";
                                                              SendFriendReqButton.setText("Cancel Friend Request");

                                                              DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                              DeclineFriendReqButton.setEnabled(false);
                                                          }

                                                      }
                                                  });





                                      }

                                  }
                              });


                          }

                      }
                  });
    }


    private void IntializeFieleds()
    {

        userProfileImage = (CircleImageView) findViewById(R.id.person_profile);

        userStatus = (TextView) findViewById(R.id.person_status);
        userName = (TextView) findViewById(R.id.person_username);
        userFullname = (TextView) findViewById(R.id.person_fullname);
        userCountry = (TextView) findViewById(R.id.person_country);
        userRelationship = (  TextView) findViewById(R.id.person_relationship);
        userGender = (TextView) findViewById(R.id.person_gender);
        userDob = (TextView) findViewById(R.id.person_dob);
        SendFriendReqButton = (Button) findViewById(R.id.person_friend_send_requset_btn);
        DeclineFriendReqButton = (Button) findViewById(R.id.decline_friend_requset);

        Current_State = "not_friends";

    }


}
