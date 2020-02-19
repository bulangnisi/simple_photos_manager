import 'package:flutter/material.dart';
import 'package:simple_photos_manager/simple_photos_manager.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<SimplePhoto> _photos = [];

  @override
  void initState() {
    super.initState();
    initPhotos();
  }

  void initPhotos(){
    var start = DateTime.now();
    SimplePhotosManager.getAlbumPhotos(size: 300).then((res){
      var diff = DateTime.now().difference(start).inMilliseconds;
      print('init done in $diff ms');
      setState(() {
        _photos = res;
      });
    }).catchError((e){
      print(e);
    });
  }

  void _showPhoto(BuildContext context, SimplePhoto photo){
    SimplePhotosManager.getOriginPhotos(ids: [photo.id], size: 600).then((res){
      if(res.length > 0){
        showDialog(
          context: context,
          builder: (BuildContext context){
            return Dialog(
              child: Image.memory(res[0].data, fit: BoxFit.cover),
            );
          }
        );
      }
    }).catchError((e){
      print(e);
    });
    
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Simple Photos Manager'),
        ),
        body: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4
          ), 
          itemCount: _photos.length,
          itemBuilder: (BuildContext context, int inx){
            SimplePhoto photo = _photos[inx];
            return GestureDetector(
              onTap: () => _showPhoto(context, photo),
              child: Image.memory(
                _photos[inx].data,
                fit: BoxFit.cover,
              ),
            );
          }
        ),
      ),
    );
  }
}

