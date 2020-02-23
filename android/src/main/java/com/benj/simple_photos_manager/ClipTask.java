package com.benj.simple_photos_manager;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.content.ContentResolver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import android.graphics.Matrix;

class ClipTask implements Callable<Photo> {

    private ContentResolver contentResolver;

    private Uri uri;

    private int size;

    private Bitmap.CompressFormat type;

    private String id;

    private int inx;

    private float rotate;

    private boolean isThumbnail;

    protected ClipTask(ContentResolver contentResolver, Uri uri, int size, String type, String id, int inx, float rotate, boolean isThumbnail){
        if("image/jpeg".equals(type)){
            this.type = Bitmap.CompressFormat.JPEG;
        }else{
            this.type = Bitmap.CompressFormat.PNG;
        }
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.size = size;
        this.id = id;
        this.inx = inx;
        this.rotate = rotate;
        this.isThumbnail = isThumbnail;
    }

    @Override
    public Photo call(){
        Photo result = null;
        Bitmap originBitmap = null;
        Bitmap tmpBitmap = null; 
        try{
            /**
             *  由于处理bitmap耗时较高，处理一张3000 * 4000 的图耗时300-500 ms，
             *  在本机vivo nex A 的测试中，开启9线程，循环处理1000张图片耗时达到了几十秒
             *  所以此处采用两种方式
             *  1：获取缩略图，当未指定id时，获取全部图片，采用这种方式
             *  PS：Android Q可以使用ContentResolver.loadThumbnail方式获取，但是考虑到兼容低版本机器，所以使用了废弃的方法获取
             *  2：获取指定id图片时，多线程操作，指定大小处理图片，该方式下，size = 0 时，将返回原图最大尺寸
             */
            if(isThumbnail){
                // 缩略图
                tmpBitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(id), MediaStore.Images.Thumbnails.MICRO_KIND, null);
            }else{
                // 指定图片
                originBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
                int originWidth = originBitmap.getWidth();
                int originHeight = originBitmap.getHeight();
                // 仅处理size大于0 并且 不大于原图宽高最大值之一
                if(size > 0 && size < (originWidth >= originHeight ? originWidth : originHeight)){
                    int tmpWidth, tmpHeight;
                    // 这里逻辑与ios默认逻辑一样，取宽高最大值最为基准
                    if(originWidth > originHeight){
                        tmpWidth = size;
                        tmpHeight = (int)((float)size / (float) originWidth * (float) originHeight);
                    }else{
                        tmpHeight = size;
                        tmpWidth = (int)((float)size / (float)originHeight * (float)originWidth);
                    }
                    tmpBitmap = Bitmap.createScaledBitmap(originBitmap, tmpWidth, tmpHeight, false);
                }else{
                    tmpBitmap = originBitmap;
                }
            }
            if(rotate > 0){
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                tmpBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix, false);
            }
            String byteStr = bitmapToBase64(tmpBitmap);
            result = new Photo(this.id, this.inx, tmpBitmap.getWidth(), tmpBitmap.getHeight(), byteStr);
            
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(originBitmap != null && !originBitmap.isRecycled()){
                originBitmap.recycle();
            }
            if(tmpBitmap != null && !tmpBitmap.isRecycled()){
                tmpBitmap.recycle();
            }
            return result;
        }
    }

    private String bitmapToBase64(Bitmap bitmap){
        String result = null;
        ByteArrayOutputStream byteStream = null;
        try{
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(this.type, 100, byteStream);
            byteStream.flush();
            byteStream.close();
            byte[] bitmapBytes = byteStream.toByteArray();
            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                byteStream.flush();
                byteStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }
    }
}