package com.it_tech613.tvmulti.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class FullModel implements Serializable {
    private int category_id;
    private String category_name;
    private List<EPGChannel> channels;
    private  int catchable_count;
    public FullModel(int category_id, List<EPGChannel> channels, String category_name, int catchable_count) {
        this.category_id=category_id;
        this.channels = channels;
        this.category_name = category_name;
        this.catchable_count = catchable_count;
    }

    public List<EPGChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<EPGChannel> channels) {
        this.channels = channels;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public int getCatchable_count() {
        return catchable_count;
    }

    public void setCatchable_count(int catchable_count) {
        this.catchable_count = catchable_count;
    }
}
