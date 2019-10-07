package com.crestron.aurora.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "show_table")
public class Show {

    @PrimaryKey
    @ColumnInfo(name = "show_link")
    @NonNull
    private String link;

    @ColumnInfo(name = "show_name")
    @NonNull
    private String name;

    @ColumnInfo(name = "number_of_shows")
    private int showNum = 0;

    public Show(@NonNull String link, @NonNull String name) {
        this.link = link;
        this.name = name;
    }

    @Ignore
    public Show(String link, @NonNull String name, int showNum) {
        this.link = link;
        this.name = name;
        this.showNum = showNum;
    }

    public int getShowNum() {
        return showNum;
    }

    public void setShowNum(int showNum) {
        this.showNum = showNum;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }
}
