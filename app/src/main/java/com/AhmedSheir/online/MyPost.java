package com.AhmedSheir.online;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPost extends AppCompatActivity {

    private Toolbar MyToolbar;
    private RecyclerView MyPostList ;

    private DatabaseReference PostRef , UserRef , LikesRef;
    private FirebaseAuth mAuth ;
    private String Current_user_id ;
    Boolean LikeCheker = false ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        mAuth = FirebaseAuth.getInstance();
        Current_user_id = mAuth.getCurrentUser().getUid();

        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");




        MyToolbar = (Toolbar) findViewById(R.id.my_post_bar);
        setSupportActionBar(MyToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        MyPostList = (RecyclerView) findViewById(R.id.my_all_post_list);
        MyPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        MyPostList.setLayoutManager(linearLayoutManager);

        DisplayAllUserPost();

    }

    private void DisplayAllUserPost()
    {

        Query myPostQurry = PostRef.orderByChild("uid")
                .startAt(Current_user_id).endAt(Current_user_id + "\uf8ff");

        FirebaseRecyclerAdapter<Posts ,MyPostViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, MyPostViewHolder>
                (

                        Posts.class,
                        R.layout.all_post_layout,
                        MyPostViewHolder.class,
                        myPostQurry




                )
        {
            @Override
            protected void populateViewHolder(MyPostViewHolder viewHolder, Posts model, int position)
            {
                final String PostKey = getRef(position).getKey();


                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setUserprofileimage(getApplicationContext(), model.getUserprofileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());


                viewHolder.setLikesButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent clickPostIntent = new Intent(MyPost.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);

                    }
                });

                viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent commentPost = new Intent(MyPost.this, CommentsActivity.class);
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
                                    if(dataSnapshot.child(PostKey).hasChild(Current_user_id))
                                    {
                                        LikesRef.child(PostKey).child(Current_user_id).removeValue();
                                        LikeCheker = false ;
                                    }

                                    else
                                    {
                                        LikesRef.child(PostKey).child(Current_user_id).setValue(true);
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

        MyPostList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyPostViewHolder extends RecyclerView.ViewHolder {

        View mView ;

        private ImageButton LikePostButton , CommentPostButton ;
        private TextView DisblynofLike;

        int countLikes ;
        String currentUserID ;
        DatabaseReference LikesRef ;

        public MyPostViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView ;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisblynofLike = (TextView) mView.findViewById(R.id.disbly_no_of_like);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid() ;

        }

        public void setLikesButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserID)) {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisblynofLike.setText(Integer.toString(countLikes) + (" Likes"));
                    } else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisblynofLike.setText(Integer.toString(countLikes) + (" Likes"));
                    }

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

            public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setUserprofileimage(Context ctx, String userprofileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(userprofileimage).into(image);
        }

        public void setTime(String time) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("  " + time);
        }

        public void setDate(String date) {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("  " + date);
        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage) {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }


}
