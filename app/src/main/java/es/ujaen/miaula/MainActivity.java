package es.ujaen.miaula;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import data.UserData;

public class MainActivity extends AppCompatActivity implements FragmentAuth.OnFragmentInteractionListener {

    private static final String DEBUG_TAG = "HTTP";
    private UserData ud=null;
    ConnectTask mTask = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Log.d("ARRANCANDO","La aplicación móvil se está iniciando");
        FragmentManager fm = getSupportFragmentManager();
        Fragment temp = fm.findFragmentById(R.id.main_container);
        if(temp==null) {
            FragmentTransaction ft = fm.beginTransaction();
            FragmentAuth fragment = FragmentAuth.newInstance("", "");
            ft.add(R.id.main_container, fragment, "login");
            ft.commit();
        } else
            Toast.makeText(this,getString(R.string.mainactivity_fragmentepresent), Toast.LENGTH_SHORT).show();

       if(savedInstanceState!=null){
           String domain = savedInstanceState.getString("domain");
           ud = new UserData();
           ud.setDomain(domain);

       }
       else {
           ud = new UserData();

       }

        changetitle(ud.getDomain());
    }

    public void changetitle(String title){
        TextView tuser = findViewById(R.id.main_apptitle);
        tuser.setText(title);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("domain",ud.getDomain());
    }

    @Override
    public void onFragmentInteraction(UserData udata) {

        ConnectTask mTask = new ConnectTask();

        mTask.execute(udata);


    }


    public String readServer(UserData udata){
        try {
            //URL url = new URL(domain);
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            Socket socket = new Socket(udata.getDomain(),udata.getPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF("GET /~jccuevas/ssmm/login.php?user=user1&pass=12341234 HTTP/1.1\r\nhost:www4.ujaen.es\r\n\r\n");
            dataOutputStream.flush();

            StringBuilder sb = new StringBuilder();
            BufferedReader bis;
            bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            while((line = bis.readLine())!=null) {
                sb.append(line);
                mTask.onProgressUpdate(line.length());

            }
            final String datos= sb.toString();


            return datos;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String domain,String user,String pass) throws IOException {
        InputStream is = null;
        String result = "";

        HttpURLConnection conn = null;
        try {
            String contentAsString="";
            String tempString="";
            String url = "http://"+domain+"/~jccuevas/ssmm/autentica.php"+"?user="+user+"&pass="+pass;
            URL service_url = new URL(url);
            System.out.println("Abriendo conexión: " + service_url.getHost()
                    + " puerto=" + service_url.getPort());
            conn = (HttpURLConnection) service_url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            final int response = conn.getResponseCode();
            final int contentLength = conn.getHeaderFieldInt("Content-length",1000);
            String mimeType=conn.getHeaderField("Content-Type");
            String encoding=mimeType.substring(mimeType.indexOf(";"));

            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            BufferedReader br = new BufferedReader( new InputStreamReader(is, "UTF-8"));

            while((tempString=br.readLine())!=null)
            {
                contentAsString = contentAsString + tempString;
                //task.onProgressUpdate(contentAsString.length());
            }


            return contentAsString;
        } catch (MalformedURLException mex){
            result = "URL mal formateada: " + mex.getMessage();
            System.out.println(result);
         //   mURL.post(new Runnable() {
          //      @Override
          //      public void run() {
         //           mURL.setError(getString(R.string.network_url_error));
         //       }
         //   });
        } catch (IOException e) {
            result = "Excepción: " + e.getMessage();
            System.out.println(result);


        } finally {
            if (is != null) {
                is.close();
                conn.disconnect();
            }
        }
        return result;
    }
    class ConnectTask extends AsyncTask<UserData,Integer,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView banner = findViewById(R.id.main_degree);
            banner.setText(R.string.main_connecting);
        }

        @Override
        protected String doInBackground(UserData... userData) {
            try {
                String url = "http://"+userData[0].getDomain();
                return downloadUrl(url,userData[0].getUserName(),userData[0].getPassword());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(),getString(R.string.main_progress)+" "+String.valueOf(values[0]),Toast.LENGTH_LONG).show();

        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            TextView banner = findViewById(R.id.main_degree);
            banner.setText(R.string.main_connected);
        }
    }
}
