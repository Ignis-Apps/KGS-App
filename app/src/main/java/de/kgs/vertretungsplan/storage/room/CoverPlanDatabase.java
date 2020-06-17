package de.kgs.vertretungsplan.storage.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CoverItem.class, CoverPlan.class}, version = 1)
public abstract class CoverPlanDatabase extends RoomDatabase {

    private static CoverPlanDatabase instance;

    public static synchronized CoverPlanDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    CoverPlanDatabase.class, "cover_plan_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract CoverPlanDao coverPlanDao();

    public abstract CoverItemDao coverItemDao();

}
