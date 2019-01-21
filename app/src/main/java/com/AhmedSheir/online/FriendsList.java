package com.AhmedSheir.online;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsList extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendListRef, UserRef ;
    private FirebaseAuth mAuth;
    private String Online_user_id ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        mAuth = FirebaseAuth.getInstance();
        Online_user_id = mAuth.getCurrentUser().getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendListRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(Online_user_id);


        myFriendList = (RecyclerView) findViewById(R.id.friend_list);

        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriend();

    }


    public void updataUserStatus (String satus)
    {
        String saveCurrentDate , saveCurrentTime ;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentdate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currenttime.format(calForTime.getTime());

        Map CurrentSatusMap = new HashMap();
        CurrentSatusMap.put("time" , saveCurrentTime);
        CurrentSatusMap.put("date" , saveCurrentDate);
        CurrentSatusMap.put("type" , satus);

        UserRef.child(Online_user_id).child("updatesatus").updateChildren(CurrentSatusMap);

    }


    @Override
    protected void onStart() {
        super.onStart();

        updataUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        updataUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updataUserStatus("offline");
    }

    private void DisplayAllFriend()
    {
        FirebaseRecyclerAdapter<Firends,FriendViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Firends, FriendViewHolder>
                (
                        Firends.class,
                        R.layout.friend_list_display,
                        FriendViewHolder.class,
                        FriendListRef

                )
        {
            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Firends model, int position)
            {
                viewHolder.setDate(model.getDate());

                final String AllUsersIDS = getRef(position).getKey();

                UserRef.child(AllUsersIDS).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String username = dataSnapshot.child("FullName").getValue().toString();
                            String profileimage = dataSnapshot.child("profileimage").getValue().toString();

                            final String type;

                                    if(dataSnapshot.hasChild("updatesatus"))
                                    {
                                        type = dataSnapshot.child("updatesatus").child("type").getValue().toString();

                                       if(type.equals("online"))
                                       {
                                           viewHolder.onlineStatusImage.setVisibility(View.VISIBLE);
                                       }
                                       else
                                           {

                                               viewHolder.onlineStatusImage.setVisibility(View.INVISIBLE);

                                           }
                                    }

                            viewHolder.setFullName(username);
                            viewHolder.setProfileimage(getApplicationContext(),profileimage);


                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {

                                    CharSequence Options[] = new CharSequence[]
                                            {
                                              username + "'s Profile",
                                                    "Send Message"
                                            };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsList.this);
                                    builder.setTitle("Select Option");
                                    builder.setItems(Options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            if (i == 0)
                                            {
                                                Intent profileUser = new Intent(FriendsList.this , PersonProfileActivity.class);
                                                profileUser.putExtra("visit_user_id", AllUsersIDS);
                                                startActivity(profileUser);

                                            }

                                            if (i == 1)
                                            {

                                                Intent Chat = new Intent(FriendsList.this , ChatActivity.class);
                                                Chat.putExtra("visit_user_id", AllUsersIDS);
                                                Chat.putExtra("UserName" , username);
                                                startActivity(Chat);

                                            }

                                        }
                                    });
                                    builder.show();
                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder
    {
        View mView ;


        ImageView onlineStatusImage;

        public FriendViewHolder(View itemView) {
            super(itemView);

            mView = itemView ;

            onlineStatusImage = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }


        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_user_profileimage_list);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullName(String fullName)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_user_name_list);
            myName.setText(fullName);
        }


        public void setDate(String date)
        {
            TextView FriendsDate = (TextView) mView.findViewById(R.id.all_users_date_time);
            FriendsDate.setText("Friends Since: " + date);
        }



    }
}
