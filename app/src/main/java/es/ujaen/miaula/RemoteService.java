package es.ujaen.miaula;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import data.UserData;

public class RemoteService extends Service {
    public static final String MESSAGE_SID = "message";
    UserData userData;
    Handler handler = null;
    public static final String PARAM_USER = "user";
    public static final String PARAM_PASS = "pass";
    public static final String PARAM_DOMAIN = "domain";
    public static final String PARAM_PORT = "port";
    public RemoteService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        String user= intent.getExtras().getString(PARAM_USER);
        String pass= intent.getExtras().getString(PARAM_PASS);
        String domain = intent.getExtras().getString(PARAM_DOMAIN);
        short port = intent.getExtras().getShort(PARAM_PORT);
        userData=new UserData(user,pass,domain,port);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {

                switch (inputMessage.what){
                    case 1:
                        String message = inputMessage.getData().getString(RemoteService.MESSAGE_SID);
                        Toast.makeText(getApplicationContext(),"HANDLER: "+message,Toast.LENGTH_SHORT).show();
                        //Intent serviceractivity = new Intent(getApplicationContext(),ServiceActivity.class);
                        //startActivity(intent);
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }

            }
        };
        new Thread(new UserLogin()).start();
        return START_NOT_STICKY;
    }

    private void enviarMensaje(String datos) {
        if (handler != null) {
            Message mensaje = Message.obtain(handler, 1);
            Bundle datosmensaje = new Bundle();
            datosmensaje.putString(MESSAGE_SID, datos);

            mensaje.setData(datosmensaje);
            mensaje.sendToTarget();
        }
    }

    public class UserLogin implements Runnable {
        private static final String RESOURCE = "/ssmm/autentica.php";

        private static final int CODE_HTTP_OK = 200;

        @Override
        public void run() {

            UserData result = null;
            if (userData != null) {


                //TODO hacer la conexión y la autenticación

                String service = "http://" + userData.getDomain() + ":" +
                        userData.getPort() + RESOURCE + "?" + PARAM_USER + "=" + userData.getUserName() + "&" +
                        PARAM_PASS + "=" + userData.getPassword();

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
                                        result = UserData.processSession(userData, parts[0], parts[1]);
                                        Thread.sleep(10000);
                                        enviarMensaje(result.getUserName() + " " + result.getSid());
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

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
