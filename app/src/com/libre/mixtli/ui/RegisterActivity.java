package com.libre.mixtli.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.libre.mixtli.R;

/**
 * Created by hgallardo on 07/03/18.
 */

public class RegisterActivity  extends Activity implements View.OnClickListener {
    private static final String TAG = "EmailPasswordActivity";
    private EditText edtMail, edtPassword,edtName,edtLastName;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mTextViewProfile;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        context=this;
        edtMail =(EditText) findViewById(R.id.edtCorreo);
        edtName = (EditText) findViewById(R.id.edtNombre);
        edtLastName = (EditText) findViewById(R.id.edtApellidos);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnRegister=(Button) findViewById(R.id.btnRegistrar);
        btnRegister.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnRegistrar:

                final FirebaseUser firebaseUser = mAuth.getCurrentUser();
                firebaseUser.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(context, "Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_LONG
                            ).show();

                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                break;
        }
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }
       // showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    mTextViewProfile.setTextColor(Color.RED);
                    mTextViewProfile.setText(task.getException().getMessage());
                } else {
                    mTextViewProfile.setTextColor(Color.DKGRAY);
                }
               // hideProgressDialog();
            }
        });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }
        //showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    mTextViewProfile.setTextColor(Color.RED);
                    mTextViewProfile.setText(task.getException().getMessage());
                } else {

                    task.getResult().getUser();
                    mTextViewProfile.setTextColor(Color.DKGRAY);
                }
               // hideProgressDialog();
            }
        });
    }

    private void signOut() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Sing out");
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                updateUI(null);
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(edtMail.getText().toString())) {
            //mLayoutEmail.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(edtPassword.getText().toString())) {
            // mLayoutPassword.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(edtName.getText().toString())) {
            //mLayoutName.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(edtLastName.getText().toString())) {
            // mLayoutLastName.setError("Required.");
            return false;
        } else {
            //mLayoutEmail.setError(null);
            //mLayoutPassword.setError(null);
            return true;
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            mTextViewProfile.setText("DisplayName: " + user.getDisplayName());
            mTextViewProfile.append("\n\n");
            mTextViewProfile.append("Email: " + user.getEmail());
            mTextViewProfile.append("\n\n");
            mTextViewProfile.append("Firebase ID: " + user.getUid());
            mTextViewProfile.append("\n\n");
            mTextViewProfile.append("Email Verification: " + user.isEmailVerified());

            if (user.isEmailVerified()) {
                findViewById(R.id.btnRegistrar).setVisibility(View.GONE);
            } else {
                findViewById(R.id.btnRegistrar).setVisibility(View.VISIBLE);
            }


        } else {
            mTextViewProfile.setText(null);

        }
        //hideProgressDialog();
    }


}