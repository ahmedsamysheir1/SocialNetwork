package com.AhmedSheir.online;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.choota.dev.ctimeago.TimeAgo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionbartoggle;
    private RecyclerView Postlis;
    private Toolbar toolbar;
    private ImageButton AddNewPostButton;
    private String getSaveCurrentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference UserReference, PostRef , LikesRef;
    private CircleImageView NavProfileimage;
    private TextView NavProfileUserName;
    private String CurrentUserID;

    Boolean LikeCheker = false ;

    TimeAgo timeAgo = new TimeAgo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");



        toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_pos_button);


        drawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        actionbartoggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionbartoggle);
        actionbartoggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        Postlis = (RecyclerView) findViewById(R.id.all_user_post_list);
        Postlis.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        Postlis.setLayoutManager(linearLayoutManager);


        View mavView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileimage = (CircleImageView) mavView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) mavView.findViewById(R.id.nav_user_name);


        UserReference.child(CurrentUserID).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("FullName")) {
                        String fullname = dataSnapshot.child("FullName").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileimage);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);
                return false;
            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPost();


    }

    public void updataUserStatus (String satus)
    {
        String saveCurrentDate , saveCurrentTime ;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentdate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a\"");
        saveCurrentTime = currenttime.format(calForTime.getTime());

        Map CurrentSatusMap = new HashMap();
        CurrentSatusMap.put("time" , saveCurrentTime);
        CurrentSatusMap.put("date" , saveCurrentDate);
        CurrentSatusMap.put("type" , satus);

        UserReference.child(CurrentUserID).child("updatesatus").updateChildren(CurrentSatusMap);

    }





    private void DisplayAllUsersPost() {

        Query SortPostsInDecendingOrder = PostRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts, postViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, postViewHolder>
                        (
                                Posts.class,
                                R.layout.all_post_layout,
                                postViewHolder.class,
                                SortPostsInDecendingOrder
                        )

                {
                    @Override
                    protected void populateViewHolder(final postViewHolder viewHolder, Posts model, int position) {

                        final String PostKey = getRef(position).getKey();



                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setUserprofileimage(getApplicationContext(), model.getUserprofileimage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.setLikesButtonStatus(PostKey);

                        viewHolder.Mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);

                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                Intent commentPost = new Intent(MainActivity.this, CommentsActivity.class);
                                commentPost.putExtra("PostKey", PostKey);
                                startActivity(commentPost);

                            }
                        });


                                viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {

                                            LikeCheker = true ;

                                            LikesRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot)
                                                {

                                                    if(LikeCheker.equals(true))
                                                    {
                                                        if(dataSnapshot.child(PostKey).hasChild(CurrentUserID))
                                                        {
                                                            LikesRef.child(PostKey).child(CurrentUserID).removeValue();
                                                            LikeCheker = false ;
                                                        }

                                                        else
                                                        {
                                                            LikesRef.child(PostKey).child(CurrentUserID).setValue(true);
                                                            LikeCheker = false ;
                                                        }
                                                    }


                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });


                            }
                        });
                    }
                };

        Postlis.setAdapter(firebaseRecyclerAdapter);

        updataUserStatus("online");
    }

    public static class postViewHolder extends RecyclerView.ViewHolder {

        private ImageButton LikePostButton , CommentPostButton ;
        private TextView DisblynofLike;

        int countLikes ;
        String currentUserID ;
        DatabaseReference LikesRef ;


        View Mview;

        public postViewHolder(View itemView)
        {
            super(itemView);

            Mview = itemView;


            LikePostButton = (ImageButton) Mview.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) Mview.findViewById(R.id.comment_button);
            DisblynofLike = (TextView) Mview.findViewById(R.id.disbly_no_of_like);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid() ;


        }

        public void setLikesButtonStatus(final String PostKey)
        {
              LikesRef.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot)
                  {
                      if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                      {
                          countLikes = (int)dataSnapshot.child(PostKey).getChildrenCount();
                          LikePostButton.setImageResource(R.drawable.like);
                          DisblynofLike.setText(Integer.toString(countLikes)+(" Likes"));
                      }

                      else
                          {
                              countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                              LikePostButton.setImageResource(R.drawable.dislike);
                              DisblynofLike.setText(Integer.toString(countLikes)+(" Likes"));
                          }

                  }


                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });









        }

        public void setFullname(String fullname) {
            TextView username = (TextView) Mview.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setUserprofileimage(Context ctx, String userprofileimage) {
            CircleImageView image = (CircleImageView) Mview.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(userprofileimage).into(image);
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) Mview.findViewById(R.id.post_time);
            PostTime.setText("  " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) Mview.findViewById(R.id.post_date);
            PostDate.setText("  " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) Mview.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage) {
            ImageView PostImage = (ImageView) Mview.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }


    }


    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser CurrentUser = mAuth.getCurrentUser();

        if (CurrentUser == null) {
            SendUserToLoginActivity();
        } else {
            CheckUserExistence();

        }
    }


    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        UserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupAcivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void SendUserToSetupAcivity() {
        Intent setupintent = new Intent(MainActivity.this, SetupActivity.class);
        setupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupintent);
        finish();

    }


    private void SendUserToLoginActivity() {

        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionbartoggle.onOptionsItemSelected(item)) {
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_new_post:
                SendUserToPostActivity();
                break;

            case R.id.profile:
                SendUserToProfileActivity();
                break;

            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.friends:
               SendUserToFriendList();
                break;

            case R.id.find_frined:
               SendUserToFindFrinedActivity();
                break;

            case R.id.messages:
               SendUserToFriendList();
                break;

            case R.id.settings:

                SendUserToSettingActivity();

                break;

            case R.id.logout:
                updataUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;


        }


    }

    private void SendUserToFriendList()
    {
        Intent friendIntent = new Intent(MainActivity.this , FriendsList.class);
        startActivity(friendIntent);
    }

    private void SendUserToFindFrinedActivity()
    {
        Intent finedfrinedintent = new Intent(MainActivity.this, FindFrinedActivity.class);
        startActivity(finedfrinedintent);
    }

    private void SendUserToProfileActivity()
    {
        Intent profileintent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileintent);
    }


    private void SendUserToSettingActivity() {

        Intent SettingIntent = new Intent(MainActivity.this, SetteingAcivity.class);
        startActivity(SettingIntent);

    }


}