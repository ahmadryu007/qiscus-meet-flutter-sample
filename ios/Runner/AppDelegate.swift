import UIKit
import Flutter
import QiscusMeet

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    
    QiscusMeet.setup(appId: "kawan-seh-g857ffuuw9b", url: "https://meet.qiscus.com")
    
    if let controller = self.window.rootViewController as? FlutterViewController {
        
        FlutterMethodChannel.init(name: "qiscusmeet_plugin", binaryMessenger: controller.binaryMessenger).setMethodCallHandler { (call, result) in
                
                let meetConfig = MeetJwtConfig()
                meetConfig.email = "user2_sample_call@example.com"
                QiscusMeetConfig.shared.setJwtConfig = meetConfig
            
                if call.method == "video_call" {
                    let vc = QiscusViewController()
                    let navigationController = UINavigationController(rootViewController: vc)
                    self.window.rootViewController = navigationController
                    self.window.makeKeyAndVisible()
                }
            }
        }
    
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    
}
