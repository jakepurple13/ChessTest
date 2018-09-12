package com.crestron.aurora.otherfun;

public class NormalLink {
    private Normal normal;

    public Normal getNormal() {
        return normal;
    }

    public void setNormal(Normal normal) {
        this.normal = normal;
    }

    @Override
    public String toString() {
        return "ClassPojo [normal = " + normal.toString() + "]";
    }
}

class Normal {
    private Storage[] storage;

    public Storage[] getStorage() {
        return storage;
    }

    public void setStorage(Storage[] storage) {
        this.storage = storage;
    }

    @Override
    public String toString() {
        return "ClassPojo [storage = " + storage + "]";
    }
}


class Storage {
    private String sub;

    private String source;

    private String link;

    private String quality;

    private String filename;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "ClassPojo [sub = " + sub + ", source = " + source + ", link = " + link + ", quality = " + quality + ", filename = " + filename + "]";
    }
}
