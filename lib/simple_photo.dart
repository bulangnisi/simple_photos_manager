import 'dart:convert';
import 'dart:typed_data';

class SimplePhoto {

  final String id;      /// 照片id
  final int inx;        /// 序号
  final int width;      /// 宽
  final int height;     /// 高
  final Uint8List data; /// base64 数据

  SimplePhoto(this.id, this.inx, this.width, this.height, this.data);

  SimplePhoto.fromJson(Map<String, dynamic> json)
      : id = json['id'],
        inx = json['inx'],
        width = json['width'],
        height = json['height'],
        data = base64.decode(json['data']);

  Map<String, dynamic> toJson() =>
    <String, dynamic>{
      'id': id,
      'inx': inx,
      'width': width,
      'height': height,
      'data': data
    };
}