package com.it_tech613.tvmulti.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.FirstServer;
import com.it_tech613.tvmulti.models.FullModel;
import com.it_tech613.tvmulti.ui.catchup.FragmentCatchupCategory;
import com.it_tech613.tvmulti.ui.home.FragmentHome;

import com.it_tech613.tvmulti.ui.liveTv.FragmentExoLiveTv;
import com.it_tech613.tvmulti.ui.liveTv.FragmentIjkLiveTv;
import com.it_tech613.tvmulti.ui.liveTv.FragmentLiveTv;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.it_tech613.tvmulti.models.SideMenu;
import com.it_tech613.tvmulti.ui.catchup.FragmentCatchupDetail;
import com.it_tech613.tvmulti.ui.movies.FragmentAllMovie;
import com.it_tech613.tvmulti.ui.movies.FragmentMovieDetail;
import com.it_tech613.tvmulti.ui.movies.FragmentMovies;
import com.it_tech613.tvmulti.ui.multi.FragmentMultiScreen;
import com.it_tech613.tvmulti.ui.multi.PinMultiScreenDlg;
import com.it_tech613.tvmulti.ui.series.FragmentEpisodes;
import com.it_tech613.tvmulti.ui.series.FragmentSeries;
import com.it_tech613.tvmulti.ui.series.FragmentSeriesHolder;
import com.it_tech613.tvmulti.ui.settings.FragmentSettings;
import com.it_tech613.tvmulti.ui.tvGuide.FragmentTvGuide;
import com.it_tech613.tvmulti.ui.series.FragmentSeasons;
import com.it_tech613.tvmulti.utils.MyFragment;
import com.it_tech613.tvmulti.vpn.fastconnect.core.OpenConnectManagementThread;
import com.it_tech613.tvmulti.vpn.fastconnect.core.OpenVpnService;
import com.it_tech613.tvmulti.vpn.fastconnect.core.VPNConnector;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public List<MyFragment> fragmentList;
    private VPNConnector mConn;
    private ConstraintLayout rootview;
    int select_player = 0;
    private int selected_page=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        RecyclerView menu_recyclerview = findViewById(R.id.menu_recyclerview);
        rootview = findViewById(R.id.rootview);
        menu_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentHome());
//        if (MyApp.firstServer == FirstServer.first)
            fragmentList.add(new FragmentLiveTv());
//        if (MyApp.firstServer == FirstServer.first)
            fragmentList.add(new FragmentMultiScreen());
        fragmentList.add(new FragmentMovies());
        fragmentList.add(new FragmentSeries());
//        if (MyApp.firstServer == FirstServer.first)
            fragmentList.add(new FragmentTvGuide());
//        if (MyApp.firstServer == FirstServer.first)
            fragmentList.add(new FragmentCatchupCategory());
        fragmentList.add(new FragmentSettings());

        fragmentList.add(new FragmentSeriesHolder());//7
        fragmentList.add(new FragmentSeasons());//8
        fragmentList.add(new FragmentAllMovie());//9
        fragmentList.add(new FragmentMovieDetail());//10
        fragmentList.add(new FragmentEpisodes());//11
        fragmentList.add(new FragmentCatchupDetail());//12


        getSupportFragmentManager().beginTransaction().add(R.id.container,fragmentList.get(0)).addToBackStack(null).commit();

        final List<SideMenu> list = new ArrayList<SideMenu>();
        list.add(new SideMenu("Home"));
//        if (MyApp.firstServer == FirstServer.first)
            list.add(new SideMenu("Live Tv"));
//        if (MyApp.firstServer == FirstServer.first)
            list.add(new SideMenu("Multi TV"));
        list.add(new SideMenu("Movies"));
        list.add(new SideMenu("Series"));
//        if (MyApp.firstServer == FirstServer.first)
            list.add(new SideMenu("Tv Guide"));
//        if (MyApp.firstServer == FirstServer.first)
            list.add(new SideMenu("Catchup"));
        list.add(new SideMenu("Settings"));
        list.add(new SideMenu("Switch Server"));

        menu_recyclerview.setAdapter(new MenuAdapter(list, this, (sideMenu, position) -> {
            if (sideMenu.getName().equals("Multi TV")){
                FullModel fullModel = Constants.getAllFullModel(MyApp.fullModels);
                if (fullModel!=null && fullModel.getChannels().size()==0){
                    Toast.makeText(this,"No Channels",Toast.LENGTH_LONG).show();
                    return null;
                }
                showScreenModeList();
            }else {
                if (sideMenu.getName().equals("Live TV")){
                    FullModel fullModel = Constants.getAllFullModel(MyApp.fullModels);
                    if (fullModel!=null && fullModel.getChannels().size()==0){
                        Toast.makeText(this,"No Channels",Toast.LENGTH_LONG).show();
                        return null;
                    }
                }else if (sideMenu.getName().equals("Movies")){
                    if (MyApp.movieModels!=null && MyApp.movieModels.size()==0){
                        Toast.makeText(this,"No Movies",Toast.LENGTH_LONG).show();
                        return null;
                    }
                }else if (sideMenu.getName().equals("Series")){
                    if (MyApp.seriesModels!=null && MyApp.seriesModels.size()==0){
                        Toast.makeText(this,"No Series",Toast.LENGTH_LONG).show();
                        return null;
                    }
                }else if (sideMenu.getName().equals("Switch Server")){
                    finish();
                    return null;
                }

                replaceFragment(fragmentList.get(position),position);
            }
            return null;
        }));

        Constants.getVodFilter();
        Constants.getLiveFilter();
        Constants.getSeriesFilter();

        if (MyApp.firstServer==FirstServer.first) {
            select_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
            switch (select_player) {
                case 0:
                    fragmentList.set(1, new FragmentLiveTv());
                    break;
                case 1:
                    fragmentList.set(1, new FragmentIjkLiveTv());
                    break;
                case 2:
                    fragmentList.set(1, new FragmentExoLiveTv());
                    break;
            }
        }
    }

    private int selected_item;

    private void showScreenModeList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select One Mode");

        String[] screen_mode_list = {"Four Way Screen", "Three Way Screen", "Dual Screen"};

        builder.setSingleChoiceItems(screen_mode_list, 0,
                (dialog, which) -> selected_item = which);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selected_item==0) {
                    boolean remember_four=false;
                    if (MyApp.instance.getPreference().get("remember_four_screen")!=null) remember_four=(boolean) MyApp.instance.getPreference().get("remember_four_screen");
                    if (!remember_four){
                        PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(MainActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                            @Override
                            public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                if(!pin_code.equals(Constants.GetPin4(MainActivity.this))) {
                                    Toast.makeText(MainActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                MyApp.instance.getPreference().put("remember_four_screen",is_remember);
                                MyApp.num_screen = 4;
                                replaceFragment(fragmentList.get(2),2);
                            }

                            @Override
                            public void OnCancelClick(Dialog dialog, String pin_code) {

                            }
                        },remember_four);
                        pinMultiScreenDlg.show();
                    }else {
                        MyApp.num_screen = 4;
                        replaceFragment(fragmentList.get(2),2);
                    }
                }
                else if (selected_item==1) {
                    boolean remember_three=false;
                    if (MyApp.instance.getPreference().get("remember_three_screen")!=null) remember_three=(boolean) MyApp.instance.getPreference().get("remember_three_screen");
                    if (!remember_three){
                        PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(MainActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                            @Override
                            public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                if(!pin_code.equals(Constants.GetPin3(MainActivity.this))) {
                                    Toast.makeText(MainActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                MyApp.instance.getPreference().put("remember_three_screen",is_remember);
                                MyApp.num_screen = 3;
                                replaceFragment(fragmentList.get(2),2);
                            }

                            @Override
                            public void OnCancelClick(Dialog dialog, String pin_code) {

                            }
                        },remember_three);
                        pinMultiScreenDlg.show();
                    }else {
                        MyApp.num_screen = 3;
                        replaceFragment(fragmentList.get(2),2);
                    }
                }
                else {
                    boolean remember_two=false;
                    if (MyApp.instance.getPreference().get("remember_two_screen")!=null) remember_two=(boolean) MyApp.instance.getPreference().get("remember_two_screen");
                    if (!remember_two){
                        PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(MainActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                            @Override
                            public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                                if(!pin_code.equals(Constants.GetPin2(MainActivity.this))) {
                                    Toast.makeText(MainActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                MyApp.instance.getPreference().put("remember_two_screen",is_remember);
                                MyApp.num_screen = 2;
                                replaceFragment(fragmentList.get(2),2);
                            }

                            @Override
                            public void OnCancelClick(Dialog dialog, String pin_code) {

                            }
                        },remember_two);
                        pinMultiScreenDlg.show();
                    }else {
                        MyApp.num_screen = 2;
                        replaceFragment(fragmentList.get(2),2);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void replaceFragment(Fragment fragment,int selected_page_id){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container,fragment)
                .addToBackStack(null)
                .commit();
        if (selected_page_id<8)
            selected_page=selected_page_id;
    }

    private boolean is_full = false;

    public boolean getIs_full(){
        return is_full;
    }
    public void toggleFullScreen(boolean full){
        if (is_full==full) return;
        if (full){
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootview);
            constraintSet.setGuidelinePercent(R.id.guideline4, 0.0f);
            constraintSet.applyTo(rootview);
        }else {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootview);
            constraintSet.setGuidelinePercent(R.id.guideline4, 0.15f);
            constraintSet.applyTo(rootview);
            ((RecyclerView)findViewById(R.id.menu_recyclerview)).scrollToPosition(selected_page);
        }
        is_full=full;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            Log.e("keycode_back","clicked on MainActivity");
            for (MyFragment myFragment : fragmentList)
                if (myFragment.isVisible()) {
                    if (!myFragment.myOnKeyDown(event)) return false;
                }
//            if (event.getKeyCode()==KeyEvent.KEYCODE_BACK){
//                String string, string1, string2;
//                if (MyApp.num_server==1){
//                    string = "DO YOU WISH TO EXIT APP?";
//                    string1 = "Yes";
//                    string2 = "No";
//                }else {
//                    string = "DO YOU WISH TO EXIT APP OR SWITCH SERVER?";
//                    string1 = "EXIT";
//                    string2 = "SWITCH SERVER";
//                }
//                ConnectionDlg connectionDlg = new ConnectionDlg(MainActivity.this, new ConnectionDlg.DialogConnectionListener() {
//                    @Override
//                    public void OnYesClick(Dialog dialog) {
//                        dialog.dismiss();
//                        stopVPN();
//                        finish();
//                    }
//
//                    @Override
//                    public void OnNoClick(Dialog dialog) {
//                        dialog.dismiss();
//                        if (MyApp.num_server!=1) {
//                            startActivity(new Intent(MainActivity.this, InitializeActivity.class));
//                            stopVPN();
//                            finish();
//                        }
//                    }
//                }, string,string1, string2);
//                connectionDlg.show();
//                return false;
//            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.instance.setKpHUD(this);
        if(MyApp.is_vpn){
            findViewById(R.id.ly_vpn).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.ly_vpn).setVisibility(View.GONE);
        }
        mConn = new VPNConnector(this, true) {
            @Override
            public void onUpdate(OpenVpnService service) {

            }
        };
    }

    private void stopVPN() {
        if (mConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(this);
        } else {
            mConn.service.stopVPN();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVPN();
    }
}
