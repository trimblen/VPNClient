package com.vpnclient;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinRas;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;


public interface Rasapi32 extends StdCallLibrary {
    Rasapi32 INSTANCE = Native.loadLibrary("Rasapi32", Rasapi32.class, W32APIOptions.DEFAULT_OPTIONS);

    int RasEnumConnections(WinRas.RASCONN[] lprasconn, IntByReference lpcb, IntByReference lpcConnections);
    int RasGetConnectionStatistics(WinNT.HANDLE hrasconn, WinRas.RAS_STATS.ByReference lpStatistics);
    int RasGetConnectStatus(WinNT.HANDLE hrasconn, WinRas.RASCONNSTATUS lprasconnstatus);
    int RasGetCredentials(String lpszPhonebook, String lpszEntry, WinRas.RASCREDENTIALS.ByReference lpCredentials);
    int RasSetCredentials(String lpszPhonebook, String lpszEntry, WinRas.RASCREDENTIALS.ByReference lpCredentials, Boolean fClearCredentials);
    int RasDial(WinRas.RASDIALEXTENSIONS.ByReference lpRasDialExtensions, String lpszPhonebook, WinRas.RASDIALPARAMS.ByReference lpRasDialParams, Integer dwNotifierType, WinRas.RasDialFunc2 lpvNotifier, WinNT.HANDLEByReference lphRasConn);
    int RasHangUp(WinNT.HANDLE hrasconn);
    int RasGetErrorString(int uErrorValue, char[] lpszErrorString, int cBufSize);
    int RasGetEntryProperties(String lpszPhonebook, String lpszEntry, WinRas.RASENTRY.ByReference lpRasEntry, IntByReference lpdwEntryInfoSize, Pointer lpbDeviceInfo, Pointer lpdwDeviceInfoSize);
    int RasSetEntryProperties(String lpszPhonebook, String lpszEntry, WinRas.RASENTRY.ByReference lpRasEntry, Integer dwEntryInfoSize, byte[] lpbDeviceInfo, Integer dwDeviceInfoSize);
    int RasGetEntryDialParams(String lpszPhonebook, WinRas.RASDIALPARAMS.ByReference lprasdialparams, WinDef.BOOLByReference lpfPassword);
    int RasDeleteEntry(String lpszPhonebook,String lpszEntry);
}

