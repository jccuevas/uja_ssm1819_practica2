package es.ujaen.miaula;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.AccessLog;
import data.Preferences;
import data.UserData;

public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener, FragmentAuth.OnFragmentInteractionListener {

    public static final String PREFS_PORT = "port";
    private static final String DEBUG_TAG = "HTTP";
    public static final String STATUS_DOMAIN = "domain";

    public static final String PREFS_DOMAIN = "domain";
    public static final String PREFS_USER = "user";

    private UserData ud = null;
    ConnectTask mTask = null;

    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                String message = inputMessage.getData().getString(RemoteService.MESSAGE_SID);

                switch (inputMessage.what) {
                    case 1:

                        Toast.makeText(getApplicationContext(), R.string.LOGIN_CORRECT, Toast.LENGTH_SHORT).show();
                        Intent serviceractivity = new Intent(getApplicationContext(), ServiceActivity.class);
                        startActivity(serviceractivity);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }

            }
        };


        Log.d("ARRANCANDO", "La aplicación móvil se está iniciando");
        FragmentManager fm = getSupportFragmentManager();
        Fragment temp = fm.findFragmentById(R.id.main_container);
        if (temp == null) {
            FragmentTransaction ft = fm.beginTransaction();
            FragmentAuth fragment = FragmentAuth.newInstance("", "");
            ft.add(R.id.main_container, fragment, "login");
            ft.commit();
        } else
            Toast.makeText(this, getString(R.string.mainactivity_fragmentepresent), Toast.LENGTH_SHORT).show();

        SharedPreferences sf = getPreferences(MODE_PRIVATE);
        String nombre = sf.getString("USER", "");
        String expires = sf.getString("EXPIRES", "");
        String sid = sf.getString("SID", "");
        if (nombre != "" && expires != "") {
            //Control de sesión
            Toast.makeText(this, "Bienvenido " + nombre, Toast.LENGTH_LONG).show();
            SimpleDateFormat sdf = new SimpleDateFormat("y-M-d-H-m-s");
            Date expirationDate = sdf.parse(expires, new ParsePosition(0));
            Date instant = new Date(System.currentTimeMillis());
            if (expirationDate.getTime() > instant.getTime()) {
                //Autenticar de manera transparente
                Intent intent = new Intent(this, ServiceActivity.class);
                intent.putExtra(ServiceActivity.PARAMETER_USER, nombre);
                intent.putExtra(ServiceActivity.PARAMETER_EXPIRES, expires);
                intent.putExtra(ServiceActivity.PARAMETER_SID, sid);
                startActivity(intent);

            }
            //comprobar si expires > momento actual
            //Si es mayor -> abro actividad (sesión válida)
            // ----> startActivity()
            //Si es menor -> la sesión ha caducado
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_log:
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment temp = fm.findFragmentById(R.id.main_container);
                if (temp == null) {
                    ft.remove(temp);
                }
                ft.add(R.id.main_container, ItemFragment.newInstance(1), "log");
                ft.addToBackStack("log");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(UserData udata) {

        Intent service = new Intent(this, RemoteService.class);
        service.putExtra(RemoteService.PARAM_USER, udata.getUserName());
        service.putExtra(RemoteService.PARAM_PASS, udata.getPassword());
        service.putExtra(RemoteService.PARAM_DOMAIN, udata.getDomain());
        service.putExtra(RemoteService.PARAM_PORT, udata.getPort());
        service.putExtra("handler", new Messenger(handler));
        startService(service);
//        Autentica auth = new Autentica();
//        auth.execute(udata);

//        Preferences.saveCredentials(this,udata);


    }

    @Override
    public void onListFragmentInteraction(String item) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class Autentica extends AsyncTask<UserData, Void, UserData> {

        private static final String RESOURCE = "/ssmm/autentica.php";
        private static final String PARAM_USER = "user";
        private static final String PARAM_PASS = "pass";
        private static final int CODE_HTTP_OK = 200;

        @Override
        protected UserData doInBackground(UserData... userData) {
            UserData data;
            UserData result = null;
            if (userData != null) {
                data = userData[0];

                //TODO hacer la conexión y la autenticación

                String service = "http://" + data.getDomain() + ":" +
                        data.getPort() + RESOURCE + "?" + PARAM_USER + "=" + data.getUserName() + "&" +
                        PARAM_PASS + "=" + data.getPassword();

                try {
                    URL urlService = new URL(service);
                    HttpURLConnection connection = (HttpURLConnection) urlService.openConnection();
                    connection.setReadTimeout(10000 /* milliseconds */);
                    connection.setConnectTimeout(15000 /* milliseconds */);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();

                    int code = connection.getResponseCode();
                    if (code == CODE_HTTP_OK) {
                        InputStreamReader is = new InputStreamReader(connection.getInputStream());
                        BufferedReader br = new BufferedReader(is);
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith("SESSION-ID=")) {
                                String parts[] = line.split("&");
                                if (parts.length == 2) {
                                    if (parts[1].startsWith("EXPIRES=")) {
                                        result = UserData.processSession(data, parts[0], parts[1]);
                                    }
                                }
                            }
                        }
                        br.close();
                        is.close();
                    }

                    connection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();

                } catch (IOException ioex) {
                    ioex.printStackTrace();

                } finally {
                    return result;
                }


            } else
                return null;
        }

        @Override
        protected void onPostExecute(UserData userData) {
            super.onPostExecute(userData);
            if (userData != null) {

                Toast.makeText(getApplicationContext(), R.string.auth_correct, Toast.LENGTH_LONG).show();

                SharedPreferences sp = getPreferences(MODE_PRIVATE);
                //SharedPreferences sp = getSharedPreferences(userData.getUserName(),MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("USER", userData.getUserName());
                editor.putString("SID", userData.getSid());
                editor.putString("EXPIRES", userData.getExpires());
                editor.commit();

                //SharedPreferences def = getPreferences(MODE_PRIVATE);
                //SharedPreferences.Editor edit2 = def.edit();
                //edit2.putString("LAST_USER",userData.getUserName());
                //edit2.commit();

                AccessLog.addRecord(getApplicationContext(), new Date(System.currentTimeMillis()).toString() + " " + userData.getUserName() + " " + userData.getDomain() + " " + userData.getPort() + " Ok");
//
                Intent intent = new Intent(getApplicationContext(), ServiceActivity.class);
                intent.putExtra(ServiceActivity.PARAMETER_USER, userData.getUserName());
                intent.putExtra(ServiceActivity.PARAMETER_SID, userData.getSid());
                intent.putExtra(ServiceActivity.PARAMETER_EXPIRES, userData.getExpires());
                startActivity(intent);
            } else {
                SharedPreferences sp = getSharedPreferences(userData.getUserName(), MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("USER", userData.getUserName());
                editor.putString("SID", "");
                editor.putString("EXPIRES", "");
                editor.commit();
                AccessLog.addRecord(getApplicationContext(), new Date(System.currentTimeMillis()).toString() + " " + userData.getUserName() + " " + userData.getDomain() + " " + userData.getPort() + " Error");
//
                Toast.makeText(getApplicationContext(), R.string.auth_error, Toast.LENGTH_LONG).show();
            }

        }


    }


    public String readServer(UserData udata) {
        try {
            //URL url = new URL(domain);
            //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            Socket socket = new Socket(udata.getDomain(), udata.getPort());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF("GET /~jccuevas/ssmm/login.php?user=user1&pass=12341234 HTTP/1.1\r\nhost:www4.ujaen.es\r\n\r\n");
            dataOutputStream.flush();

            StringBuilder sb = new StringBuilder();
            BufferedReader bis;
            bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            while ((line = bis.readLine()) != null) {
                sb.append(line);
                mTask.onProgressUpdate(line.length());

            }
            final String datos = sb.toString();


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
    private String downloadUrl(String domain, String user, String pass) throws IOException {
        InputStream is = null;
        String result = "";

        HttpURLConnection conn = null;
        try {
            String contentAsString = "";
            String tempString = "";
            String url = "http://" + domain + "/ssmm/autentica.php" + "?user=" + user + "&pass=" + pass;
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
            final int contentLength = conn.getHeaderFieldInt("Content-length", 1000);
            String mimeType = conn.getHeaderField("Content-Type");
            String encoding = mimeType.substring(mimeType.indexOf(";"));

            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            while ((tempString = br.readLine()) != null) {
                contentAsString = contentAsString + tempString;
                //task.onProgressUpdate(contentAsString.length());
            }


            return contentAsString;
        } catch (MalformedURLException mex) {
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

    class ConnectTask extends AsyncTask<UserData, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView banner = findViewById(R.id.main_degree);
            banner.setText(R.string.main_connecting);
        }

        @Override
        protected String doInBackground(UserData... userData) {
            try {
                String url = "http://" + userData[0].getDomain();
                return downloadUrl(url, userData[0].getUserName(), userData[0].getPassword());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), getString(R.string.main_progress) + " " + String.valueOf(values[0]), Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            TextView banner = findViewById(R.id.main_degree);
            banner.setText(R.string.main_connected);
        }
    }
}
