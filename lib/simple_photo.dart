import 'dart:convert';
import 'dart:typed_data';

class SimplePhoto {

  final String id;      /// 照片id
  final int inx;        /// 序号
  final int width;      /// 宽
  final int height;     /// 高
  final Uint8List data; /// base64 数据

  SimplePhoto(this.id, this.inx, this.width, this.height, this.data);

  static Uint8List decode(String data){
    data = data.replaceAll("\n", "").replaceAll("\t", "");
    return base64.decode(data);
  }

  SimplePhoto.fromJson(Map<String, dynamic> json)
      : id = json['id'],
        inx = json['inx'],
        width = json['width'],
        height = json['height'],
        data = decode(json['data']);//base64.decode(json['data']);

  Map<String, dynamic> toJson() =>
    <String, dynamic>{
      'id': id,
      'inx': inx,
      'width': width,
      'height': height,
      'data': data
    };
}