package com.crestron.aurora.db;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Show show);

    //Look at truncate
    @Query("DELETE FROM show_table")
    void deleteAllShows();

    @Query("DELETE FROM episode_watched")
    void deleteAllEpisodes();

    @Query("SELECT * FROM SHOW_TABLE")
    List<Show> getAllShows();

    default List<Show> getShowsFromSource(ShowSource s) {
        return getShowsFromSource("%" + s.s + "%");
    }

    @Query("SELECT * FROM show_table where show_link like (:s)")
    List<Show> getShowsFromSource(String s);

    @Query("SELECT COUNT(show_name) FROM show_table where show_name==(:showName)")
    int isInDatabase(String showName);

    @Query("SELECT COUNT(show_link) FROM show_table where show_link==(:showLink)")
    int isUrlInDatabase(String showLink);

    @Query("DELETE FROM show_table where show_name==(:name)")
    void deleteShow(String name);

    @Update
    void updateShow(Show show);

    @Query("select * from show_table where show_name=:name")
    Show getShow(String name);

    @Query("select * from show_table where show_link=:link")
    Show getShowByURL(String link);

    @Query("select * from episode_watched where showName=:name")
    List<Episode> getEpisodes(String name);

    @Query("select * from episode_watched where showName=:url")
    List<Episode> getEpisodesByUrl(String url);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEpisode(Episode episode);

    @Query("DELETE FROM episode_watched where episodeNumber==(:episodeNumber)")
    void deleteEpisode(int episodeNumber);

    @Transaction
    @Query("select show_name, episodeNumber, showName, showUrl " +
            "from show_table inner join episode_watched on " +
            "show_table.show_name = episode_watched.showName" +
            " where show_name=:name order by episodeNumber")
    List<Episode> getEpisodeFromShow(String name);
}
