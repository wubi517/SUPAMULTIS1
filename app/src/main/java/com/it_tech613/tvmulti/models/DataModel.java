package com.it_tech613.tvmulti.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataModel {
    @Expose
    @SerializedName("image_urls")
    private List<String> image_urls;
    @Expose
    @SerializedName("vpn_ip")
    private String vpn_ip;
    @Expose
    @SerializedName("pin_4")
    private String pin_4;
    @Expose
    @SerializedName("pin_3")
    private String pin_3;
    @Expose
    @SerializedName("pin_2")
    private String pin_2;
    @Expose
    @SerializedName("app_url")
    private String app_url;
    @Expose
    @SerializedName("version")
    private String version;
    @Expose
    @SerializedName("url")
    private String url;
    @Expose
    @SerializedName("slider_time")
    private String slider_time;
    @Expose
    @SerializedName("message_time")
    private String message_time;
    @Expose
    @SerializedName("message_on_off")
    private String message_on_off;
    @Expose
    @SerializedName("message")
    private String message;

    public List<String> getImage_urls() {
        return image_urls;
    }

    public String getVpn_ip() {
        return vpn_ip;
    }

    public String getPin_4() {
        return pin_4;
    }

    public String getPin_3() {
        return pin_3;
    }

    public String getPin_2() {
        return pin_2;
    }

    public String getApp_url() {
        return app_url;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getSlider_time() {
        return slider_time;
    }

    public String getMessage_time() {
        return message_time;
    }

    public String getMessage_on_off() {
        return message_on_off;
    }

    public String getMessage() {
        return message;
    }

    public void setImage_urls(List<String> image_urls) {
        this.image_urls = image_urls;
    }

    public void setVpn_ip(String vpn_ip) {
        this.vpn_ip = vpn_ip;
    }

    public void setPin_4(String pin_4) {
        this.pin_4 = pin_4;
    }

    public void setPin_3(String pin_3) {
        this.pin_3 = pin_3;
    }

    public void setPin_2(String pin_2) {
        this.pin_2 = pin_2;
    }

    public void setApp_url(String app_url) {
        this.app_url = app_url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSlider_time(String slider_time) {
        this.slider_time = slider_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public void setMessage_on_off(String message_on_off) {
        this.message_on_off = message_on_off;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
