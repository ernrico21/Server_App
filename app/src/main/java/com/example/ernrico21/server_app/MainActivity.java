package com.example.ernrico21.server_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText txtPort;
    Button btnServer;
    Button btnBrodcast;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this,MessageActivity.class);

        btnServer = (Button) findViewById(R.id.btnServer);

        txtPort= (EditText) findViewById(R.id.txtPort);

        btnServer.setOnClickListener(buttonServerListener);

    }


    View.OnClickListener buttonServerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int Port=0;

            String portText=txtPort.getText().toString();

            try {
                Port=Integer.parseInt(portText);
            }catch (NumberFormatException e){
                Toast.makeText(MainActivity.this,"INSERT A VALID PORT NUMBER",Toast.LENGTH_LONG).show();
                return;
            }
            intent.putExtra("Port",portText);
            startActivity(intent);
        }
    };



}

