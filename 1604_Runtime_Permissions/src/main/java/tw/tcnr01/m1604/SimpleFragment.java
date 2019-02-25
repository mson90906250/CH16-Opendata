package tw.tcnr01.m1604;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class SimpleFragment extends Fragment {

    private static final String TAG = "SimpleFragment";
    private static final int PERMISSION_REQUEST_CALL_LOG = 456;

    private TextView txtCallLog;

    public SimpleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_simple, container, false);
        txtCallLog = (TextView) rootView.findViewById(R.id.txt_call_log);
        Button btnCallLog = (Button) rootView.findViewById(R.id.btn_call_log);
        btnCallLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //顯示打電話的歷史檔
                showLastCallLog(getActivity());
            }
        });

        return rootView;
    }

    private void showLastCallLog(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermission();
        } else {
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC limit 1;");

            if (cursor != null) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);

                if (cursor.moveToNext()) {
                    String number = cursor.getString(numberIndex);
                    int type = cursor.getInt(typeIndex);
                    String date = cursor.getString(dateIndex);
                    String dayTime = new Date(Long.valueOf(date)).toString();
                    int typeDrawableResId = 0;
                    switch (type) {
                        case CallLog.Calls.OUTGOING_TYPE://打出去的
                            typeDrawableResId = android.R.drawable.sym_call_outgoing;
                            break;

                        case CallLog.Calls.INCOMING_TYPE://打進來的
                            typeDrawableResId = android.R.drawable.sym_call_incoming;
                            break;

                        case CallLog.Calls.MISSED_TYPE://未接
                            typeDrawableResId = android.R.drawable.sym_call_missed;
                            break;
                    }
                    txtCallLog.setCompoundDrawablesWithIntrinsicBounds(0, typeDrawableResId, 0, 0);
                    txtCallLog.setText(number + "\n" + dayTime);
                }
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CALL_LOG) {
            // 注意：這邊因為範例只有要求一個權限，所以可直接判斷grantResults[0]，若可能有多個權限，應該用個迴圈檢查
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showLastCallLog(getActivity());
            } else {
                // 如果權限是必須取得的，
                // 也可以像這樣在沒要到權限的時候，利用shouldShowRequestPermissionRationale來區分是否勾選了不再詢問
                // 如果使用者勾選了不再顯示
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
                    // 顯示提示對話框，當使用者按下按鈕時，開啟App設定頁（因為使用者已勾選不再詢問，所以request也沒用）
                    Util.showDialog(getContext(), R.string.dialog_msg_call_log, R.string.go_app_setting, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Util.openAppSetting(getContext());
                        }
                    });
                } else {
                    // 這裡僅顯示提示對話框
                    Util.showDialog(getContext(), R.string.dialog_msg_call_log, android.R.string.ok, null);
                }
            }
        }
    }

    /**
     * request需要的權限
     */
    private void requestNeededPermission() {
        // 用Fragment的requestPermissions
        requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSION_REQUEST_CALL_LOG);
    }

}
