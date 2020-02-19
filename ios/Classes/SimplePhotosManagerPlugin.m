#import "SimplePhotosManagerPlugin.h"
#if __has_include(<simple_photos_manager/simple_photos_manager-Swift.h>)
#import <simple_photos_manager/simple_photos_manager-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "simple_photos_manager-Swift.h"
#endif

@implementation SimplePhotosManagerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSimplePhotosManagerPlugin registerWithRegistrar:registrar];
}
@end
