package tw.tcnr01.m1601;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class M1601 extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1601);
        setupViewComponent();
    }

    private void setupViewComponent() {
        // 動態調整高度 抓取使用裝置尺寸
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newscrollheight = displayMetrics.heightPixels * 75 / 100; // 設定ScrollView使用尺寸的4/5
        //
        listView = (ListView) findViewById(R.id.listView1);
        listView.getLayoutParams().height = newscrollheight;
        listView.setLayoutParams(listView.getLayoutParams()); // 重定ScrollView大小

        // XML直接網路下載，網路操作一定要在新的執行序
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // XML讀取完會得到一個ArrayList
                    final ArrayList<HashMap<String, Object>> arrayList = parse();
                    // LISTVIEW是繼承VIEW的，所以相關操作一定要在原來的UI主執行序
                    // 可以呼叫ACTIVITY下的runOnUiThread方法便可以
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), arrayList, R.layout.list,
                                    new String[] { "County","SiteName", "PM2.5","PublishTime"}, new int[] { R.id.t001, R.id.t002,R.id.t003,R.id.t004 });
                            listView.setAdapter(adapter);
                        }
                    });
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //
    public InputStream getUrlData(String url) throws URISyntaxException, ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(new URI(url));
        HttpResponse res = client.execute(method);
        return res.getEntity().getContent();
    }

    // 解析空氣品質的OPEN DATA返回一個ArrayList集合
    public ArrayList<HashMap<String, Object>> parse() throws URISyntaxException {
        String tagName = null;
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        // 記錄出現次數
        int findCount = 0;
        try {
            // XmlPullParserFactory
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            // XmlPullParser
            XmlPullParser parser = factory.newPullParser();
            // 獲取xml輸入數據 http://opendata.epa.gov.tw/ws/Data/AQI/?$format=xml
            parser.setInput(new InputStreamReader(getUrlData("http://opendata.epa.gov.tw/ws/Data/AQI/?$format=xml"))); //2018/01/15
            // 開始解析事件
            int eventType = parser.getEventType();
            // 處理事件，不碰到文檔結束就一直處理
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // 因為XmlPullParser預先定義了一堆靜態常量，所以這裡可以用switch
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        // 給當前標籤起個名字
                        tagName = parser.getName();
                        // 看到感興趣的標籤個計數
                        if (findCount == 0 && tagName.equals("Data")) {
                            findCount++;
                        }
                        break;

                    case XmlPullParser.TEXT:
//					<Data>
//					<SiteName>基隆</SiteName>
//					<County>基隆市</County>
//					<PSI>39</PSI>
//					<Pollutant>臭氧</Pollutant>
//					<Status>良好</Status>
//					<SO2>1</SO2>
//					<CO>.36</CO>
//					<O3>47</O3>
//					<PM10>30</PM10>
//					<PM2.5>15</PM2.5>
//					<NO2>27</NO2>
//					<WindSpeed>.75</WindSpeed>
//					<WindDirec>263.94</WindDirec>
//					<PublishTime>2014-01-27 21:00</PublishTime>
//					</Data>
                        if (tagName.equals("SiteName") && hashMap.containsKey("SiteName") == false) {
                            hashMap.put("SiteName", parser.getText());	}
                        if (tagName.equals("County") && hashMap.containsKey("County") == false) {
                            hashMap.put("County", parser.getText());	}
                        if (tagName.equals("PM2.5") && hashMap.containsKey("PM2.5") == false) {
                            hashMap.put("PM2.5", parser.getText());	}
                        if (tagName.equals("PublishTime") && hashMap.containsKey("PublishTime") == false) {
                            hashMap.put("PublishTime", parser.getText());	}
                        break;

                    case XmlPullParser.END_TAG:
                        // 嘗試取得當前標籤名稱，若是Data才可以增加到arrayList，並且重置
                        String trytagName = parser.getName();
                        if (trytagName.equals("Data")) {
                            tagName = parser.getName();
                            findCount = 0;
                            arrayList.add(hashMap);
                            hashMap = new HashMap<String, Object>();
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                }
                // 別忘了一定要用next方法處理下一個事件，忘了的結果就成無窮環圈#_#
                eventType = parser.next();
            }
            return arrayList;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }


}
