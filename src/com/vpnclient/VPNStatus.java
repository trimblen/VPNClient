package com.vpnclient;

import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinRas;

import javax.swing.*;

import static javax.swing.JOptionPane.showMessageDialog;


public class VPNStatus extends Thread {
    private Rasapi32 Win32Lib           = Rasapi32.INSTANCE;
    public WinNT.HANDLE VPNConn         = null;
    private JButton ButtonConn;
    WinRas.RASCONNSTATUS  lprasconnst   = new WinRas.RASCONNSTATUS();
    private VPNConnector VPNConnObj;

    public VPNStatus(JButton buttCon, VPNConnector VPNConnObject){
        this.ButtonConn = buttCon;
        this.VPNConnObj = VPNConnObject;
    };

    public void CheckVPNConnectionStatus(WinNT.HANDLE VPNConnection, VPNConnector VPNCnt){
        int RSConnStatus   = Win32Lib.RasGetConnectStatus(VPNConnection, lprasconnst);

        if (RSConnStatus !=0){
            this.ButtonConn.setText("Connect");
            VPNCnt.connState = false;
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
                    showMessageDialog(null, "Connection was lost!");

                    this.interrupt();
                };
            } catch (InterruptedException e) {
                showMessageDialog(null, e.getMessage());
            };
        };
    };
}
