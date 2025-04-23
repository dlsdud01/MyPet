package com.example.mypet;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView textTemperature, textHumidity;
    private MqttAndroidClient mqttAndroidClient;

    // MQTT 브로커(공개 테스트 서버)
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private static final String SUBSCRIPTION_TOPIC = "mypet/data"; // 반드시 라즈베리파이에서 맞는 토픽명 확인!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 텍스트뷰 연결
        textTemperature = findViewById(R.id.textTemperature);
        textHumidity = findViewById(R.id.textHumidity);

        // 카메라 카드뷰 연결 및 클릭 시 CamActivity(WebView)로 이동
        CardView camCard = findViewById(R.id.cam);
        if (camCard != null) {
            camCard.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "카드 클릭됨!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CamActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e("MainActivity", "camCard가 null! 레이아웃 XML에서 ID 확인 필요");
        }

        // MQTT 클라이언트 초기화
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), SERVER_URI, CLIENT_ID);

        // MQTT 콜백 설정
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("MQTT", "MQTT 브로커 연결 성공: " + serverURI);
                subscribeToTopic();
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "MQTT 연결 끊김", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d("MQTT", "수신된 메시지: " + message.toString());
                updateUIWithSensorData(message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        connectToMqtt();
    }

    // MQTT 브로커 연결 함수
    private void connectToMqtt() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "연결 성공");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "연결 실패", exception);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "MQTT 연결 실패", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 토픽 구독 함수
    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(SUBSCRIPTION_TOPIC, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 수신된 JSON 메시지를 파싱해서 텍스트뷰에 반영
    private void updateUIWithSensorData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            double temp = json.getDouble("temperature");
            double humid = json.getDouble("humidity");

            runOnUiThread(() -> {
                textTemperature.setText(String.format("%.1f°C", temp));
                textHumidity.setText(String.format("습도: %.1f%%", humid));
            });
        } catch (Exception e) {
            Log.e("MQTT", "JSON 파싱 오류", e);
        }
    }
}
