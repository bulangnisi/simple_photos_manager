import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'simple_photo.dart';
export 'simple_photo.dart';

class SimplePhotosManager {
  static const MethodChannel _channel = const MethodChannel('plugin.benj.monster/simple_photos_manager');

  /// 获取相册中所有图片
  /// size 参数控制图片宽、高属性中最大一个的值的上限
  /// 如一张 1024 * 2048 的图片，指定 size 为 100 后，返回图片的宽、高为 50 * 100
  static Future<List<SimplePhoto>> getAlbumPhotos({int size = 100}) async {
    var res = await _channel.invokeMethod('getAlbumPhotos', size);
    List<SimplePhoto> imgs = _conversion(res);
    return imgs;
  }

  /// 根据id获取原图
  static Future<List<SimplePhoto>> getOriginPhotos({List<String> ids = const [], int size = 0}) async {
    Map<String, dynamic> params = Map<String, dynamic>();
    params['ids'] = ids;
    params['size'] = size;
    var res = await _channel.invokeMethod('getOriginPhotos', params);
    List<SimplePhoto> imgs = _conversion(res);
    return imgs;
  }

  static _conversion(dynamic res){
    if(res == null){
      return [];
    }
    return List<dynamic>.from(json.decode(res)).map((f) => SimplePhoto.fromJson(f)).toList();
  }
}
