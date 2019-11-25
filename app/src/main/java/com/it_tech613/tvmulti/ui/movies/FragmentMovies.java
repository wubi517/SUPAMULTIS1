package com.it_tech613.tvmulti.ui.movies;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.utils.MyFragment;


public class FragmentMovies extends MyFragment {

    private RecyclerView category_recyclerview;
    private MovieCategoryAdapter movieCategoryAdapter;
    private boolean isOnSearch=false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        category_recyclerview = view.findViewById(R.id.category_recyclerview);
        final TextInputEditText textInputLayout = view.findViewById(R.id.editText);
        textInputLayout.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE){
                Log.e("ActionDone","clicked");
                if (!isOnSearch)
                    movieCategoryAdapter.search(textInputLayout.getText().toString());
            }
            return false;
        });
        Button submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            if (isOnSearch) return;
            movieCategoryAdapter.search(textInputLayout.getText().toString());
        });
        category_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        setRecyclerView();
    }

    private void setRecyclerView() {
        movieCategoryAdapter = new MovieCategoryAdapter(
                requireContext(),
                (position, homeCategory, movieModels) -> {
                    if (movieModels.size()==0) return null;
                    MyApp.subMovieModels = movieModels;
                    if(position==0){
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(((MainActivity)requireActivity()).fragmentList.size()-4))//FragmentAllMovie
                                .addToBackStack(null).commit();
                    }else{
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(((MainActivity)requireActivity()).fragmentList.size()-3))//FragmentMovieDetail
                                .addToBackStack(null).commit();
                    }
                    return null;
                }
        ){
            @Override
            public void setBlocked(boolean isBlocked) {
                isOnSearch = isBlocked;
            }
        };
        movieCategoryAdapter.setList(MyApp.vod_categories_filter,true);
        category_recyclerview.setAdapter(movieCategoryAdapter);
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
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
