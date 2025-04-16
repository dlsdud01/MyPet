package com.example.mypet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
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

    final String serverUri = "tcp://test.mosquitto.org:1883"; // MQTT 브로커 주소
    final String clientId = MqttClient.generateClientId();
    final String subscriptionTopic = "mypet/data"; // 라즈베리파이에서 퍼블리시하는 토픽명

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기존 온습도 TextView 연결
        textTemperature = findViewById(R.id.textTemperature);
        textHumidity = findViewById(R.id.textHumidity);

        // 👇 CardView 가져오기
        CardView camCard = findViewById(R.id.cam);

        // 👇 클릭 리스너 설정
        camCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 👇 CamActivity로 전환
                Intent intent = new Intent(MainActivity.this, CamActivity.class);
                startActivity(intent);
            }
        });

        // MQTT 클라이언트 초기화
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        // 콜백 설정
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("MQTT", "MQTT 브로커 연결 성공: " + serverURI);
                subscribeToTopic(); // 연결되면 구독 실행
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "MQTT 연결 끊김", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d("MQTT", "수신된 메시지: " + message.toString());
                updateUIWithSensorData(message.toString()); // 수신된 메시지 처리
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        connectToMqtt();
    }

    // MQTT 브로커 연결 함수
    private void connectToMqtt() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true); // 자동 재접속
        options.setCleanSession(true);       // 새 세션 시작

        try {
            mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "연결 성공");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "연결 실패", exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 토픽 구독 함수
    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 1); // QoS 1로 구독
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 수신된 JSON 메시지를 파싱하여 텍스트뷰 업데이트
    private void updateUIWithSensorData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            double temp = json.getDouble("temperature");
            double humid = json.getDouble("humidity");

            // UI 쓰레드에서 텍스트 업데이트
            runOnUiThread(() -> {
                textTemperature.setText(String.format("%.1f°C", temp));
                textHumidity.setText(String.format("습도: %.1f%%", humid));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
