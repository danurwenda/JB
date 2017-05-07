/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.timer;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskUtils {

    private DiskUtils() {
    }

    public static String getFirstSerialNumber() {
        char driveLetter = 'C';
        String res = "";
        while (res.equals("") && driveLetter <= 'Z') {
            res = getSerialNumber(String.valueOf(driveLetter));
            driveLetter++;
        }
        return res;
    }

    /**
     * mengembalikan harddisk serial number
     *
     * @param drive
     * @return
     */
    public static String getSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            try (FileWriter fw = new java.io.FileWriter(file)) {
                String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                        + "Set colDrives = objFSO.Drives\n"
                        + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                        + "Wscript.Echo objDrive.SerialNumber";  // see note
                fw.write(vbs);
            } // see note
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return result.trim();
    }

    /**
     * mengembalikan MAC Address dari network interface pertama
     *
     * @return
     */
    public static String getMACAddress() {
        String s = "";
        try {

            NetworkInterface ni;
            Enumeration<NetworkInterface> lni = NetworkInterface.getNetworkInterfaces();
            for (; lni.hasMoreElements();) {
                ni = lni.nextElement();
//        System.out.println("ni: " + ni);
                byte[] b = ni.getHardwareAddress();
                if (!ni.getName().equals("lo") && b != null) {
                    s += bytesToHex(b);
                    break;
                }
            }

            //       MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            //       System.out.println("s: " + s);
            //       md.update( s.getBytes() );
            //       s = md.digest().toString();
        } catch (SocketException ex) {
            Logger.getLogger(DiskUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    /**
     * mengembalikan software ID
     *
     * @return
     */
    public static String getSoftwareID() {
        return DiskUtils.getSerialNumber("C") + DiskUtils.getMACAddress();
    }
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getSoftwareIDHash() {
        return sha256(getSoftwareID());
    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
