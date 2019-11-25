package com.it_tech613.tvmulti.ui.settings;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.CategoryModel;
import com.it_tech613.tvmulti.models.CategoryType;
import com.it_tech613.tvmulti.models.FirstServer;
import com.it_tech613.tvmulti.models.Settings;
import com.it_tech613.tvmulti.ui.LoginActivity;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.VpnActivity;
import com.it_tech613.tvmulti.ui.liveTv.FragmentExoLiveTv;
import com.it_tech613.tvmulti.ui.liveTv.FragmentIjkLiveTv;
import com.it_tech613.tvmulti.ui.liveTv.FragmentLiveTv;
import com.it_tech613.tvmulti.utils.MyFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentSettings extends MyFragment {

    private String[] category_names;
    private int[] category_ids;
    private boolean[] checkedItems;
    private List<Integer> selectedIds= new ArrayList<>();
    private int current_position = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        current_position = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());

        RecyclerView settings_recyclerview = view.findViewById(R.id.settings_recyclerview);
        settings_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Settings> list = new ArrayList<>();
        list.add(new Settings("Parental Control","action_parental_control"));
        list.add(new Settings("Sorting Method","action_sorting"));
        list.add(new Settings("Select Players","action_external_player"));
        list.add(new Settings("Reload Portal","action_reload"));
//        list.add(new Settings("Reboot Device","action_reboot"));
        list.add(new Settings("Hide Live Categories","action_hide_live"));
        list.add(new Settings("Hide Vod Categories","action_hide_vod"));
        list.add(new Settings("Hide Series Categories","action_hide_series"));
        list.add(new Settings("User Account","action_account"));
//        list.add(new Settings("VPN","action_vpn"));
        list.add(new Settings("Logout","action_logout"));
        settings_recyclerview.setAdapter(new SettingsAdapter(list, (position, settings) -> {
            switch (settings.getAction()){
                case "action_parental_control"://Parental Control
                    ParentContrlDlg dlg = new ParentContrlDlg(requireContext(), new ParentContrlDlg.DialogUpdateListener() {
                        @Override
                        public void OnUpdateNowClick(Dialog dialog, int code) {
                            if(code==1){
                                dialog.dismiss();
                            }
                        }
                        @Override
                        public void OnUpdateSkipClick(Dialog dialog, int code) {
                            dialog.dismiss();
                        }
                    });
                    dlg.show();
                    break;
                case "action_sorting":
                    showSortingDlg();
                    break;
                case "action_external_player"://External Players
                    showInternalPlayers();
                    break;
                case "action_reload"://Reload Portal
                    ReloadDlg reloadDlg = new ReloadDlg(requireContext(), new ReloadDlg.DialogUpdateListener() {
                        @Override
                        public void OnUpdateNowClick(Dialog dialog) {
                            dialog.dismiss();
                            startActivity(new Intent(requireContext(),LoginActivity.class));
                            requireActivity().finish();
                        }

                        @Override
                        public void OnUpdateSkipClick(Dialog dialog) {
                            dialog.dismiss();
                        }
                    });
                    reloadDlg.show();
                    break;
                case "action_reboot"://Reboot Device
                    try {
                        Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","reboot now"});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "action_hide_live"://Hide Live Categories
                    showMultiSelection(CategoryType.live);
                    break;
                case "action_hide_vod"://Hide Vod Categories
                    showMultiSelection(CategoryType.vod);
                    break;
                case "action_hide_series"://Hide Series Categories
                    showMultiSelection(CategoryType.series);
                    break;
                case "action_account"://User Account
                    AccountDlg accountDlg = new AccountDlg(requireContext(), dialog -> {
                    });
                    accountDlg.show();
                    break;
                case "action_vpn"://VPN
                    startActivity(new Intent(requireContext(), VpnActivity.class));
                    break;
                case "action_logout"://Logout
                    MyApp.instance.getPreference().remove(Constants.getLoginInfo());
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                    break;
            }
        }));
    }

    private void showInternalPlayers(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Player Option");
        String[] screen_mode_list = {"VLC Player", "IJK Player", "Exo Player"};
        if (MyApp.instance.getPreference().get(Constants.getCurrentPlayer())!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
        else current_position = 0;
        builder.setSingleChoiceItems(screen_mode_list, current_position,
                (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (MyApp.firstServer== FirstServer.first) {
                switch (current_position) {
                    case 0:
                        ((MainActivity) requireActivity()).fragmentList.set(1, new FragmentLiveTv());
                        break;
                    case 1:
                        ((MainActivity) requireActivity()).fragmentList.set(1, new FragmentIjkLiveTv());
                        break;
                    case 2:
                        ((MainActivity) requireActivity()).fragmentList.set(1, new FragmentExoLiveTv());
                        break;
                }
            }
            MyApp.instance.getPreference().put(Constants.getCurrentPlayer(), current_position);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showSortingDlg(){
        if (MyApp.instance.getPreference().get(Constants.getCurrentSorting())!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.getCurrentSorting());
        else current_position = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Sorting Method");
        String[] screen_mode_list = {"Sort by Last Added", "Sort by Top to Bottom", "Sort by Bottom to Top"};
        builder.setSingleChoiceItems(screen_mode_list, current_position, (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) ->
                MyApp.instance.getPreference().put(Constants.getCurrentSorting(), current_position));
        builder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    
    private void showMultiSelection(final CategoryType categoryType) {
        int i_live=2,i_vod=3,i_series=3;
        switch (categoryType){
            case vod:
                if (MyApp.instance.getPreference().get(Constants.getINVISIBLE_VOD_CATEGORIES())!=null)
                    selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_VOD_CATEGORIES());
                category_names=new String[MyApp.vod_categories.size()-i_vod];
                category_ids=new int[MyApp.vod_categories.size()-i_vod];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.vod_categories.size()-i_vod; i++){
                    CategoryModel categoryModel= MyApp.vod_categories.get(i+i_vod);
                    category_names[i]=categoryModel.getName();
                    category_ids[i]=categoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModel.getId());
                }
                break;
            case live:
                if (MyApp.instance.getPreference().get(Constants.getINVISIBLE_LIVE_CATEGORIES())!=null)
                    selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_LIVE_CATEGORIES());
                category_names=new String[MyApp.live_categories.size()-i_live];
                category_ids=new int[MyApp.live_categories.size()-i_live];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.live_categories.size()-i_live; i++){
                    CategoryModel CategoryModel= MyApp.live_categories.get(i+i_live);
                    category_names[i]=CategoryModel.getName();
                    category_ids[i]=CategoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(CategoryModel.getId());
                }
                break;
            case series:
                if (MyApp.instance.getPreference().get(Constants.getINVISIBLE_SERIES_CATEGORIES())!=null)
                    selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_SERIES_CATEGORIES());
                category_names=new String[MyApp.series_categories.size()-i_series];
                category_ids=new int[MyApp.series_categories.size()-i_series];
                checkedItems=new boolean[category_names.length];
                for (int i = 0; i< MyApp.series_categories.size()-i_series; i++){
                    CategoryModel CategoryModel= MyApp.series_categories.get(i+i_series);
                    category_names[i]=CategoryModel.getName();
                    category_ids[i]=CategoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(CategoryModel.getId());
                }
                break;
        }
        HideCategoryDlg dlg=new HideCategoryDlg(requireContext(), category_names, checkedItems, new HideCategoryDlg.DialogSearchListener() {
            @Override
            public void OnItemClick(Dialog dialog, int position, boolean checked) {
                if (!checked){
                    if (!selectedIds.contains(category_ids[position])){
                        selectedIds.add(category_ids[position]);
                    }
                }else {
                    if (selectedIds.contains(category_ids[position])){
                        selectedIds.removeAll(Arrays.asList(category_ids[position]));
                    }
                }
            }

            @Override
            public void OnOkClick(Dialog dialog) {
                selectedIds=new ArrayList<>();
                for (int m=0;m<checkedItems.length;m++){
                    if (!checkedItems[m]) selectedIds.add(category_ids[m]);
                }
                switch (categoryType){
                    case series:
                        MyApp.instance.getPreference().put(Constants.getINVISIBLE_SERIES_CATEGORIES(),selectedIds);
                        Constants.getSeriesFilter();
                        break;
                    case live:
                        MyApp.instance.getPreference().put(Constants.getINVISIBLE_LIVE_CATEGORIES(),selectedIds);
                        Constants.getLiveFilter();
                        break;
                    case vod:
                        MyApp.instance.getPreference().put(Constants.getINVISIBLE_VOD_CATEGORIES(),selectedIds);
                        Constants.getVodFilter();
                        break;
                }
            }

            @Override
            public void OnCancelClick(Dialog dialog) {

            }

            @Override
            public void OnSelectAllClick(Dialog dialog) {
                for(int i=0;i<checkedItems.length;i++){
                    checkedItems[i]=true;
                    selectedIds.clear();
                }
            }
        });
        dlg.show();
    }
    
    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        if (event.getAction()==KeyEvent.ACTION_UP){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(0))//FragmentCatchupDetail
                            .addToBackStack(null).commit();
                    return false;
            }
        }
        return super.myOnKeyDown(event);
    }
}
