package com.vpnclient;

import chrriis.dj.nativeswing.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
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

import static javax.swing.JOptionPane.showMessageDialog;


public class ClientGUI extends JFrame {
    private JPanel panel1;
    private  JComboBox VPNSelector;
    private  JLabel About;
    private  JLabel ConnectLabel;
    private JLabel LabelStatus;
    private  JLabel labelVisitSite;
    private  JLabel ShareLabel;
    private  JLabel HelpLabel;
    private  JLabel WebSiteMapLabel;
    private  JLabel CountUsLabel;
    private JPanel rightpanel;
    private Rasapi32 Win32Lib                   = Rasapi32.INSTANCE;
    private WinNT.HANDLE hrasConn;
    public static final String APPLICATION_NAME = "VPNClient";
    public static final String ICON_STR         = "/images/icon32x32.png";
    private TrayIcon[] Arrtray;
    private String HostName;
    private String imageUrl;

    public ClientGUI(VPNConnector vpnConn) {
        setContentPane(panel1);
        setVisible(true);

        setResizable(false);
        Dimension screenSize = new Dimension(1024, 768);

        setPreferredSize(screenSize);
        setMinimumSize(screenSize);
        setMaximumSize(screenSize);

        this.setIconImage(new ImageIcon(getClass().getResource("/images/icon32x32.png")).getImage());

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

       // repaint();

        VPNPreferences VPNPrefs     = new VPNPreferences();
        MiscSingleton MSingleton    = MiscSingleton.getInstance();

        try{
            String JsonVPN      = MSingleton.GetVPNConnData();
            JSONObject VPNObj   = new JSONObject(JsonVPN);

            if (VPNObj.getBoolean("success")){
                JSONArray SettingsArr = VPNObj.getJSONArray("result");
                if (SettingsArr.length()>0) {
                    for (int i = 0; i < SettingsArr.length(); i++) {
                        JSONObject VPNObject    = (JSONObject) SettingsArr.get(i);

                        VPNSelector.addItem(new VPNItem(Integer.valueOf(String.valueOf(VPNObject.get("countryId"))), String.valueOf(VPNObject.get("name"))));

                        VPNPrefs.deletePreference(String.valueOf(VPNObject.get("countryId")));
                        VPNPrefs.setPreference(String.valueOf(VPNObject.get("countryId")), SettingsArr.get(i).toString());
                    };
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

        try{
            String JsonImgVPN       = MSingleton.GetImageData();
            JSONObject ImgObj       = new JSONObject(JsonImgVPN);

            if (ImgObj.getBoolean("success")) {
                JSONArray ImgArr = ImgObj.getJSONArray("result");
                if (ImgArr.length() > 0) {
                    JSONObject ImageObject = (JSONObject) ImgArr.get(0);

                    String[] StrImg = String.valueOf(ImageObject.get("image")).split("/");
                    if (StrImg.length > 0) {
                        String StrImagePath = StrImg[StrImg.length - 1];
                        String TmpImagePth = MSingleton.downloadFromUrl((String) ImageObject.get("image"), StrImagePath);

                        ImageIcon icon = new ImageIcon(TmpImagePth);
                        Image img = icon.getImage();
                        Image newimg = img.getScaledInstance(About.getWidth(), About.getHeight(), java.awt.Image.SCALE_SMOOTH);
                        icon = new ImageIcon(newimg);

                        About.setIcon(icon);
                        About.setText(null);
                    };

                    //probably without https
                    if (String.valueOf(ImageObject.get("link")).contains("https://")) {
                        imageUrl = String.valueOf(ImageObject.get("link"));
                    } else {
                        imageUrl = "https://" + String.valueOf(ImageObject.get("link"));
                    };
                };
            };
        } catch (Exception exp){
            showMessageDialog(null, exp.getMessage());
        };

        ConnectLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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

                VPNStatus VPNConnStatus = new VPNStatus(LabelStatus, vpnConn, VPNSelected.getCountry(), VPNSelector);
                SystemTray tray         = SystemTray.getSystemTray();

                if (!vpnConn.connState) {
                    SetLabelIcon(ConnectLabel, "/images/connecting.png");
                    LabelStatus.setText(VPNSelected.getCountry() + ": 正在连接…");
                    try{

                        hrasConn                = vpnConn.Connect(EntryName);
                        VPNConnStatus.VPNConn   = hrasConn;
                        vpnConn.connState       = true;

                        VPNConnStatus.start();
                        LabelStatus.setText(VPNSelected.getCountry() + ": 连接中");
                       // showMessageDialog(null,"Connection was established!");
                        VPNSelector.setEnabled(false);
                    } catch (Rasapi32Util.Ras32Exception ex) {
                        vpnConn.connState = false;

                        showMessageDialog(null, ex.getMessage());
                        VPNSelector.setEnabled(true);
                        LabelStatus.setText("");

                        return;
                    };

                    if (vpnConn.connState){
                        SetLabelIcon(ConnectLabel, "/images/disconnect.png");
                        LabelStatus.setText(VPNSelected.getCountry() + ": 已连接");
                        VPNSelector.setEnabled(false);
                    }else{
                        SetLabelIcon(ConnectLabel, "/images/clicktoconnect.png");
                        LabelStatus.setText(VPNSelected.getCountry() + ": 已断开");
                        VPNSelector.setEnabled(true);
                    };
                }else{
                    LabelStatus.setText(VPNSelected.getCountry() + ": 正在断开…");
                    if (!VPNConnStatus.isInterrupted()){
                        vpnConn.connState = false;
                        VPNConnStatus.interrupt();
                    };

                    try{
                        vpnConn.Disconnect(hrasConn);
                        SetLabelIcon(ConnectLabel, "/images/clicktoconnect.png");
                        //showMessageDialog(null,"Connection was closed!");
                        LabelStatus.setText(VPNSelected.getCountry() + ": 已断开");
                        VPNSelector.setEnabled(true);
                    }catch (Rasapi32Util.Ras32Exception ex) {
                        if (VPNConnStatus.isInterrupted()){
                            vpnConn.connState = true;
                            VPNConnStatus.start();
                        };
                        showMessageDialog(null, ex.getMessage());
                        SetLabelIcon(ConnectLabel, "/images/disconnect.png");
                        LabelStatus.setText(VPNSelected.getCountry() + ": 已连接");
                        VPNSelector.setEnabled(false);
                    };
                };
            };
        });

        //about button action listeners
        About.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {


                About.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                try {
                    Desktop.getDesktop().browse(new URL(imageUrl).toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        //side buttons action listeners
        labelVisitSite.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.17vpn.xyz").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        ShareLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.17vpn.xyz/resource/index.php").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        HelpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.17vpn.xyz/blog/help.php").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        CountUsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.17vpn.xyz/blog/us.php").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

        WebSiteMapLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://www.17vpn.xyz/link/index.php").toURI());
                } catch (Exception esx) {
                    showMessageDialog(null, esx.getMessage());
                };
            };
        });

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

    private void SetLabelIcon(JLabel LabelName, String IcoPath){
        URL imageURL        = ClientGUI.class.getResource(IcoPath);
        ImageIcon icon      = createImageIcon(IcoPath);

        if(icon != null){
            Image img = icon.getImage();
            Image newimg = img.getScaledInstance(LabelName.getWidth(), LabelName.getHeight(),  java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(newimg);
            LabelName.setIcon(icon);
            LabelName.setText(null);
        }
        else{
            LabelName.setText("Image not found");
            LabelName.setIcon(null);
        }
    };

    protected static ImageIcon createImageIcon(String path) {
        URL imgURL;
        imgURL = ClientGUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    };

    public static void main(String[] args) throws Exception {
        NativeInterface.initialize();
        UIUtils.setPreferredLookAndFeel();
        NativeInterface.open();

//        LocalDate localDate = LocalDate.now();
//        LocalDate endDate   = LocalDate.of(2020, 12, 31);
//
//        if (localDate.compareTo(endDate)>0){
//           showMessageDialog(null, "Trial period has expired!");
//            System.exit(1);
//        };

        MiscSingleton MS = MiscSingleton.getInstance();

        MS.DeleteAllConnections();

        if (MS.isAppActive("com.vpnclient")) {
            showMessageDialog(null, "Application is already started!");
            System.exit(1);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    VPNConnector vpc    = new VPNConnector();
                    JFrame GUIClient    = new ClientGUI(vpc);

                    GUIClient.setTitle("VPNClient");
                    GUIClient.pack();
                    GUIClient.setLocationRelativeTo(null);
                };
            });
        };
        NativeInterface.runEventPump();
    };
}
