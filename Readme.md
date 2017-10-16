# Network 기본예제

## 네트워킹이란?
- 네트워킹은 인터넷망에 연결되어 있는 원격지의 서버 또는 원격지의 단말과 통신을 통해 데이터를 주고받는 일반적인 일들
- 주로 활용되는 예로 SNS가 대표적이라고 할 수 있음
- 웹페이지를 보기위해 사용하는 HTTP프로토콜, 파일전송을 위한 FTP프로토콜, 메일을 위한 POP3프로토콜 등이 있음
- 2-tier(클라이언트 - 서버)와 3-tier(클라이언트-응용서버-데이터서버) 등이 있음

### 사용방법

```Java
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

        }
    }
}
```
- 네트워킹은 쓰레드와 밀접한 관련이 있음. 때문에 쓰레드를 관리하는 핸들러를 다루는 법이 필요
- NetworkThread클래스에서 StringBuilder 객체를 생성한다. 이유는 StringBuilder는 문자열의 추가,삭제 등이 여러 장점이 있기 때문임

※
- 문자열의 저장 및 변경을 위한 메모리 공간을 지닌 클래스
​- 문자열 데이터의 추가를 위해 append와 insert메소드를 지니고있다.
- String클래스는 문자열상수를 지니는 메모리 공간을 지니고 있으나, StringBuffer,StringBuilder안의
메모리 공간은 값이 변경 가능한 변수의 성격을 지님.​
- 버퍼를 저장할때, 문자열의 형태로 저장되는게 아니라 단순한 데이터의 형태로 저장되고, 나중에 toString소드에 의해 문자열로 반환되어 나옴.
ex) result.toString - 스트링빌더 객체인 result는 toString을 통해 문자열로 바꿔야함.


- HttpURLConnection으로 url.openConnection을 할수없기에 casting해준다.(http가 앞에 있으므로!)
-
```Java
con.setRequestMethod("GET");
// 통신이 성공인지 체크
if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
```

- RequestMethod로 GET을 입력, 받을때는 getResponseCode로 HttpURLConnection.HTTP_OK로 받은후 로직실행
- 서버로 보내는 RequestMethod에는 GET과 POST가 있음

#### GET VS POST

##### GET

- 모든 파라미터를 URL을 통해서 직접 전달

- 그렇기 때문에 구글에서 검색을 했을때 모든 파라미터 값이 16진수로 변환되어 URL 창에 나타남을 확인

- get은 가시적인 보안(사실은 그렇게 큰 영향을 끼치지는 못한다고 합니다.)에도 좋지 않고 보낼 수 있는 데이터의 양에도 한계

- http protocol의 default 방식은 get

##### POST

- 전달하려는 정보가 HTTP 바디에 포함되어 전달

- 웹브라우저 사용자의 눈에 보이지 않음 (이것을 일부 책에서는 가시적인 보안이라고 표기한다고 하지만 그것이 장점으로 작용하지는 않는다고 합니다. GET과 POST로 보안이 강화 된다고 한다면 말의 아귀가 맞겠지만 실제로 웹의 보안은 훨씬 복잡한 문제를 내포하고 있을테니까요.)

- 보낼 수 있는 양의 제한이 없음



                    출처: http://jeongri.tistory.com/131 [정리킴]
