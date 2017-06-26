package com.ppsk.ppsk_wizualizacja;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    public MqttMessage message;
    public MqttMessage messageHeater;
    private Button b1;
    private final String topic = "door/status";
    private ImageView imageView1;
    public MqttAndroidClient client;
    private int qos;
    private boolean button1IsPressed = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_heat_exchanger:
                    titleChange(R.string.title_heat_exchanger);
                    imageView1 = (ImageView) findViewById(R.id.imageView1);
                    if(message.toString().equals(" ")) {
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_n);
                    } else if(message.toString().equals("open")) {
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
                    } else {
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
                    }
                    b1 = (Button) findViewById(R.id.button1);
                    qos = 1;
                    b1.setText(R.string.title_heat_exchanger);
                    b1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if(message.toString().equals("open")) {
                                message = new MqttMessage("close".getBytes());
                                imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
                            } else{
                                message = new MqttMessage("open".getBytes());
                                imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
                            }
                            publish(client,message);
                        }
                    });
                    return true;
                case R.id.navigation_heater:
                    titleChange(R.string.title_heater);
                    imageView1 = (ImageView) findViewById(R.id.imageView1);
                    if(messageHeater.toString().equals(" ")) {
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_n);
                    } else if(messageHeater.toString().equals("open")) {
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
                    } else{
                        imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
                    }
                    b1 = (Button) findViewById(R.id.button1);
                    qos = 2;
                    b1.setText(R.string.title_heater);
                    b1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if(messageHeater.toString().equals("open")) {
                                messageHeater = new MqttMessage("close".getBytes());
                                imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
                            } else {
                                messageHeater = new MqttMessage("open".getBytes());
                                imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
                            }
                            publish(client,messageHeater);
                        }
                    });
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleChange(R.string.title_heat_exchanger);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setUserName("tkykagdm");
        options.setPassword("lEtftZCEXKc1".toCharArray());
        String clientId = MqttClient.generateClientId();

        message = new MqttMessage(" ".getBytes());
        messageHeater = new MqttMessage(" ".getBytes());

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        if(message.toString().equals(" ")) {
            imageView1.setImageResource(R.drawable.heat_exchanger_icon_n);
        } else if(message.toString().equals("open")) {
            imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
        } else {
            imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
        }


        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://m11.cloudmqtt.com:15498", clientId);
        final MqttAndroidClient client1 = new MqttAndroidClient(this.getApplicationContext(), "tcp://m11.cloudmqtt.com:15498", clientId);

        b1 = (Button) findViewById(R.id.button1);
        qos = 1;
        b1.setText(R.string.title_heat_exchanger);
        if (b1 != null) {
            b1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        button1IsPressed=true;
                        if (message.toString().equals("open")) {
                            message = new MqttMessage("close".getBytes());
                            imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
                            publish(client1, message);
                        } else {
                            message = new MqttMessage("open".getBytes());
                            imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
                            publish(client1, message);
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        button1IsPressed=false;
                    }
                    return button1IsPressed;
                }
            });
        }

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setCallback(MainActivity.this);

                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                client.close();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                Toast.makeText(MainActivity.this, "Nie można połączyć z: " + topic, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (MqttException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Brak połączenia ze sterownikiem", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        if(message.toString().equals("close")) {
            imageView1.setImageResource(R.drawable.heat_exchanger_icon_off);
        } else {
            imageView1.setImageResource(R.drawable.heat_exchanger_icon_on);
        }
        this.message = message;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public synchronized void publish(final MqttAndroidClient client, final MqttMessage message)
    {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    client.setCallback(MainActivity.this);

                    final String topic = "door/status";
                    try {
                        IMqttToken subToken = client.publish(topic, message);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                client.close();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {

                            }
                        });
                    } catch (MqttException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Brak połączenia ze sterownikiem", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    protected void titleChange(int title) {
        setTitle(title);
    }
}

