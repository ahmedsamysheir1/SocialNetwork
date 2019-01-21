package com.AhmedSheir.online;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button editButton, deleteButton;
    private String PostKey, current_user_id, datebaseUserid, description, image;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);


        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        editButton = (Button) findViewById(R.id.post_edit);
        deleteButton = (Button) findViewById(R.id.post_delete);


        deleteButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();

                    datebaseUserid = dataSnapshot.child("uid").getValue().toString();


                    postDescription.setText(description);
                    Picasso.with(ClickPostActivity.this).load(image).into(postImage);

                    if (current_user_id.equals(datebaseUserid))
                    {

                        deleteButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);


                    }

                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {

                            EditCurrentPost(description);

                        }
                    });
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DeleteCurrentPost();

            }
        });

    }

    private void EditCurrentPost(String description)
    {
         AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputFieled = new EditText(ClickPostActivity.this);
        inputFieled.setText(description);
        builder.setView(inputFieled);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
               clickPostRef.child("description").setValue(inputFieled.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post has been updated", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

                dialogInterface.cancel();



            }
        });



        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);


        dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light);





    }

    private void DeleteCurrentPost() {
        clickPostRef.removeValue();
        SendUserrToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();

    }

    private void SendUserrToMainActivity() {

        Intent intentmainactvity = new Intent(ClickPostActivity.this, MainActivity.class);
        intentmainactvity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentmainactvity);
        finish();
    }

}
