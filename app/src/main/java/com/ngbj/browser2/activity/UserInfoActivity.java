package com.ngbj.browser2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.ngbj.browser2.MyApplication;
import com.ngbj.browser2.R;
import com.ngbj.browser2.bean.AdObjectBean;
import com.ngbj.browser2.bean.LoginBean;
import com.ngbj.browser2.bean.LoginOutBean;
import com.ngbj.browser2.bean.UserInfoBean;
import com.ngbj.browser2.db.DBManager;
import com.ngbj.browser2.dialog.DeleteAlertDialog;
import com.ngbj.browser2.dialog.IosAlertDialog;
import com.ngbj.browser2.event.CleanHistoryEvent;
import com.ngbj.browser2.event.CloseEvent;
import com.ngbj.browser2.event.UpdateEvent;
import com.ngbj.browser2.network.retrofit.helper.RetrofitHelper;
import com.ngbj.browser2.network.retrofit.response.BaseObjectSubscriber;
import com.ngbj.browser2.network.retrofit.response.BaseSubscriber;
import com.ngbj.browser2.network.retrofit.response.ResponseSubscriber;
import com.ngbj.browser2.util.BitmapHelper;
import com.ngbj.browser2.util.FileUtils;
import com.ngbj.browser2.util.SDCardHelper;
import com.ngbj.browser2.util.SPHelper;
import com.ngbj.browser2.util.ToastUtil;
import com.ngbj.browser2.view.AvatarWindow;
import com.ngbj.browser2.view.GenderWindow;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


/***
 *
 */

public class UserInfoActivity extends CommonHeadActivity {

    @BindView(R.id.root_layout)
    RelativeLayout root_layout;//????????????


    @BindView(R.id.head_icon)
    CircleImageView head_icon;//??????


    @BindView(R.id.name)
    TextView name;//??????

    @BindView(R.id.nickname)
    TextView nickname;//??????

    @BindView(R.id.sex)
    TextView sex;//??????

    @BindView(R.id.phone)
    TextView phone;

    String imgName;
    String nameName;
    String sexName;
    String phoneName;

    AvatarWindow avatarWindow;
    GenderWindow genderWindow;
    String gender;
    Bitmap bitmap;//?????????????????????
    Uri uri;//?????????uri
    int type;//1 ??????  2 ??????

    DBManager dbManager;
    UserInfoBean userInfoBean;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_userinfo;
    }

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

    //?????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(UpdateEvent event) {
        nameName = event.getLoginBean().getData().getNickname();
        name.setText(nameName);
        nickname.setText(nameName);
    }


    //??????????????????????????? 0 ??? 1 ???
    @Override
    protected void initDatas() {
        dbManager = DBManager.getInstance(this);
        center_title.setText("????????????");

        if(dbManager.queryUserInfo() != null && dbManager.queryUserInfo().size() != 0){
            userInfoBean = dbManager.queryUserInfo().get(0);
            nameName = userInfoBean.getNickname();
            sexName = userInfoBean.getGender() + "";
            imgName= userInfoBean.getHead_img();
            phoneName = userInfoBean.getMobile();
        }


        if(!TextUtils.isEmpty(imgName)){
            Glide.with(mContext)
                    .load(imgName)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(head_icon);
        }

        if(sexName.equals("1")){
            sex.setText("???");
        }else
            sex.setText("???");



        name.setText(nameName);
        nickname.setText(nameName);
        phone.setText(phoneName);

        initGender();

        //TODO ????????????
        EventBus.getDefault().post(new CloseEvent());
    }

    private void initGender() {
        genderWindow = new GenderWindow(this,root_layout);
        genderWindow.setCallBack(new GenderWindow.CallBack() {
            @Override
            public void sure(boolean isMan) {
//                gender = isMan?"???":"???";
                gender = isMan?"1":"0";
                sex.setText(isMan?"???":"???");
                uploadSex();
                KLog.d(gender);
            }
        });
    }

    @OnClick(R.id.back)
    public void toBack(){
        finish();
    }


    @OnClick(R.id.nickname_layout)
    public void nickname_layout(){
        Intent intent  = new Intent(this,UserInfoModigfyActivity.class);
        intent.putExtra("type","1");
        intent.putExtra("name",nameName);
        startActivity(intent);
    }

    @OnClick(R.id.sex_layout)
    public void sex_layout(){
        genderWindow.showGenderWindow();
    }


    @OnClick(R.id.phone_layout)
    public void phone_layout(){
        //????????????????????? -- ?????????????????????
        dbManager.deleteUserInfo();
        SPHelper.put(this,"isLogin",false);

        Intent intent = new Intent(this,LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }


    @OnClick(R.id.exit)
    public void exit(){
        logout2();
    }

    private void logout2() {

        final IosAlertDialog iosAlertDialog = new IosAlertDialog(this).builder();
        iosAlertDialog.setPositiveButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 logout();
            }
        }).setNegativeButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).setTitle("??????").setMsg("?????????????").setCanceledOnTouchOutside(false);
        iosAlertDialog.show();
    }

    @SuppressLint("CheckResult")
    private void logout() {
        SPHelper.put(UserInfoActivity.this,"isLogin",false);
        SPHelper.put(UserInfoActivity.this,"token","");
        DBManager.getInstance(UserInfoActivity.this).deleteUserInfo();
        finish();
    }


    //??????
    @OnClick(R.id.head_icon)
    public void head_icon(){
        avatarWindow = new AvatarWindow(this,root_layout);
        avatarWindow.showAvatarWindow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case AvatarWindow.REQUEST_IMAGE_CODE:
                    if (data != null) {
                        //???????????????uri
                        Uri uri = data.getData();
                        KLog.d("??????uri :" + uri);
                        //?????????????????????????????????
                        avatarWindow.editPicture(uri);
                    }
                    break;

                case AvatarWindow.REQUEST_EDIT_PIC:
                    uploadHeadImg();
                    break;
            }
        }
    }


    public String Bitmap2StrByBase64(Bitmap bit) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bit.compress(Bitmap.CompressFormat.PNG, 100, bos);//??????100???????????????
            bos.flush();
            bos.close();
            byte[] bytes = bos.toByteArray();
            com.ngbj.browser2.util.Base64 base64 = new com.ngbj.browser2.util.Base64();
            String dataSS = base64.encodeToString(bytes);
            return dataSS;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uploadHeadImg()  {
        File editFile = avatarWindow.getOutputEditImageFile();
        KLog.d("path : " + editFile.getAbsolutePath());

        String name = SDCardHelper.getSDCardPrivateCacheDir(this) + "/" + "headedit.png";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap = BitmapFactory.decodeStream(fis);
        //TODO ???????????????
        String string = "data:image/png;base64,"  + Bitmap2StrByBase64(bitmap);
        Map<String,String> map = new HashMap<>();
        map.put("base64_img",string);

        Gson gson = new Gson();
        String jsonString = gson.toJson(map);
        KLog.d("jsonString: " + jsonString);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),jsonString.toString());
        String token = (String) SPHelper.get(this,"token","");

        //?????????
        RetrofitHelper
                .getAppService()
                .updataHeadUser(token,requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResponseSubscriber<ResponseBody>(){
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        String jsonString ;
                        try {
                            jsonString = responseBody.string();
                            Gson gson1 = new Gson();
                            LoginBean bean = gson1.fromJson(jsonString,LoginBean.class);

                            UserInfoBean userInfoBean = dbManager.queryUserInfo().get(0);
                            userInfoBean.setHead_img(bean.getData().getHead_img());
                            dbManager.updateUserInfo(userInfoBean);
                            //??????
                            Glide.with(mContext)
                                    .load(bean.getData().getHead_img())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .crossFade()
                                    .into(head_icon);

                            KLog.d("??????????????????");


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


    private void uploadSex() {
        //0??? 1???
        Map<String,String> map = new HashMap<>();
        map.put("gender",gender);

        Gson gson = new Gson();
        String jsonString = gson.toJson(map);
        KLog.d("jsonString: " + jsonString);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),jsonString.toString());
        String token = (String) SPHelper.get(this,"token","");
        //?????????
        RetrofitHelper
                .getAppService()
                .updataUser(token,requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResponseSubscriber<ResponseBody>(){
                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        String jsonString ;
                        try {
                            jsonString = responseBody.string();
                            Gson gson1 = new Gson();
                            LoginBean bean = gson1.fromJson(jsonString,LoginBean.class);
                            KLog.d("????????????");
                            UserInfoBean userInfoBean = dbManager.queryUserInfo().get(0);
                            userInfoBean.setGender(bean.getData().getGender());
                            dbManager.updateUserInfo(userInfoBean);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

}
