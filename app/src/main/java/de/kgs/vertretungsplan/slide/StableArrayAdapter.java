package de.kgs.vertretungsplan.slide;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverItem;

public class StableArrayAdapter extends ArrayAdapter<CoverItem> {
    private final Context context;
    private final List<CoverItem> values;
    private String dailyInfoHeader,dailyInfoMessage;

    StableArrayAdapter(Context context, List<CoverItem> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    void setDataSet(List<CoverItem> items){

        System.out.println("SETTING NEW DATASET " + items.size());

        values.clear();
        // Platzhalter für die Tages Nachricht
        values.add(new CoverItem());
        values.addAll(items);
        // Platzhalter für die Bewertung
        values.add(new CoverItem());
        notifyDataSetChanged();

    }

    void setDailyMessage(String title,String message){

        dailyInfoHeader = title;
        dailyInfoMessage = message;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItemView;

        if(inflater==null)
            return new View(context);

        if(position==0){
            listItemView = inflater.inflate(R.layout.listview_item_daily_info, parent, false);
            TextView dailyMessageTitle = listItemView.findViewById(R.id.dailyViewTitle);
            TextView dailyMessageText = listItemView.findViewById(R.id.dailyViewMessage);

            if(!dailyInfoHeader.trim().isEmpty()&&!dailyInfoMessage.trim().isEmpty()) {
                dailyMessageTitle.setText(dailyInfoHeader.trim());
                dailyMessageText.setText(dailyInfoMessage.trim());
            }else{
                return new View(context);
            }

            return listItemView;
        }

        if(position==values.size()-1) {
            listItemView = inflater.inflate(R.layout.listview_item_share, parent, false);
            return listItemView;
        }


            listItemView = inflater.inflate(R.layout.listview_item_standard,parent,false);

            TextView textKlasse = listItemView.findViewById(R.id.textViewKlasse);
            TextView textStunde = listItemView.findViewById(R.id.textViewStunde);
            TextView textFach = listItemView.findViewById(R.id.textViewFach);
            TextView textRaum = listItemView.findViewById(R.id.textViewRaum);
            ImageView imageInfo = listItemView.findViewById(R.id.imageViewInfo);

           // System.out.println("POSITION " + position + "VAL LENGTH = " + values.size());

            CoverItem ci = values.get(position);// -1 Cause Element 0 is the daily info
            textKlasse.setText(ci.Class);
            textStunde.setText(ci.Hour);
            textFach.setText(ci.Fach);
            textRaum.setText(ci.Room);

            if(ci.Annotation.trim().equals("") && ci.Ver_From.trim().equals("") && ci.Annotation_Lesson.trim().equals("")){
                imageInfo.setVisibility(View.INVISIBLE);
            }

            LinearLayout background = listItemView.findViewById(R.id.listViewLn);
            if(ci.getsDropped()){
                background.setBackgroundColor(context.getResources().getColor(R.color.colorEntfall));
                textKlasse.setTextColor(Color.parseColor("#ffffff"));
                textStunde.setTextColor(Color.parseColor("#ffffff"));
                textFach.setTextColor(Color.parseColor("#ffffff"));
                textRaum.setTextColor(Color.parseColor("#ffffff"));
                imageInfo.setImageResource(R.drawable.ic_action_info_light);
            }

        return listItemView;


    }


}
