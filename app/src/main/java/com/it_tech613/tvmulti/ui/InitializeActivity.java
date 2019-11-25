package com.it_tech613.tvmulti.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.it_tech613.tvmulti.BuildConfig;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.apps.MyVideoView;
import com.it_tech613.tvmulti.models.DataModel;
import com.it_tech613.tvmulti.models.FirstServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InitializeActivity extends Activity implements View.OnClickListener {

    SharedPreferences serveripdetails;
    String version,app_Url;
    private MyVideoView videoView;
    private boolean is_video_played = false;
    int num_server = 3;
    static {
        System.loadLibrary("notifications");
    }
    ImageButton icon1,icon2,icon3;

    public native String get1();
    public native String getTwo();
    public native String getThree();
    public native String getFour();
    public native String get2();
    public native String get3();
    public native String get4();
    public native String getUrl1();
    public native String getUrl2();
    public native String getUrl3();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (num_server==1) setContentView(R.layout.activity_initialize);
        else setContentView(R.layout.activity_select_server);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        serveripdetails = this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            SharedPreferences.Editor server_editor = serveripdetails.edit();
            server_editor.putString("url", new String (Base64.decode(new String (Base64.decode(getTwo(), Base64.DEFAULT)), Base64.DEFAULT)));
//            Log.e("url",new String (Base64.decode(new String (Base64.decode(getTwo(), Base64.DEFAULT)), Base64.DEFAULT)));
            server_editor.putString("key1",new String (Base64.decode(get1(), Base64.DEFAULT)));
            server_editor.putString("key2",new String (Base64.decode(get2(), Base64.DEFAULT)));
            server_editor.putString("key3",new String (Base64.decode(get3(), Base64.DEFAULT)));
            server_editor.putString("key4",new String (Base64.decode(get4(), Base64.DEFAULT)));
//            Log.e("key",new String (Base64.decode(get1(), Base64.DEFAULT)));
            server_editor.apply();
        }catch (Exception e){
            Toast.makeText(InitializeActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
        }

        try {
            SharedPreferences.Editor server_editor = serveripdetails.edit();
            server_editor.putString("autho1",new String (Base64.decode(new String(Base64.decode(getFour().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
//            Log.e("autho1",new String (Base64.decode(new String(Base64.decode(getFour().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
            server_editor.putString("autho2",new String (Base64.decode(new String(Base64.decode(getThree().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
//            Log.e("autho2",new String (Base64.decode(new String(Base64.decode(getThree().substring(12),Base64.DEFAULT)).substring(12),Base64.DEFAULT)));
            server_editor.apply();
        }catch (Exception e){
            Toast.makeText(InitializeActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
        }

        videoView = findViewById(R.id.video_view);
        videoView.setVisibility(View.GONE);
        is_video_played=true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            CheckSDK23Permission();
        } else if(num_server==1){
            getRespond();
        }
//        if (is_video_played){
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//                CheckSDK23Permission();
//            } else if(num_server==1){
//                getRespond();
//            }
//        }
//        else {
//            videoView.setVisibility(View.VISIBLE);
//            videoView.setVideoSize(MyApp.SCREEN_WIDTH,MyApp.SCREEN_HEIGHT);
//            String path = "android.resource://" + getPackageName() + "/" + R.raw.video;
//            videoView.setVideoURI(Uri.parse(path));
//            videoView.start();
//            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    videoView.setVisibility(View.GONE);
//                    is_video_played=true;
//                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//                        CheckSDK23Permission();
//                    } else if(num_server==1){
//                        getRespond();
//                    }
//                }
//            });
//        }
        if (num_server!=1) {
            icon1 = findViewById(R.id.icon1);
            icon2 = findViewById(R.id.icon2);
            icon3 = findViewById(R.id.icon3);
            icon1.setOnClickListener(this);
            icon2.setOnClickListener(this);
            icon3.setOnClickListener(this);
            Glide.with(this).load(new String(Base64.decode(getUrl1(),Base64.DEFAULT)))
                    .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon).signature(new ObjectKey("myKey2")))
                    .into(icon1);
            Glide.with(this).load(new String(Base64.decode(getUrl2(),Base64.DEFAULT)))
                    .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon).signature(new ObjectKey("myKey3")))
                    .into(icon2);
            Glide.with(this).load(new String(Base64.decode(getUrl3(),Base64.DEFAULT)))
                    .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon).signature(new ObjectKey("myKey4")))
                    .into(icon3);
        }
    }

    private void getRespond(){
        try {
            String response = MyApp.instance.getIptvclient().login(Constants.GetKey(this));
            Log.e("response",response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    JSONObject data = object.getJSONObject("data");
                    Gson gson = new Gson();
                    Constants.userDataModel = gson.fromJson(data.toString(),DataModel.class);
                    if(MyApp.instance.getPreference().get(Constants.getPIN_CODE())==null){
                        MyApp.instance.getPreference().put(Constants.getPIN_CODE(),"0000");
                    }
                    if(MyApp.instance.getPreference().get(Constants.getOSD_TIME())==null){
                        MyApp.instance.getPreference().put(Constants.getOSD_TIME(),7);
                    }

                    if (MyApp.instance.getPreference().get(Constants.getDIRECT_VPN_PIN_CODE())==null){
                        MyApp.instance.getPreference().put(Constants.getDIRECT_VPN_PIN_CODE(),"8888");
                    }

                    if(MyApp.instance.getPreference().get(Constants.getCurrentPlayer())==null){
                        MyApp.instance.getPreference().put(Constants.getCurrentPlayer(),0);
                    }
//                            getUpdate();
                    MyApp.instance.loadVersion();
                    getStart();

                } else {
                    Toast.makeText(InitializeActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckSDK23Permission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("READ / WRITE SD CARD");
        if (!addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE))
            permissionsNeeded.add("READPHONE");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    124);
        } else if (num_server==1){
            getRespond();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return shouldShowRequestPermissionRationale(permission);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("PermissionsResult", "onRequestPermissionsResult");
        if (num_server==1)getRespond();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getUpdate(){
        MyApp.instance.versionCheck();
        double code = 0.0;
        try {
            code = Double.parseDouble(version);
        }catch (Exception e){
            code = 0.0;
        }
        MyApp.instance.loadVersion();
        double app_vs = Double.parseDouble(MyApp.version_name);
        if (code > app_vs) {
            UpdateDlg updateDlg = new UpdateDlg(this, new UpdateDlg.DialogUpdateListener() {
                @Override
                public void OnUpdateNowClick(Dialog dialog) {
                    dialog.dismiss();
                    new versionUpdate().execute(app_Url);
                }
                @Override
                public void OnUpdateSkipClick(Dialog dialog) {
                    dialog.dismiss();
                    getStart();
                }
            });
            updateDlg.show();
        }else {
            getStart();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.icon1:
                MyApp.firstServer = FirstServer.first;
                break;
            case R.id.icon2:
                MyApp.firstServer = FirstServer.second;
                break;
            case R.id.icon3:
                MyApp.firstServer = FirstServer.third;
                break;
        }
        getRespond();
    }

    @SuppressLint("StaticFieldLeak")
    class versionUpdate extends AsyncTask<String, Integer, String> {
        ProgressDialog mProgressDialog;
        File file;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(InitializeActivity.this);
            mProgressDialog.setMessage(getResources().getString(R.string.request_download));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                String destination = Environment.getExternalStorageDirectory() + "/";
                String fileName = "supanewui.apk";
                destination += fileName;
                final Uri uri = Uri.parse("file://" + destination);
                file = new File(destination);
                if(file.exists()){
                    file.delete();
                }
                output = new FileOutputStream(file, false);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0)
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getApplicationContext(),"Update Failed",Toast.LENGTH_LONG).show();
                getStart();
            } else
                startInstall(file);
        }
    }

    private void getStart() {
        startActivity(new Intent(InitializeActivity.this, LoginActivity.class));
        if (num_server==1) finish();
    }

    private void startInstall(File fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(InitializeActivity.this, BuildConfig.APPLICATION_ID + ".provider",fileName), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}
