package de.kgs.vertretungsplan.slide;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverItem;

public class StableArrayAdapter extends ArrayAdapter<CoverItem> {
    private final Context context;
    private final List<CoverItem> values;
    private String dailyInfoHeader,dailyInfoMessage;
    private boolean hasDailyMessage;
    private String TAG = "ignislog arrayadapter";

    StableArrayAdapter(Context context, List<CoverItem> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    void setDataSet(List<CoverItem> items){

        Log.d(TAG, "Setting new dataset ");
        values.clear();

        // Fügt alle Daten hinzu
        values.addAll(items);
        // Fügt einen Platzhalter für die Bewertung hinzu
        values.add(new CoverItem());
        notifyDataSetChanged();

    }

    void setDailyMessage(String title,String message){

        dailyInfoHeader = title;
        dailyInfoMessage = message;

        // Überprüft ob die Tages Nachricht angezeigt werden soll
        hasDailyMessage = !title.isEmpty()&&!message.isEmpty();

        // Falls es eine Tagesnachricht gibt wird ein Platzhalter in die Daten eingebaut
        if (hasDailyMessage) {
            values.add(0, new CoverItem());
        }

        System.out.println("SETTING DAILY MESSAGE : " + hasDailyMessage);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Falls wir keinen Inflater haben geben wir einen leeren View zurück
        if(inflater==null)
            return new View(context);

        // Tagesnachricht
        if(position==0&&hasDailyMessage)
            return getDailyMessageView(inflater,parent);

        // Die letzte Position ist für den Bewertungs View
        if(position==values.size()-1)
            return getShareView(inflater,parent);

        View listItemView = convertView;


        // Falls es keinen ViewHolder gibt erstelle einen neuen und schreib ihn in den Tag
        if(convertView==null){
            listItemView = inflater.inflate(R.layout.listview_item_standard,parent,false);
            listItemView.setTag(getViewHolder(listItemView));
        }else {
            if(convertView.getTag()==null){
                listItemView = inflater.inflate(R.layout.listview_item_standard,parent,false);
                listItemView.setTag(getViewHolder(listItemView));
            }
        }

        // Extrahiere den ViewHolder aus dem convertView
        ViewHolder viewHolder = getViewHolder(listItemView);
        CoverItem ci = values.get(position);

        insertDataIntoItem(viewHolder,ci);

        return listItemView;

    }

    private void insertDataIntoItem(ViewHolder holder, CoverItem data){

        if(data.Annotation.concat(data.Ver_From).concat(data.Annotation_Lesson).equals(""))
            holder.imageInfo.setVisibility(View.INVISIBLE);
        else {
            holder.imageInfo.setVisibility(View.VISIBLE);
        }

        adjustColors(holder,data.getsDropped());

        holder.textKlasse.setText(data.Class);
        holder.textStunde.setText(data.Hour);
        holder.textFach.setText(data.Fach);
        holder.textRaum.setText(data.Room);

    }

    private void adjustColors(ViewHolder holder, boolean dropped){

        if(holder.dropped != null)
            if(dropped == holder.dropped)
                return;

        if(dropped){

            int red = context.getResources().getColor(R.color.colorAccent);
            int white = Color.parseColor("#ffffff");

            holder.background.setBackgroundColor(red);
            holder.textKlasse.setTextColor(white);
            holder.textStunde.setTextColor(white);
            holder.textFach.setTextColor(white);
            holder.textRaum.setTextColor(white);

            holder.imageInfo.setImageResource(R.drawable.ic_action_info_light);

        }else {

            int black = Color.parseColor("#000000");

            holder.background.setBackgroundColor(0);
            holder.textKlasse.setTextColor(black);
            holder.textStunde.setTextColor(black);
            holder.textFach.setTextColor(black);
            holder.textRaum.setTextColor(black);

            holder.imageInfo.setImageResource(R.drawable.ic_action_info);

        }

    }

    private ViewHolder getViewHolder(View listItemView){

        ViewHolder v = (ViewHolder) listItemView.getTag();

        if (v!=null)
            return v;

        ViewHolder holder = new ViewHolder();

        holder.textKlasse  = listItemView.findViewById(R.id.textViewKlasse);
        holder.textStunde  = listItemView.findViewById(R.id.textViewStunde);
        holder.textFach    = listItemView.findViewById(R.id.textViewFach);
        holder.textRaum    = listItemView.findViewById(R.id.textViewRaum);
        holder.imageInfo   = listItemView.findViewById(R.id.imageViewInfo);
        holder.background  = listItemView.findViewById(R.id.listViewLn);

        return holder;
    }

    private View getDailyMessageView(LayoutInflater inflater,ViewGroup parent){

        View listItemView = inflater.inflate(R.layout.listview_item_daily_info, parent, false);
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

    private View getShareView(LayoutInflater inflater,ViewGroup parent){
        return inflater.inflate(R.layout.listview_item_share, parent, false);
    }


    static class ViewHolder{

        TextView textKlasse;
        TextView textStunde;
        TextView textFach;
        TextView textRaum;
        ImageView imageInfo;
        LinearLayout background;
        Boolean dropped;

    }

}
