package com.example.ernrico21.server_app;

import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    ServerThred serverThred;
    MessageActivity activity;
    ServerSocket serverSocket;
    int Port;
    List<ConnClient> clientList;//lista dove saranno presenti i thread e socket che servono per la comunicazione con i client
    TextView infoPort;
    /*
    Classe per la creazione del Server
    Activity che contiene la ListView da modificare
    La porta dove sarà in ascolto il serversocket
     */
    Server(MessageActivity activity,int Port) {
        this.activity=activity;
        this.Port=Port;
        clientList = new ArrayList<ConnClient>();
        serverThred = new ServerThred();
        serverThred.start();

    }
    //Thread dove sarà in ascolto serversocket
    private class ServerThred extends Thread {


        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(Port);//creo il socket
                activity.changePortText(Port);//cambio il testo nella View principale mettendo numero di porta e ip

                while (true) {

                    socket = serverSocket.accept();
                    ConnClient client = new ConnClient();//in caso di connesione creo un oggetto client
                    clientList.add(client);//aggiungo oggetto alla lista
                    MessageThread messageThread = new MessageThread(client, socket, activity);//faccio partire il thread per il socket del client appena connesso
                    messageThread.start();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();//in caso di errore provo a chiudere il server
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /*
    Thread con il socket per i client
     */
    private class MessageThread extends Thread {
        Socket socket;
        ConnClient connClient;
        String messageToCLient = "";
        MessageActivity activity;

        MessageThread(ConnClient connClient, Socket socket, MessageActivity activity) {
            this.connClient = connClient;
            this.socket = socket;
            //assegno all'oggetto del client il socket e il thread
            connClient.socket = socket;
            connClient.thread = this;
            this.activity = activity;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String username = dataInputStream.readUTF();

                connClient.username = username;
                //invio l'ack per l'avvenuta connesione
                dataOutputStream.writeUTF( "\nconnection with server has been successfully established\n");
                dataOutputStream.flush();

                while (true) {
                    if (dataInputStream.available() > 0) {

                        final String clientMessage = dataInputStream.readUTF();
                        //se il client si è disconesso chiudo il socket
                        if (clientMessage.equals("ClientDisconnectedCloseSocket")){
                            socket.close();
                            return;
                        }
                        else {
                            //modifico la LIstView nel MainThread
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.messageList.add(clientMessage);
                                    activity.adapter.notifyDataSetChanged();
                                }
                            });

                            //invio ack per la ricevuta dei messaggi
                            messageToCLient = "Server Recived The Message";
                        }
                    }
                    //se la variabile è stata modificata allora la invio come messaggio
                    if (!messageToCLient.equals("")) {
                        dataOutputStream.writeUTF("\n"+messageToCLient+"\n");
                        dataOutputStream.flush();
                        messageToCLient = "";//riazzero la variabile
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(socket!=null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //metodo per la modifica della variabile che verrà poi inviata ai client
        public void sendMessage(String message) {
            messageToCLient = message;
        }

    }
    //Thread per la chiusura del serversocket e dei socket collegati ai client
    private class CloseThread extends Thread{
        @Override
        public void run(){

            for(ConnClient connClient:clientList){
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(connClient.socket.getOutputStream());
                    dataOutputStream.writeUTF( "\nSERVER DISCONNECTED\n");
                    dataOutputStream.flush();
                    connClient.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void onDestroy() {
        CloseThread closeThread=new CloseThread();
        closeThread.start();
    }
    //metodo per inviare messagi in broadcast
    public void messageBroadcast(String message) {
        for (ConnClient connClient : clientList) {
            connClient.thread.sendMessage(message);
        }
    }
    //classe per i la connesione dei Client
    private class ConnClient {

        MessageThread thread;
        String username;
        Socket socket;
    }
}