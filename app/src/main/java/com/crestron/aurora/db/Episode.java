package com.crestron.aurora.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "episode_watched",
        primaryKeys = {"showName", "episodeNumber"},
        foreignKeys = {
                @ForeignKey(entity = Show.class,
                        parentColumns = "show_name",
                        childColumns = "showName",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = @Index("showName")
)
public class Episode {

    //@PrimaryKey
    @NonNull
    public int episodeNumber;

    //@ColumnInfo(name = "showName")
    @NonNull
    public String showName;

    public Episode(int episodeNumber, String showName) {
        this.episodeNumber = episodeNumber;
        this.showName = showName;
    }

    @Ignore
    public String toString() {
        return showName + ": " + episodeNumber;
    }
}
