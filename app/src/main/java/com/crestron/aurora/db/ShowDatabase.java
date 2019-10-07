package com.crestron.aurora.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Show.class, Episode.class}, version = 3)
public abstract class ShowDatabase extends RoomDatabase {

    public abstract ShowDao showDao();

    private static ShowDatabase INSTANCE;

    public static ShowDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ShowDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ShowDatabase.class, "show_database")
                                   //.addMigrations(MIGRATION_2_3)
                                   //.addMigrations(MIGRATION_3_4)
                                   .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void migrate(Context context) {
        Room.databaseBuilder(context, ShowDatabase.class, "show_database")
            .addMigrations(MIGRATION_2_3).build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `episodes_watched` (`episodeNumber` INTEGER, "
                                     + "`showName` TEXT)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE episode_watched ADD COLUMN showUrl TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            //database.execSQL("ALTER TABLE episode_watched ADD COLUMN showUrl TEXT Primary Key");
        }
    };
}
