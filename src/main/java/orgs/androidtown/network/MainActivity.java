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
    - 네트워킹
    1. 권한설정 > 런타임권한 (X)
    2. Thread > 네트워크를 통한 데이터 이용은 Sub Thread
    3. HttpUrlConnection > 내장 Api
       > Retrofit (내부에 Thread 포함)
       > Rx (내부에 Thread 포함, Thread 관리기능 포함, 예외처리 특화)
 */
public class MainActivity extends AppCompatActivity {
    TextView textView;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 999:
                    setData((String) msg.obj);
                    break;
            }
        }
    };

    public void setData(String data) {
        textView.setText(data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        NetworkThread network = new NetworkThread(handler);
        network.start();
    }

    /*
        - HttpURLConnection 사용하기
        1. URL 객체를 선언 (웹주소를 가지고 생성)
        2. URL 객체에서 서버연결을 해준다 > HttpURLConnection 을 생성 = Stream
        3. 커넥션의 방식을 설정 (기본값 = GET)
        4. 연결되어 있는 Stream 을 통해서 데이터를 가져온다.
        5. 연결(Stream)을 닫는다.
     */
    class NetworkThread extends Thread {
        Handler handler;

        public NetworkThread(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            final StringBuilder result = new StringBuilder();
            try {
                URL url = new URL("http://fastcampus.co.kr");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                // 통신이 성공인지 체크
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // 여기서 부터는 파일에서 데이터를 가져오는 것과 동일
                    InputStreamReader isr = new InputStreamReader(con.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String temp = "";
                    while ((temp = br.readLine()) != null) {
                        result.append(temp).append("\n");
                    }
                    br.close();
                    isr.close();
                } else {
                    Log.e("ServerError", con.getResponseCode() + "");
                }
                con.disconnect();
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
            // 핸들러로 obj 전달하기
            Message msg = new Message();
            msg.what = 999;
            msg.obj = result.toString();
            handler.sendMessage(msg);
            // 메인액티비티에 직접 값 넣기
            /*
                data = result.toString();
                handler.sendEmptyMessage(999);
            */
            // 메인thread에서 코드 실행하기
            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(result.toString());
                }
            });
            */
        }
    }
}