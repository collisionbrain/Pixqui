package com.libre.mixtli.prefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by hugo on 8/04/18.
 */

public class Utils {
    public static String objectToString (Serializable obj ){

        if (obj == null)
            return "";

        try {
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream;
            objStream = new ObjectOutputStream(serialObj);
            objStream.writeObject(obj);
            objStream.close();
            return asHexStr(serialObj.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    public static Serializable stringToObject (String str){

        if (str == null || str.length() == 0)
            return null;
        try {
            ByteArrayInputStream serialObj = new ByteArrayInputStream(asBytes(str));
            ObjectInputStream objStream;
            objStream = new ObjectInputStream(serialObj);
            return (Serializable) objStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }
    public static String asHexStr (byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            if (( buf[i] & 0xff) < 0x10)
                strbuf.append("0");
            strbuf.append(Long.toString( buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }



    public static byte[] asBytes (String s) {
        String s2;
        byte[] b = new byte[s.length() / 2];
        int i;
        for (i = 0; i < s.length() / 2; i++) {
            s2 = s.substring(i * 2, i * 2 + 2);
            b[i] = (byte)(Integer.parseInt(s2, 16) & 0xff);
        }
        return b;
    }

}