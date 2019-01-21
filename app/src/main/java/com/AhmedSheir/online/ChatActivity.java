package com.AhmedSheir.online;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChatToolbar ;
    private ImageButton SendMessageButton , SendImageFileButton ;
    private EditText UserInputMessage ;
    private RecyclerView MessageList ;
    private String MessageReceiverID , MessageReceiverName , MessageSenderID , saveCurrentDate ,getSaveCurrentTime , postRandomName ;

    private TextView ReciverName , UserLastSeen ;
    private CircleImageView ReciverProfileImage;
    private DatabaseReference RootRef , UserRef ;
    private FirebaseAuth mAuth ;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager ;
    private MessagesAdapter messagesAdapter ;




    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        MessageSenderID = mAuth.getCurrentUser().getUid();



        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        MessageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        MessageReceiverName = getIntent().getExtras().get("UserName").toString();


        Log.i(TAG, "onCreate: " + MessageReceiverName);

        IntializFieleds();

        DisplayReceiverInfo();


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                SendMessage();
            }
        });

        FetchMessages();



    }

    private void FetchMessages()
    {
        RootRef.child("Messages").child(MessageSenderID).child(MessageReceiverID)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.exists())
                {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage()
    {

        updataUserStatus("online");

        String MessageText = UserInputMessage.getText().toString();

        if(TextUtils.isEmpty(MessageText))
        {
            Toast.makeText(this, "please type a message frist...", Toast.LENGTH_SHORT).show();
        }

        else
            {
                String message_Sender_ref = "Messages/" + MessageSenderID + "/" +MessageReceiverID ;
                String messgae_Receiver_ref = "Messages/" +MessageReceiverID + "/" + MessageSenderID ;

                DatabaseReference user_message_key = RootRef.child("Message").child(MessageSenderID)
                        .child(MessageReceiverID).push();

                String message_puch_id = user_message_key.getKey();

                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd,MMM,yyyy");
                saveCurrentDate = currentDate.format(calFordDate.getTime());

                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                getSaveCurrentTime = currentTime.format(calFordTime.getTime());

                postRandomName = saveCurrentDate + getSaveCurrentTime ;

                Map messageTextBody = new HashMap();
                messageTextBody.put("message", MessageText);
                messageTextBody.put("time" , getSaveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type" ,"text");
                messageTextBody.put("from" , MessageSenderID);

                Map messageBodyDetalis = new HashMap();
                messageBodyDetalis.put(message_Sender_ref + "/" + message_puch_id , messageTextBody);
                messageBodyDetalis.put(messgae_Receiver_ref + "/" + message_puch_id , messageTextBody);

                RootRef.updateChildren(messageBodyDetalis).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();

                            UserInputMessage.setText("");

                        }
                        else
                            {
                                String message = task.getException().getMessage();

                                Toast.makeText(ChatActivity.this, "Error Ouccred" + message, Toast.LENGTH_SHORT).show();
                                UserInputMessage.setText("");

                            }


                    }
                });


            }
    }



    public void updataUserStatus (String satus) {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentdate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currenttime.format(calForTime.getTime());

        Map CurrentSatusMap = new HashMap();
        CurrentSatusMap.put("time", saveCurrentTime);
        CurrentSatusMap.put("date", saveCurrentDate);
        CurrentSatusMap.put("type", satus);

        UserRef.child(MessageSenderID).child("updatesatus").updateChildren(CurrentSatusMap);

    }


    private void DisplayReceiverInfo()
    {

        if(ReciverName != null)
            ReciverName.setText(MessageReceiverName);
        else
            Log.i(TAG, "DisplayReceiverInfo: Not found");


        RootRef.child("Users").child(MessageReceiverID).addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot)
              {
                  if(dataSnapshot.exists())
                  {

                      final String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                     final  String type = dataSnapshot.child("updatesatus").child("type").getValue().toString();
                   final    String lastDate = dataSnapshot.child("updatesatus").child("date").getValue().toString();
                    final   String lastTime = dataSnapshot.child("updatesatus").child("time").getValue().toString();

                      Picasso.with(ChatActivity.this).load(profileimage).placeholder(R.drawable.profile).into(ReciverProfileImage);

                      if (type.equals("online"))
                      {
                          UserLastSeen.setText("online");

                      }
                      else
                          {
                              UserLastSeen.setText("last seen: " + lastTime + "  " + lastDate);
                          }
                  }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          });

    }

    private void IntializFieleds()
    {

//        mFrameLayout = findViewById(R.id.Chat_bar_layout);
//        ReciverName = mFrameLayout.findViewById(R.id.custom_profile_name);
//        ReciverProfileImage = mFrameLayout.findViewById(R.id.custom_profile_image);



        ChatToolbar = (Toolbar) findViewById(R.id.Chat_bar_layout);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionViewBar = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(actionViewBar);


        SendMessageButton = (ImageButton) findViewById(R.id.send_message);
        SendImageFileButton = (ImageButton) findViewById(R.id.send_image_button);
        UserInputMessage = (EditText) findViewById(R.id.input_message);


        ReciverName = (TextView) ChatToolbar.findViewById(R.id.custom_profile_name);
        ReciverProfileImage = (CircleImageView) ChatToolbar.findViewById(R.id.custom_profile_image);
        UserLastSeen = (TextView) ChatToolbar. findViewById(R.id.user_custom_lastseen);


        messagesAdapter = new MessagesAdapter(messagesList);

        MessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        MessageList.setHasFixedSize(true);
        MessageList.setLayoutManager(linearLayoutManager);
        MessageList.setAdapter(messagesAdapter);


    }
}
