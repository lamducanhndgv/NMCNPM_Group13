package group13.ntphat.evernote.ui.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import group13.ntphat.evernote.Model.USER;
import group13.ntphat.evernote.R;

public class SettingActivity extends AppCompatActivity {
    private BroadcastReceiver killReceiver;

    private void addKillReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CLOSE_ALL");
        killReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SettingActivity.this.finish();
            }
        };
        registerReceiver(killReceiver, intentFilter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("Cài đặt");
        addKillReceiver();

        TextView fullname = (TextView) findViewById(R.id.fullname);
        TextView email = (TextView) findViewById(R.id.email);
        fullname.setText(USER.getInstance().getFullName());
        email.setText(USER.getInstance().getUserEmail());

        View btn = findViewById(R.id.btn_account_info);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AccountInfoActivity.class);
                SettingActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(this.killReceiver);
        super.onDestroy();
    }
}
