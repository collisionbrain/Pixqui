package com.libre.mixtli.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.libre.mixtli.ClassifierActivity;
import com.libre.mixtli.DetectorActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.pojos.Contact;
import com.libre.mixtli.prefs.NetworkUtils;
import com.libre.mixtli.prefs.Pref;
import com.libre.mixtli.prefs.Utils;
import com.unstoppable.submitbuttonview.SubmitButton;

import static com.libre.mixtli.env.Constants.FACE_XML;
import static com.libre.mixtli.env.Constants.URL;

/**
 * Created by hgallardo on 07/03/18.
 */

public class RegisterContactActivity extends Activity implements  View.OnClickListener  {
    private SubmitButton btnRegister;
    private Context context;
    private Pref prefs;
    private Dialog dialogError;
    private TextView messageError;
    private int netStatus;
    private SubmitButton dialogButton;
    private boolean registerSuccess;
    private ListView contactList;
    private String userGuid;
    private DatabaseReference mDatabase;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_contact_activity);
        context=this;
        prefs=new Pref(this);
        Bundle bundle = getIntent().getExtras();
        userGuid = bundle.getString("REGISTER_USER_KEY");
        dialogError = new Dialog(context);
        dialogError.setContentView(R.layout.dialog_error);
        dialogButton = (SubmitButton)dialogError .findViewById(R.id.dialogButtonOK);
        messageError = (TextView)dialogError .findViewById(R.id.txtMensaje);
        dialogButton.setOnClickListener(dialogListener);
        netStatus= NetworkUtils.getConnectivityStatus(context);
        btnRegister.setOnClickListener(this);
        btnRegister.setOnResultEndListener(finishListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userGuid).addValueEventListener(addContectListener);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.btnRegistrar:

                if (netStatus != 0) {
                    if(validateList()) {
                        registerSuccess=true;
                }
            }else
                {
                    messageError.setText("Verifica tu conexion");
                    dialogError.show();
                    btnRegister.doResult(false);
                    registerSuccess=false;

                }

        }
    }


    private boolean validateList() {
        if (contactList.getCount()<1) {

            setErrorMessage("Agrega un contacto ");
            return false;
        } else {

            return true;
        }
    }


    View.OnClickListener dialogListener=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        dialogError.dismiss();
        btnRegister.reset();
        dialogButton.reset();
        }
    };


    public void setErrorMessage(String message){
        messageError.setText(message);
        dialogError.show();
    }
    SubmitButton.OnResultEndListener finishListener=new SubmitButton.OnResultEndListener() {
        @Override
        public void onResultEnd() {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
            View mView = layoutInflaterAndroid.inflate(R.layout.dialog_add_contact, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
            alertDialogBuilderUserInput.setView(mView);

            final EditText edtNombre = (EditText) mView.findViewById(R.id.edtNombre);
            final EditText edtCorreo = (EditText) mView.findViewById(R.id.edtCorreo);
            final EditText edtPhone = (EditText) mView.findViewById(R.id.edtPhone);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                           String name= edtNombre.getText().toString();
                            String mail= edtCorreo.getText().toString();
                            String phone= edtPhone.getText().toString();
                            Contact contactUser = new Contact(name, mail, phone);
                            mDatabase.child("users").child(userGuid).setValue(contactUser,new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        setErrorMessage("Error creando contacto");

                                    } else {

                                        Intent registerIntent = new Intent(RegisterContactActivity.this, ClassifierActivity.class);
                                        RegisterContactActivity.this.startActivity(registerIntent);
                                        RegisterContactActivity.this.finish();

                                    }
                                }
                            });
                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });

            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();

           /* if(registerSuccess) {

                prefs.saveData("REGISTER_USER_KEY", userGuid);




            }else{
                setErrorMessage("Error descargando archivos extras");

            }*/
        }
    };

    ValueEventListener addContectListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Contact contact = dataSnapshot.getValue(Contact.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}