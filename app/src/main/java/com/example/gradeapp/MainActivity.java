package com.example.gradeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    ClientChannel channel;
    TextView shellOutput;
    String host, username, password;
    Integer port;
    String command;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        host = "empress.csusm.edu";
        username = "kslott001";
        port = 22;
        password = "211seito";

        command = "cat item.txt\n";

        // Setting user.com property manually
        // since isn't set by default in android
        String key = "user.home";
        Context Syscontext;
        Syscontext = getApplicationContext();
        String val = Syscontext.getApplicationInfo().dataDir;
        System.setProperty(key, val);

        // Creating a client instance
        SshClient client = SshClient.setUpDefaultClient();
        client.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
        client.start();

        // Starting new thread because network processes
        // can interfere with UI if started in main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Connection establishment and authentication
                    try (ClientSession session = client.connect(username, host, port).verify(10000).getSession()) {
                        session.addPasswordIdentity(password);
                        session.auth().verify(50000);
                        System.out.println("Connection establihed");

                        // Create a channel to communicate
                        channel = session.createChannel(Channel.CHANNEL_SHELL);
                        System.out.println("Starting shell");

                        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                        channel.setOut(responseStream);

                        // Open channel
                        channel.open().verify(5, TimeUnit.SECONDS);
                        try (OutputStream pipedIn = channel.getInvertedIn()) {
                            pipedIn.write(command.getBytes());
                            pipedIn.flush();
                        }

                        // Close channel
                        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                                TimeUnit.SECONDS.toMillis(5));

                        // Output after converting to string type
                        String responseString = new String(responseStream.toByteArray());
                        System.out.println(responseString);
                        //shellOutput.setText(responseString);

                        String[] split = responseString.split("\n",999);

                        ArrayList<String> items = new ArrayList<>(Arrays.asList(split).subList(3, split.length - 1));

                        /*String[] arr = new String[items.size()];
                        for (int i = 0; i < items.size(); i++)
                            arr[i] = items.get(i);*/
                        System.out.println(items);
                        Spinner spinner = findViewById(R.id.spinner);



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Populate the spinner
                                //List<String> data = Arrays.asList("item1", "item2", "item3");
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        MainActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        items
                                );


                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);

                            }
                            });



                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        client.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();



    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        //make toastof name of course
        // which is selected in spinner
        /*Toast.makeText(getApplicationContext(),
                        items[pos],
                        Toast.LENGTH_LONG)
                .show();*/
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void authenticate(View view) {

        // Create an intent for sshActivity
        Intent intent = new Intent(this, MainActivity2.class);

        /*Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item from the spinner
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Start the second activity and pass the selected item as an extra
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("selected_item", selectedItem);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });*/

        // Declare fields
        EditText prog_num = (EditText) findViewById(R.id.editTextNumber);
        EditText pts = (EditText) findViewById(R.id.editTextNumber2);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);


        // Get input data from fields
        String number = prog_num.getText().toString();
        String points = pts.getText().toString();
        String select = (String) spinner.getSelectedItem();


        // Pass on data to next activity via intent
        intent.putExtra("progNumIP", number);
        intent.putExtra("pointsIP", points);
        intent.putExtra("selected_item", select);

        startActivity(intent);
        finish();
    }

    }





