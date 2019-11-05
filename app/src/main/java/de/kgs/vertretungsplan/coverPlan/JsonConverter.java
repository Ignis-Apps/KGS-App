package de.kgs.vertretungsplan.coverPlan;

import com.crashlytics.android.Crashlytics;


import org.json.JSONException;
import org.json.JSONObject;

class JsonConverter {

    private static final String key_class = "class";
    private static final String key_hour = "hour";
    private static final String key_dropped = "dropped";
    private static final String key_fach = "fach";
    private static final String key_room = "room";
    private static final String key_annotation = "annotation";
    private static final String key_ver = "ver_from";
    private static final String key_annotation_lesson = "annotation_lesson";
    private static final String key_item = "item-";
    private static final String key_title = "title";
    private static final String key_last_updated = "lastUpdated";
    private static final String key_item_amount = "amountOfItems";
    private static final String key_coverplan_info = "coverPlanInfos";
    private static final String key_coverplan_items = "coverPlanItems";

    private static final String key_dailymessage_header = "dailyMessageHeader";
    private static final String key_dailymessage_item = "dailyMessageItem-";
    private static final String key_dailymessage_items = "dailyMessageItems";
    private static final String key_dailymessage_item_text = "dailyMessageItemText";
    private static final String key_dailymessage_item_amount = "dailyMessageItemAmount";


    static JSONObject getJSONFromCoverPlan(CoverPlan p)  {

        JSONObject json = null;

        try{

            json = new JSONObject();

            JSONObject coverPlanInfos = new JSONObject();
                coverPlanInfos.put(key_title,p.title);
                coverPlanInfos.put(key_last_updated,p.lastUpdate);
                coverPlanInfos.put(key_item_amount,p.getCoverItems().length);
                coverPlanInfos.put(key_dailymessage_header,p.dailyInfoHeader);
                coverPlanInfos.put(key_dailymessage_item_amount,p.dailyInfoRows.size());
            json.put(key_coverplan_info,coverPlanInfos);

            JSONObject coverPlanItems = new JSONObject();

            int index = 0;

            for(CoverItem ci:p.getCoverItems()) {

                JSONObject item = new JSONObject();
                item.put(key_class,ci.getTargetClass());
                item.put(key_hour,ci.getHour());
                item.put(key_fach,ci.getSubject());
                item.put(key_room,ci.getRoom());
                item.put(key_annotation,ci.getAnnotation());
                item.put(key_ver,ci.getRelocated());
                item.put(key_annotation_lesson,ci.isNewEntry());
                coverPlanItems.put(key_item+index,item);
                index++;
            }

            json.put(key_coverplan_items,coverPlanItems);

            JSONObject dailyMessageRows = new JSONObject();

            index = 0;

            for(String s:p.dailyInfoRows){

                JSONObject item = new JSONObject();
                item.put(key_dailymessage_item_text,s);
                dailyMessageRows.put(key_dailymessage_item+index,item);
                index++;
            }

            json.put(key_dailymessage_items,dailyMessageRows);



        }catch (JSONException e){
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return json;

    }

    static CoverPlan getCoverPlanFromJSON(JSONObject o) throws Exception {

        CoverPlan p = new CoverPlan();
        JSONObject coverPlanInfos = o.getJSONObject(key_coverplan_info);
        JSONObject items = o.getJSONObject(key_coverplan_items);
        p.title = coverPlanInfos.getString(key_title);
        p.lastUpdate = coverPlanInfos.getString(key_last_updated);
        p.dailyInfoHeader = coverPlanInfos.optString(key_dailymessage_header);
        int itemAmount = coverPlanInfos.getInt(key_item_amount);
        int index = 0;

        while( index < itemAmount ){

            JSONObject item = items.getJSONObject(key_item+index);

            CoverItem coverItem = new CoverItem.Builder()
                    .setClass(item.getString(key_class))
                    .setHour(item.getString(key_hour))
                    .setSubject(item.getString(key_fach))
                    .setAnnotation(item.getString(key_annotation))
                    .setRelocated(item.getString(key_ver))
                    .isNewEntry(item.getString(key_annotation_lesson).equals("X"))
                    .build();

            p.coverItems.add(coverItem);
            index++;
        }
        items = o.optJSONObject(key_dailymessage_items);
        itemAmount = coverPlanInfos.getInt(key_dailymessage_item_amount);
        index = 0;
        while(index<itemAmount){
            JSONObject rowItem = items.optJSONObject(key_dailymessage_item+index);
            p.dailyInfoRows.add(rowItem.getString(key_dailymessage_item_text));
            index++;
        }

        return p;

    }


}
