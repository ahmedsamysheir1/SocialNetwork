package com.AhmedSheir.online;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RestPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_password);

        mAuth = FirebaseAuth.getInstance();


        ResetPasswordSendEmailButton = (Button) findViewById(R.id.reset_password_button);
        ResetEmailInput = (EditText) findViewById(R.id.reset_password_email);
        mToolbar = (Toolbar) findViewById(R.id.forget_password_link_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String userEmail = ResetEmailInput.getText().toString();

                if (TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(RestPasswordActivity.this, "please write valid email first...", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(RestPasswordActivity.this, "please check your email account if you want rest your password", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(RestPasswordActivity.this , LoginActivity.class));
                                }

                                else
                                    {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(RestPasswordActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
                                    }


                            }
                        });
                    }

            }
        });


    }
}
