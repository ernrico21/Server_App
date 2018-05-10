package com.example.ernrico21.server_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    TextView lblServerInfo;
    Server server;
    ListView lstMessage;
    List<String>messageList;
    Button btnBroadCast;
    ArrayAdapter<String> adapter;
    EditText txtMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        String Port= getIntent().getStringExtra("Port");

        lblServerInfo = (TextView) findViewById(R.id.lblServerInfo);
        lstMessage = (ListView)findViewById(R.id.lstMessage);
        btnBroadCast = (Button)findViewById(R.id.btnSendMessage);
        txtMessage = (EditText)findViewById(R.id.txtMessage);

        messageList=new ArrayList<String>();
        messageList.add("Messages:");
        adapter= new ArrayAdapter<String>(this,R.layout.raw,messageList);
        lstMessage.setAdapter(adapter);

        server = new Server(MessageActivity.this, Integer.parseInt(Port));

        btnBroadCast.setOnClickListener(buttonSendMessageListener);
    }


    View.OnClickListener buttonSendMessageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String messageToSend=txtMessage.getText().toString();
            if(!messageToSend.equals("")){
                txtMessage.setText("");
                txtMessage.clearFocus();
                btnBroadCast.requestFocus();
                //nascondo la tastiera
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                server.messageBroadcast(messageToSend+"\n");
            }
            else{
                Toast.makeText(MessageActivity.this,"WRITE A MESSAGE TO SEND",Toast.LENGTH_LONG).show();
            }
        }
    };

    public void changePortText(int Port) throws SocketException {
        String ip= get_ip();
        lblServerInfo.setText("Server Online IP: "+ip+"\nPort: "+Port);
    }

    public void showMessage(String message){
        messageList.add(message);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
    Se viene premuto il tasto "Indietro" deve essere chiuso il socketserver
    altrimenti se dopo provo a ricrearlo va in errore.
    Chiedo creo un AlertBox per chiedere se è sicurto dell'operazione
     */
    @Override
    public void onBackPressed() {
        final Intent intent2 = new Intent(this,MainActivity.class);
        final AlertDialog alertDialog = new AlertDialog.Builder(MessageActivity.this).create();
        alertDialog.setTitle("ATTENTION");
        alertDialog.setMessage("THIS ACTION WILL CLOSE THE SERVER");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CONTINUE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        server.onDestroy();
                        startActivity(intent2);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private String get_ip() throws SocketException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4) {
                        //4 for ipv4, 16 for ipv6
                        String ip= ia.getAddress().toString();
                        return ip.replace("/","");
                    }
                }
            }
        }
        return null;
    }
}
