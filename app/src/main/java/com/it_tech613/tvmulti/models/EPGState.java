package com.it_tech613.tvmulti.models;

import android.os.Parcelable;
import android.view.View;

/**
 * Created by MVRM on 19/04/2017.
 */

public class EPGState extends View.BaseSavedState {

    private EPGEvent currentEvent=null;

    public EPGState(Parcelable superState) {
        super(superState);
    }

    public EPGEvent getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(EPGEvent currentEvent) {
        this.currentEvent = currentEvent;
    }
}
