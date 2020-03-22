package de.kgs.vertretungsplan.views.handler;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.coverPlan.Grade;
import de.kgs.vertretungsplan.coverPlan.GradeSubClass;

public class SpinnerHandler implements OnItemSelectedListener {

    private ApplicationData applicationData = ApplicationData.getInstance();
    private Broadcast broadcast;

    private Spinner spinnerGrade;
    private Spinner spinnerClass;

    public SpinnerHandler(Activity activity, Broadcast broadcast) {
        this.broadcast = broadcast;
        setupSpinners(activity);
    }

    private void setupSpinners(Activity activity) {

        // Grade Spinner (5. Klasse, 6. Klasse, ...)
        spinnerGrade = activity.findViewById(R.id.spinnerGrade);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.spinner_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(adapter);
        spinnerGrade.setSelection(applicationData.getCurrentGrade().getGradeLevel());
        spinnerGrade.setOnItemSelectedListener(this);

        // Class Spinner ( a, b, ...)
        spinnerClass = activity.findViewById(R.id.spinnerClass);
        ArrayAdapter<CharSequence> adapterClass = ArrayAdapter.createFromResource(activity, R.array.spinner_array_class, R.layout.spinner_item);
        adapterClass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapterClass);
        spinnerClass.setSelection(applicationData.getCurrentGradeSubClass().getClassLevel());
        spinnerClass.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        if (adapterView == spinnerGrade) {
            Grade grade = Grade.getGradeByName(spinnerGrade.getSelectedItem().toString());
            spinnerClass.setVisibility(grade.hasSubClasses() ? View.VISIBLE : View.GONE);
            applicationData.setCurrentGrade(grade);
            broadcast.send(BroadcastEvent.CURRENT_GRADE_CHANGED);
            return;
        }

        if (adapterView == spinnerClass) {
            GradeSubClass subClass = GradeSubClass.getByClassName(spinnerClass.getSelectedItem().toString());
            applicationData.setCurrentGradeSubClass(subClass);
            broadcast.send(BroadcastEvent.CURRENT_CLASS_CHANGED);
            return;
        }

        throw new AssertionError("Unimplemented action for adapter view");

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
