package com.AhmedSheir.online;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink , ForgetPasswordLink ;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private ImageView googleSignInButton;

    private static int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";
    private Boolean CheckEmail ;

    private DatabaseReference UsersRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        NeedNewAccountLink = (TextView) findViewById(R.id.register_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        googleSignInButton = (ImageView) findViewById(R.id.google_singin_button);
        LoginButton = (Button) findViewById(R.id.login_account);

        loadingBar = new ProgressDialog(this);

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToRegisterActivity();
            }
        });


        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(LoginActivity.this ,RestPasswordActivity.class));
            }
        });


        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AllowingUserToLogin();
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this, "connotation failed to singing to google", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API ,gso)
                .build();


        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                signIn();
            }
        });


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            loadingBar.setTitle("Google Sign IN");
            loadingBar.setMessage("Please wait , while we are allowing you to login using your Google account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();

                firebaseAuthWithGoogle(account);

                Toast.makeText(this, "please wait, while we are getting your auth result...", Toast.LENGTH_SHORT).show();
            }

            else
                {
                    Toast.makeText(this, "can't get Auth result ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

        }
    }

        private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Log.d(TAG, "signInWithCredential:success");
                                SendUserrToMainActivity();
                                loadingBar.dismiss();

                            } else {

                                  Log.w(TAG, "signInWithCredential:failure", task.getException());
                                  String message = task.getException().getMessage();

                                Toast.makeText(LoginActivity.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

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


    private void AllowingUserToLogin() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "please write your email", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "please write your password", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait , while we are allowing you to login into your  Account...");
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email , password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {

                                String Online_user_id = mAuth.getCurrentUser().getUid();
                                String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                                UsersRef.child(Online_user_id).child("device_token").setValue(DeviceToken)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                VerifyEmailAddres();
                                                loadingBar .dismiss();
                                            }
                                        });




                            }

                            else
                            {
                                String message = task.getException().getMessage();
                                SendUserToLoginActivity();
                                Toast.makeText(LoginActivity.this, "Error Occured" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    private void VerifyEmailAddres ()
    {
         FirebaseUser User = mAuth.getCurrentUser();

         CheckEmail = User.isEmailVerified();

        if(CheckEmail)
        {

            SendUserrToMainActivity();

        }
        else
            {

                Toast.makeText(this, "please verify your account first", Toast.LENGTH_SHORT).show();
                mAuth.signOut();

            }
    }

    private void SendUserToLoginActivity()
    {
        Intent intentLogin = new Intent(LoginActivity.this , LoginActivity.class);
        intentLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentLogin);
    }

    private void SendUserrToMainActivity() {

        Intent intentmainactvity = new Intent(LoginActivity.this , MainActivity.class);
        intentmainactvity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentmainactvity);
        finish();
    }

    private void SendUserToRegisterActivity() {

        Intent RegisterIntent = new Intent(LoginActivity.this ,RegsiterActivity.class);
        startActivity(RegisterIntent);

    }


}
