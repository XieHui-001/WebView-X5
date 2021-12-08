package com.example.my_webview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test_PIC extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pic);
        requestPower();
        getAllPhotoInfo();
    }
    //获取所有相片
    private void getAllPhotoInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<LiveBean> liveBean = new ArrayList<>();
                //所有照片
                HashMap<String, List<LiveBean>> allPhotosTemp = new HashMap<>();//所有照片
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projImage = {MediaStore.Images.Media._ID
                        , MediaStore.Images.Media.DATA
                        , MediaStore.Images.Media.SIZE
                        , MediaStore.Images.Media.DISPLAY_NAME};
                final Cursor mCursor = getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png","image/jpg"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");

//                AcacheUserBean userBean = (AcacheUserBean) ACache.get(Test_PIC.this).getAsObject(AppConfig.AcacheUserBean);
//                //判断userBean不等于null
//                String userName = "";
//                if (userBean != null) {
//                    userName = userBean.getName();
//                }
                //&& !StringUtils.isEmpty(userName)
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE)) / 1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //用于展示相册初始化界面
                        if (path.contains(Environment.getExternalStorageDirectory().getPath()) && path.contains("cqset_"+"test")) {
                            liveBean.add(new LiveBean(path, size, displayName));
                        }
                    }
                    mCursor.close();
                }
//                recyclerView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.addData(liveBean);
//                        if (liveBean == null || liveBean.size() == 0) {
//                            adapter.setEmptyView(nullView);
//                        }
//                    }
//
//
//                });

            }
        }).start();
    }

    //提示给权限
    public void requestPower() {
        // checkSelfPermission 判断是否已经申请了此权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(Test_PIC.this,"请开启文件读写权限",Toast.LENGTH_SHORT).show();
            //如果应用之前请求过此权限但用户拒绝了请求，shouldShowRequestPermissionRationale将返回 true。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
        } else {
            getAllPhotoInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(Test_PIC.this,"请开启文件读写权限",Toast.LENGTH_SHORT).show();
        } else {
            getAllPhotoInfo();
        }
    }


//    private void TestImg(){
//        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
//        startActivityForResult(intent, Constants.REQUEST_GALLERY);
//    }
}