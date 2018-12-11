package es.ujaen.miaula;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import data.Protocolo;
import data.UserData;

public class PartQueryService extends Service {
    public static final String MESSAGE_SID = "message";
    private String query;
    private String domain;
    private short port=80;
    Messenger messenger = null;
    public static final String PARAM_DOMAIN = "domain";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_PORT = "port";

    public PartQueryService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        messenger = (Messenger) intent.getParcelableExtra("handler");

        Toast.makeText(this,"Iniciando Servicio",Toast.LENGTH_SHORT).show();
        query = intent.getExtras().getString(PARAM_QUERY);
        domain = "192.168.66.253";//intent.getExtras().getString(PARAM_DOMAIN);
        port = 80;//intent.getExtras().getShort(PARAM_PORT);

        new Thread(new RemoteQuery()).start();
        return START_NOT_STICKY;
    }

    private void enviarMensaje(int tipo,String datos) {

        if (messenger != null) {
            Message mensaje = Message.obtain(null, tipo);
            Bundle datosmensaje = new Bundle();
            datosmensaje.putString(MESSAGE_SID, datos);

            mensaje.setData(datosmensaje);
            //mensaje.sendToTarget();
            try {
                messenger.send(mensaje);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    public class RemoteQuery implements Runnable, Protocolo {
        private static final String RESOURCE = "/query";

        private static final int CODE_HTTP_OK = 200;

        @Override
        public void run() {
             String response = query(query);

             enviarMensaje(1,response);

        }

        @Override
        public UserData login(UserData userData) {
            return null;
        }

        @Override
        public String query(String myQuery) {
            String result = null;
            if (myQuery != null) {


                //TODO hacer la conexión y la autenticación

                String service = "http://" + domain + ":" +
                        port + RESOURCE + "?" + "query" + "=" + myQuery;

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
                            result= line;
                        }
                        br.close();
                        is.close();
                    }

                    connection.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    enviarMensaje(2,"URL INCORRECTA");


                } catch (IOException ioex) {
                    ioex.printStackTrace();
                    enviarMensaje(2,"URL FALLO EN LA CONEXIÓN");

                }finally {
                    return result;
                }

            }
            return result;
        }
    }
}
