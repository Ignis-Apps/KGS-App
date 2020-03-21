package de.kgs.vertretungsplan.slide;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.singetones.DataStorage;


public class BlackboardFragment extends Fragment implements OnClickListener {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.black_board_layout, container, false);

        CardView newspaper = v.findViewById(R.id.card_newspaper);
        CardView mensa = v.findViewById(R.id.card_mensa);
        CardView contact = v.findViewById(R.id.card_contact);

        newspaper.setOnClickListener(this);
        mensa.setOnClickListener(this);
        contact.setOnClickListener(this);

        return v;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
    }

    public void onClick(View view) {

        Context context = getContext();
        if (context == null)
            return;

        switch (view.getId()) {

            case R.id.card_mensa:
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(DataStorage.getInstance().school_mensa_url)));
                break;

            case R.id.card_newspaper:
                Intent emailIntent = new Intent("android.intent.action.SENDTO", Uri.parse("mailto:ignis.apps@gmail.com"));
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Neuer Eintrag - Schwarzes Brett");
                context.startActivity(Intent.createChooser(emailIntent, "Sende Email mit ..."));
                break;

            case R.id.card_contact:
                MainActivity m = (MainActivity) getContext();
                if (m != null) {
                    m.kgsWebView.loadWebPage(DataStorage.getInstance().student_newspaper, true);
                    m.toolbar2.setTitle("freistunde.blog");
                }
                break;
        }
    }
}
