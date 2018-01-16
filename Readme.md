# ADS04 Android

## 수업 내용

- NetWorking 기본개념 및 예제 학습

## Code Review

### MainActivity

```Java
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
                //HttpURLConnection으로 url.openConnection 형변환해준다.
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
```

- UI 를 변경하는 경우에는 Sub Thread 에서는 변경이 불가능하기 때문에 다음과 같은 여러가지 방법을 사용할 수 있다.

1. 메시지의 ID에 해당하는 값을 전달할 때 사용
```Java
boolean Handler.sendEmptyMessage(int what)
```
2. ID만으로 불가능하고 좀 더 내용이 있는 정보를 전송할 때 사용
```Java
boolean Handler.sendMessage(Message msg)
```
3. 메시지가 큐에 순서대로 쌓여서 FIFO(First In First Out)형태로 처리되지만 이 메서드를 사용하면 노래방에서 우선예약 하듯이 사용

```Java
boolean sendMessageAtFrontOfQueue(Message msg)
```
4. 밑에서 다룰 runOnUiThread



## 보충설명

### HTTP란?
>> HTTP(HyperText Transfer Protocol, 문화어: 초본문전송규약, 하이퍼본문전송규약)는 WWW 상에서 정보를 주고받을 수 있는 프로토콜이다. 주로 HTML 문서를 주고받는 데에 쓰인다. TCP와 UDP를 사용하며, 80번 포트를 사용한다. 1996년 버전 1.0, 그리고 1999년 1.1이 각각 발표되었다.
>> HTTP는 클라이언트와 서버 사이에 이루어지는 요청/응답(request/response) 프로토콜이다. 예를 들면, 클라이언트인 웹 브라우저가 HTTP를 통하여 서버로부터 웹페이지나 그림 정보를 요청하면, 서버는 이 요청에 응답하여 필요한 정보를 해당 사용자에게 전달하게 된다. 이 정보가 모니터와 같은 출력 장치를 통해 사용자에게 나타나는 것이다.
>> HTTP를 통해 전달되는 자료는 http:로 시작하는 URL(인터넷 주소)로 조회할 수 있다.

#### 참고
![참고자료](http://cfile219.uf.daum.net/image/13238A244A8C58D23212A8)

![참고자료](http://www.httpdebugger.com/images/article/http_protocol/http-session.jpg)

### 네트워킹이란?

- 네트워킹은 인터넷망에 연결되어 있는 원격지의 서버 또는 원격지의 단말과 통신을 통해 데이터를 주고받는 일반적인 일들
- 주로 활용되는 예로 SNS가 대표적이라고 할 수 있음
- 웹페이지를 보기위해 사용하는 HTTP프로토콜, 파일전송을 위한 FTP프로토콜, 메일을 위한 POP3프로토콜 등이 있음
- 2-tier(클라이언트 - 서버)와 3-tier(클라이언트-응용서버-데이터서버) 등이 있음

#### ※ StringBuilde
r
- 문자열의 저장 및 변경을 위한 메모리 공간을 지닌 클래스 
​- 문자열 데이터의 추가를 위해 append와 insert메소드를 지니고 있음.
- String클래스는 문자열상수를 지니는 메모리 공간을 지니고 있으나, StringBuffer,StringBuilder안의 메모리 공간은 값이 변경 가능한 변수의 성격을 지님.​
- 버퍼를 저장할때, 문자열의 형태로 저장되는게 아니라 단순한 데이터의 형태로 저장되고, 나중에 toString소드에 의해 문자열로 반환되어 나옴.
ex) result.toString - 스트링빌더 객체인 result는 toString을 통해 문자열로 바꿔야함.





- 서버로 보내는 RequestMethod에는 GET과 POST가 있음

### GET VS POST

#### GET

- 모든 파라미터를 URL을 통해서 직접 전달
- 그렇기 때문에 구글에서 검색을 했을때 모든 파라미터 값이 16진수로 변환되어 URL 창에 나타남을 확인
- get은 가시적인 보안(사실은 그렇게 큰 영향을 끼치지는 못한다고는 함)에도 좋지 않고 보낼 수 있는 데이터의 양에도 한계
- http protocol의 default 방식은 get

##### POST

- 전달하려는 정보가 HTTP 바디에 포함되어 전달
- 웹브라우저 사용자의 눈에 보이지 않음 (이것을 일부 책에서는 가시적인 보안이라고 표기한다고 하지만 그것이 장점으로 작용하지는 않는다고 함. GET과 POST로 보안이 강화 된다고 한다면 말의 아귀가 맞겠지만 실제로 웹의 보안은 훨씬 복잡한 문제를 내포)
- 보낼 수 있는 양의 제한이 없음

### runOnUiThead


- runOnUiThread 는 Thread 안에서 UI 이 접근을 가능하게 도와주는 역할
- 안드로이드에서 제공하는 Message나 Runnable 객체를 UI 스레드 쪽에서 동작시키기 원할 경우 사용하는 방법
- runOnUiThread와 Handler 를 사용하는 방법의 차이는 Handler는 post 방식을 통해 매번 이벤트를 발생시키지만, runOnUiThread는 현재 시점이 UI 스레드이면 바로 실행시킨다는 점에서 좀 더 효율적이라고 할 수 있음.
- 현재 스레드가 UI 스레드라면 UI 자원을 사용하는 행동에 대해서는 즉시 실행, 만약 현재 스레드가 UI 스레드가 아니라면 행동은 UI 스레드의 자원 사용 이벤트 큐에 들어가게 되는 것
- 
- 사용예제
```Java
new Thread(new Runnable() {
     @Override 
     public void run() { 
         for(i = 0; i<=100; i++) { 
             // 현재 UI 스레드가 아니기 때문에 메시지 큐에 Runnable을 등록 함 
             runOnUiThread(new Runnable() { 
                 public void run() { 
                     // 메시지 큐에 저장될 메시지의 내용 
                     textView.setText("runOnUiThread 님을 통해 텍스트 설정"); 
                     }
                }); 
              } 
            } 
        }).start();
```




### 출처

- [출처] : https://ko.wikipedia.org/wiki/HTTP
- [출처] : http://jeongri.tistory.com/131 [정리킴]
- [출처] : http://iw90.tistory.com/130 [woong's]
- [출처] : https://github.com/Hooooong/DAY25_HTTPConnect

## TODO

- networking에 대한 더 깊은 이해가 필요
- 추후에 backEnd에 협업할 때 관련 http response code 숙지하기.

## Retrospect

- 기본적인 예제지만 많은 내용을 담고 있는 것 같다. 네트워킹에 대해서 깊게 공부를 해보고싶음(추후에 공부를 하게될 웹, 백엔드를 위해서)


## Output
- 생략




