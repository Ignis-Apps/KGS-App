package de.kgs.vertretungsplan.storage.room;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CoverPlanRepository {

    private CoverItemDao coverItemDao;
    private CoverPlanDao coverPlanDao;

    private LiveData<List<CoverItem>> itemsToday;
    private LiveData<List<CoverItem>> itemsTomorrow;

    private LiveData<CoverPlan> coverPlanDataToday;
    private LiveData<CoverPlan> coverPlanDataTomorrow;

    public CoverPlanRepository(Application application) {

        CoverPlanDatabase database = CoverPlanDatabase.getInstance(application);
        coverItemDao = database.coverItemDao();
        coverPlanDao = database.coverPlanDao();

        itemsToday = coverItemDao.getAllCoverItems("TODAY");
        itemsTomorrow = coverItemDao.getAllCoverItems("TOMORROW");

        coverPlanDataToday = coverPlanDao.getCoverPlanData("TODAY");
        coverPlanDataTomorrow = coverPlanDao.getCoverPlanData("TOMORROW");

    }

    public void insert(CoverPlan coverPlanData, List<CoverItem> coverItems) {
        CoverPlanDatabaseAsyncTask task = new CoverPlanDatabaseAsyncTask(coverItemDao, coverPlanDao);
        task.setData(coverItems, coverPlanData);
        task.execute(DATABASE_OPERATION.INSERT);
    }

    public LiveData<List<CoverItem>> getItemsToday() {
        return itemsToday;
    }

    public LiveData<List<CoverItem>> getItemsTomorrow() {
        return itemsTomorrow;
    }

    public LiveData<CoverPlan> getCoverPlanDataToday() {
        return coverPlanDataToday;
    }

    public LiveData<CoverPlan> getCoverPlanDataTomorrow() {
        return coverPlanDataTomorrow;
    }

    private enum DATABASE_OPERATION {
        INSERT,
        DELETE,
    }

    private static class CoverPlanDatabaseAsyncTask extends AsyncTask<DATABASE_OPERATION, Void, Void> {

        private CoverItemDao coverItemDao;
        private CoverPlanDao coverPlanDao;

        private List<CoverItem> coverItemProcessQueue;
        private CoverPlan coverPlanProcessQueue;

        private CoverPlanDatabaseAsyncTask(CoverItemDao coverItemDao, CoverPlanDao coverPlanDao) {
            this.coverItemDao = coverItemDao;
            this.coverPlanDao = coverPlanDao;
        }

        private void setData(List<CoverItem> items, CoverPlan data) {
            coverItemProcessQueue = items;
            coverPlanProcessQueue = data;
        }

        @Override
        protected Void doInBackground(DATABASE_OPERATION... database_operations) {

            if (database_operations.length == 0)
                return null;

            switch (database_operations[0]) {
                case INSERT:
                    coverPlanDao.insert(coverPlanProcessQueue);
                    coverItemDao.insert(coverItemProcessQueue);
                    return null;
                case DELETE:
                    return null;
                default:
                    throw new AssertionError();

            }

        }
    }
}
