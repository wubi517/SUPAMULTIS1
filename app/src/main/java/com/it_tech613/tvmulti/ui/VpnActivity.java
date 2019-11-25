package com.it_tech613.tvmulti.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.vpn.fastconnect.api.VpnConstant;
import com.it_tech613.tvmulti.vpn.fastconnect.core.OpenConnectManagementThread;
import com.it_tech613.tvmulti.vpn.fastconnect.core.OpenVpnService;
import com.it_tech613.tvmulti.vpn.fastconnect.core.VPNConnector;
import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ErrorReporter;

import java.util.UUID;

//import androidx.appcompat.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;

public class VpnActivity extends AppCompatActivity {
    public static final String EXTRA_UUID = ".UUID";

    private TextView mStatusText;
    private EditText mVpnAddress;
    private EditText mUserName;
    private EditText mPassword;
    private Button mConnectButton;

    private String mUUID;
    private VPNConnector mConn;
    private int mConnectionState;
    private boolean isConnected;
    private SharedPreferences mPrefs;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(getBaseContext(), OpenVpnService.class);
            intent.putExtra(OpenVpnService.EXTRA_UUID, mUUID);
            intent.putExtra(VpnConstant.address, mVpnAddress.getText().toString());
            intent.putExtra(VpnConstant.type, "SSL");
            intent.putExtra(VpnConstant.username, mUserName.getText().toString());
            intent.putExtra(VpnConstant.password, mPassword.getText().toString());
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mConn = new VPNConnector(this, true) {
            @Override
            public void onUpdate(OpenVpnService service) {
                updateUI(service);
            }
        };
    }

    @Override
    protected void onPause() {
        mConn.stopActiveDialog();
        mConn.unbind();
        super.onPause();
    }

    private void updateUI(OpenVpnService service) {
        int newState = service.getConnectionState();

        service.startActiveDialog(this);

        if (mConnectionState != newState) {
            if (newState == OpenConnectManagementThread.STATE_DISCONNECTED) {
                mStatusText.setText(R.string.connection_state_disconnected);
                mConnectButton.setText(R.string.connect);
            } else if (mConnectionState == OpenConnectManagementThread.STATE_DISCONNECTED) {
                mStatusText.setText(R.string.connection_state_connected);
                mConnectButton.setText(R.string.disconnect);
            }
            mConnectionState = newState;
        }

        switch (newState) {
            case OpenConnectManagementThread.STATE_CONNECTING:
                mStatusText.setText("Connecting...");
                mStatusText.setTextColor(Color.parseColor("#E02A1C"));
                mConnectButton.setText("CONNECTING...");
                return;
            case OpenConnectManagementThread.STATE_CONNECTED:
                MyApp.is_vpn = true;
                mStatusText.setText("Connected");
                mStatusText.setTextColor(Color.parseColor("#8ECC50"));
                mConnectButton.setText("DISCONNECT");
                if (this.isConnected == false) {
                    this.isConnected = true;
                    Toast.makeText(this, "Connection Succeeded", Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case OpenConnectManagementThread.STATE_DISCONNECTED:
                MyApp.is_vpn = false;
                mStatusText.setText("Disconnected");
                mStatusText.setTextColor(Color.parseColor("#FFFFFF"));
                mConnectButton.setText("CONNECT");
                this.isConnected = false;
                return;
            case OpenConnectManagementThread.STATE_FAILED:
                MyApp.is_vpn = false;
                mStatusText.setText("Failed to connect");
                return;
            default:
                MyApp.is_vpn = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mStatusText = findViewById(R.id.tv_status);
        mVpnAddress = findViewById(R.id.address);
        mUserName = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mConnectButton = findViewById(R.id.btn_connect);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mPassword.setLongClickable(false);
        mUserName.requestFocus();
        initData();
//        mVpnAddress.setText("195.123.208.105");
//        mUserName.setText("slyx6mvpn254");
//        mPassword.setText("1865");

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectButton.getText().equals("CONNECT")) {
                    if(!TextUtils.isEmpty(mVpnAddress.getText()) && !TextUtils.isEmpty(mUserName.getText()) && !TextUtils.isEmpty(mPassword.getText())){
                        mUUID = UUID.randomUUID().toString();
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString("service_mUUID", mUUID);
                        editor.putString(VpnConstant.address, mVpnAddress.getText().toString());
                        editor.putString(VpnConstant.username, mUserName.getText().toString());
                        editor.putString(VpnConstant.password, mPassword.getText().toString());
                        editor.apply();
                        startVPN();
                    }
                } else {
                    stopVPN();
                }
            }
        });
    }

    private void initData() {
//        mVpnAddress.setText(mPrefs.getString(VpnConstant.address, ""));
        mVpnAddress.setText(Constants.GetVPN_IP(this));
        mUserName.setText(mPrefs.getString(VpnConstant.username, ""));
        mPassword.setText(mPrefs.getString(VpnConstant.password, ""));
    }

    private void stopVPN() {
        if (mConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(this);
        } else {
            mConn.service.stopVPN();
        }
    }

    private void startVPN() {
        Intent prepIntent;
        try {
            prepIntent = VpnService.prepare(this);
        } catch (Exception e) {
            reportBadRom(e);
            finish();
            return;
        }

        if (prepIntent != null) {
            try {
                startActivityForResult(prepIntent, 0);
            } catch (Exception e) {
                reportBadRom(e);
                finish();
            }
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    private void reportBadRom(Exception e) {
        ACRAConfiguration cfg = ACRA.getConfig();
        cfg.setResDialogText(R.string.bad_rom_text);
        cfg.setResDialogCommentPrompt(R.string.bad_rom_comment_prompt);
        ACRA.setConfig(cfg);

        ErrorReporter er = ACRA.getErrorReporter();
        er.putCustomData("cause", "reportBadRom");
        er.handleException(e);
    }
}
