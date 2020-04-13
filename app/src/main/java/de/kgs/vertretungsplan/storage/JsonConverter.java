package de.kgs.vertretungsplan.storage;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.kgs.vertretungsplan.coverPlan.CoverItem;
import de.kgs.vertretungsplan.coverPlan.CoverPlan;

public class JsonConverter {

    private static final String key_class = "class";
    private static final String key_hour = "hour";
    private static final String key_dropped = "dropped";
    private static final String key_subject = "fach";
    private static final String key_room = "room";
    private static final String key_annotation = "annotation";
    private static final String key_ver = "ver_from";
    private static final String key_annotation_lesson = "annotation_lesson";
    private static final String key_item = "item-";
    private static final String key_title = "title";
    private static final String key_last_updated = "lastUpdated";
    private static final String key_item_amount = "amountOfItems";
    private static final String key_cover_plan_info = "coverPlanInfos";
    private static final String key_cover_plan_items = "coverPlanItems";

    private static final String key_daily_message_header = "dailyMessageHeader";
    private static final String key_daily_message_item = "dailyMessageItem-";
    private static final String key_daily_message_items = "dailyMessageItems";
    private static final String key_daily_message_item_text = "dailyMessageItemText";
    private static final String key_daily_message_item_amount = "dailyMessageItemAmount";


    public static JSONObject getJSONFromCoverPlan(CoverPlan coverPlan) {

        JSONObject json = null;

        try {

            json = new JSONObject();

            JSONObject coverPlanInfos = new JSONObject();
            coverPlanInfos.put(key_title, coverPlan.getTitle());
            coverPlanInfos.put(key_last_updated, coverPlan.getLastUpdate());
            coverPlanInfos.put(key_item_amount, coverPlan.getCoverItems().length);
            coverPlanInfos.put(key_daily_message_header, coverPlan.getDailyInfoHead());
            coverPlanInfos.put(key_daily_message_item_amount, coverPlan.getDailyInfoBody().size());
            json.put(key_cover_plan_info, coverPlanInfos);

            JSONObject coverPlanItems = new JSONObject();

            int index = 0;

            for (CoverItem coverItem : coverPlan.getCoverItems()) {

                JSONObject item = new JSONObject();
                item.put(key_class, coverItem.getTargetClass());
                item.put(key_hour, coverItem.getHour());
                item.put(key_subject, coverItem.getSubject());
                item.put(key_room, coverItem.getRoom());
                item.put(key_annotation, coverItem.getAnnotation());
                item.put(key_ver, coverItem.getRelocated());
                item.put(key_annotation_lesson, coverItem.isNewEntry());
                coverPlanItems.put(key_item + index, item);
                index++;
            }

            json.put(key_cover_plan_items, coverPlanItems);

            JSONObject dailyMessageRows = new JSONObject();

            index = 0;

            for (String s : coverPlan.getDailyInfoBody()) {

                JSONObject item = new JSONObject();
                item.put(key_daily_message_item_text, s);
                dailyMessageRows.put(key_daily_message_item + index, item);
                index++;
            }

            json.put(key_daily_message_items, dailyMessageRows);


        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return json;

    }

    public static CoverPlan getCoverPlanFromJSON(JSONObject o) throws JSONException {

        CoverPlan.Builder p = new CoverPlan.Builder();
        JSONObject coverPlanInfos = o.getJSONObject(key_cover_plan_info);
        JSONObject items = o.getJSONObject(key_cover_plan_items);

        p.setTitle(coverPlanInfos.getString(key_title));
        p.setLastUpdate(coverPlanInfos.getString(key_last_updated));
        p.setDailyInfo(coverPlanInfos.optString(key_daily_message_header));

        int itemAmount = coverPlanInfos.getInt(key_item_amount);
        int index = 0;

        List<CoverItem> coverItems = new ArrayList<>();

        while (index < itemAmount) {

            JSONObject item = items.getJSONObject(key_item + index);

            CoverItem coverItem = new CoverItem.Builder()
                    .setClass(item.getString(key_class))
                    .setHour(item.getString(key_hour))
                    .setSubject(item.getString(key_subject))
                    .setRoom(item.getString(key_room))
                    .setAnnotation(item.getString(key_annotation))
                    .setRelocated(item.getString(key_ver))
                    .isNewEntry(item.getString(key_annotation_lesson).equals("X"))
                    .build();

            coverItems.add(coverItem);
            index++;
        }
        p.setCoverItems(coverItems);

        items = o.optJSONObject(key_daily_message_items);
        itemAmount = coverPlanInfos.getInt(key_daily_message_item_amount);
        index = 0;

        List<String> dailyInfoRows = new LinkedList<>();
        while (index < itemAmount) {
            JSONObject rowItem = items != null ? items.optJSONObject(key_daily_message_item + index) : null;
            dailyInfoRows.add(rowItem != null ? rowItem.getString(key_daily_message_item_text) : null);
            index++;
        }
        p.setDailyInfoBody(dailyInfoRows);
        return p.build();

    }


}