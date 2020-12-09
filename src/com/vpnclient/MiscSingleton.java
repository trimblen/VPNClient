package com.vpnclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Properties;

import static javax.swing.JOptionPane.showMessageDialog;

public class MiscSingleton {
    private static MiscSingleton INSTANCE;
    FileLock lock;
    FileChannel channel;

    private MiscSingleton() {
    }

    public static MiscSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new MiscSingleton();
        }

        return INSTANCE;
    };

    public boolean isAppActive(String appName) throws Exception{
        File file = new File(System.getProperty("user.home"), appName + ".tmp");
        channel = new RandomAccessFile(file, "rw").getChannel();

        lock = channel.tryLock();
        if (lock == null) {
            //lock.release();
            //channel.close();
            return true;
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    lock.release();
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return false;
    }

    public void DeleteAllConnections(){
        String HostName = "PCDefault";

        try {
            InetAddress Addr;
            Addr        = InetAddress.getLocalHost();
            HostName    = Addr.getHostName();
        } catch (UnknownHostException ex) {
            showMessageDialog(null, "Hostname can not be resolved. Setting default...");
        };

        try{
            String JsonVPN      = GetVPNConnData();
            JSONObject VPNObj   = new JSONObject(JsonVPN);

            if (VPNObj.getBoolean("success")){
                JSONArray SettingsArr = VPNObj.getJSONArray("result");
                if (SettingsArr.length()>0) {
                    for (int i = 0; i < SettingsArr.length(); i++) {
                        JSONObject VPNObject    = (JSONObject) SettingsArr.get(i);
                        String EntryName        = HostName + String.valueOf(VPNObject.get("name")) + String.valueOf(VPNObject.get("countryId"));

                        Rasapi32.INSTANCE.RasDeleteEntry(null, EntryName);
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
    };

    // getters and setters
    public String GetVPNConnData() throws IOException {
        // curl_init and url
        Properties ApiProps = GetApiProps();
        String StringURL    = "";

        if (!ApiProps.getProperty("app.apipath").isEmpty()) {
            StringURL = ApiProps.getProperty("app.apipath");
        };

        URL url = new URL(StringURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //  CURLOPT_POST
        con.setRequestMethod("POST");

        // CURLOPT_FOLLOWLOCATION
        con.setInstanceFollowRedirects(true);

        String postData = "my_data_for_posting";
        con.setRequestProperty("Content-length", String.valueOf(postData.length()));

        con.setDoOutput(true);
        con.setDoInput(true);

        DataOutputStream output = new DataOutputStream(con.getOutputStream());

        output.writeBytes(postData);

        output.close();

        // "Post data send ... waiting for reply");
        int code = con.getResponseCode(); // 200 = HTTP_OK
        System.out.println("Response    (Code):" + code);
        System.out.println("Response (Message):" + con.getResponseMessage());

        // read the response
        DataInputStream input = new DataInputStream(con.getInputStream());

        int c;

        StringBuilder resultBuf = new StringBuilder();

        while ( (c = input.read()) != -1) {
            resultBuf.append((char) c);
        };

        input.close();

        return resultBuf.toString();
    };

    // getters and setters
    public String GetImageData() throws IOException {
        // curl_init and url
        Properties ApiProps = GetApiProps();
        String StringURL    = "";

        if (!ApiProps.getProperty("app.imagepath").isEmpty()) {
            StringURL = ApiProps.getProperty("app.imagepath");
        };

        URL url = new URL(StringURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //  CURLOPT_POST
        con.setRequestMethod("POST");

        // CURLOPT_FOLLOWLOCATION
        con.setInstanceFollowRedirects(true);

        String postData = "my_data_for_posting";
        con.setRequestProperty("Content-length", String.valueOf(postData.length()));

        con.setDoOutput(true);
        con.setDoInput(true);

        DataOutputStream output = new DataOutputStream(con.getOutputStream());

        output.writeBytes(postData);

        output.close();

        // "Post data send ... waiting for reply");
        int code = con.getResponseCode(); // 200 = HTTP_OK
        System.out.println("Response    (Code):" + code);
        System.out.println("Response (Message):" + con.getResponseMessage());

        // read the response
        DataInputStream input = new DataInputStream(con.getInputStream());

        int c;

        StringBuilder resultBuf = new StringBuilder();

        while ( (c = input.read()) != -1) {
            resultBuf.append((char) c);
        };

        input.close();

        return resultBuf.toString();
    };

    private Properties GetApiProps(){
        Properties prop = new Properties();
        String fileName = "app.config";
        InputStream is = null;

        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            showMessageDialog(null, ex.getMessage());
        };

        try {
            prop.load(is);
        } catch (IOException ex) {
            showMessageDialog(null, ex.getMessage());
        };

        return prop;
    };

    public void ExecutePowerShellCommand(String CommandsToExecute) throws IOException, InterruptedException {
        Runtime proc = Runtime.getRuntime();

        Process processCmd = proc.exec(CommandsToExecute);

        int sucs = processCmd.waitFor();
    };

    public String downloadFromUrl(String urlDownload, String localFilename) throws IOException {
        OutputStream os     = null;
        InputStream is      = null;
        String fileUrl      = urlDownload;
        String tempDir      = System.getProperty("java.io.tmpdir");
        String outputPath   = tempDir + localFilename;

        try {
            // create a url object
            URL url = new URL(fileUrl);
            // connection to the file
            URLConnection connection = url.openConnection();
            // get input stream to the file
            is = connection.getInputStream();
            // get output stream to download file
            os = new FileOutputStream(outputPath);
            final byte[] b = new byte[2048];
            int length;
            // read from input stream and write to output stream
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close streams
            if (os != null)
                os.close();
            if (is != null)
                is.close();
        }
        return outputPath;
    }
}
