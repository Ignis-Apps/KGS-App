package de.kgs.vertretungsplan.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.DataStorage;

public class KgsWebView extends WebView implements Broadcast.Observer {

    private static final String LOADING_MSG = "Lädt ...";

    private boolean clearHistoryWhenLoaded;
    private Broadcast broadcast;
    private ProgressDialog progressDialog;

    public KgsWebView(Context context) {
        super(context);
        initView();
    }

    public KgsWebView(Context context, Broadcast broadcast2) {
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
        this.broadcast.subscribe(this, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
        this.progressDialog = ProgressDialog.show(getContext(), null, LOADING_MSG, true);
        this.clearHistoryWhenLoaded = true;
        getSettings().setJavaScriptEnabled(allowJs);
        loadUrl(url);
    }

    public void close() {
        this.broadcast.unsubscribe(this);
        dismissLoadingDialog();
        getSettings().setJavaScriptEnabled(false);
        setVisibility(GONE);
        clearHistory();
    }

    public boolean consumesBackPress() {
        if (canGoBack()) {
            goBack();
            return true;
        }
        close();
        return false;
    }

    private void dismissLoadingDialog() {
        ProgressDialog progressDialog2 = this.progressDialog;
        if (progressDialog2 != null) {
            progressDialog2.dismiss();
        }
    }

    @Override
    public void onEventTriggered(BroadcastEvent event) {
        NavigationItem current = DataStorage.getInstance().currentNavigationItem;
        if (current != NavigationItem.NEWS && current != NavigationItem.APPOINTMENTS && current != NavigationItem.PRESS) {
            close();
        }
    }
}
