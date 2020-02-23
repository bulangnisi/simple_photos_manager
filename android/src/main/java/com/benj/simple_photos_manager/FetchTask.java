package com.benj.simple_photos_manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FetchTask implements Callable<ArrayList<Photo>> {

    final Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    // 查询排序
    final String sortOrder = MediaStore.Images.Media.DATE_MODIFIED+" desc";

    // 查询字段
    final String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.ORIENTATION
    };

    // 手机CPU核心数
    final int processNum = Runtime.getRuntime().availableProcessors();

    // 固定最大数为1000，因为flutter默认图片缓存适配为1000张 100M大小
    final int maxCount = 1000;

    private int size;

    private String selection = "("+MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?)";

    private String[] selectArgs = {"image/jpeg", "image/png"};

    private ContentResolver contentResolver;

    private boolean isThumbnail;

    FetchTask(ContentResolver contentResolver, ArrayList<String> ids, int size){
        this.contentResolver = contentResolver;
        this.size = size;
        this.isThumbnail = ids.size() == 0;
        if(!this.isThumbnail){
            ArrayList<String> tmpList = new ArrayList<>(Arrays.asList(selectArgs.clone()));
            String prefAdd = " and (";
            StringBuffer sb = new StringBuffer(prefAdd);
            for(String str: ids){
                sb.append(prefAdd.equals(sb.toString()) ? "" : " or ");
                sb.append(MediaStore.Images.Media._ID + "=?");
                tmpList.add(str);
            }
            sb.append(")");
            selection += sb.toString();
            selectArgs = new String[tmpList.size()];
            selectArgs = tmpList.toArray(selectArgs);
        }
    }

    @Override
    public ArrayList<Photo> call(){
        ArrayList<Photo> photos = new ArrayList<>();
        // 以核心数作为线程池大小
        ExecutorService executor = Executors.newFixedThreadPool(processNum + 1);
        ArrayList<Future<Photo>> futures = new ArrayList<>();
        Cursor cursor = contentResolver.query(externalUri, projection, selection, selectArgs, sortOrder);
        if(null != cursor){
            while (cursor.moveToNext()){
                if(futures.size() >= maxCount){
                    break;
                }
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                float rotate = cursor.getFloat(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                Uri uri = ContentUris.withAppendedId(externalUri, Long.parseLong(id));
                ClipTask task = new ClipTask(contentResolver, uri, size, type, id, cursor.getPosition(), rotate, isThumbnail);
                Future<Photo> future = executor.submit(task);
                futures.add(future);

            }

            for(Future<Photo> future : futures){
                Photo photo = null;
                try {
                    photo = future.get(2, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } finally {
                    future.cancel(true);
                    if(photo != null){
                        photos.add(photo);
                    }
                }
            }

            cursor.close();
            if(!executor.isShutdown()){
                executor.shutdown();
            }
        }
        return photos;
    }
}
