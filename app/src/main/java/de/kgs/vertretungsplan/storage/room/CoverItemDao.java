package de.kgs.vertretungsplan.storage.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CoverItemDao {

    @Insert
    void insert(List<CoverItem> item);

    @Query("SELECT * FROM cover_item_table WHERE target_day LIKE :day")
    LiveData<List<CoverItem>> getAllCoverItems(String day);

}
