package com.libre.mixtli.env;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.libre.mixtli.R;
import com.libre.mixtli.pojos.Contact;

import java.util.ArrayList;

/**
 * Created by hugo on 15/08/18.
 */

    public class ContactAdapter extends ArrayAdapter<Contact> {

        public ContactAdapter(Context context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Contact contact = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.nombre);
            TextView tvHome = (TextView) convertView.findViewById(R.id.mail);
            // Populate the data into the template view using the data object
            tvName.setText(contact.username);
            tvHome.setText(contact.email);
            // Return the completed view to render on screen
            return convertView;
        }
    }
