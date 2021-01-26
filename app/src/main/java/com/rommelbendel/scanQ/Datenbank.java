package com.rommelbendel.scanQ;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Vokabel.class, Kategorie.class}, version = 3)
public abstract class Datenbank extends RoomDatabase {
    public static final String DATABASE_NAME = "VokabelDatenbank";

    public abstract VokabelnDao vokabelnDao();
    public abstract KategorienDao kategorienDao();

    private static volatile Datenbank INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    /*
                    VokabelnDao vokabelnDao = INSTANCE.vokabelnDao();
                    vokabelnDao.deleteAlles();

                    Vokabel voc1 = new Vokabel("hello", "Hallo", "Test");
                    Vokabel voc2 = new Vokabel("welcome", "Willkommen", "Test");
                    Vokabel voc3 = new Vokabel("to", "bei", "Test");
                    Vokabel voc4 = new Vokabel("ScanQ", "ScanQ", "Test");

                    voc1.setMarkiert(true);

                    vokabelnDao.insertAlleVokabeln(voc1, voc2, voc3, voc4);
                     */
                }
            });
        }
    };

    static Datenbank getDatenbank(@NotNull final Context context) {
        if (INSTANCE == null) {
            synchronized (Datenbank.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Datenbank.class,
                            DATABASE_NAME).addMigrations(MIGRATION_2_3).addCallback(roomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `KATEGORIEN` (`name` TEXT, `id` INTEGER NOT NULL, Primary Key(`id`))");
        }
    };

    /*
    static final Migration MIGRATION_1_2  = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE vokabeln ADD COLUMN markiert BOOLEAN");
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };
     */
}
