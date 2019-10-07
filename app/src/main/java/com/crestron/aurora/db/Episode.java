package com.crestron.aurora.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "episode_watched",
        primaryKeys = {"showName", "showUrl"},
        foreignKeys = {
                @ForeignKey(entity = Show.class,
                        parentColumns = "show_link",
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

    @NonNull
    public String showUrl;

    public Episode(int episodeNumber, @NonNull String showName, @NonNull String showUrl) {
        this.episodeNumber = episodeNumber;
        this.showName = showName;
        this.showUrl = showUrl;
    }

    @Ignore
    public String toString() {
        return showName + ": " + episodeNumber + ": " + showUrl;
    }
}
