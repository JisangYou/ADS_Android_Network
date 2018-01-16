package orgs.androidtown.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 - Network
 1. 권한 설정 -> Runtime Permission 이 아님
 2. Thread -> Network 를 통한 data 이용은 Sub Thread 를 사용
 3. HttpUrlConnection : 내장 API
    > 내장 API : HttpUrlConnection, AsyncTask( 시작과 끝이 있는 Connection Thread )
    > Retrofit ( 내부에 Thread 처리 포함 )
    > Rx ( 내부에 Thread 처리 및 관리 기능 포함 , 예외 처리가 특화되어 있음 )
 */
public class MainActivity extends AppCompatActivity {

    private static final int ACTION_SEND = 22;
    private TextView textView;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);
        NetworkThread network = new NetworkThread(handler);
        network.start();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ACTION_SEND:
                    // Message 에 담긴 obj 를 통해 TextView 를 변경한다.
                    textView.setText(msg.obj.toString());
                    break;
                case 999:
                    // 변경된 결과값을 통해 TextView 를 변경한다.
                    textView.setText(data);
                    break;
            }
        }
    };

    /*
     - HttpURLConnection 사용하기
     1. URL 객체 선언 ( 웹 주소를 가지고 생성 )
     2. URL 객체에서 서버 연결을 해준다 -> HttpURLConnection 을 생성 ( Stream)
     3. Connection 방식을 선언 ( Default : GET )
     4. 연결되어 있는 Stream 을 통해서 Data 를 가져온다.
     5. 연결 Stream 을 닫는다.
     https://ko.wikipedia.org/wiki/HTTP_상태_코드
     */
    class NetworkThread extends Thread{

        Handler handler;
        StringBuilder result = new StringBuilder();

        public NetworkThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // Network 처리
                // 1. URL 객체 선언 ( 웹 주소를 가지고 생성 )
                URL url = new URL("http://fastcampus.co.kr");
                // 2. URL 객체에서 서버 연결을 해준다
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                // 3. Connection 방식을 선언 ( Default : GET )
                urlConnection.setRequestMethod("GET");

                // 통신이 성공적인지 체크
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 4. 연결되어 있는 Stream 을 통해서 Data 를 가져온다.
                    // 여기서부터는 File 에서 Data 를 가져오는 방식과 동일
                    InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String temp = "'";
                    while ((temp = br.readLine()) != null) {
                        result.append(temp).append("\n");
                    }

                    // 5. 연결 Stream 을 닫는다.
                    br.close();
                    isr.close();
                } else {
                    Log.e("ServerError", urlConnection.getResponseCode() + " , "  + urlConnection.getResponseMessage());
                }
                urlConnection.disconnect();
            }catch (Exception e){
                Log.e("Error", e.toString());
            }

            // UI 를 변경하는 경우에는 Sub Thread 에서는 변경이 불가능하기 때문에 3가지 방법을 사용한다.

            // 1. Handler 에 결과값을 Message.obj 에 담아 보낸다.
            sendMessage(result);

            // 2. Handler 에 빈 Message 를 보내 변경된 결과값을 사용한다.
            sendEmptyMessage(result);

            // 3. runOnUiThread 를 사용하여 결과값을 사용한다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(result.toString());
                }
            });
        }
    }

    public void sendMessage(StringBuilder result){
        Message msg = new Message();
        msg.what = ACTION_SEND;
        msg.obj = result;
        handler.sendMessage(msg);
    }

    public void sendEmptyMessage(StringBuilder result){
        data = result.toString();
        handler.sendEmptyMessage(999);
    }
}