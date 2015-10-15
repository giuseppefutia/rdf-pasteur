package it.polito.nexa.pasteur.importers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DefaultJSONImporter implements JSONImporter {

    public String getJSON(String source, String typeOfSource){

        String data = "";

        if(typeOfSource == "URL") {
            try {
                URL u = new URL(source);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.connect();
                int status = c.getResponseCode();

                if (status == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    data = sb.toString();
                }
                else System.out.println("Connection error");
            } catch (MalformedURLException ex) {
                System.out.println(ex);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        } else if(typeOfSource == "FILE") {
            try {
                BufferedReader br = new BufferedReader(new FileReader(source));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                data = sb.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }
}
