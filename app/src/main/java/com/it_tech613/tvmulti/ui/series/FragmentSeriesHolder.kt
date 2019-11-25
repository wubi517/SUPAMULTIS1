package com.it_tech613.tvmulti.ui.series


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.it_tech613.tvmulti.R
import com.it_tech613.tvmulti.ui.MainActivity
import com.it_tech613.tvmulti.utils.MyFragment

class FragmentSeriesHolder : MyFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_series_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragment = (requireActivity() as MainActivity).fragmentList.get((requireActivity() as MainActivity).fragmentList.size-5) as FragmentSeasons
        fragment.parent = this
        replaceFragment(fragment)
    }

    fun replaceFragment(fragment:Fragment){
        childFragmentManager.beginTransaction().replace(R.id.series_container,fragment).commit()
    }

}
