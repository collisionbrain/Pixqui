package com.libre.mixtli.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.libre.mixtli.DetectorActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.prefs.Pref;
import com.spark.submitbutton.SubmitButton;

/**
 * Created by hgallardo on 07/03/18.
 */

public class RegisterActivity  extends Activity implements View.OnClickListener {
    private static final String TAG = "EmailPasswordActivity";
    private EditText edtMail, edtPassword,edtName,edtPasswordConfirm;
    private SubmitButton btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mTextViewProfile;
    private Context context;
    private Pref prefs;
    private Dialog dialogError;
    private LayoutInflater inflater;
    private   TextView messageError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        context=this;
        prefs=new Pref(this);
        edtMail =(EditText) findViewById(R.id.edtCorreo);
        edtName = (EditText) findViewById(R.id.edtNombre);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPasswordConfirm= (EditText) findViewById(R.id.edtPasswordConfirmation);
        btnRegister=(SubmitButton) findViewById(R.id.btnRegistrar);
        dialogError = new Dialog(context);
        dialogError.setContentView(R.layout.dialog_error);
        Button dialogButton = (Button)dialogError .findViewById(R.id.dialogButtonOK);
        messageError = (TextView)dialogError .findViewById(R.id.txtMensaje);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogError.dismiss();

            }
        });





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
                //updateUI(user);
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
                String email=edtMail.getText().toString();
                String password=edtPassword.getText().toString();
                try {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        prefs.saveData("REGISTER_USER_KEY", task.getResult().getUser().getUid());
                                        Intent registerIntent = new Intent(RegisterActivity.this, DetectorActivity.class);
                                        RegisterActivity.this.startActivity(registerIntent);
                                        RegisterActivity.this.finish();
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            messageError.setText(e.getMessage());
                                            dialogError.show();
                                            //error_weak_password
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            messageError.setText(e.getMessage());
                                            dialogError.show();
                                            //error_invalid_email
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            messageError.setText(e.getMessage().toString());
                                            dialogError.show();

                                            //error_user_exists
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }
                            });
                }catch (Exception ex){
                    Log.e(TAG, ex.getMessage());
                }

        }
    }


    private boolean validateForm() {
        if (TextUtils.isEmpty(edtMail.getText().toString())) {
            //mLayoutEmail.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(edtPassword.getText().toString())) {
            // mLayoutPassword.setError("Required.");
            return false;
        } else if (!edtPassword.getText().toString().equals(edtPasswordConfirm.getText().toString())) {
            //mLayoutName.setError("Required.");
            return false;
        } else if (TextUtils.isEmpty(edtName.getText().toString())) {
            //mLayoutName.setError("Required.");
            return false;
        } else {
            //mLayoutEmail.setError(null);
            //mLayoutPassword.setError(null);
            return true;
        }
    }




}