package com.it_tech613.tvmulti.ui.home;

import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.it_tech613.tvmulti.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.ui.LiveExoPlayActivity;
import com.it_tech613.tvmulti.ui.LiveIjkPlayActivity;
import com.it_tech613.tvmulti.ui.LivePlayActivity;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.liveTv.PinDlg;
import com.it_tech613.tvmulti.ui.series.FragmentSeriesHolder;
import com.it_tech613.tvmulti.utils.MyFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends MyFragment {

    private SliderLayout mDemoSlider;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDemoSlider = view.findViewById(R.id.slider_viewpager);
        RecyclerView category_recyclerview = view.findViewById(R.id.category_recyclerview);

        category_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        category_recyclerview.setAdapter(new HomeListAdapter(requireContext(), (categoryPos, itemPos, epgChannel, movieModel, seriesModel) -> {
            switch (categoryPos){
                case 0:
                    if (epgChannel.getCategory_id()==Constants.xxx_category_id){
                        PinDlg pinDlg = new PinDlg(requireContext(), new PinDlg.DlgPinListener() {
                            @Override
                            public void OnYesClick(Dialog dialog, String pin_code) {
                                dialog.dismiss();
                                String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                                if(pin_code.equalsIgnoreCase(pin)){
                                    dialog.dismiss();
                                    playVideo(epgChannel);
                                }else {
                                    dialog.dismiss();
                                    Toast.makeText(requireContext(), "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void OnCancelClick(Dialog dialog, String pin_code) {
                                dialog.dismiss();
                            }
                        });
                        pinDlg.show();
                    }else playVideo(epgChannel);

                    break;
                case 1:
                    MyApp.subMovieModels = new ArrayList<>();
                    MyApp.subMovieModels.add(movieModel);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(((MainActivity)requireActivity()).fragmentList.size()-3))//FragmentMovieDetail
                            .addToBackStack(null).commit();
                    break;
                case 2:
                    List<SeriesModel> seriesModels = new ArrayList<>();
                    seriesModels.add(seriesModel);
                    MyApp.selectedSeriesModelList = seriesModels;
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,new FragmentSeriesHolder())//FragmentSeasons
                            .addToBackStack(null).commit();
                    break;
            }
            return null;
        }));
    }

    private void playVideo(EPGChannel epgChannel) {
        String url = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user,MyApp.pass,String.valueOf(epgChannel.getStream_id()),"ts");
        Log.e("Iptvclient",url);
        int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
        Intent intent;
        switch (current_player){
            case 1:
                intent = new Intent(requireContext(), LiveIjkPlayActivity.class);
                break;
            case 2:
                intent = new Intent(requireContext(), LiveExoPlayActivity.class);
                break;
            default:
                intent = new Intent(requireContext(), LivePlayActivity.class);
                break;
        }
        intent.putExtra("title",epgChannel.getName());
        intent.putExtra("img",epgChannel.getImageURL());
        intent.putExtra("url",url);
        intent.putExtra("stream_id",epgChannel.getStream_id());
        intent.putExtra("is_live",true);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("FragmentHome","onResume");
        final List<String> slideModels = getSlides();
        mDemoSlider.removeAllSliders();
//        deleteCache(requireContext());
        for(String url : slideModels){
            DefaultSliderView textSliderView = new DefaultSliderView (requireContext());
            // initialize a SliderLayout
            textSliderView
//                    .description(name)
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

            //add your extra information
            //            textSliderView.bundle(new Bundle());
            //            textSliderView.getBundle()
            //                    .putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        //        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(slideModels.size()*Constants.GetSlideTime(requireContext()));
    }

    private List<String> getSlides() {
        List<String> list = new ArrayList<>();
        Picasso.with(requireContext()).invalidate(Constants.GetAd1(requireContext()));
        list.add(Constants.GetAd1(requireContext()));
        Picasso.with(requireContext()).invalidate(Constants.GetAd2(requireContext()));
        list.add(Constants.GetAd2(requireContext()));
        Picasso.with(requireContext()).invalidate(Constants.GetAd3(requireContext()));
        list.add(Constants.GetAd3(requireContext()));
        Picasso.with(requireContext()).invalidate(Constants.GetAd4(requireContext()));
        list.add(Constants.GetAd4(requireContext()));
        return list;
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        if (event.getAction()==KeyEvent.ACTION_UP){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    requireActivity().finish();
                    Log.e("keycode_back","clicked on home");
                    return false;
            }
        }
        return super.myOnKeyDown(event);
    }
}
