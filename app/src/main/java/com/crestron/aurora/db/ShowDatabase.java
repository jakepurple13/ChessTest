package com.crestron.aurora.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {Show.class, Episode.class}, version = 2)
public abstract class ShowDatabase extends RoomDatabase {

    public abstract ShowDao showDao();
    private static ShowDatabase INSTANCE;

    public static ShowDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ShowDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ShowDatabase.class, "show_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void migrate(Context context) {
        Room.databaseBuilder(context, ShowDatabase.class, "show_database")
                .addMigrations(MIGRATION_1_2).build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `episodes_watched` (`episodeNumber` INTEGER, "
                    + "`showName` TEXT)");

        }
    };

}
