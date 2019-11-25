package com.it_tech613.tvmulti.models;

import com.google.gson.annotations.SerializedName;
import com.it_tech613.tvmulti.apps.Constants;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Kristoffer.
 */
public class  EPGEvent implements Serializable {
    @SerializedName("id")
    private String id="";
    @SerializedName("channel_id")
    private String channel_id ="";
    private String correct;
//    private long start=0;
//    private long end=0;
    @SerializedName("title")
    private String title ="";
    @SerializedName("description")
    private String dec="";
    @SerializedName("epg_id")
    private String epg_id ="";
    private String category;
    private String director;
    private String actor;
    @SerializedName("start")
    private String t_time;
    @SerializedName("end")
    private String t_time_to;
    @SerializedName("start_timestamp")
    private String start_timestamp="";
    @SerializedName("stop_timestamp")
    private String stop_timestamp="";
    private int duration;
    private int mark_memo;
    private int mark_archive;
    @SerializedName("now_playing")
    private int now_playing=0;
    @SerializedName("lang")
    private String lang_code="";
    private EPGChannel channel;

    private Date startTime=null;
    private Date endTime=null;
    private EPGEvent previousEvent;
    private EPGEvent nextEvent;

    //is this the current selected event?
    private boolean selected;

    long wrongMedialaanTime = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();

    public EPGEvent(){}

    public EPGEvent(EPGChannel epgChannel, long start, long end, String title, String dec) {
        this.channel = epgChannel;
//        this.start = start;
//        this.end = end;
        this.title = title;
        this.dec = dec;
    }

    public EPGChannel getChannel() {
        return channel;
    }

    public void setChannel(EPGChannel epgChannel){
        this.channel=epgChannel;
    }

//    public long getStart() {
//        return start;
//    }
//
//    public long getEnd() {
//        return end;
//    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public String getDec() {
        return dec;
    }

    public void setDec(String dec){
        this.dec=dec;
    }

//    public boolean isCurrent() {
//        long now = System.currentTimeMillis();
//        return now >= start && now <= end;
//    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setNextEvent(EPGEvent nextEvent) {
        this.nextEvent = nextEvent;
    }

    public EPGEvent getNextEvent() {
        return nextEvent;
    }

    public void setPreviousEvent(EPGEvent previousEvent) {
        this.previousEvent = previousEvent;
    }

    public EPGEvent getPreviousEvent() {
        return previousEvent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getEpg_id() {
        return epg_id;
    }

    public void setEpg_id(String epg_id) {
        this.epg_id = epg_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getT_time() {
        return t_time;
    }

    public void setT_time(String t_time) {
        this.t_time = t_time;
    }

    public String getT_time_to() {
        return t_time_to;
    }

    public void setT_time_to(String t_time_to) {
        this.t_time_to = t_time_to;
    }

    public String getStart_timestamp() {
        return start_timestamp;
    }

    public void setStart_timestamp(String start_timestamp) {
        this.start_timestamp = start_timestamp;
    }

    public String getStop_timestamp() {
        return stop_timestamp;
    }

    public void setStop_timestamp(String stop_timestamp) {
        this.stop_timestamp = stop_timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMark_memo() {
        return mark_memo;
    }

    public void setMark_memo(int mark_memo) {
        this.mark_memo = mark_memo;
    }

    public int getMark_archive() {
        return mark_archive;
    }

    public void setMark_archive(int mark_archive) {
        this.mark_archive = mark_archive;
    }

    public int getNow_playing() {
        return now_playing;
    }

    public void setNow_playing(int now_playing) {
        this.now_playing = now_playing;
    }

    public String getLang_code() {
        return lang_code;
    }

    public void setLang_code(String lang_code) {
        this.lang_code = lang_code;
    }

    public Date getStartTime() {
        if (startTime!=null)return startTime;
        try {
            startTime = Constants.epgFormat.parse(start_timestamp);
        } catch (ParseException e) {
            try {
                startTime = Constants.stampFormat.parse(t_time);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        if (endTime!=null)return endTime;
        try {
            endTime = Constants.epgFormat.parse(stop_timestamp);
        } catch (ParseException e) {
            try {
                endTime = Constants.stampFormat.parse(t_time_to);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
