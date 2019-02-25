package tw.tcnr01.m1604;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class M1604 extends AppCompatActivity  implements View.OnClickListener{

    private static final int PERMISSION_REQUEST_CALL = 123;//數字為自定義
    private EditText editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1604);

        editPhone = (EditText) findViewById(R.id.edit_phone);
        Button btnCall = (Button) findViewById(R.id.btn_call);
        btnCall.setOnClickListener(this);

        // Add A fragment to Activity
        SimpleFragment fragment = new SimpleFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CALL) {
            // 注意：這邊因為範例只有要求一個權限，所以可直接判斷grantResults[0]，若可能有多個權限，應該用個迴圈檢查
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeACall();
            } else {
                // 沒有取得權限的處理，這邊是跳對話框，按下按鈕開啟App設定頁
                Util.showDialog(this, R.string.dialog_msg_call, R.string.go_app_setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Util.openAppSetting(M1604.this);
                    }
                });
            }
        }

    }

    @Override
    public void onClick(View v) {
        makeACall();
    }

    private void makeACall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // 這邊是照官網說法，在確認沒有權限的時候，確認是否需要說明原因
            // 需要的話就先顯示原因，在使用者看過原因後，再request權限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                Util.showDialog(this, R.string.dialog_msg_call, android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestNeededPermission();
                    }
                });
            } else {
                // 否則就直接request
                requestNeededPermission();
            }
        } else {
            // 用intent打電話
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + editPhone.getText()));
            startActivity(intent);
        }

    }

    /*** request需要的權限      */
    private void requestNeededPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE},
                PERMISSION_REQUEST_CALL);
    }


}
