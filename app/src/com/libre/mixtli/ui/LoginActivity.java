package com.libre.mixtli.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.libre.mixtli.ClassifierActivity;
import com.libre.mixtli.DetectorActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.prefs.NetworkUtils;
import com.libre.mixtli.prefs.Pref;
import com.libre.mixtli.prefs.Utils;
import com.unstoppable.submitbuttonview.SubmitButton;

import static com.libre.mixtli.env.Constants.FACE_XML;
import static com.libre.mixtli.env.Constants.URL;

/**
 * Created by hugo on 26/05/18.
 */

public class LoginActivity  extends Activity implements  View.OnClickListener  {
    private static final String TAG = "EmailPasswordActivity";
    private EditText edtMail, edtPassword;

    private SubmitButton btnEntrar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context context;
    private Pref prefs;
    private Dialog dialogError;
    private  SubmitButton dialogButton;
    private boolean loginSuccess;
    private int netStatus;
    private   TextView messageError;
    private String userGuid;
    private FirebaseStorage storage;
    final long ONE_MEGABYTE = 1024 * 1024;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        context=this;
        prefs=new Pref(this);
        edtMail =(EditText) findViewById(R.id.edtCorreo);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        btnEntrar=(SubmitButton) findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(this);
        btnEntrar.setOnResultEndListener(finishListener);
        dialogError = new Dialog(context);
        messageError = (TextView)dialogError .findViewById(R.id.txtMensaje);
        netStatus= NetworkUtils.getConnectivityStatus(context);
        mAuth = FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();

    }

    @Override
    public void onClick(View v) {

        Log.e("xxxxxxxxxx","xxxxxxxxx");

        switch (v.getId()) {

            case R.id.btnEntrar:

                if (netStatus != 0) {
                    if(validateForm()) {

                            try {
                                String email = edtMail.getText().toString();
                                String password = edtPassword.getText().toString();

                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    userGuid=task.getResult().getUser().getUid();
                                                    userGuid=user.getUid();
                                                    StorageReference fileRef = storage.getReferenceFromUrl(URL).child(FACE_XML);
                                                    if (fileRef != null) {
                                                        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                            @Override
                                                            public void onSuccess(byte[] bytes) {

                                                                Utils.saveXml(bytes);
                                                                btnEntrar.doResult(true);
                                                                loginSuccess=true;


                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                btnEntrar.doResult(false);
                                                                loginSuccess=false;
                                                            }
                                                        });
                                                    }else{
                                                        btnEntrar.doResult(false);

                                                    }
                                                } else {
                                                    btnEntrar.doResult(false);
                                                    loginSuccess=false;
                                                }

                                                // ...
                                            }
                                        });
                            } catch (Exception ex) {
                                Log.e(TAG, ex.getMessage());
                            }

                    }
                }else
                {
                    messageError.setText("Verifica tu conexion");
                    dialogError.show();
                    btnEntrar.doResult(false);
                    loginSuccess=false;

                }

        }
    }
    private boolean validateForm() {
        if (TextUtils.isEmpty(edtMail.getText().toString())) {
            edtMail.requestFocus();
            btnEntrar.doResult(false);
            loginSuccess=false;
            setErrorMessage("Correo no puede ir Vacio");
            return false;
        } else if (TextUtils.isEmpty(edtPassword.getText().toString())) {
            edtPassword.requestFocus();
            btnEntrar.doResult(false);
            loginSuccess=false;
            setErrorMessage("Contraseña no puede ir Vacio");
            return false;
        }  else {

            return true;
        }
    }
    public void setErrorMessage(String message){
        messageError.setText(message);
        dialogError.show();
    }
    SubmitButton.OnResultEndListener finishListener=new SubmitButton.OnResultEndListener() {
        @Override
        public void onResultEnd() {
            if(loginSuccess) {
                prefs.saveData("REGISTER_USER_KEY", userGuid);
                Intent registerIntent = new Intent(LoginActivity.this, ClassifierActivity.class);
                LoginActivity.this.startActivity(registerIntent);
                LoginActivity.this.finish();
                } else {
                    btnEntrar.doResult(false);
                    loginSuccess=false;
                }



        }
    };


}
