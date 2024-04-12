import Flutter
import UIKit
import AVFoundation
import Foundation


public class KeepAliveServicePlugin: NSObject, FlutterPlugin {
    
    
 var audioPlayer :AVAudioPlayer?
    var filePathUrl:NSURL?
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "keep_alive_service", binaryMessenger: registrar.messenger())
    let instance = KeepAliveServicePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    instance.setUpFilePath()
  }


    func setUpFilePath()  {
        let myBundle = Bundle(for: Self.self).url(forResource: "keepAliveBundle", withExtension: "bundle")
        if let resourceBundle = Bundle(url: myBundle!) {
            if let filePath = resourceBundle.path(forResource: "1234567890", ofType: "wav") {
                filePathUrl = NSURL.init(fileURLWithPath: filePath)
            }
        }
        NotificationCenter.default.addObserver(self, selector: #selector(audioAudioChangeListener(_:)), name: AVAudioSession.interruptionNotification, object: nil)
            
    }

    @objc func audioAudioChangeListener(_ notification:Notification) {
        ///来电和闹钟会打断音频
        guard let userInfo = notification.userInfo, let reasonValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt else { return }
           switch reasonValue {
           case AVAudioSession.InterruptionType.began.rawValue://Began
                self.pause();
               break
           case AVAudioSession.InterruptionType.ended.rawValue://End
               let optionKey = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt
               if optionKey == AVAudioSession.InterruptionOptions.shouldResume.rawValue {
                   //指示另一个音频会话的中断已结束，本应用程序可以恢复音频。
                  self.play();
               }
               break
           default: break
           }
        
        
        
    }
    
    
    
    
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      if(call.method == "start"){
          start()
          result(true)
      }else if(call.method == "stop"){
          stop()
          result(true)
      }
  }
    
  
    func start(){
        do {
            stop()
            if(filePathUrl == nil){
                return
            }
            let avAduioSession = AVAudioSession.sharedInstance()
            let category = avAduioSession.category
            let mode = avAduioSession.mode
            try avAduioSession
                .setCategory(category,
                             mode: mode,
                             policy: .default,
                             options: .mixWithOthers)
            self.audioPlayer = try? AVAudioPlayer(contentsOf: filePathUrl! as URL)
            self.audioPlayer?.prepareToPlay()
            self.audioPlayer?.volume = 0.0
            self.audioPlayer?.numberOfLoops = -1
            self.audioPlayer?.play()
            print("start")
        } catch{
            print(error)
        }
      
    }
    
    func stop(){
        if(self.audioPlayer != nil){
            self.audioPlayer?.stop()
            self.audioPlayer = nil
            do {
                try AVAudioSession.sharedInstance().setActive(false,options: .notifyOthersOnDeactivation)
                print("stop")
            }catch {
                print(error)
            }
        }
    }
    
    func pause(){
        self.audioPlayer?.pause()
    }
    
    func play(){
        self.audioPlayer?.play()
    }
  
}
