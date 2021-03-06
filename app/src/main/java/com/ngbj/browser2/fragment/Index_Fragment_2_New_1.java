package com.ngbj.browser2.fragment;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.gson.Gson;
import com.ngbj.browser2.R;
import com.ngbj.browser2.activity.SearchActivity;
import com.ngbj.browser2.activity.WebViewGetUrlActivity;
import com.ngbj.browser2.adpter.HomeFragmentAdapter;
import com.ngbj.browser2.adpter.IndexGridViewAdapter;
import com.ngbj.browser2.adpter.IndexViewPagerAdapter;
import com.ngbj.browser2.adpter.Index_Cool_Adapter;
import com.ngbj.browser2.bean.AdBean;
import com.ngbj.browser2.bean.AdObjectBean;
import com.ngbj.browser2.bean.BookMarkData;
import com.ngbj.browser2.bean.CountData;
import com.ngbj.browser2.bean.HistoryData;
import com.ngbj.browser2.bean.WeatherBean;
import com.ngbj.browser2.bean.WeatherSaveBean;
import com.ngbj.browser2.constant.ApiConstants;
import com.ngbj.browser2.db.DBManager;
import com.ngbj.browser2.event.CollectEvent;
import com.ngbj.browser2.event.DataToTopEvent;
import com.ngbj.browser2.event.History_CollectionEvent;
import com.ngbj.browser2.event.NewsShowFragmentEvent;
import com.ngbj.browser2.event.RefreshDataEvent;
import com.ngbj.browser2.event.RefreshDataSecondEvent;
import com.ngbj.browser2.event.TypeEvent;
import com.ngbj.browser2.network.retrofit.helper.RetrofitHelper;
import com.ngbj.browser2.network.retrofit.response.BaseObjectSubscriber;
import com.ngbj.browser2.util.AppUtil;
import com.ngbj.browser2.util.SPHelper;
import com.ngbj.browser2.util.StringUtils;
import com.ngbj.browser2.util.ToastUtil;
import com.ngbj.browser2.view.StickyNavLayout2;
import com.socks.library.KLog;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.DOWNLOAD_SERVICE;

/***
 * ?????????Fragment -- ?????????
 *
 * 1.?????????
 * 0.?????????
 */
public class Index_Fragment_2_New_1 extends BaseFragment {


    @BindView(R.id.viewPager_gridView)
    ViewPager mViewPagerGridView;//??????gridView???VP

    @BindView(R.id.progressBar)
    ProgressBar pg;

    WebView webview;

    @BindView(R.id.webView_ll)
    LinearLayout webView_ll;

    @BindView(R.id.center_title)
    TextView center_title;

    @BindView(R.id.webView_addpart)
    LinearLayout webView_addpart;

    @BindView(R.id.par3)
    LinearLayout par3;

    @BindView(R.id.part1)
    RelativeLayout part1;

    List<AdBean> adTop2BeanList = new ArrayList<>();
    List<String> list_Title = new ArrayList<>();//??????


    private int totalPage;//????????????
    private int mPageSize = 8;//???????????????????????????
    private List<View> viewPagerList;

    GridView gridView;
    IndexGridViewAdapter mIndexGridViewAdapter;


    SimpleDateFormat simpleDateFormat;
    Date date;
    boolean isRefresh ;
    String saveTitle;
    String saveUrl;
    String currentUrl ;
    String currentTitle;


    //TODO ??????V1.1.0
    /** -----------------------------------------------------------------*/
    @BindView(R.id.cool_recycleView)
    RecyclerView cool_recycleView;
    Index_Cool_Adapter indexCoolAdapter;

    private void initCoolRecycleView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),4);
        //?????????????????????
        cool_recycleView.setLayoutManager(layoutManager);
        //??????Adapter
        indexCoolAdapter = new Index_Cool_Adapter(adTop2BeanList);
        cool_recycleView.setAdapter(indexCoolAdapter);
        //???????????????????????? ??????CUSTOM??????
        indexCoolAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        //????????????
        indexCoolAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                AdBean adBean = adTop2BeanList.get(position);
                MobclickAgent.onEvent(getActivity(), "CoolSiteModel");//?????????????????????
                if(!TextUtils.isEmpty(adBean.getType()) && adBean.getType().equals("0")){//??????
                    map.put("ad_id",adBean.getId());
                    MobclickAgent.onEvent(mContext, "CoolSiteAd", map);//??????????????????
                    addAdUserClick(adBean.getId(),"CoolSiteAdUserNum");
                }
                addModleUserClick(adBean.getShow_position());//?????????????????????

                if("1".equals(adBean.getLink())){
                    ToastUtil.customToastGravity(getActivity(),"????????????",2, Gravity.CENTER,0,0);
                    return;
                }
                startWebViewRequestLink(adBean.getLink());
            }
        });
    }


    private void getAdData2_2() {
        indexCoolAdapter.setNewData(adTop2BeanList);
    }

    /** -----------------------------------------------------------------*/


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    //home????????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewsShowFragmentEvent(NewsShowFragmentEvent event) {
        if(event.getType() == 2){
            part1.setClickable(false);
            startWebViewRequest(event.getLink());
        }
    }


    int type = 2 ;
    //???????????????????????????type
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypeEvent(TypeEvent event) {
        type =  event.getType();
    }




    //??????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshDataEvent(RefreshDataEvent event) {
        isRefresh = true;
        if(event.getIndex() == 1){
            if(webView_ll.getVisibility() == View.VISIBLE){
                webview.reload(); //??????
                return;
            }else{
                refreshAdData();
            }
        }
    }

    //?????? ????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCollectEvent(CollectEvent event) {
        if(webView_ll.getVisibility() == View.VISIBLE){
            if(event.getType() == 1){
                saveToBookMarkSql( webview.getTitle(),webview.getUrl());
                Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //???????????? + ?????? ??????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHistory_CollectionEvent(History_CollectionEvent event) {
        if(event.getIndex().equals("2"))
            startWebViewRequestLink(event.getLink());
    }



    private void refreshAdData() {
        isNetwork = (boolean) SPHelper.get(getActivity(),"is_network",false);
        if(isNetwork){
            getHomeData();
        }else{

        }
    }


    @SuppressLint("CheckResult")
    private void getHomeData(){
        //?????????
        RetrofitHelper.getAppService()
                .getAdData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseObjectSubscriber<AdObjectBean>(){
                    @Override
                    public void onSuccess(AdObjectBean adObjectBean) {
                        if(null != adObjectBean){
                            adTop2BeanList.clear();
                            adTop2BeanList.addAll(adObjectBean.getCool_site());
                            changeOriData();
                        }
                    }
                });
    }

    private void sendDataToUm() {

        if(adTop2BeanList != null && adTop2BeanList.size() != 0){
            for (AdBean adBean:adTop2BeanList) {
                if(adBean.getType().equals("0")){
                    map.put("ad_id",adBean.getId());
                    MobclickAgent.onEvent(mContext, "CoolSiteShowAd", map);//??????????????????
//                      KLog.d(" -- CoolSiteShowAd -- ");
                }
            }
        }

    }



    private void transformToCountData(AdBean adBean) {
        countData = new CountData();
        countData.setAdShowName(adBean.getTitle());
        countData.setAd_id(adBean.getId());
        countData.setImg_url(adBean.getImg_url());
        countData.setAd_link(adBean.getLink());
        countData.setType(adBean.getType());
        countData.setShow_num(1);//?????????????????????1??????????????????1??????
        countData.setShow_position(adBean.getShow_position());
        dbManager.insertUser(countData);
    }


    private void addSqlAndToWeb(AdBean adBean, int type) {
        if("1".equals(adBean.getLink())){
            ToastUtil.customToastGravity(getActivity(),"????????????",2, Gravity.CENTER,0,0);
            return;
        }
        startWebViewRequestLink(adBean.getLink());
        addClickCountToSql(adBean.getTitle(),adBean.getId(),type);
    }



    private void changeOriData() {
        //????????????
        getAdData2_2();
    }


    public static Index_Fragment_2_New_1 getInstance(){
        return new Index_Fragment_2_New_1();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.index_big_fragment_1;
    }

    @Override
    protected void initData() {
        initCoolRecycleView();
        refreshAdData();
    }


    private void getAdData2() {

        //????????????????????????????????????????????????Math.ceil(3.5)=4:?????????????????????????????????+1  Math.floor(3.5)=3???????????????  Math.round(3.5)=4:???????????????
        totalPage = (int) Math.ceil(adTop2BeanList.size() * 1.0 / mPageSize);
        viewPagerList = new ArrayList<>();

        for(int i=0;i<totalPage;i++){
            //??????????????????inflate??????????????????
            gridView = (GridView) LayoutInflater.from(getActivity()).inflate(R.layout.index_tag2_item,mViewPagerGridView,false);
            mIndexGridViewAdapter = new IndexGridViewAdapter(getActivity(),adTop2BeanList,i,mPageSize);
            gridView.setAdapter(mIndexGridViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    AdBean adBean = adTop2BeanList.get(position);

                    MobclickAgent.onEvent(getActivity(), "CoolSiteModel");//?????????????????????
                    KLog.d("CoolSiteModel");
                    if(!TextUtils.isEmpty(adBean.getType()) && adBean.getType().equals("0")){//??????
                        map.put("ad_id",adBean.getId());
                        MobclickAgent.onEvent(mContext, "CoolSiteAd", map);//??????????????????
                        addAdUserClick(adBean.getId(),"CoolSiteAdUserNum");
                    }
                    addModleUserClick(adBean.getShow_position());//?????????????????????
                    KLog.d("addModleUserClick");
                    if("1".equals(adBean.getLink())){
                        ToastUtil.customToastGravity(getActivity(),"????????????",2, Gravity.CENTER,0,0);
                        return;
                    }
                    startWebViewRequestLink(adBean.getLink());

//                    AdBean adBean = adTop2BeanList.get(position);
//                    addSqlAndToWeb(adBean,Integer.parseInt(adBean.getShow_position()));
//                    MobclickAgent.onEvent(getActivity(), "cool_ad" + position + 1);//?????????????????????????????????ID

                }
            });
            //?????????GridView????????????View???????????????ViewPager?????????
            viewPagerList.add(gridView);
        }

        //??????ViewPager?????????
        mViewPagerGridView.setAdapter(new IndexViewPagerAdapter(viewPagerList));
    }




    @OnClick(R.id.search_text)
    public void Opensearch(){
        if(StringUtils.isFastClick()){
            return;
        }
        startActivityForResult(new Intent(getActivity(),SearchActivity.class),100);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100){
            if (data != null){

                String content = data.getStringExtra("content");
//                KLog.d("?????????fragment???????????????" + content);
                //TODO ???????????????????????????
                part1.setClickable(true);
                startWebViewRequest(content);

            }
        }else if(resultCode == 200){
            if(data != null){
                String content = data.getStringExtra("content");
                startWebViewRequestNoClean(content);
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();


        if(webview != null){
            webview.onPause();
            webview.pauseTimers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sendDataToUm();

        if(webview != null){
            webview.resumeTimers();
            webview.onResume();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(webview != null){
            webview.destroy();
            webview.removeAllViews();
            webview = null;
        }
    }

    private void endWebView() {
        center_title.setText("");
        if (webview != null) {
            webview.stopLoading();
            webview.clearFormData();
            webview.clearHistory();
            webview.clearView();
            webview.destroy();
            webview = null;
            webView_addpart.removeAllViews();
        }
    }


    private void startWebViewRequestLink(String urlLink) {
        endWebView();
        webview = new WebView(getActivity());
        webview.setId(R.id.webview);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webView_addpart.addView(webview,lp);
        setSetting();
        initWebViewClient();
        initWebChromeClient();
        webview.loadUrl(urlLink);
        webView_ll.setVisibility(View.VISIBLE);
    }


    private void startWebViewRequestNoClean(String content) {
        urlLogin(content);
    }

    private void startWebViewRequest(String content) {
//        KLog.d("???????????????????????????" + content);
        endWebView();
        webview = new WebView(AppUtil.getContext());
        webview.setId(R.id.webview);
        webView_addpart.addView(webview);
        setSetting();
        initWebViewClient();
        initWebChromeClient();
        urlLogin(content);
        webView_ll.setVisibility(View.VISIBLE);
    }

    private void urlLogin(String url) {
        //???????????????url??????????????????
        if (url.length() >= 4 && url.substring(0, 4).equals("http")) {
            if (StringUtils.isUrl(url)) {
                //??????
                webview.loadUrl(url);
            } else {
                //???????????????????????????
                url = ApiConstants.SOUGOU + "web/sl?keyword=" + url;
                webview.loadUrl(url);
            }
        } else {//???????????????url?????????????????????
            String url1 = "http://" + url;
            String url2 = "https://" + url;
            if (StringUtils.isUrl(url1)) {
                //??????
                url1 = ApiConstants.SOUGOU + "web/sl?keyword=" + url;
                webview.loadUrl(url1);
            } else if (StringUtils.isUrl(url2)) {
                //??????
                webview.loadUrl(url2);
            } else {
                //???????????????????????????
//                url = ApiConstants.BAIDUURL + "s?wd=" + url;//URL???????????????????????????????????????????????????url???????????????
                url = ApiConstants.SOUGOU + "web/sl?keyword=" + url;//URL???????????????????????????????????????????????????url???????????????
                webview.loadUrl(url);
            }
        }
    }

    WebSettings webSettings;
    @SuppressLint({"NewApi"})
    private void setSetting() {
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);//????????????js
        webSettings.setSupportZoom(true); //??????????????????
        webSettings.setBuiltInZoomControls(true);
        //???????????????????????? file url ????????? Javascript ????????????????????????(??????http???https??????)
        webview.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webSettings.setUseWideViewPort(true); //????????????????????????webview?????????
        webSettings.setLoadWithOverviewMode(true); // ????????????????????????
        webSettings.setSupportZoom(true); //????????????????????????true??????????????????????????????
        webSettings.setBuiltInZoomControls(true); //????????????????????????????????????false?????????WebView????????????
        webSettings.setDisplayZoomControls(false); //???????????????????????????
        webview.getSettings().setBlockNetworkImage(false); // ?????????????????????
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        //??????????????????
        webSettings.setDatabaseEnabled(true);
        String dir = getActivity().getApplicationContext().getDir("database",Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true); //????????????????????????
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //????????????JS???????????????
        webSettings.setLoadsImagesAutomatically(true); //????????????????????????
        webSettings.setDefaultTextEncodingName("utf-8");//??????????????????
        webSettings.setDomStorageEnabled(true);//??????DOM API

        //?????????????????????????????????????????????????????????????????????????????????HTTP???HTTPS?????????????????????????????????
//        webview.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
//        webview.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
        registerForContextMenu(webview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //???????????????????????????????????????????????????????????????????????????WebView?????????,
            // ??????????????????,WebView????????????????????????????????????????????????????????????????????????????????????.
            // ??????app???????????????????????????????????????????????????
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//??????app??????????????????https???????????????
        }
        //TODO ??????
        webview.setDownloadListener(new MyWebViewDownLoadListener());
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
        final WebView.HitTestResult webViewHitTestResult = webview.getHitTestResult();
        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            contextMenu.setHeaderTitle("?????????????????????");
            contextMenu.add(0, 1, 0, "????????????")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String DownloadImageURL = webViewHitTestResult.getExtra();
                            if (URLUtil.isValidUrl(DownloadImageURL)) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                request.allowScanningByMediaScanner();
                                //???????????????????????????
                                request.setDestinationInExternalPublicDir("SmallBrowse/Pic/",System.currentTimeMillis() + "." + "png");
                                DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);
                                Toast.makeText(getActivity(), "???????????????" + "/SmallBrowse/Pic/?????????", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "????????????", Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
        }
    }


    @BindView(R.id.part3)
    LinearLayout part3;

    @BindView(R.id.edit_title)
    EditText edit_title;


    //????????????????????????
    @OnClick(R.id.part1)
    public void part1(){

        String myUrl = "";
        if(!TextUtils.isEmpty(currentUrl)){
            myUrl= currentUrl;
        }else if(!TextUtils.isEmpty(currentTitle)){
            myUrl = currentTitle;
        }

        if(!TextUtils.isEmpty(myUrl)){
            Intent intent = new Intent(getActivity(), WebViewGetUrlActivity.class);
            intent.putExtra("weburl",myUrl);
            startActivityForResult(intent,200);
        }
    }


    private void initWebViewClient() {
        //??????shouldOverrideUrlLoading()????????????????????????????????????????????????????????? ????????????WebView?????????
        webview.setWebViewClient(new WebViewClient(){
            boolean if_load;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if_load = false;
                currentUrl = url;
//                view.loadUrl(url);//??????????????????????????????
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();// ???????????????????????????
                //super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if_load = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(if_load ){
//               KLog.d("url: " + view.getUrl() + " ?????????"  + view.getTitle() + " ??????????????? " + view.getOriginalUrl());

                    if(webview.getSettings().getCacheMode() == WebSettings.LOAD_DEFAULT){

                        if(null != view.copyBackForwardList().getCurrentItem()){
                            saveTitle = view.copyBackForwardList().getCurrentItem().getTitle();
                            saveUrl = view.copyBackForwardList().getCurrentItem().getUrl();

                            if(!TextUtils.isEmpty(saveTitle) &&
                                    !TextUtils.isEmpty(saveUrl)){
                                //TODO ??????????????????
                                saveToHistorySql(saveTitle,saveUrl);
                            }
                        }
                        if_load = false;
                    }
                }
            }

        });
    }

    private void saveToHistorySql(String saveTitle, String saveUrl) {
        if(!TextUtils.isEmpty(saveTitle) && !TextUtils.isEmpty(saveUrl)){
            HistoryData historyData = new HistoryData();
            historyData.setVisit_link(saveUrl);
            historyData.setTitle(saveTitle);
            historyData.setKeyword(saveTitle);
            historyData.setType("1");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss");// HH:mm:ss
            //??????????????????
            Date date = new Date(System.currentTimeMillis());
            historyData.setCurrentTime(simpleDateFormat.format(date));
            dbManager.insertHistrory(historyData);
        }
    }

    private void saveToBookMarkSql(String saveTitle, String saveUrl) {
        if(!TextUtils.isEmpty(saveTitle) && !TextUtils.isEmpty(saveUrl)){
            BookMarkData bookMarkData = new BookMarkData();
            bookMarkData.setVisit_link(saveUrl);
            bookMarkData.setTitle(saveTitle);
            simpleDateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss");// HH:mm:ss
            date = new Date(System.currentTimeMillis());//????????????
            KLog.d("Date????????????????????????"+ simpleDateFormat.format(date));
            bookMarkData.setCurrentTime(simpleDateFormat.format(date));
            dbManager.insertBookMark(bookMarkData);
        }
    }


    private void initWebChromeClient() {
        //??????????????????
        webview.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100){
                    pg.setVisibility(View.GONE);//??????????????????????????????
                }
                else{
                    pg.setVisibility(View.VISIBLE);//????????????????????????????????????
                    pg.setProgress(newProgress);//???????????????
                }
                super.onProgressChanged(view, newProgress);
            }

            //??????????????????
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                currentTitle = title;
                center_title.setText(title);
                currentUrl = view.getUrl();
            }

        });
    }





}
