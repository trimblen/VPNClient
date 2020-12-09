package com.vpnclient;

import com.sun.jna.platform.win32.Rasapi32Util;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinRas;

import javax.swing.*;

import static javax.swing.JOptionPane.showMessageDialog;


public class VPNStatus extends Thread {
    private Rasapi32 Win32Lib           = Rasapi32.INSTANCE;
    public WinNT.HANDLE VPNConn         = null;
    private JLabel ButtonConn;
    private JComboBox VPNSel;
    WinRas.RASCONNSTATUS  lprasconnst   = new WinRas.RASCONNSTATUS();
    private VPNConnector VPNConnObj;
    private String selCountry;

    public VPNStatus(JLabel buttCon, VPNConnector VPNConnObject, String SelectedCountry, JComboBox VSelector){
        this.ButtonConn         = buttCon;
        this.VPNConnObj         = VPNConnObject;
        this.selCountry         = SelectedCountry;
        this.VPNSel             = VSelector;
    };

    public void CheckVPNConnectionStatus(WinNT.HANDLE VPNConnection, VPNConnector VPNCnt){
        int RSConnStatus   = Win32Lib.RasGetConnectStatus(VPNConnection, lprasconnst);

        if (RSConnStatus != WinError.ERROR_SUCCESS){
            this.ButtonConn.setText(this.selCountry + ": 已连接");
            this.VPNSel.setEnabled(true);

            VPNCnt.connState = false;

            showMessageDialog(null, Rasapi32Util.getRasErrorString(RSConnStatus) + " Please, check your network status!");
        };
    };

    public void run() {
        while (!isInterrupted()) {
            try {
                if (this.VPNConnObj.connState) {
                    CheckVPNConnectionStatus(this.VPNConn, this.VPNConnObj);
                    // move my object, then sleep for 5 milliseconds
                    sleep(5);
                }else {
                    //showMessageDialog(null, "Connection was lost!");

                    this.interrupt();
                };
            } catch (InterruptedException e) {
                showMessageDialog(null, e.getMessage());
            };
        };
    };
}
