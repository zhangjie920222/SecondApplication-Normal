package com.ngbj.browser2.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.ngbj.browser2.R;
import com.ngbj.browser2.adpter.HomeFragmentAdapter;
import com.ngbj.browser2.adpter.KeyHistoryAdapter;
import com.ngbj.browser2.adpter.MyRecyclerAdapter;
import com.ngbj.browser2.base.BaseActivity;
import com.ngbj.browser2.bean.AdBean;
import com.ngbj.browser2.bean.AdObjectBean;
import com.ngbj.browser2.bean.BigModelCountData;
import com.ngbj.browser2.bean.CountData;
import com.ngbj.browser2.bean.HistoryData;
import com.ngbj.browser2.bean.KeyBean;
import com.ngbj.browser2.bean.LoginBean;
import com.ngbj.browser2.bean.ModelBean;
import com.ngbj.browser2.bean.OriData;
import com.ngbj.browser2.bean.StatisticsBean;
import com.ngbj.browser2.bean.UpHistoryBean;
import com.ngbj.browser2.bean.UploadCountBean;
import com.ngbj.browser2.constant.ApiConstants;
import com.ngbj.browser2.db.DBManager;
import com.ngbj.browser2.dialog.IosAlertDialog;
import com.ngbj.browser2.event.ChangeFragmentEvent;
import com.ngbj.browser2.event.UpdateEvent;
import com.ngbj.browser2.network.retrofit.helper.RetrofitHelper;
import com.ngbj.browser2.network.retrofit.response.BaseListSubscriber;
import com.ngbj.browser2.network.retrofit.response.BaseObjectSubscriber;
import com.ngbj.browser2.network.retrofit.response.ResponseSubscriber;
import com.ngbj.browser2.util.DeviceIdHepler;
import com.ngbj.browser2.util.SPHelper;
import com.ngbj.browser2.util.StringUtils;
import com.ngbj.browser2.util.ToastUtil;
import com.ngbj.browser2.view.CustomDecoration;
import com.ngbj.browser2.view.myview.Tool;
import com.socks.library.KLog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/***
 * 1.??????????????? ???????????? ?????? -- ??????
 * 2.?????? -- ????????????api ?????? -- ??????????????? --
 * 3.???????????? ???????????????
 * 4.???????????????????????????????????????EditText???????????????
 */

public class SearchActivity extends BaseActivity {

    DBManager dbManager = DBManager.getInstance(this);
    @BindView(R.id.center_title)
    EditText et_search;

    @BindView(R.id.history_ll)
    LinearLayout history_ll;

    @BindView(R.id.mRecyclerView)
    RecyclerView mRecyclerView;


    @BindView(R.id.id_flowlayout)
    TagFlowLayout tagFlowLayout;



    AdBean adBean;
    List<AdBean> adBeanList = new ArrayList<>();
    LinearLayoutManager layoutManager;
    List<KeyBean> keyList = new ArrayList<>();
    KeyHistoryAdapter myRecyclerAdapter;

    SimpleDateFormat simpleDateFormat;
    Date date;
    HashMap<String,String> map = new HashMap<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }


    @Override
    protected void initDatas() {
        //??????
        getData();
        initRecycleView();
        initEvent();
    }


    private void getData() {
//        adBean = new AdBean();
//        for (int i = 0; i < 5; i++) {
//            adBean.setTitle("??????");
//            adBeanList.add(adBean);
//        }

        // {"success":true,"code":200,"data":[{"id":"36","title":"???????????????","link":"1","img_url":"","begin_time":"0","end_time":"0","type":"2","show_position":"4","order":"1"}],"message":"OK"}
        RetrofitHelper.getAppService()
                .getAdHotData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new BaseListSubscriber<AdBean>() {
                    @Override
                    public void onSuccess(List<AdBean> t) {
                      adBeanList.addAll(t);
                        initData();
                    }
                });



    }

    private void initRecycleView() {
        layoutManager = new LinearLayoutManager(SearchActivity.this);
        //?????????????????????
        mRecyclerView.setLayoutManager(layoutManager);
        //??????????????????????????????????????????
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //??????Adapter
        myRecyclerAdapter = new KeyHistoryAdapter(keyList);
        mRecyclerView.setAdapter(myRecyclerAdapter);
        //?????????
        mRecyclerView.addItemDecoration(new CustomDecoration(this,
                CustomDecoration.VERTICAL_LIST,R.drawable.divider,0));

        //????????????????????????
        List<KeyBean> list =  DBManager.getInstance(this).queryKeyList();
        if(null != list && list.size() != 0){
            keyList.addAll(list);
            myRecyclerAdapter = new KeyHistoryAdapter(keyList);
            mRecyclerView.setAdapter(myRecyclerAdapter);
        }else{
            history_ll.setVisibility(View.GONE);
        }
    }



    private void initEvent() {
        // ????????????????????????????????????
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
//                    KLog.d("????????????");
                } else {
//                    KLog.d("????????????");
                }
                String tempName = et_search.getText().toString();
//                KLog.d("??????????????? :" + tempName);

            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {//????????????action
                        //TODO ???????????????
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        if (TextUtils.isEmpty(et_search.getText().toString().trim())){
                            return true;
                        }

                        queryData( et_search.getText().toString().trim());

                        return true;
                    }
                    return false;
                }
            });


        myRecyclerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if(StringUtils.isFastClick()){
                    KLog.d("??????????????????");
                    return;
                }
                queryData(keyList.get(position).getKeyName());


            }
        });

    }

    private void insertKeyData(String keyName) {
        //??????????????????????????????????????????updata
        KeyBean keyBean = dbManager.queryKey(keyName);
        simpleDateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss");// HH:mm:ss
        //??????????????????
        date = new Date(System.currentTimeMillis());
        if(null == keyBean){
            keyBean = new KeyBean(keyName);
            keyBean.setCurrentTime(simpleDateFormat.format(date));
            dbManager.insertKey(keyBean);
        }else {
            keyBean.setCurrentTime(simpleDateFormat.format(date));
            dbManager.updateKeyBean(keyBean);
        }

    }

    boolean isNetwork;
    private void queryData(String content) {
        isNetwork = (boolean) SPHelper.get(SearchActivity.this,"is_network",false);
        if(isNetwork){
            insertKeyData(content);
            Intent data = new Intent();
            data.putExtra("content",content);
            setResult(100,data);
            finish();
        }else {
            ToastUtil.showShort(SearchActivity.this,"????????????");
            return;
        }

    }

    private void startToHtml(String content) {
//        Intent intent = new Intent(SearchActivity.this, WebViewTestActivity.class);
//        String url = ApiConstants.BAIDUURL + "s?wd=" + content;//URL???????????????????????????????????????????????????url???????????????
//        intent.putExtra("url",url);
//        intent.putExtra("type","1");
//        startActivity(intent);
    }


    private void initData() {


//        final String [] titls = new String[]{"????????????","????????? ?????????","????????????","???????????????","?????????"};
//        final String [] titls = new String[]{"????????????","????????? ?????????"};
        tagFlowLayout.setAdapter(new TagAdapter<AdBean>(adBeanList) {
            @Override
            public View getView(FlowLayout parent, int position, AdBean adBean) {
                TextView tag_txt ;
                tag_txt = (TextView) LayoutInflater.from(SearchActivity.this).inflate(R.layout.house_type_tag,
                        tagFlowLayout, false);
                tag_txt.setText(adBean.getTitle());
                return tag_txt;
            }
        });

        tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {

                if(StringUtils.isFastClick()){
                    return true;
                }

                KLog.d("?????????????????????" + adBeanList.get(position).toString());
                AdBean adBean = adBeanList.get(position);

                //TODO ?????????4
                //TODO 2018.11.7 ???????????? ??????????????????
                MobclickAgent.onEvent(SearchActivity.this, "HotSearchModel");//?????????????????????
                if(!TextUtils.isEmpty(adBean.getType()) && adBean.getType().equals("0")){//??????
                    map.put("ad_id",adBean.getId());
                    MobclickAgent.onEvent(mContext, "HotSearchAd", map);//??????????????????
                    addAdUserClick(adBean.getId(),"HotSearchAdUserNum");
                }
                addModleUserClick(adBean.getShow_position());//?????????????????????
                KLog.d("HotSearchAdUserNum");
                queryData(adBean.getTitle());


                return true;
            }
        });

    }


    @OnClick(R.id.cancle)
    public void cancle(){
        finish();
    }

    @OnClick(R.id.tv_deleteAll)
    public void tv_deleteAll(){

        final IosAlertDialog iosAlertDialog = new IosAlertDialog(this).builder();
        iosAlertDialog.setPositiveButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBManager.getInstance(SearchActivity.this).deleteAllKeyData();
                //????????????
                history_ll.setVisibility(View.GONE);
            }
        }).setNegativeButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).setTitle("??????").setMsg("????????????????????????").setCanceledOnTouchOutside(false);
        iosAlertDialog.show();


    }


    public void addAdUserClick(String adId,String adModelName){
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        date = new Date(System.currentTimeMillis());  //??????????????????
        String currentYear_Month_Day = simpleDateFormat.format(date);
        KLog.d("???????????????:",currentYear_Month_Day);


        String str2 = (String) SPHelper.get(SearchActivity.this,"last_today_time","");
        KLog.d("??????????????????????????????:",str2);

        if(!TextUtils.isEmpty(str2) && currentYear_Month_Day.compareTo(str2) > 0){//?????????
            StatisticsBean statisticsBean = dbManager.queryAdUser(adId);
            if(null == statisticsBean){
                statisticsBean = new StatisticsBean(adId,currentYear_Month_Day,false);
                dbManager.insertAdUser(statisticsBean);
            }else{
                statisticsBean.setIs_clicked(false);
            }
            KLog.d("statisticsBean: " + statisticsBean.getIs_clicked());

            if(!statisticsBean.isIs_clicked()){//?????????????????????????????????,??????????????????true,????????????????????????
                //??????
                statisticsBean.setIs_clicked(true);
                statisticsBean.setDate(currentYear_Month_Day);
                dbManager.updateAdUser(statisticsBean);
                //??????xml
                SPHelper.put(SearchActivity.this,"last_today_time",currentYear_Month_Day);

                MobclickAgent.onEvent(mContext, adModelName, map);
                KLog.d("?????????" + adModelName);
            }

        }else{//?????? ????????????????????? -- ???????????????????????????
            StatisticsBean statisticsBean = dbManager.queryAdUser(adId);
            if(null == statisticsBean){
                statisticsBean = new StatisticsBean(adId,str2,false);
                dbManager.insertAdUser(statisticsBean);
            }
            KLog.d("statisticsBean: " + statisticsBean.getIs_clicked());


            if(currentYear_Month_Day.compareTo(statisticsBean.getDate()) > 0){
                statisticsBean.setIs_clicked(true);
                statisticsBean.setDate(currentYear_Month_Day);
                dbManager.updateAdUser(statisticsBean);
                MobclickAgent.onEvent(mContext, adModelName, map);
                KLog.d("?????????" + adModelName);
            }else{
                if(!statisticsBean.isIs_clicked()){//??????????????????????????????????????????????????????true
                    statisticsBean.setIs_clicked(true);
                    dbManager.updateAdUser(statisticsBean);
                    MobclickAgent.onEvent(mContext, adModelName, map);
                    KLog.d("?????????" + adModelName);
                }
            }
        }
    }



    public void addModleUserClick(String modelId){
        String modelName = "";
        if(modelId.equals("1")){
            modelName = "NavigationModelUserNum";
        }else  if(modelId.equals("2")){
            modelName = "CoolSiteModelUserNum";
        }else  if(modelId.equals("3")){
            modelName = "TabModelUserNum";
        }else  if(modelId.equals("4")){
            modelName = "HotSearchModelUserNum";
        }


        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        date = new Date(System.currentTimeMillis());  //??????????????????
        String currentYear_Month_Day = simpleDateFormat.format(date);
        KLog.d("???????????????:",currentYear_Month_Day);

        String str2 = (String) SPHelper.get(SearchActivity.this,"last_today_time","");
        KLog.d("??????????????????????????????:",str2);

        if(!TextUtils.isEmpty(str2) && currentYear_Month_Day.compareTo(str2) > 0){//?????????
            ModelBean modelBean = dbManager.queryModelUser(modelId);
            if(null == modelBean){
                modelBean = new ModelBean(modelId,currentYear_Month_Day,false);
                dbManager.insertModelUser(modelBean);
            }else{
                modelBean.setIs_clicked(false);
            }
            KLog.d("modelBean: " + modelBean.getIs_clicked());

            if(!modelBean.isIs_clicked()){//?????????????????????????????????,??????????????????true,????????????????????????
                //??????
                modelBean.setIs_clicked(true);
                modelBean.setDate(currentYear_Month_Day);
                dbManager.updateModelUser(modelBean);
                //??????xml
                SPHelper.put(SearchActivity.this,"last_today_time",currentYear_Month_Day);

                MobclickAgent.onEvent(mContext, modelName);
                KLog.d("?????????" + modelName);
            }

        }else{//?????? ????????????????????? -- ???????????????????????????
            ModelBean modelBean = dbManager.queryModelUser(modelId);
            if(null == modelBean){
                modelBean = new ModelBean(modelId,str2,false);
                dbManager.insertModelUser(modelBean);
            }
            KLog.d("modelBean: " + modelBean.getIs_clicked());

            if(currentYear_Month_Day.compareTo(modelBean.getDate()) > 0){
                modelBean.setIs_clicked(true);
                modelBean.setDate(currentYear_Month_Day);
                dbManager.updateModelUser(modelBean);
                MobclickAgent.onEvent(mContext, modelName);
                KLog.d("?????????" + modelName);
            }else{
                if(!modelBean.isIs_clicked()){//??????????????????????????????????????????????????????true

                    modelBean.setIs_clicked(true);
                    dbManager.updateModelUser(modelBean);
                    MobclickAgent.onEvent(mContext, modelName);
                    KLog.d("?????????" + modelName);
                }
            }
        }
    }






}
