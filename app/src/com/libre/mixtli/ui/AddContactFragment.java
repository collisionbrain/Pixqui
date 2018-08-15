package com.libre.mixtli.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.libre.mixtli.ClassifierActivity;
import com.libre.mixtli.R;
import com.libre.mixtli.pojos.Contact;
import com.libre.mixtli.prefs.NetworkUtils;
import com.libre.mixtli.prefs.Pref;
import com.unstoppable.submitbuttonview.SubmitButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class AddContactFragment extends Fragment implements  View.OnClickListener  {
    private Context contexto;
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
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.register_contact_activity, null);
        contexto=this.getActivity().getBaseContext();
        prefs=new Pref(contexto);

        userGuid =prefs.loadData("REGISTER_USER_KEY");

        dialogError = new Dialog(context);
        dialogError.setContentView(R.layout.dialog_error);
        dialogButton = (SubmitButton)dialogError.findViewById(R.id.dialogButtonOK);
        btnRegister = (SubmitButton)root.findViewById(R.id.btnAgregar);
        messageError = (TextView)dialogError .findViewById(R.id.txtMensaje);
        dialogButton.setOnClickListener(dialogListener);
        netStatus= NetworkUtils.getConnectivityStatus(context);
        btnRegister.setOnClickListener(this);
        btnRegister.setOnResultEndListener(finishListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userGuid).addValueEventListener(addContectListener);

        return root;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);



    }

    @Override
    public void onPause(){

        super.onPause();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();

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
