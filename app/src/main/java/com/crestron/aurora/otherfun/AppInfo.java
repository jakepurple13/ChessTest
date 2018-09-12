package com.crestron.aurora.otherfun;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppInfo {

    @SerializedName("version")
    @Expose
    private double version;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("devNotes")
    @Expose
    private String devNotes;

    /**
     * No args constructor for use in serialization
     *
     */
    public AppInfo() {
    }

    /**
     *
     * @param link
     * @param devNotes
     * @param version
     */
    public AppInfo(double version, String link, String devNotes) {
        super();
        this.version = version;
        this.link = link;
        this.devNotes = devNotes;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDevNotes() {
        return devNotes;
    }

    public void setDevNotes(String devNotes) {
        this.devNotes = devNotes;
    }

    @Override
    public String toString() {
        return "version: " + version + " link: " + link + " devNotes: " + devNotes;
        //return new ToStringBuilder(this).append("version", version).append("link", link).append("devNotes", devNotes).toString();
    }

}