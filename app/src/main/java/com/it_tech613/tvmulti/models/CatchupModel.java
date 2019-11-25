package com.it_tech613.tvmulti.models;

import java.util.List;

public class CatchupModel {
    private String name;
    private List<EPGEvent> epgEvents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EPGEvent> getEpgEvents() {
        return epgEvents;
    }

    public void setEpgEvents(List<EPGEvent> epgEvents) {
        this.epgEvents = epgEvents;
    }
}
