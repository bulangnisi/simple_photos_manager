# simple_photos_manager

<p>Easy and quick access to album resources plugin for IOS and Android</p>
<p>简单快速获取相册资源插件，支持IOS和Android</p>

## IOS
plist.info 加入
<key>NSPhotoLibraryUsageDescription</key>
<string>请求访问相册</string>

## Install
<p>pubspec.yaml 中加入</p>

```yaml
simple_photos_manager: {
  git: {
    url: https://github.com/bulangnisi/simple_photos_manager.git
  }
}
```

## Getting Started
```dart
/// Get all photos in the album
/// 获取相册所有图片
/// size: Set the maximum value of the width or height of the image to this, and the other to scale down
/// size: 将图像的宽度或高度的最大值设为此值，另一个则按比例缩小
/// PS: 由于android考虑支持低版本（最低为19），无法使用最新的Api去获取指定大小的缩略图
///     而原本使用呢Bitmap做裁剪的效率太低，对于获取较多图片的情况下速度太慢，所以使用了较为旧的api
///     直接获取了 96 * 96 版本的缩略图，所以当获取全部图片时，此处指定的size直接忽略了
static Future<List<SimplePhoto>> getAlbumPhotos({int size = 100})

/// Gets the image for the specified id
/// 获取指定id的图片
/// ids: Query the image by id
/// ids: 指定id查询图片
/// size: Set the maximum value of the width or height of the image to this, and the other to scale down
/// size: 将图像的宽度或高度的最大值设为此值，另一个则按比例缩小
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
```

## Remark
The use scene of this plug-in is used for photo album with Flutter, so the maximum loading quantity is 1000
本插件使用场景是配合Flutter做相册使用，所以加载最大数量为1000

## Example
[example](https://github.com/bulangnisi/simple_photos_manager/blob/master/example/lib/main.dart)
