package com.vpnclient;

import com.sun.jna.platform.win32.Rasapi32Util;
import com.sun.jna.platform.win32.WinNT;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDate;

import static javax.swing.JOptionPane.showMessageDialog;


public class ClientGUI extends JFrame {
    private JButton buttConn;
    private JPanel panel1;
    private JComboBox VPNSelector;
    private JLabel About;
    private Rasapi32 Win32Lib = Rasapi32.INSTANCE;
    private WinNT.HANDLE hrasConn;
    public static final String APPLICATION_NAME = "VPNClient";
    public static final String ICON_STR = "/images/icon32x32.png";
    private TrayIcon[] Arrtray;
    String HostName;

    public ClientGUI(VPNConnector vpnConn) {
        setContentPane(panel1);
        setVisible(true);

        setResizable(false);
        Dimension screenSize = new Dimension(800, 600);

        setPreferredSize(screenSize);
        setMinimumSize(screenSize);
        setMaximumSize(screenSize);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        VPNPreferences VPNPrefs     = new VPNPreferences();
        MiscSingleton MSingleton    = MiscSingleton.getInstance();

        try{
            String JsonVPN      = MSingleton.GetVPNConnData();
            JSONObject VPNObj   = new JSONObject(JsonVPN);

            if (VPNObj.getBoolean("success")){
                JSONArray SettingsArr = VPNObj.getJSONArray("result");
                if (SettingsArr.length()>0) {
                    for (int i = 0; i < SettingsArr.length(); i++) {
                        JSONObject VPNObject = (JSONObject) SettingsArr.get(i);
                        VPNSelector.addItem(new VPNItem(Integer.valueOf(String.valueOf(VPNObject.get("countryId"))), String.valueOf(VPNObject.get("name"))));

                        VPNPrefs.deletePreference(String.valueOf(VPNObject.get("countryId")));
                        VPNPrefs.setPreference(String.valueOf(VPNObject.get("countryId")), SettingsArr.get(i).toString());
                    }
                }else{
                    showMessageDialog(null, "No settings data is available!");
                    return;
                }
            } else {
                showMessageDialog(null, "No settings data is available!");
                return;
            };
        } catch (Exception e){
             showMessageDialog(null, e.getMessage());
        };

        buttConn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                VPNItem VPNSelected = (VPNItem) VPNSelector.getSelectedItem();
                String ConnString   = VPNPrefs.getPreference(String.valueOf(VPNSelected.getCountryId()));
                JSONObject VPNData  = new JSONObject(ConnString);

                HostName = "PCDefault";

                try {
                    InetAddress Addr;
                    Addr        = InetAddress.getLocalHost();
                    HostName    = Addr.getHostName();
                } catch (UnknownHostException ex) {
                    showMessageDialog(null, "Hostname can not be resolved. Setting default...");
                };

                String EntryName = HostName + VPNSelected.getCountry() + String.valueOf(VPNSelected.getCountryId());

                vpnConn.createConn(VPNData, EntryName);

                VPNStatus VPNConnStatus = new VPNStatus(buttConn, vpnConn);
                SystemTray tray         = SystemTray.getSystemTray();

               if (!vpnConn.connState) {
                    try{
                        hrasConn                = vpnConn.Connect(EntryName);
                        VPNConnStatus.VPNConn   = hrasConn;
                        vpnConn.connState       = true;

                        VPNConnStatus.start();

                        showMessageDialog(null,"Connection was established!");
                    } catch (Rasapi32Util.Ras32Exception ex) {
                        vpnConn.connState = false;
                        showMessageDialog(null, ex.getMessage());
                        return;
                    };

                    if (vpnConn.connState){
                        buttConn.setText("Disconnect");
                    };
                }else{
                   vpnConn.Disconnect(hrasConn);

                   if (!VPNConnStatus.isInterrupted()){
                       VPNConnStatus.interrupt();
                   };

                   buttConn.setText("Connect");

                   showMessageDialog(null,"Connection was closed!");
                };
            };
        });

        About.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.youtube.com/watch?v=Vr6wPsVFa1E").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        About.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setTrayIcon(this,vpnConn);
    };

    private static void setTrayIcon(JFrame WindowFrame, VPNConnector VPNModule) {
        if(! SystemTray.isSupported() ) {
            return;
        };

        PopupMenu trayMenu  = new PopupMenu();
        MenuItem itemOpen   = new MenuItem("Open");

        itemOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WindowFrame.setVisible(true);
            }
        });

        trayMenu.add(itemOpen);

        MenuItem item = new MenuItem("Exit");

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (VPNModule.connState){
                    VPNModule.Disconnect(VPNModule.VPNConnExternalVb);
                }
                System.exit(0);
            }
        });

        trayMenu.add(item);

        URL imageURL        = ClientGUI.class.getResource(ICON_STR);
        Image icon          = Toolkit.getDefaultToolkit().getImage(imageURL);
        TrayIcon trayIcon   = new TrayIcon(icon, APPLICATION_NAME, trayMenu);

        trayIcon.setImageAutoSize(true);

        SystemTray tray = SystemTray.getSystemTray();

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        };

        trayIcon.displayMessage(APPLICATION_NAME, "Client is started!",
                TrayIcon.MessageType.INFO);
    };

    public static void main(String[] args) throws Exception {
        LocalDate localDate = LocalDate.now();
        LocalDate endDate   = LocalDate.of(2020, 12, 31);

        if (localDate.equals(endDate)){
            showMessageDialog(null, "Trial period has expired!");
            System.exit(1);
        };

        MiscSingleton MS = MiscSingleton.getInstance();

        MS.DeleteAllConnections();

        if (MS.isAppActive("com.vpnclient")) {
            showMessageDialog(null, "Application is already started!");
            System.exit(1);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    VPNConnector vpc = new VPNConnector();
                    new ClientGUI(vpc);
                }
            });
        }
    };
}
