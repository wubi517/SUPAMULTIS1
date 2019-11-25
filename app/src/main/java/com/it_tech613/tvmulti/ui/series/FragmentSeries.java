package com.it_tech613.tvmulti.ui.series;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.utils.MyFragment;

import java.util.ArrayList;


public class FragmentSeries extends MyFragment {

    private RecyclerView category_recyclerview;
    private SeriesCategoryAdapter seriesCategoryAdapter;
    private boolean isOnSearch=false;
    private int position=-1;
    private TextInputEditText textInputLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_series, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        category_recyclerview = view.findViewById(R.id.category_recyclerview);
        category_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        textInputLayout = view.findViewById(R.id.editText);
        textInputLayout.setOnEditorActionListener((textView, i, keyEvent) -> {
            Log.e("ActionDone","clicked");
            if (i == EditorInfo.IME_ACTION_DONE){
                if (!isOnSearch)
                    seriesCategoryAdapter.search(textInputLayout.getText().toString());
            }
            return false;
        });
        Button submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            if (isOnSearch) return;
            seriesCategoryAdapter.search(textInputLayout.getText().toString());
        });
        setRecyclerView();
    }

    private void setRecyclerView() {
        seriesCategoryAdapter = new SeriesCategoryAdapter(
                requireContext(),
                (categoryPosition, homeCategory, seriesModels, isClicked) -> {
                    if (isClicked && seriesModels.size()!=0){
                        MyApp.selectedSeriesModelList = new ArrayList<>(seriesModels);
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,new FragmentSeriesHolder())//FragmentSeasons
                                .addToBackStack(null).commit();
                    }else {
                        position=categoryPosition;
                    }
                    return null;
                }
        ){
            @Override
            public void setBlocked(boolean isBlocked) {
                isOnSearch = isBlocked;
            }
        };
        seriesCategoryAdapter.setList(MyApp.series_categories_filter,true);
        category_recyclerview.setAdapter(seriesCategoryAdapter);
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        if (event.getAction()==KeyEvent.ACTION_UP){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (position==0) textInputLayout.requestFocus();
                    return false;
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
