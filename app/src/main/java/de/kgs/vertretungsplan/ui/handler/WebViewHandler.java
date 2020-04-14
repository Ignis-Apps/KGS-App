package de.kgs.vertretungsplan.ui.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.ui.NavigationItem;

public class WebViewHandler extends WebView implements Broadcast.Receiver {

    private static final String LOADING_MSG = "Lädt ...";

    private boolean clearHistoryWhenLoaded;
    private Broadcast broadcast;
    private ProgressDialog progressDialog;
    private Trace webPageLoadingTrace = FirebasePerformance.getInstance().newTrace("load_webpage");

    public WebViewHandler(Context context) {
        super(context);
        initView();
    }

    public WebViewHandler(Context context, Broadcast broadcast) {
        super(context);
        this.broadcast = broadcast;
        initView();
    }

    private void initView() {
        setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webPageLoadingTrace.start();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (clearHistoryWhenLoaded) {
                    clearHistory();
                    clearHistoryWhenLoaded = false;
                }
                webPageLoadingTrace.stop();
                dismissLoadingDialog();
                setVisibility(VISIBLE);
            }
        });
        setVisibility(GONE);
        broadcast.subscribe(this, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);

    }

    public void loadWebPage(String url, boolean allowJs) {
        progressDialog = ProgressDialog.show(getContext(), null, LOADING_MSG, true);
        clearHistoryWhenLoaded = true;
        getSettings().setJavaScriptEnabled(allowJs);
        loadUrl(url);
    }

    public void close() {
        dismissLoadingDialog();
        getSettings().setJavaScriptEnabled(false);
        setVisibility(GONE);
        clearHistory();
    }

    public boolean consumesBackPress() {

        if (getVisibility() == GONE)
            return false;

        if (canGoBack()) {
            goBack();
        } else {
            close();
        }
        broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
        return true;
    }

    private void dismissLoadingDialog() {
        if (this.progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onEventTriggered(BroadcastEvent event) {

        NavigationItem current = ApplicationData.getInstance().getCurrentNavigationItem();
        System.out.println("Trigger");
        if (current.isOnPageViewer()) {
            close();
            return;
        }

        GlobalVariables gv = GlobalVariables.getInstance();
        System.out.println("Loading");
        switch (current) {

            case NEWS:
                loadWebPage(gv.school_news_url, false);
                break;
            case APPOINTMENTS:
                loadWebPage(gv.school_events_url, false);
                break;
            case PRESS:
                loadWebPage(gv.school_press_url, false);
                break;
            case STUDENT_NEWS_PAPER:
                loadWebPage(gv.student_newspaper, true);
                break;

        }

    }
}
