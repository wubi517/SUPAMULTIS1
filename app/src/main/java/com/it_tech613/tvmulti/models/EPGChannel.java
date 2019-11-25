package com.it_tech613.tvmulti.models;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class EPGChannel implements Serializable {
    @SerializedName("num")
    private String number ="";
    @SerializedName("name")
    private String name="";
    @SerializedName("stream_type")
    private String stream_type="";
    @SerializedName("stream_id")
    private int stream_id=-1;
    @SerializedName("stream_icon")
    private String imageURL ="";
    @SerializedName("epg_channel_id")
    private String Id ="";
    @SerializedName("added")
    private String added="";
    @SerializedName("category_id")
    private int category_id=-1;
    @SerializedName("custom_sid")
    private String custom_sid="";
    @SerializedName("tv_archive")
    private int tv_archive=0;
    @SerializedName("direct_source")
    private String direct_source="";
    @SerializedName("tv_archive_duration")
    private String tv_archive_duration="";
    @SerializedName("is_locked")
    private boolean is_locked=false;
    @SerializedName("is_favorite")
    private boolean is_favorite=false;
    private boolean is_favorite_catch=false;
    @SerializedName("cell")
    private int cell=-1;
    private int channelID;

    private List<EPGEvent> events = Lists.newArrayList();
    private EPGChannel previousChannel;
    private EPGChannel nextChannel;
    public boolean selected;

    public EPGChannel(){}

    public EPGChannel(String imageURL, String name, int channelID, String Id, String number,int stream_id) {
        this.imageURL = imageURL;
        this.name = name;
        this.channelID = channelID;
        this.Id = Id;
        this.number = number;
        this.stream_id = stream_id;
    }
    public int getCell(){return cell;}

    public void setCell(int cell){this.cell = cell;}

    public String getNumber(){return number;}

    public void setNumber(String number){this.number = number;}

    public String getName(){return name;}

    public void setName(String name){this.name = name;}

    public String getStream_type(){return stream_type;}

    public void setStream_type(String stream_type){this.stream_type = stream_type;}

    public int getStream_id(){return stream_id;}

    public void setStream_id(int stream_id){this.stream_id = stream_id;}

    public String getImageURL(){return imageURL;}

    public void setImageURL(String imageURL){this.imageURL = imageURL;}

    public String getId(){
        if (Id !=null)return Id;
        else return "";
    }

    public void setId(String id){this.Id = id;}


    public String getAdded(){return added;}

    public void setAdded(String added){this.added = added;}

    public int getCategory_id(){return category_id;}

    public void setCategory_id(int category_id){this.category_id = category_id;}

    public String getCustom_sid(){return custom_sid;}

    public void setCustom_sid(String custom_sid){this.custom_sid = custom_sid;}

    public int getTv_archive(){return tv_archive;}

    public void setTv_archive(int tv_archive){this.tv_archive = tv_archive;}

    public String getDirect_source(){return direct_source;}

    public void setDirect_source(String direct_source){this.direct_source = direct_source;}

    public String getTv_archive_duration(){return tv_archive_duration;}

    public void setTv_archive_duration(String tv_archive_duration){this.tv_archive_duration = tv_archive_duration;}

    public boolean is_locked() {
        return is_locked;
    }

    public void setIs_locked(boolean is_locked) {
        this.is_locked = is_locked;
    }

    public boolean is_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    public List<EPGEvent> getEvents() {
        return events;
    }

    public void setEvents(List<EPGEvent> events) {
        this.events = events;
    }

    public EPGChannel getPreviousChannel() {
        return previousChannel;
    }

    public void setPreviousChannel(EPGChannel previousChannel) {
        this.previousChannel = previousChannel;
    }

    public EPGChannel getNextChannel() {
        return nextChannel;
    }

    public void setNextChannel(EPGChannel nextChannel) {
        this.nextChannel = nextChannel;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public EPGEvent addEvent(EPGEvent event) {
        this.events.add(event);
        return event;
    }

    public boolean isIs_favorite_catch() {
        return is_favorite_catch;
    }

    public void setIs_favorite_catch(boolean is_favorite_catch) {
        this.is_favorite_catch = is_favorite_catch;
    }
}
