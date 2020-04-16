package de.kgs.vertretungsplan.ui.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.ui.NavigationItem;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class WebViewHandler implements Broadcast.Receiver {

    private static final String LOADING_MSG = "Lädt ...";
    private final Broadcast broadcast;
    private final Trace webPageLoadingTrace = FirebasePerformance.getInstance().newTrace("load_webpage");
    private final Context context;

    private boolean clearHistoryWhenLoaded;
    private ProgressDialog progressDialog;
    private WebView webView;

    public WebViewHandler(Context context, Broadcast broadcast) {
        this.context = context;
        this.broadcast = broadcast;
        broadcast.subscribe(this, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
    }

    private void initView() {
        RelativeLayout contentMain = ((MainActivity) context).findViewById(R.id.contentMainRl);

        webView = new WebView(context);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webPageLoadingTrace.start();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (clearHistoryWhenLoaded) {
                    webView.clearHistory();
                    clearHistoryWhenLoaded = false;
                }
                webPageLoadingTrace.stop();
                dismissLoadingDialog();
                webView.setVisibility(VISIBLE);
            }
        });
        webView.setVisibility(GONE);
        contentMain.addView(webView);


    }

    public void loadWebPage(String url, boolean allowJs) {

        progressDialog = ProgressDialog.show(context, null, LOADING_MSG, true);

        if (webView == null)
            initView();

        clearHistoryWhenLoaded = true;
        webView.getSettings().setJavaScriptEnabled(allowJs);
        webView.loadUrl(url);
    }

    public void close() {

        if (webView == null)
            return;

        dismissLoadingDialog();
        webView.getSettings().setJavaScriptEnabled(false);
        webView.setVisibility(GONE);
        webView.clearHistory();
    }

    public boolean consumesBackPress() {

        if (webView == null || webView.getVisibility() == GONE)
            return false;

        if (webView.canGoBack()) {
            webView.goBack();
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
