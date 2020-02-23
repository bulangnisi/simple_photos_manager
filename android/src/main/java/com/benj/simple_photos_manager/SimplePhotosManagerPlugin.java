package com.benj.simple_photos_manager;

import android.Manifest;
import androidx.annotation.NonNull;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;

/** SimplePhotosManagerPlugin */
public class SimplePhotosManagerPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener{

  // 需要的权限
  private final static String READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

  // 权限请求码
  private final static int PERMISSION_REQUEST_CODE = 201;

  // 查询指定id的图片
  private ArrayList<String> ids = new ArrayList<>();

  // 查询指定id时，指定图片的size
  private int size = 0;

  private Registrar registrar ;

  private Result result ;

  private SimplePhotosManagerPlugin(Registrar registrar){
    this.registrar = registrar;
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "plugin.benj.monster/simple_photos_manager");
    final SimplePhotosManagerPlugin _self = new SimplePhotosManagerPlugin(registrar);
    channel.setMethodCallHandler(_self);
    registrar.addRequestPermissionsResultListener(_self);
  }

  @Override
  public boolean onRequestPermissionsResult(int id, String[] permissions, int[] grantResults) {
    if (id == PERMISSION_REQUEST_CODE && READ_STORAGE_PERMISSION.equals(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if(this.result != null){
        fetchPhotos();
      }
      return true;
    } else{
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.result = result;
    if (call.method.equals("getAlbumPhotos")) {
      // 目前此属性对于查缩略图其实已忽略
      // 为了调用统一性才加上的
      this.size = (int)call.arguments;
      executeFetch();
    } else if(call.method.equals("getOriginPhotos")){
      HashMap<String, Object> map = (HashMap<String, Object>)call.arguments;
      this.size = (int)map.get("size");
      this.ids = (ArrayList<String>)map.get("ids");
      executeFetch();
    } else {
      result.notImplemented();
    }
  }

  private void executeFetch(){
    String[] perms = new String[]{READ_STORAGE_PERMISSION};
    boolean hasPermissions = EasyPermissions.hasPermissions(this.registrar.activity(), perms);
    if(hasPermissions){
        fetchPhotos();
    }else{
        EasyPermissions.requestPermissions(this.registrar.activity(), "需要读取存储卡权限", PERMISSION_REQUEST_CODE, perms);
    }
  }

  @SuppressWarnings("unchecked")
  private void fetchPhotos(){
    ArrayList<String> tmpIds = (ArrayList<String>)this.ids.clone();
    this.ids.clear();
    FetchTask task = new FetchTask(this.registrar.context().getContentResolver(), tmpIds, this.size);
    this.size = 0;
    FutureTask<ArrayList<Photo>> futureTask = new FutureTask<ArrayList<Photo>>(task);
    Thread thread = new Thread(futureTask);
    thread.start();
    try {
        ArrayList<Photo> list = futureTask.get();
        String res = JSON.toJSONString(list);
        this.result.success(res);
    } catch (ExecutionException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
  }

}
