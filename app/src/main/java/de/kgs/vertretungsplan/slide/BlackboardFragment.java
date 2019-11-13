package de.kgs.vertretungsplan.slide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.kgs.vertretungsplan.DataStorage;
import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.manager.FirebaseManager;

public class BlackboardFragment extends Fragment{

    private CardView newspaper;
    private CardView mensa;
    private CardView contact;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.black_board_layout,container,false);

        newspaper = v.findViewById(R.id.card_newspaper);
        mensa = v.findViewById(R.id.card_mensa);
        contact = v.findViewById(R.id.card_contact);

        return v;
    }

   public void setOnClickListener(View.OnClickListener onClickListener){
        newspaper.setOnClickListener(onClickListener);
        mensa.setOnClickListener(onClickListener);
        contact.setOnClickListener(onClickListener);
   }

   @SuppressLint("SetJavaScriptEnabled")
   public void onClick(View view, Context context, final FirebaseManager firebaseManager, DataStorage ds){
       if(view == mensa){
           firebaseManager.logEventSelectContent("mensa", FirebaseManager.ANALYTICS_BLACK_BOARD);

           Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_mensa_url));
           context.startActivity(browserIntent);
       }else if(view == contact){
           firebaseManager.logEventSelectContent("contact", FirebaseManager.ANALYTICS_BLACK_BOARD);

           Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ignis.apps@gmail.com"));
           emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Neuer Eintrag - Schwarzes Brett");
           context.startActivity(Intent.createChooser(emailIntent, "Sende Email mit ..."));
       }else if(view == newspaper){
           firebaseManager.logEventSelectContent("newspaper", FirebaseManager.ANALYTICS_BLACK_BOARD);
           MainActivity m = (MainActivity) getContext();
           if(m==null)
               return;

           m.showPageInWebview(DataStorage.getInstance().student_newspaper);
           m.webView.getSettings().setJavaScriptEnabled(true);
           m.toolbar.setTitle("freistunde.blog");
       }
   }

}
