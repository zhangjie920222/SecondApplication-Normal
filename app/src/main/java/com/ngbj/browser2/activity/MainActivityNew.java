package com.ngbj.browser2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ngbj.browser2.MyApplication;
import com.ngbj.browser2.R;
import com.ngbj.browser2.adpter.NewWindowAdapter;
import com.ngbj.browser2.bean.NewWindowBean;
import com.ngbj.browser2.bean.NewsBean;
import com.ngbj.browser2.bean.WeatherBean;
import com.ngbj.browser2.dialog.BottomAlertDialog;
import com.ngbj.browser2.dialog.DeleteAlertDialog;
import com.ngbj.browser2.dialog.IosAlertDialog;
import com.ngbj.browser2.event.ChangeFragmentEvent;
import com.ngbj.browser2.event.CollectEvent;
import com.ngbj.browser2.event.CollectHomeEvent;
import com.ngbj.browser2.event.DataToTopEvent;
import com.ngbj.browser2.event.RefreshDataEvent;
import com.ngbj.browser2.event.RefreshHomeDataEvent;
import com.ngbj.browser2.event.TypeEvent;
import com.ngbj.browser2.fragment.Index_Fragment_2_New_1;
import com.ngbj.browser2.fragment.Index_Fragment_3_New_1;
import com.ngbj.browser2.fragment.Index_Fragment_4_New_1;
import com.ngbj.browser2.fragment.Index_Fragment_New_1;
import com.ngbj.browser2.mvp.contract.app.HomeContract;
import com.ngbj.browser2.mvp.presenter.app.HomePresenter;
import com.ngbj.browser2.service.MsgPushService;
import com.ngbj.browser2.service.MyJobService;
import com.ngbj.browser2.util.AppUpdateUtil;
import com.ngbj.browser2.util.AppUpdateUtilByN;
import com.ngbj.browser2.util.AppUtil;
import com.ngbj.browser2.util.BitmapHelper;
import com.ngbj.browser2.util.SDCardHelper;
import com.ngbj.browser2.util.SPHelper;
import com.ngbj.browser2.util.ScreenHelper;
import com.ngbj.browser2.util.StringUtils;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

public class MainActivityNew extends BaseMainActivity<HomePresenter> implements HomeContract.View {

    @BindView(R.id.window_count)
    TextView window_count;

    @BindView(R.id.frameLayout)
    FrameLayout frameLayout;

    @BindView(R.id.recycleView)
    RecyclerView recyclerView;

    @BindView(R.id.new_window_ll)
    LinearLayout new_window_ll;


    Index_Fragment_New_1   index_fragment_new_1;
    Index_Fragment_2_New_1 index_fragment_2_new_1;
    Index_Fragment_3_New_1 index_fragment_3_new_1;
    Index_Fragment_4_New_1 index_fragment_4_new_1;
    Fragment currentFragment;

    private long mExitTime; //??????????????????
    NewWindowAdapter newWindowAdapter;
    List<NewWindowBean> newWindowBeanList = new ArrayList<>();

    LinearLayout ll;//fragment???webview??????
    WebView webview;//fragment???webview??????


    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
            ,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //2???????????????mPermissionList??????????????????????????????????????????????????????????????????mPerrrmissionList???
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;//???????????????

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main_new;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {//6.0??????????????????
            initPermission();
        }
    }

    //?????????????????????
    private void initPermission() {
        mPermissionList.clear();//???????????????????????????
        //?????????????????????????????????????????????
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//???????????????????????????
            }
        }

        //????????????
        if (mPermissionList.size() > 0) {//????????????????????????????????????
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }


    //??????????????????????????????
    //????????? requestCode  ???????????????????????????????????????
    //????????? permissions  ????????????????????????????????????
    //????????? grantResults ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????0?????????????????????-1?????????????????????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//?????????????????????
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                    //???????????????????????????????????????
                    boolean showRequestPermission = ActivityCompat
                            .shouldShowRequestPermissionRationale(MainActivityNew.this, permissions[i]);
                    KLog.d(showRequestPermission);
                }
            }
            //?????????????????????????????? -- ???????????????
//            if (hasPermissionDismiss) {
//                showPermissionDialog();//????????????????????????????????????????????????????????????????????????????????????
//                KLog.d("what?");
//            }else{
//                //?????????????????????????????????????????????????????????
//                goToNextPage();
//            }
        }

    }


    public void toOutLinkForDefaultBrowser(Intent intent){
        //???????????????
        String webUri;//????????????????????????
        Uri uri = intent.getData();
        if(null != uri) {
//            KLog.d("uri: " + uri);
            if (!TextUtils.isEmpty(uri.toString())) {
                webUri = uri.toString();
                Intent intent2 = new Intent(MainActivityNew.this, WebViewHao123Activity.class);
                intent2.putExtra("url", webUri);
                startActivity(intent2);

            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //TODO ??????
        toOutLinkForDefaultBrowser(intent);
    }

    private void doService() {
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, MyJobService.class));  //????????????JobService????????????
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS); //???????????????????????????
            builder.setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);  //???????????????????????????
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING);  //?????????????????????
            builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR);//??????????????????
            builder.setRequiresCharging(false); // ???????????????
            jobScheduler.schedule(builder.build());
        }
    }

    //????????????
    public void bindLife(){
        doService();
    }

    @SuppressLint("NewApi")
    @Override
    protected void initDatas() {
        //TODO ??????
        toOutLinkForDefaultBrowser(getIntent());
        //TODO
        bindLife();
//        bindService();

        //?????????????????????????????????????????????fragment
        initFragment1();
        initRecycleView();
        initEvent();
        EventBus.getDefault().register(this);
    }

    private void bindService() {
        Intent service = new Intent(this, MsgPushService.class);
        startService(service);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void initInject() {
//        String s1 = "1.0.1";
//        String s2 = "1.0";
//       if(  s1.compareTo(s2) > 0){//t1 > t2
//           KLog.d("??????");
//       }
        //TODO ????????????
        String myVersion = AppUtil.getVersionName(this);
        String lastVersion = (String) SPHelper.get(this,"lastVersion","1.0");
//      KLog.d("myVersion: " + myVersion + " " + "lastVersion:"+ lastVersion );
        String apkUrl = (String) SPHelper.get(this,"downlink","");

        //TODO ??????
//        apkUrl = "http://qn.xnapp.com/1015975425bce7558d5ddf8.00204010.apk";
        if(lastVersion.compareTo(myVersion) > 0){
            //TODO ????????????
            if (Build.VERSION.SDK_INT >= 23) {
                AppUpdateUtilByN appUpdateUtilByN = new AppUpdateUtilByN(MainActivityNew.this,apkUrl);
                appUpdateUtilByN.showUpdateNoticeDialog("???????????????");
            }else {
                AppUpdateUtil appUpdateUtil = new AppUpdateUtil(MainActivityNew.this,apkUrl);
                appUpdateUtil.showUpdateNoticeDialog("???????????????");
            }
        }
    }





    //?????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshHomeDataEvent(RefreshHomeDataEvent event) {
        //????????????Fragment
        if(currentFragment != null && currentFragment instanceof Index_Fragment_New_1){
            EventBus.getDefault().post(new RefreshDataEvent(0));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_2_New_1){
            EventBus.getDefault().post(new RefreshDataEvent(1));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_3_New_1){
            EventBus.getDefault().post(new RefreshDataEvent(2));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_4_New_1){
            EventBus.getDefault().post(new RefreshDataEvent(3));
        }
    }

    //?????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCollectHomeEvent(CollectHomeEvent event) {
        if(currentFragment != null && currentFragment instanceof Index_Fragment_New_1){
            EventBus.getDefault().post(new CollectEvent(0));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_2_New_1){
            EventBus.getDefault().post(new CollectEvent(1));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_3_New_1){
            EventBus.getDefault().post(new CollectEvent(2));
        }else if(currentFragment != null && currentFragment instanceof Index_Fragment_4_New_1){
            EventBus.getDefault().post(new CollectEvent(3));
        }
    }

    //?????????Fragment
    private void initFragment1() {
        //???????????????fragment?????????????????????????????????
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //??????????????????add???????????????fragment?????????????????????????????????null???new??????
        if(index_fragment_new_1 == null){
            index_fragment_new_1 = Index_Fragment_New_1.getInstance();
            transaction.add(R.id.frameLayout, index_fragment_new_1);
        }
        currentFragment = index_fragment_new_1;
        //????????????
        window_count.setText("1");
        //????????????
        transaction.commit();
    }

     //??????Fragment
      private void showFragment(Fragment fragment) {
            if (currentFragment != fragment) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.hide(currentFragment);
                     currentFragment = fragment;
                     if (!fragment.isAdded()) {
                             transaction.add(R.id.frameLayout, fragment).show(fragment).commit();
                         } else {
                             transaction.show(fragment).commit();
                         }
                 }
             }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO ???????????? 2018.10.21 ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        uploadAdBigModel();
        //TODO ??????????????????
        uploadHistoryList();
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mReturningWithResult = true;
//        resultCode1 = resultCode;
//    }
//
//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        if (mReturningWithResult) {
//            // Commit your transactions here.
//        }
//            // Reset the boolean flag back to false for next time.
//            mReturningWithResult = false;
//        }

    private void change201() {
        Fragment myCurrentFragment = null;
        if(index_fragment_new_1 == null){
            index_fragment_new_1 = Index_Fragment_New_1.getInstance();
            myCurrentFragment = index_fragment_new_1;
        }else if(index_fragment_2_new_1 == null){
            index_fragment_2_new_1 = Index_Fragment_2_New_1.getInstance();
            myCurrentFragment = index_fragment_2_new_1;
        }else if(index_fragment_3_new_1 == null){
            index_fragment_3_new_1 = Index_Fragment_3_New_1.getInstance();
            myCurrentFragment = index_fragment_3_new_1;
        }else if(index_fragment_4_new_1 == null){
            index_fragment_4_new_1 = Index_Fragment_4_New_1.getInstance();
            myCurrentFragment = index_fragment_4_new_1;
        }
        showFragment(myCurrentFragment);
    }

    //type -- bitmap -- fragment
    @OnClick(R.id.index_new)
    public void openNewWindow(){
        Bitmap bitmap = ScreenHelper.takeScreenShot(this);
        String name = StringUtils.getCurrentFragmentName(currentFragment);
        BitmapHelper.storeImageCache(bitmap, name,this);
        //TODO ??????Bean,?????????????????????????????????
        NewWindowBean newWindowBean = new NewWindowBean();
        newWindowBean.setType(StringUtils.getFragmentTyep(name));
        newWindowBeanList.add(0,newWindowBean);
        newWindowAdapter.notifyDataSetChanged();
        new_window_ll.setVisibility(View.VISIBLE);

    }


    private void initRecycleView() {
        GridLayoutManager layoutManager = new GridLayoutManager(MainActivityNew.this,2);
        recyclerView.setLayoutManager(layoutManager);//?????????????????????
        recyclerView.setItemAnimator(new DefaultItemAnimator());//?????????????????????????????????
        newWindowAdapter = new NewWindowAdapter(newWindowBeanList);
        recyclerView.setAdapter(newWindowAdapter);
    }


    private void initEvent() {
        // ??????
        newWindowAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                //?????????????????????????????????  -- ?????????activity??????fragment
                String myType = newWindowBeanList.get(position).getType();
                removeFragment(myType);
                newWindowBeanList.remove(position);
                newWindowAdapter.notifyDataSetChanged();
                if(newWindowBeanList.size() == 0){
                    //????????????fragment
                    initFragment1();
                    new_window_ll.setVisibility(View.GONE);
                }
            }
        });
        //??????????????????
        newWindowAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String myType = newWindowBeanList.get(position).getType();
                //TODO
                window_count.setText( getFragmentSize() + "");
                EventBus.getDefault().post(new TypeEvent(Integer.parseInt(myType)));
                Fragment mFragment = getFragment(myType);
                showFragment(mFragment);
                //?????????????????????????????????
                newWindowBeanList.remove(position);
                new_window_ll.setVisibility(View.GONE);
            }
        });
    }


    //??????type??????????????????Fragment
    private  Fragment getFragment(String myType){
        Fragment mFragment = null;
        if(myType.equals("1")){
            mFragment = index_fragment_new_1;
        }else if(myType.equals("2")){
            mFragment = index_fragment_2_new_1;
        }else if(myType.equals("3")){
            mFragment = index_fragment_3_new_1;
        }else if(myType.equals("4")){
            mFragment = index_fragment_4_new_1;
        }
        KLog.d("mFragment: " + mFragment);
        return mFragment;
    }

    //???activity?????????
    private void removeFragment(String type) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getFragment(type);
        transaction.remove(fragment);
        transaction.commit();
        //TODO ???????????????????????????????????????null
        if(fragment == index_fragment_new_1){
            index_fragment_new_1 = null;
        }else if(fragment == index_fragment_2_new_1){
            index_fragment_2_new_1 = null;
        }else if(fragment == index_fragment_3_new_1){
            index_fragment_3_new_1 = null;
        }else if(fragment == index_fragment_4_new_1){
            index_fragment_4_new_1 = null;
        }
    }


    //??????
    @OnClick(R.id.delete_window)
    public void deleteWindow(){
        DeleteAlertDialog deleteAlertDialog =  new DeleteAlertDialog(this).builder().setContextText("??????????????????");
        deleteAlertDialog.setDeleteButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < newWindowBeanList.size(); i++) {
                    removeFragment(newWindowBeanList.get(i).getType());
                }
                newWindowBeanList.clear();
                initFragment1();
                new_window_ll.setVisibility(View.GONE);
            }
        });
        deleteAlertDialog.show();
    }

    //??????
    @OnClick(R.id.new_window_btn)
    public void new_window_btn(){
        if(StringUtils.isFastClick()){
            return;
        }

        if(newWindowBeanList != null && newWindowBeanList.size() == 4){
            Toast.makeText(this,"????????????????????????",Toast.LENGTH_SHORT).show();
            return;
        }
         new_window_ll.setVisibility(View.GONE);
         change201();
         window_count.setText(  getFragmentSize() + "");
    }

    @OnClick(R.id.index_menu)
    public void openMenu(){
        if(StringUtils.isFastClick()){
            return;
        }

        String name = StringUtils.getCurrentFragmentName(currentFragment);
        String type = StringUtils.getFragmentTyep(name);
        new BottomAlertDialog(this)
                .builder()
                .setCanceledOnTouchOutside(true)
                .setType(type)
                .show();
    }


    private void endWebView() {
        if (webview != null) {
            webview.stopLoading();
            webview.destroy();
            webview = null;
        }
    }


    @OnClick(R.id.index_home)
    public void index_home(){
        //?????????WebView????????????
        getSubWebViewLayout();
        //TODO ll?????????fragment????????????WebView?????????
        if(ll.getVisibility() == View.VISIBLE){
            endWebView();
            ll.setVisibility(View.GONE);
            return;
        }else{
            int index = (int) SPHelper.get(this,"home_fragment_posotion",0);
            EventBus.getDefault().post(new DataToTopEvent(index));
        }
    }

    private void getSubWebViewLayout() {
         ll =  currentFragment.getView().findViewById(R.id.webView_ll);
         webview =  currentFragment.getView().findViewById(R.id.webview);
    }


    @Override
    public void showNewsData( NewsBean newsBean) {
    }

    @Override
    public void showWeatherData(final WeatherBean weatherBean) { }


    //????????????????????? :??????fragment ??? ??????type?????????fragment?????????
    @OnClick(R.id.new_window_back)
    public void newWindowBack() {
        if(new_window_ll.getVisibility() == View.VISIBLE){
            String name = StringUtils.getCurrentFragmentName(currentFragment);
            String fragment_type = StringUtils.getFragmentTyep(name);
            boolean isExit  = false;
            for (int i = 0; i < newWindowBeanList.size(); i++) {
                if(newWindowBeanList.get(i).getType().equals(fragment_type)){
                    isExit = true;
                    break;
                }
            }

            if(!isExit){//???????????????????????????????????????fragment,??????????????????
                showFragment(getFragment(newWindowBeanList.get(newWindowBeanList.size() - 1).getType()));
            }

            new_window_ll.setVisibility(View.GONE);
            newWindowBeanList.remove(newWindowBeanList.size() - 1);
            window_count.setText( getFragmentSize() + "");
            return ;
        }
    }



    //????????????????????????
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
            //?????????
            if(new_window_ll.getVisibility() == View.VISIBLE){
                String name = StringUtils.getCurrentFragmentName(currentFragment);
                String fragment_type = StringUtils.getFragmentTyep(name);
                boolean isExit  = false;
                for (int i = 0; i < newWindowBeanList.size(); i++) {
                    if(newWindowBeanList.get(i).getType().equals(fragment_type)){
                        isExit = true;
                        break;
                    }
                }

                if(!isExit){//???????????????????????????????????????fragment,??????????????????
                    showFragment(getFragment(newWindowBeanList.get(newWindowBeanList.size() - 1).getType()));
                }

                new_window_ll.setVisibility(View.GONE);
                newWindowBeanList.remove(newWindowBeanList.size() - 1);
                //TODO ????????????????????????
                window_count.setText( getFragmentSize() + "");
                SDCardHelper.removeFileFromSDCard(SDCardHelper.getSDCardPrivateCacheDir(this) + "/" +  name + ".jpg");
                return true;
            }

            //Fragment1 + Fragment2 + ....
             return goBackLogic(currentFragment);

        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.index_back)
    public void index_back(){
        backLogic();
    }

    private void backLogic() {
        //?????????
        if(new_window_ll.getVisibility() == View.VISIBLE){
            //?????????????????????
            window_count.setText( getFragmentSize() + "");

            String name = StringUtils.getCurrentFragmentName(currentFragment);
            String fragment_type = StringUtils.getFragmentTyep(name);
            boolean isExit  = false;
            for (int i = 0; i < newWindowBeanList.size(); i++) {
                if(newWindowBeanList.get(i).getType().equals(fragment_type)){
                    isExit = true;
                    break;
                }
            }

            if(!isExit){//???????????????????????????????????????fragment,???????????????
                showFragment(getFragment(newWindowBeanList.get(0).getType()));
            }

            new_window_ll.setVisibility(View.GONE);
            //TODO ?????????????????????????????????Bean???????????????
            newWindowBeanList.remove(newWindowBeanList.size() - 1);
            SDCardHelper.removeFileFromSDCard(SDCardHelper.getSDCardPrivateCacheDir(this) + "/" +  name + ".jpg");
            return ;
        }

        //Fragment1 + Fragment2 + ....
        goBackNotLogic(currentFragment);
    }

    //??????fragment?????????

    private int getFragmentSize() {
        int count = 0;
        if(index_fragment_new_1 != null){
            count++;
        }
        if(index_fragment_2_new_1 != null){
            count++;
        }
        if(index_fragment_3_new_1 != null){
            count++;
        }
        if(index_fragment_4_new_1 != null){
            count++;
        }
        return count;
    }

    private void goBackNotLogic(Fragment currentFragment) {
        //Fragment1
        if (currentFragment != null && currentFragment instanceof Index_Fragment_New_1){

            getSubWebViewLayout();

            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    webview.goBack();
                    return ;
                }else{
                    ll.setVisibility(View.GONE);
                    return ;
                }
            }else{
                exit();
                return ;
            }
        }

        //Fragment2
        if (currentFragment != null && currentFragment instanceof Index_Fragment_2_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return ;
                }else{
                    ll.setVisibility(View.GONE);
                    return ;
                }
            }else{
                exit();
                return ;
            }
        }

        //Fragment3
        if (currentFragment != null && currentFragment instanceof Index_Fragment_3_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return ;
                }else{
                    ll.setVisibility(View.GONE);
                    return ;
                }
            }else{
                exit();
                return ;
            }
        }

        //Fragment4
        if (currentFragment != null && currentFragment instanceof Index_Fragment_4_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return ;
                }else{
                    ll.setVisibility(View.GONE);
                    return ;
                }
            }else{
                exit();
                return ;
            }
        }
    }


    private boolean goBackLogic(Fragment currentFragment) {
        //Fragment1
        if (currentFragment != null && currentFragment instanceof Index_Fragment_New_1){

            getSubWebViewLayout();

            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return true;
                }else{
                    ll.setVisibility(View.GONE);
                    return true;
                }
            }else{
                exit();
                return true;
            }
        }

        //Fragment2
        if (currentFragment != null && currentFragment instanceof Index_Fragment_2_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return true;
                }else{
                    ll.setVisibility(View.GONE);
                    return true;
                }
            }else{
                exit();
                return true;
            }
        }

        //Fragment3
        if (currentFragment != null && currentFragment instanceof Index_Fragment_3_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return true;
                }else{
                    ll.setVisibility(View.GONE);
                    return true;
                }
            }else{
                exit();
                return true;
            }
        }

        //Fragment4
        if (currentFragment != null && currentFragment instanceof Index_Fragment_4_New_1){
            getSubWebViewLayout();
            if(ll.getVisibility() == View.VISIBLE){
                if(webview != null && webview.canGoBack()){
                    webview.goBack();
                    return true;
                }else{
                    ll.setVisibility(View.GONE);
                    return true;
                }
            }else{
                exit();
                return true;
            }
        }

        return false;
    }

    long currentTime;
    public void exit() {
        currentTime = System.currentTimeMillis();
        if ((currentTime - mExitTime) > 2000) {
            Toast.makeText(MainActivityNew.this, "??????????????????", Toast.LENGTH_SHORT).show();
            mExitTime = currentTime;
        } else {

            int count = (int) SPHelper.get(this,"count",0);
//            KLog.d("??????count: "+ count + " count % 2 " + (count % 2) );
            if(count % 5 == 0 ){
                boolean isg =  hasMyDefault(MainActivityNew.this);
                Message message = new Message();
                message.what = 100;
                message.obj = isg;
                handler.sendMessage(message);
            }else{
                MyApplication.getInstance().exitApp();
            }


        }
    }

    //????????????????????????????????????
    public final boolean hasMyDefault( Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.niaowifi.com/"));
        PackageManager pm = context.getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // ????????????????????????????????????????????????
        @SuppressLint("WrongConstant")
        List<ResolveInfo> resolveInfoList = pm
                .queryIntentActivities(intent,
                        PackageManager.GET_INTENT_FILTERS);
        KLog.d(resolveInfoList.size());
        for (int i = 0; i < resolveInfoList.size(); i++) {
            ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
            String packageName = activityInfo.packageName;
            if(packageName.equals(info.activityInfo.packageName)){
                if(packageName.equals(getPackageName())){//?????????????????????????????????
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private void setDefaultBrowser() {
        final IosAlertDialog iosAlertDialog = new IosAlertDialog(this).builder();
        iosAlertDialog.setPositiveButton("?????????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityNew.this,SetBrowserActivity.class));
            }
        }).setNegativeButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO ?????????????????? ?????????????????????
                MyApplication.getInstance().exitApp();
            }
        }).setTitle("??????").setMsg("????????????"+ getString(R.string.app_name)
                + "??????????????????").setCanceledOnTouchOutside(false);
        iosAlertDialog.show();
    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 100){
                boolean isg = (boolean) msg.obj;
                if(!isg){
                    setDefaultBrowser();
                }else
                    MyApplication.getInstance().exitApp();
            }
        }
    };

}
