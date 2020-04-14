package de.kgs.vertretungsplan.ui.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverplan.CoverItem;
import de.kgs.vertretungsplan.coverplan.CoverPlan;
import de.kgs.vertretungsplan.firebase.Analytics;
import de.kgs.vertretungsplan.ui.adapters.viewholder.CoverItemViewHolder;
import de.kgs.vertretungsplan.ui.adapters.viewholder.DailyMessageViewHolder;
import de.kgs.vertretungsplan.ui.adapters.viewholder.RateViewHolder;
import de.kgs.vertretungsplan.ui.dialogs.CoverItemInfo;
import de.kgs.vertretungsplan.ui.interfaces.OnCoverListItemClicked;

public class CoverListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnCoverListItemClicked {

    private static final int TYPE_COVER_ITEM = 1;
    private static final int TYPE_DAILY_MESSAGE = 2;
    private static final int TYPE_RATE_APP = 3;

    private List<CoverItem> dataSet = new ArrayList<>();
    private String dailyMessageHead = "";
    private String dailyMessageBody = "";
    private boolean hasDailyMessage;

    private Context context;

    public CoverListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_COVER_ITEM:
                return new CoverItemViewHolder(layoutInflater.inflate(R.layout.listview_item_standard, parent, false), this);
            case TYPE_DAILY_MESSAGE:
                return new DailyMessageViewHolder(layoutInflater.inflate(R.layout.listview_item_daily_info, parent, false));
            case TYPE_RATE_APP:
                return new RateViewHolder(layoutInflater.inflate(R.layout.listview_item_share, parent, false), this);
            default:
                throw new AssertionError("Unknown view type !");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case TYPE_DAILY_MESSAGE:
                DailyMessageViewHolder dailyMessageViewHolder = (DailyMessageViewHolder) holder;
                dailyMessageViewHolder.setMessage(dailyMessageHead, dailyMessageBody);
                return;
            case TYPE_COVER_ITEM:
                CoverItemViewHolder itemViewHolder = (CoverItemViewHolder) holder;
                itemViewHolder.setData(dataSet.get((hasDailyMessage) ? position - 1 : position));
                return;
            case TYPE_RATE_APP:
                return;
            default:
                throw new AssertionError("Unhandled view holder type !");
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasDailyMessage)
            return TYPE_DAILY_MESSAGE;
        if (position < getItemCount() - 1)
            return TYPE_COVER_ITEM;
        return TYPE_RATE_APP;
    }

    @Override
    public int getItemCount() {
        int itemAmount = 0;
        if (hasDailyMessage)
            itemAmount += 1;
        if (dataSet != null)
            itemAmount += dataSet.size();
        itemAmount += 1;
        return itemAmount;
    }

    public void setDataSet(@Nullable CoverPlan coverPlan) {

        if (coverPlan == null)
            return;
        dataSet = coverPlan.getCoverItemsFiltered();
        dailyMessageHead = "" + coverPlan.getDailyInfoHead().trim();
        dailyMessageBody = "" + coverPlan.getDailyInfoMessage().trim();
        hasDailyMessage = !(dailyMessageBody + dailyMessageHead).isEmpty();
        notifyDataSetChanged();
    }

    @Override
    public void onCoverItemClicked(CoverItem item) {
        CoverItemInfo.showDialog(context, item);
    }

    @Override
    public void onRateItemClicked() {
        Analytics.getInstance().logEvent("rate_app");
        try {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=de.kgs.vertretungsplan")));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=de.kgs.vertretungsplan")));
        }
    }

}

