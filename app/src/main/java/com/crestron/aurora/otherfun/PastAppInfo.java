package com.crestron.aurora.otherfun;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PastAppInfo {

    @SerializedName("versions")
    @Expose
    private List<AppInfo> versions = null;

    /**
     * No args constructor for use in serialization
     */
    public PastAppInfo() {
    }

    /**
     * @param versions
     */
    public PastAppInfo(List<AppInfo> versions) {
        super();
        this.versions = versions;
    }

    public List<AppInfo> getVersions() {
        return versions;
    }

    public void setVersions(List<AppInfo> versions) {
        this.versions = versions;
    }

}