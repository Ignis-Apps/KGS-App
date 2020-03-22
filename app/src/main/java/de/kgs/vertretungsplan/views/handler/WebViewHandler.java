package de.kgs.vertretungsplan.views.handler;

import android.app.ProgressDialog;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.views.NavigationItem;

public class WebViewHandler extends WebView implements Broadcast.Receiver {

    private static final String LOADING_MSG = "Lädt ...";

    private boolean clearHistoryWhenLoaded;
    private Broadcast broadcast;
    private ProgressDialog progressDialog;

    public WebViewHandler(Context context) {
        super(context);
        initView();
    }

    public WebViewHandler(Context context, Broadcast broadcast2) {
        this(context);
        this.broadcast = broadcast2;
    }

    private void initView() {
        setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (clearHistoryWhenLoaded) {
                    clearHistory();
                    clearHistoryWhenLoaded = false;
                }
                dismissLoadingDialog();
                setVisibility(VISIBLE);
            }
        });
        setVisibility(GONE);
    }

    public void loadWebPage(String url, boolean allowJs) {
        broadcast.subscribe(this, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
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
        if (current != NavigationItem.NEWS && current != NavigationItem.APPOINTMENTS && current != NavigationItem.PRESS) {
            close();
        }
    }
}
