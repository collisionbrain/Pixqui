package com.libre.mixtli.pojos;

public class Contact {
    public String username;
    public String email;
    public String phone;

    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Contact(String username, String email,String phone) {
        this.username = username;
        this.email = email;
        this.phone=phone;
    }
}
