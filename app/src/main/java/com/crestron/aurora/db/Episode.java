package com.crestron.aurora.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

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
