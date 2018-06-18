package kr.co.hoon.a180526news;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText keyword;

    // ListView 관련 변수
    ListView listView;
    ArrayList<Article> data;
    ArrayAdapter<Article> adapter;

    // ListView를 재출력하는 핸들러
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // ListView의 재출력
            adapter.notifyDataSetChanged();
        }
    };

    // 기사 검색을 수행하는 스레드
    class SearchThread extends Thread {
        @Override
        public void run() {
//            Log.e("스레드 진입","!");
            // 파싱할 문자열 초기화
//            String html = "";

            // 다운로드 받기
//            try {
//                // 주소 만들기
//                String addr = "http://www.yonhapnews.co.kr/home09/7091000000.html?ctype=A&query=";
//                addr += URLEncoder.encode(keyword.getText().toString(),"UTF-8");
////                Log.e("URL",addr);
//                // URL 객체
//                URL url = new URL(addr);
//                // URL 연결
//                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//                // 옵션설정
//                conn.setUseCaches(false);
//                conn.setConnectTimeout(30000);
//
//                // 문자열을 읽을 수 있는 스트림 - 모아서(Buffered) 처리하면 입출력 횟수를 줄임
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                StringBuilder sb = new StringBuilder();
//                // 문자열을 다운로드 받아서 저장
//                while(true){
//                    String line = br.readLine();
//                    if(line==null) break;
////                    Log.e("line", line);
//                    sb.append(line);
//                }
//                // 다 읽었으면 연결 해제
//                br.close();
//                conn.disconnect();
//
//                Log.e("sb",sb.toString());
//                // 문자열로 변환
//                html = sb.toString();
//                Log.e("html",html);
//
//            }catch(Exception e){
//                Log.e("다운로드 예외", e.getMessage());
//            }

            // html 파싱
            try{
                // 주소 만들기
                String addr = "http://news.donga.com/search?check_news=1&more=1&sorting=1&range=1&search_date=&query=";
                addr += URLEncoder.encode(keyword.getText().toString(),"UTF-8");
                // html 문자열을 메모리에 트리형태로 펼치기
                Document doc = Jsoup.connect(addr).get();
                // 원하는 데이터만 가져오기
                Elements tags = doc.select("div.t > p.tit > a");
                // 순회하기 전에 리스트를 초기화 - 여러번 다운로드 받으면 데이터가 쌓이기 때문에
                data.clear();
                // 데이터 가져오기
                for(Element element: tags){
                    Article article = new Article();
                    if(element.text().trim().length() != 0) {
                        article.setTitle(element.text().trim());
                        article.setLink(element.attr("href"));
                        data.add(article);
                    }
                }
                // 핸들러에게 메시지 전송
                handler.sendEmptyMessage(0);
            }catch(Exception e){
                Log.e("HTML 파싱 예외", e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 검색어 찾아오기
        keyword = (EditText)findViewById(R.id.keyword);

        // 리스트뷰 출력
        data = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setDivider(new ColorDrawable(Color.parseColor("#ddddbb")));
        listView.setDividerHeight(3);

        findViewById(R.id.btn).setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                SearchThread th = new SearchThread();
                th.start();
            }
        });

        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 클릭한 데이터의 link 가져오기
                String link = data.get(position).getLink();
                // 가져온 링크로 이동하는 인텐트
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                // 액티비티 출력
                startActivity(intent);
            }
        });
    }
}
