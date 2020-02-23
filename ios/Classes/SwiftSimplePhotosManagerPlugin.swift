import Flutter
import UIKit
import Photos

public class SwiftSimplePhotosManagerPlugin: NSObject, FlutterPlugin {

  private let maxCount = 1000

  struct Img: Codable{
    let id: String
    let inx: Int
    let width: Int
    let height: Int
    let data: Data
  }

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "plugin.benj.monster/simple_photos_manager", binaryMessenger: registrar.messenger())
    let instance = SwiftSimplePhotosManagerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
      case "getAlbumPhotos":
        let size: Int = call.arguments as? Int ?? 0
        checkPhotoLibraryPermission({() in 
          self.fetchAlbumPhotos(size, result)
        })
      case "getOriginPhotos":
        let arguments: [String:Any?] = call.arguments as? [String:Any?] ?? [:]
        let ids: [String] = arguments["ids"] as? [String] ?? []
        let size: Int = arguments["size"] as? Int ?? 0
        checkPhotoLibraryPermission({() in 
          self.fetchOriginPhotos(ids, size, result)
        })
      default:
        result(
          FlutterError.init(
            code: "UNAVAILABLE",
            message: "no such method",
            details: nil
          )
        )
    }
  }

  private func checkPhotoLibraryPermission(_ fn: @escaping () -> Void) {
    switch PHPhotoLibrary.authorizationStatus() {
      // 未选择
      case .notDetermined:
        PHPhotoLibrary.requestAuthorization({(status: PHAuthorizationStatus) in
          if(status == .authorized){
            fn()
          }
        })
      // 已授权
      case .authorized:
        fn()
      // 用户拒绝
      case .denied: break
      // 家长控制
      case .restricted: break
      default: break
    }
  }

  private func getFetchOptions() -> PHFetchOptions{
    // 获取图片可选参数
    let allPhotosOptions = PHFetchOptions()
    // 根据创建日期倒叙
    allPhotosOptions.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
    return allPhotosOptions
  }
  
  private func getImageRequestOptions() -> PHImageRequestOptions{
    let imgOptions = PHImageRequestOptions()
    // 图片处理魔兽为最快，此模式并不会精确返回图片指定size大小，如设置size为 300,300时，在一张1000+,2000+的图片中返回 168,300
    imgOptions.resizeMode = PHImageRequestOptionsResizeMode.fast
    // imgOptions.normalizedCropRect = CGRect.init(x: 0.1, y: 0.1, width: 300, height: 300)
    // 图片处理采用同步执行，这将设置deliveryMode为PHImageRequestOptionsDeliveryMode.highQualityFormat
    // PHImageRequestOptionsDeliveryMode.opportunistic，会在请求图片回调中返回两次，头一次
    // 返回最快处理出的缩略图，第二次才会加载出指定size的图片
    // PHImageRequestOptionsDeliveryMode.fastFormat，会以最快速度返回，但是可能无法达到指定精度(有可能返回更小精度的图片)
    imgOptions.isSynchronous = false
    imgOptions.deliveryMode = PHImageRequestOptionsDeliveryMode.highQualityFormat
    return imgOptions
  }

  private func fetchAlbumPhotos(_ size: Int, _ result: @escaping FlutterResult){
    let photos = PHAsset.fetchAssets(with: PHAssetMediaType.image, options: getFetchOptions())
    self.processPhotos(photos, size, result)
  }
  
  private func fetchOriginPhotos(_ ids: [String], _ size: Int, _ result: @escaping FlutterResult){
    let photos = PHAsset.fetchAssets(withLocalIdentifiers: ids, options: getFetchOptions())
    self.processPhotos(photos, size, result)
  }
  
  private func processPhotos(_ photos: PHFetchResult<PHAsset>, _ size: Int, _ result: @escaping FlutterResult){
    if(photos.count == 0){
      result(nil)
    }else{
      let imgManager = PHImageManager.default()
      var imgs = [Img]()
      // 默认设置，从图片宽，高中取最大值作为基准，设置目标size，若为 0 则返回原图
      let cgsize = size == 0 ? PHImageManagerMaximumSize : CGSize(width: size, height: size)
      // 遍历图片 处理裁剪
      photos.enumerateObjects({ (asset: PHAsset, inx: Int, nil) in
        // 图片处理大小
        imgManager.requestImage(for: asset, targetSize: cgsize, contentMode: PHImageContentMode.default, options: self.getImageRequestOptions(), resultHandler: {(image: UIImage?, info: [AnyHashable : Any]?) in
          let tmp = Img(id: asset.localIdentifier, inx: inx, width: Int(image?.size.width ?? 0), height: Int(image?.size.height ?? 0), data: Data(((image?.pngData()) ?? nil)!))
          imgs.append(tmp)
          if(imgs.count == photos.count){
              imgs = imgs.sorted {$0.inx < $1.inx}
              let encoder = JSONEncoder()
              let res = try! encoder.encode(imgs.filter {$0.inx < self.maxCount})
              result(String(data: res, encoding: .utf8)!)
          }
        })
      })
    }
  }
}
