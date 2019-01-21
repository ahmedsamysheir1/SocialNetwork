package com.AhmedSheir.online;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.awt.font.TextAttribute;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MeesageViewHolder>
{
    private List<Messages> userMessagsList  ;

    private FirebaseAuth mAuth;

    private DatabaseReference userDatabaseRef;

    public MessagesAdapter(List<Messages> userMessagsList) {
        this.userMessagsList = userMessagsList;
    }

    public class MeesageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView SenderMeesageText , RecevierMessageText ;
        public CircleImageView ReceiverProfileImage ;

        public MeesageViewHolder(View itemView) {
            super(itemView);

            SenderMeesageText = (TextView) itemView.findViewById(R.id.sender_message_id);
            RecevierMessageText = (TextView) itemView.findViewById(R.id.receiver_message_id);
            ReceiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }

    }

    @NonNull
    @Override
    public MeesageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_of_users , parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MeesageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull final MeesageViewHolder holder, int position)
    {

        String SenderMessageID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagsList.get(position);
        String FromID = messages.getFrom();
        String FromMessageType = messages.getType();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FromID);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.with(holder.ReceiverProfileImage.getContext()).load(image).placeholder(R.drawable.profile).into(holder.ReceiverProfileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (FromMessageType.equals("text"))
        {
            holder.RecevierMessageText.setVisibility(View.INVISIBLE);
            holder.ReceiverProfileImage.setVisibility(View.INVISIBLE);


            if(FromID.equals(SenderMessageID))
            {
                holder.SenderMeesageText.setBackgroundResource(R.drawable.message_sender_text_background);
                holder.SenderMeesageText.setTextColor(Color.WHITE);
                holder.SenderMeesageText.setGravity(Gravity.LEFT);
                holder.SenderMeesageText.setText(messages.getMessage());

            }else
                {
                    holder.SenderMeesageText.setVisibility(View.INVISIBLE);
                    holder.RecevierMessageText.setVisibility(View.VISIBLE);
                    holder.ReceiverProfileImage.setVisibility(View.VISIBLE);


                    holder.RecevierMessageText.setBackgroundResource(R.drawable.reciver_meesage_text_background);
                    holder.RecevierMessageText.setTextColor(Color.WHITE);
                    holder.RecevierMessageText.setGravity(Gravity.LEFT);
                    holder.RecevierMessageText.setText(messages.getMessage());

                }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagsList.size();
    }
}
