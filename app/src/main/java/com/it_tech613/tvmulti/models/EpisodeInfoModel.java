package com.it_tech613.tvmulti.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EpisodeInfoModel {
//    @Expose
//    @SerializedName("bitrate")
//    private int bitrate;
    @Expose
    @SerializedName("duration")
    private String duration;
//    @Expose
//    @SerializedName("duration_secs")
//    private int duration_secs;
    @Expose
    @SerializedName("name")
    private String name;
//    @Expose
//    @SerializedName("rating")
//    private float rating;
//    @Expose
//    @SerializedName("releasedate")
//    private String releasedate;
    @Expose
    @SerializedName("plot")
    private String plot;
    @Expose
    @SerializedName("movie_image")
    private String movie_image;

//    public int getBitrate() {
//        return bitrate;
//    }

    public String getDuration() {
        return duration;
    }

//    public int getDuration_secs() {
//        return duration_secs;
//    }

    public String getName() {
        return name;
    }

//    public float getRating() {
//        return rating;
//    }
//
//    public String getReleasedate() {
//        return releasedate;
//    }

    public String getPlot() {
        return plot;
    }

    public String getMovie_image() {
        return movie_image;
    }
}
