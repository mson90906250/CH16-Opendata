package tw.tcnr01.m1603;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class M1603 extends Activity {
    private ListView list001;
    private TextView time;
    private String TAG = "tcnr01=>";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1603);
        setupViewComponent();
    }

    private void setupViewComponent() {
        // 動態調整高度 抓取使用裝置尺寸
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newscrollheight = displayMetrics.heightPixels * 75 / 100; // 設定ScrollView使用尺寸的3/4
        //
        list001 = (ListView) findViewById(R.id.listView1);
        list001.getLayoutParams().height = newscrollheight;
        list001.setLayoutParams(list001.getLayoutParams()); // 重定ScrollView大小
        time = (TextView) findViewById(R.id.time);
        // 解析 json
        try {
            String a = new TransTask().execute("http://data.coa.gov.tw/Service/OpenData/FromM/PoultryTransBoiledChickenData.aspx").get();
            List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
//            List<String> list = new ArrayList<String>();
            // 解析 json
            JSONArray jsonArray = new JSONArray(a);
            JSONObject jsonData2 = new JSONObject();

            for (int j = 0; j < jsonArray.length(); j++) {
                 jsonData2 = jsonArray.getJSONObject(j);
                Log.d(TAG,""+jsonData2);
                String eggprice1 = jsonData2.getString("日期");//日期
                Log.d(TAG,""+eggprice1);
                String eggprice2 = jsonData2.getString("農曆");//農曆
                String eggprice3 = jsonData2.getString("雞蛋(產地)");//蛋價


                Map<String, Object> item = new HashMap<String, Object>();

                item.put("t001",
                        "\n蛋價資訊：" +
                                "\n日期：" + eggprice1 +
                                "\n農曆：" + eggprice2 +
                                "\n蛋價：" + eggprice3 );
                mList.add(item);
            }
            String datetime = jsonArray.getJSONObject(0).getString("日期");
            time.setText("更新時間：" + datetime);
            SimpleAdapter adapter1 = new SimpleAdapter(this, mList, R.layout.list, new String[]{"t001"}, new int[]{R.id.t001});
            list001.setAdapter(adapter1);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

