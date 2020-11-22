package com.vpnclient;

import com.sun.jna.platform.win32.Rasapi32Util;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinRas;
import com.sun.jna.ptr.IntByReference;
import org.json.JSONObject;

import java.io.IOException;

import static javax.swing.JOptionPane.showMessageDialog;

public class VPNConnector {
    public boolean connState        = false;
    MiscSingleton MSingl            = MiscSingleton.getInstance();
    public WinNT.HANDLE VPNConnExternalVb;

    public WinNT.HANDLE Connect(String dialEntr){
        WinNT.HANDLE hrasConn  = Rasapi32Util.dialEntry(dialEntr);
        this.VPNConnExternalVb = hrasConn;
        return hrasConn;
    };

    public void createConn(JSONObject vpnDataSettings, String EntryName) {
        String RestrExec        = "powershell.exe Set-ExecutionPolicy Unrestricted -Force";
        String cmdCert          = "powershell.exe certutil -f -addstore -enterprise root ";
        //String cmdVPN           = "powershell.exe Add-VpnConnection -Name \""+EntryName+"\" -ServerAddress \""+(String) vpnDataSettings.get("ip")+"\" -TunnelType \"ikev2\" -RememberCredential -Force -PassThru";
//        String VPNCred          = "powershell.exe Set-VpnConnectionUsernamePassword -connectionname \""+EntryName+"\" -username \"" + (String) vpnDataSettings.get("username") + "\" -password \""+ (String) vpnDataSettings.get("password") + "\"";
//        String VPNCredInstall   = "powershell.exe Install-Module -Name VPNCredentialsHelper -Verb runAs";
//        String VPNSetVPN        = "powershell.exe Set-VpnConnection -connectionname \""+EntryName+ "\" -ServerAddress "+(String) vpnDataSettings.get("ip");
        String CertFileName     = (String) vpnDataSettings.get("certificate_file");
        String UserName         = (String) vpnDataSettings.get("username");
        String Password         = (String) vpnDataSettings.get("password");
        String IP               = (String) vpnDataSettings.get("ip");

        if(CertFileName.isEmpty()){
            showMessageDialog(null, "No VPN certificate is available!");
            return;
        };

        if(UserName.isEmpty()){
            showMessageDialog(null, "No username specified!");
            return;
        };

        if(Password.isEmpty()){
            showMessageDialog(null, "No password specified!");
            return;
        };

        if(IP.isEmpty()){
            showMessageDialog(null, "No IP specified!");
            return;
        };

        try {
            MSingl.ExecutePowerShellCommand(RestrExec);
        } catch (IOException | InterruptedException ex){
            showMessageDialog(null, ex.getMessage());
            return;
        };

        try {
            String ResCerPath   = MSingl.downloadFromUrl(CertFileName, (String) vpnDataSettings.get("certificate"));
            cmdCert             = cmdCert + ResCerPath;
            MSingl.ExecutePowerShellCommand(cmdCert);
        }catch(IOException | InterruptedException ex){
            showMessageDialog(null, ex.getMessage());
            return;
        };

        try{
            Rasapi32Util.getPhoneBookEntry(EntryName);
          } catch(Rasapi32Util.Ras32Exception e){
           try {
               CreateEntryByName(EntryName, IP, UserName, Password);
            } catch (Rasapi32Util.Ras32Exception ex){
               showMessageDialog(null, e.getMessage());
               return;
          };
        };

//        try {
//            MSingl.ExecutePowerShellCommand(VPNSetVPN);
//        } catch (IOException | InterruptedException ex){
//            showMessageDialog(null, ex.getMessage());
//            return;
//        };
//
//        try {
//            MSingl.ExecutePowerShellCommand(VPNCredInstall);
//        } catch (IOException | InterruptedException ex){
//            showMessageDialog(null, ex.getMessage());
//            return;
//        };
//
//        try {
//            MSingl.ExecutePowerShellCommand(VPNCred);
//        } catch (IOException | InterruptedException ex){
//            showMessageDialog(null, ex.getMessage());
//            return;
//        };
    };

    private void CreateEntryByName(String EntryNameStr, String IPStr, String UserNameStr, String PasswordStr){
        WinRas.RASENTRY.ByReference rasEntryObject      = new WinRas.RASENTRY.ByReference();
        IntByReference lpdwEntryInfoSize                = new IntByReference(rasEntryObject.size());

        Rasapi32.INSTANCE.RasGetEntryProperties(null, "vv", rasEntryObject, lpdwEntryInfoSize, null, null);

        //getting a vpn settings
        rasEntryObject.dwVpnStrategy       = 7;
        rasEntryObject.dwType              = 2;
        rasEntryObject.dwEncryptionType    = 3;
        rasEntryObject.szDeviceType        = "vpn".toCharArray();
        rasEntryObject.szDeviceType        = "WAN Miniport (IKEv2)\u0000VPN2-0\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".toCharArray();
        rasEntryObject.dwfNetProtocols     = 12;
        rasEntryObject.dwFramingProtocol   = 1;
        rasEntryObject.dwfOptions          = 117571856;
        rasEntryObject.dwfOptions2         = 33562884;
        rasEntryObject.szLocalPhoneNumber  = IPStr.toCharArray();
        rasEntryObject.dwRedialCount       = 3;
        rasEntryObject.dwRedialPause       = 60;

        Rasapi32Util.setPhoneBookEntry(EntryNameStr, rasEntryObject);

        WinRas.RASCREDENTIALS.ByReference RasCred  =  new WinRas.RASCREDENTIALS.ByReference();

        int ResGet = Rasapi32.INSTANCE.RasGetCredentials(null, EntryNameStr, RasCred);

        if (ResGet!= WinError.ERROR_SUCCESS){
            showMessageDialog(null, Rasapi32Util.getRasErrorString(ResGet));
            return;
        };

        RasCred.dwMask = WinRas.RASCM_UserName | WinRas.RASCM_Password | WinRas.RASCM_Domain;

        RasCred.szUserName           = UserNameStr.toCharArray();
        RasCred.szPassword           = PasswordStr.toCharArray();
        RasCred.dwSize               = RasCred.size();

        int ResSet = Rasapi32.INSTANCE.RasSetCredentials(null, EntryNameStr, RasCred, false);

        if (ResSet!= WinError.ERROR_SUCCESS){
            showMessageDialog(null, Rasapi32Util.getRasErrorString(ResSet));
            return;
        };
    };

    public void Disconnect(WinNT.HANDLE hrasConn){
        try {
            Rasapi32Util.hangupRasConnection(hrasConn);
            this.connState = false;
        } catch (Rasapi32Util.Ras32Exception ex) {
            showMessageDialog(null, ex.getMessage());
            return;
        }
    };

}
