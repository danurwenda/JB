/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.timer;

import java.lang.reflect.InvocationTargetException;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    //put OpenIDE-Module-Install: com/jbatik/core/timer/Installer.class on manifest to activate
    
    private static final String GRC_KEY = "SOFTWARE\\Pxlppl\\GRC";

    @Override
    public void restored() {
        String seri = DiskUtils.getFirstSerialNumber();
        try {
            String value = WinRegistry.readString(
                    WinRegistry.HKEY_LOCAL_MACHINE, //HKEY
                    GRC_KEY, //Key
                    seri);                                              //ValueName

            long current = System.currentTimeMillis();
            if (value == null) {
                //belum terdaftar di registry
                //daftar saat pendaftaran
                String mill = String.valueOf(current);
                WinRegistry.createKey(WinRegistry.HKEY_LOCAL_MACHINE, GRC_KEY);
                WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE, GRC_KEY, seri, mill);
            } else {
                //value adalah mill nya saat running pertama kali
                //bandingkan dengan current
                long installedTime = Long.parseLong(value);

                if (installedTime > current) {
                    //berarti dimajuin tanggal waktu nginstall atau dimundurin tanggal waktu pakai
                    LifecycleManager.getDefault().exit();
                } else {
                    long dif = current - installedTime;
                    if (dif > 15552000000L) {
                        //berarti sudah lewat 6 bulan
                        LifecycleManager.getDefault().exit();
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
        }
    }

}
