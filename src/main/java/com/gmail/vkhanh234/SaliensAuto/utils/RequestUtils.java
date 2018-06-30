package com.gmail.vkhanh234.SaliensAuto.utils;

import com.gmail.vkhanh234.SaliensAuto.Main;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;


public class RequestUtils {
    public static String post(String type, String dat) {
        return sendRequest(type,dat,true);
    }

    private static String getResponseMessage(int responseCode,Exception e) {
        switch (responseCode){
            case 401: return "Unauthorized. It could mean your token is incorrect.";
            case 403: return "Forbidden";
            case 500: return "Internal Server Error";
            case 503: return "Service Unavailable. Most likely this means server goes down.  Let's wait a little while.";
        }
        return e.getLocalizedMessage();
    }

    public static String sendRequest(String type,String dat, boolean post){
        if(post && Main.token==null){
            Main.debug("&cError:&r Token hasn't been set yet");
            return null;
        }
        int responseCode = -1;
        int eresult = -1;
        String errorMessage=null;
        BufferedReader in=null;
        HttpsURLConnection conn=null;
        try {
            trustAllHosts();
            URL url;
            if(post) url = new URL("https://community.steam-api.com/"+type+"/v0001/");
            else url = new URL("https://community.steam-api.com/ITerritoryControlMinigameService/"+type+"/v0001/?"+dat);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(post?"POST":"GET");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Origin", "https://steamcommunity.com");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Referer", "https://steamcommunity.com/saliengame/play");
//            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            conn.setRequestProperty("User-Agent","SaliensAuto (https://github.com/KickVN/SaliensAuto)");
            conn.setUseCaches(false);
            if(post) {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                if (dat.length() > 0) dat += "&access_token=" + Main.token;
                else dat = "access_token=" + Main.token;
                byte[] postData = dat.getBytes(StandardCharsets.UTF_8);
                wr.write(postData);
            }
            responseCode = conn.getResponseCode();

            eresult = getEResult(conn);
            errorMessage = getErrorMessage(conn);

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            Main.debug("&cError: &rCan't connect to Steam Server.");
            Main.debug("\tResponse code: &e"+responseCode+" - &e"+getResponseMessage(responseCode,e));
        }
        finally {
            if(conn!=null) conn.disconnect();
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if(eresult>0){
                if(eresult!=1){
                    Main.debug("\tEResult: &c"+eresult+"&r"+(errorMessage!=null?(" - Error message: &c"+convertErrorMessage(eresult,errorMessage)):""));
                }
            }
        }
        return null;
    }

    private static String convertErrorMessage(int eresult, String errorMessage) {
        if(eresult==17) return "Steam doesn't allow us to join this zone";
        return errorMessage;
    }

    private static int getEResult(HttpsURLConnection conn) {
        String s = conn.getHeaderField("x-eresult");
        if(s==null) s = conn.getHeaderField("X-eresult");
        if(s==null) return -1;
        return Integer.valueOf(s);
    }

    private static String getErrorMessage(HttpsURLConnection conn) {
        String s = conn.getHeaderField("x-error_message");
        if(s==null) s = conn.getHeaderField("X-error_message");
        return s;
    }

    public static String get(String type, String dat) {
        return sendRequest(type,dat,false);
    }

    public static String githubApi(String text) {
        try {
            trustAllHosts();
            URL url = new URL("https://api.github.com/repos/KickVN/SaliensAuto/"+text);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("User-Agent","SaliensAuto (https://github.com/KickVN/SaliensAuto)");
//            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            conn.setUseCaches(false);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
            if (conn.getResponseCode() != 200) return null;
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            conn.disconnect();
            in.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Copied from stackoverflow lol
    public static void trustAllHosts()
    {
        try
        {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509ExtendedTrustManager()
                    {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers()
                        {
                            return null;
                        }
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                        {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                        {
                        }
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) throws CertificateException
                        {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) throws CertificateException
                        {
                        }
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException
                        {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException
                        {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new  HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
        catch (Exception e)
        {
        }
    }
}
