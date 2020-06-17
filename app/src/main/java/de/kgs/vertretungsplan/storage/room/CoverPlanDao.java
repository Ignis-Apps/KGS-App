package de.kgs.vertretungsplan.storage.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CoverPlanDao {

    @Insert
    void insert(CoverPlan item);

    @Query("SELECT * FROM cover_plan_meta_data_table WHERE target_day LIKE :day")
    LiveData<CoverPlan> getCoverPlanData(String day);

}
