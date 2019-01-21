package com.AhmedSheir.online;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegsiterActivity extends AppCompatActivity {
    private EditText UserEmail ,UserPassword, UserConfrimPassword ;
    private Button CreateAccount ;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter);

        mAuth = FirebaseAuth.getInstance() ;

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfrimPassword = (EditText) findViewById(R.id.registerr_confrim_password);
        CreateAccount = (Button)findViewById(R.id.register_cerate_account);

        loadingBar = new ProgressDialog(this);

        CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateNewAccount();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser CurrentUser = mAuth.getCurrentUser();

        if (CurrentUser != null)
        {
          SendUserrToMainActivity();

        }
    }
        private void SendUserrToMainActivity() {

            Intent intentmainactvity = new Intent(RegsiterActivity.this, MainActivity.class);
            intentmainactvity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intentmainactvity);
            finish();


        }



    private void CreateNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confrimpassowrd = UserConfrimPassword.getText().toString();

        if ( TextUtils.isEmpty(email))
        {

            Toast.makeText(this, "please write your email", Toast.LENGTH_SHORT).show();

        }

        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "please write your password", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(confrimpassowrd))
        {
            Toast.makeText(this, "please your confirm password", Toast.LENGTH_SHORT).show();
        }

        else if (!password.equals(confrimpassowrd))
        {
            Toast.makeText(this, "your password do not match write your confirm passowrd...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Creating New Account ");
            loadingBar.setMessage("please wait , while we are creating your  new account... ");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            mAuth.createUserWithEmailAndPassword(email , password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {

                                String DeviceToken = FirebaseInstanceId.getInstance().getToken();


                                SendEmailVerificationMessages();
                                loadingBar.dismiss();

                            }

                            else
                            {
                                String message = task.getException().getMessage();

                                Toast.makeText(RegsiterActivity.this, "Error Occured" +message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                        }
                    });

        }

    }

    private void SendEmailVerificationMessages ()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {

                    if (task.isSuccessful())
                    {


                        Toast.makeText(RegsiterActivity.this, "Register Successfully,we've sent you a mail.please check and verify your account", Toast.LENGTH_LONG).show();

                        SendUserToLoginAcivity();

                        mAuth.signOut();
                    }
                    else
                        {
                            String error = task.getException().getMessage();
                            Toast.makeText(RegsiterActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();

                            mAuth.signOut();
                        }

                }
            });
        }
    }

    private void SendUserToLoginAcivity() {

        Intent Loginintent = new Intent(RegsiterActivity.this , LoginActivity.class);
        Loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Loginintent);
        finish();


    }
}
