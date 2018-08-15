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
import com.libre.mixtli.env.ContactAdapter;
import com.libre.mixtli.pojos.Contact;
import com.libre.mixtli.prefs.NetworkUtils;
import com.libre.mixtli.prefs.Pref;
import com.unstoppable.submitbuttonview.SubmitButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class AddContactFragment extends Fragment implements  View.OnClickListener  {
    private SubmitButton btnAgregar;
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
    private EditText edtNombre;
    private EditText edtCorreo ;
    private EditText edtPhone;
    private  ContactAdapter  contactAdapter;
    ArrayList<Contact> arrayContact = new ArrayList<Contact>();
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.register_contact_activity, null);
        context=this.getActivity().getBaseContext();
        prefs=new Pref(context);
        userGuid =prefs.loadData("REGISTER_USER_KEY");
        dialogError = new Dialog(context);
        dialogError.setContentView(R.layout.dialog_error);
        dialogButton = (SubmitButton)dialogError.findViewById(R.id.dialogButtonOK);
        btnAgregar = (SubmitButton)root.findViewById(R.id.btnAgregar);
        messageError = (TextView)dialogError .findViewById(R.id.txtMensaje);
        dialogButton.setOnClickListener(dialogListener);
        netStatus= NetworkUtils.getConnectivityStatus(context);
        btnAgregar.setOnClickListener(this);
        btnAgregar.setOnResultEndListener(finishListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(userGuid).addValueEventListener(addContectListener);
        contactList=(ListView) root.findViewById(R.id.contactList);
        edtNombre = (EditText) root.findViewById(R.id.edtNombre);
        edtCorreo = (EditText) root.findViewById(R.id.edtCorreo);
        edtPhone = (EditText) root.findViewById(R.id.edtPhone);
        contactAdapter = new ContactAdapter(context, arrayContact);
        contactList.setAdapter(contactAdapter);
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

            case R.id.btnAgregar:

                if (netStatus != 0) {
                    registerSuccess=true;
                    btnAgregar.doResult(true);
                }else
                {
                    messageError.setText("Verifica tu conexion");
                    dialogError.show();
                    btnAgregar.doResult(false);
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
            btnAgregar.reset();
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
            String name= edtNombre.getText().toString();
            String mail= edtCorreo.getText().toString();
            String phone= edtPhone.getText().toString();
            Contact contactUser = new Contact(name, mail, phone);
            contactAdapter.add(contactUser);
            mDatabase.child("users").child(userGuid).setValue(contactUser,new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        setErrorMessage("Error creando contacto");
                        btnAgregar.reset();
                    } else {
                        btnAgregar.reset();

                    }
                }
            });
        }
    };

    ValueEventListener addContectListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Contact contact = dataSnapshot.getValue(Contact.class);
            contactAdapter.add(contact);
            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
            String instanceActivity=getActivity().getClass().getName();
            if (instanceActivity.equals("com.libre.mixtli.ClassifierActivity")) {
                ((ClassifierActivity) getActivity()).onBackPressed();
            }else{
                ((RegisterActivity) getActivity()).finish();
                startActivity(intent);
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}
