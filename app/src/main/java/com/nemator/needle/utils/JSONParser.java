package com.nemator.needle.utils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class JSONParser {

    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int SOCKET_TIMEOUT = 7000;

    private InputStream is = null;
    private JSONObject jObj = null;
    private String json = "";

    private int connectionTimeout = CONNECTION_TIMEOUT;
    private int socketTimeout = SOCKET_TIMEOUT;


    public JSONParser() {

    }

    public JSONParser(int connectionTimeout, int socketTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    //GET and POST requests
    public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            if(method == "POST"){
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }else if(method == "DELETE"){
                try{
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    url += "?" + paramString;
                    HttpDelete deleteConnection = new HttpDelete(url);

                    try {
                        HttpResponse httpResponse = httpClient.execute(deleteConnection);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        is = httpEntity.getContent();
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    Log.e("JSON Parser", "Error with request : " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("JSON Parser", "Error with request : " + e.getMessage());
            //e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString() + " json : "+json);
        }

        if(is != null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("JSON Parser", "Error closing input stream " + e.toString() + " json : " + json);
            }
        }


        // return JSON String
        return jObj;

    }

    public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params, JSONObject jsonObject) {
        if (method == "PUT") {
            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
                HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
                DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

                HttpPut putConnection = new HttpPut(url);
                putConnection.setHeader("json", jsonObject.toString());
                StringEntity se = new StringEntity(jsonObject.toString(), "UTF-8");
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                putConnection.setEntity(se);
                try {
                    HttpResponse httpResponse = httpClient.execute(putConnection);
                    String JSONString = EntityUtils.toString(httpResponse.getEntity(),
                            "UTF-8");
                    Log.i("JSON Parser", "Put request response: " + httpResponse.getStatusLine());
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("JSON Parser", "Error with request : " + e.getMessage());
                e.printStackTrace();
            }
        }
        /*
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("JSON Parser", "Buffer Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString() + " json : " + json);
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("JSON Parser", "Error closing input stream " + e.toString() + " json : " + json);
            }
        }
*/
        // return JSON String
        return jObj;
    }
}