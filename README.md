# simple_photos_manager

Easy and quick access to album resources plugin for IOS and Android
简单快速获取相册资源插件，支持IOS和Android

## Getting Started

IOS: plist.info 加入 NSPhotoLibraryUsageDescription

'''Dart
/// Get all photos in the album
/// 获取相册所有图片
static Future<List<SimplePhoto>> getAlbumPhotos({int size = 100})

/// Gets the image for the specified id
/// 获取指定id的图片
static Future<List<SimplePhoto>> getOriginPhotos({List<String> ids = const [], int size = 0})

/// Image objects
/// 图片对象
class SimplePhoto {
    final String id;      
    final int inx;        
    final int width;
    final int height;
    final Uint8List data; /// base64 data
    ......
'''

## Example
[example](https://github.com/bulangnisi/simple_photos_manager/blob/master/example/lib/main.dart)
