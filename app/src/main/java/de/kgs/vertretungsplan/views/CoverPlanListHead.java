package de.kgs.vertretungsplan.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;

public class CoverPlanListHead implements ViewTreeObserver.OnGlobalLayoutListener, Animation.AnimationListener {

    private static final int duration = 400; // 400
    private static final int delay = 100;

    private View headContainer;
    private View bodyContainer;

    private ScaleAnimation scaleIn;
    private ScaleAnimation scaleOut;

    private TranslateAnimation translateIn;
    private TranslateAnimation translateOut;

    private AnimationState animationState = AnimationState.SHOWN;

    public CoverPlanListHead(Activity activity, Broadcast broadcast) {

        headContainer = activity.findViewById(R.id.listview_legend_group);
        bodyContainer = activity.findViewById(R.id.viewpage);

        scaleIn = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        scaleOut = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);

        if(ApplicationData.getInstance().getCurrentlySelectedViewPage() == 0){
            animationState = AnimationState.HIDDEN;
            headContainer.setVisibility(View.GONE);
        }

        headContainer.getViewTreeObserver().addOnGlobalLayoutListener(this);
        setupObserver(broadcast);
    }

    private void setupObserver(Broadcast broadcast) {

        broadcast.subscribe(
                broadcastEvent -> performAnimation(), BroadcastEvent.CURRENT_MENU_ITEM_CHANGED,
                BroadcastEvent.CURRENT_PAGE_CHANGED);

    }

    private void performAnimation() {

        if (animationState == AnimationState.PENDING)
            return;

        if (ApplicationData.getInstance().getCurrentlySelectedViewPage() == 0) {
            if (animationState == AnimationState.HIDDEN)
                return;
            headContainer.setVisibility(View.GONE);
            if (translateOut == null){
                animationState = AnimationState.HIDDEN;
                return;
            }
            headContainer.startAnimation(scaleOut);
            bodyContainer.startAnimation(translateOut);
        } else {
            if (animationState == AnimationState.SHOWN)
                return;
            headContainer.setVisibility(View.VISIBLE);
            if (translateIn == null){
                animationState = AnimationState.SHOWN;
                return;
            }
            headContainer.startAnimation(scaleIn);
            bodyContainer.startAnimation(translateIn);
        }

    }

    private void syncAnimations(Animation... animations) {

        for (Animation animation : animations) {
            animation.setDuration(duration);
            animation.setStartOffset(delay);
            animation.setAnimationListener(this);
        }

    }

    @Override
    public void onGlobalLayout() {

        if (headContainer.getVisibility() == View.GONE)
            return;

        float headContainerHeight = headContainer.getHeight();

        System.out.println("Head container height == " + headContainerHeight);

        translateIn = new TranslateAnimation(0.0f, 0.0f, -headContainerHeight, 0);
        translateOut = new TranslateAnimation(0.0f, 0.0f, headContainerHeight, 0);

        headContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        syncAnimations(scaleIn, scaleOut, translateIn, translateOut);

    }

    @Override
    public void onAnimationStart(Animation animation) {
        animationState = AnimationState.PENDING;
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (animation == scaleIn)
            animationState = AnimationState.SHOWN;

        if (animation == scaleOut)
            animationState = AnimationState.HIDDEN;

        // The user might have changed the page while the animation was playing.
        // If everything went appropriately the function should do nothing.
        performAnimation();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private enum AnimationState {
        SHOWN,
        PENDING,
        HIDDEN
    }
}
