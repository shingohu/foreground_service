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
            break
        case AVAudioSession.InterruptionType.ended.rawValue://End
                //指示另一个音频会话的中断已结束，本应用程序可以恢复音频。
            self.play();
            break
        default: break
        }
        
        
        
    }
    
    
    
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if(call.method == "start"){
            let volume =  (call.arguments as! Dictionary<String, Any>)["volume"] as! Double
            start(volume: volume)
            result(true)
        }else if(call.method == "stop"){
            stop()
            result(true)
        }
    }
    
    
    func start(volume:Double){
        
        stop()
        if(filePathUrl == nil){
            return
        }
        let avAudioSession = AVAudioSession.sharedInstance()
        let category = avAudioSession.category
        
        if(category == .record){
            return
        }
        self.audioPlayer = try? AVAudioPlayer(contentsOf: filePathUrl! as URL)
        self.audioPlayer?.prepareToPlay()
        self.audioPlayer?.volume = Float(volume)
        self.audioPlayer?.numberOfLoops = -1
        self.audioPlayer?.play()
    }
    
    func stop(){
        if(self.audioPlayer != nil){
            self.audioPlayer?.stop()
            self.audioPlayer = nil
        }
    }
    
    func pause(){
        self.audioPlayer?.pause()
    }
    
    func play(){
        if(self.audioPlayer?.isPlaying != true){
            self.audioPlayer?.play()
        }
    }
    
}
