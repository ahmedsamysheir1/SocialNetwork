package com.AhmedSheir.online;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView CommentsList;
    private ImageButton PostCommentBtn;
    private EditText CommentsInputText ;
    private String Post_key;
    private DatabaseReference UsersRef,PostRef ;
    private FirebaseAuth mAuth;
    private String CurrentUserID ;
    private CircleImageView userCommentImage ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);



        Post_key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");


        CommentsList = (RecyclerView) findViewById(R.id.comment_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        PostCommentBtn = (ImageButton) findViewById(R.id.post_comment_btn);
        CommentsInputText = (EditText) findViewById(R.id.comment_input);
        userCommentImage = (CircleImageView) findViewById(R.id.comment_profileimage);

        PostCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                UsersRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.exists())
                        {
                            String useriamge = dataSnapshot.child("profileimage").getValue().toString();


                            String username = dataSnapshot.child("Username").getValue().toString();
                            validateCommnent(username , useriamge);

                            CommentsInputText.setText("");


                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Commnents,CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Commnents, CommentsViewHolder>
                (
                       Commnents.class,
                        R.layout.all_comment_layout,
                        CommentsViewHolder.class,
                        PostRef



                )
        {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Commnents model, int position)
            {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());

            }
        };

        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView ;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView ;
        }


        public void setUsername(String username)
        {
            TextView commentUsername = (TextView) mView.findViewById(R.id.comment_username);
            commentUsername.setText(username);
        }

        public void setTime(String time)
        {
            TextView commentTime = (TextView) mView.findViewById(R.id.comment_time);
            commentTime.setText("  Time: "+time);
        }


        public void setComment(String comment)
        {
            TextView Comment = (TextView) mView. findViewById(R.id.comment_text);
            Comment.setText(comment);
        }

        public void setProfileimage(Context ctx ,String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.comment_profileimage);
            Picasso.with(ctx).load(profileimage).into(image);
        }
    }

    private void validateCommnent(String username, String useriamge)
    {
        String CommentText = CommentsInputText.getText().toString();
        
        if(TextUtils.isEmpty(CommentText))
        {
            Toast.makeText(this, "please write comment...", Toast.LENGTH_SHORT).show();
        }

        else
            {

                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
             final String  getSaveCurrentTime = currentTime.format(calFordTime.getTime());

             final String RandomKey = CurrentUserID  +getSaveCurrentTime ;

                HashMap commentHashMap = new HashMap();
                commentHashMap.put("uid", CurrentUserID);
                commentHashMap.put("time", getSaveCurrentTime);
                commentHashMap.put("comment",CommentText);
                commentHashMap.put("username", username);
                commentHashMap.put("profileimage",useriamge);

                PostRef.child(RandomKey).updateChildren(commentHashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(CommentsActivity.this, "you have commented successfully", Toast.LENGTH_SHORT).show();
                        }

                        else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this, "Erorr Oucured try Again...", Toast.LENGTH_SHORT).show();
                            }
                    }
                });
            }
    }
}
