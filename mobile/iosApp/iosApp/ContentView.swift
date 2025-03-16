import UIKit
import SwiftUI
import ComposeApp
import MLKitFaceDetection
import MLKitCommon
import MLKitVision
import AVKit
import Vision


struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NativeAnalyzer.shared.analyzeImageNative = { img, callback in
            Analyzer().analyzeImageNative(img: NativeAnalyzer.shared.byteArrayToData(byteArray: img)) { rect, size in
                _ = callback(rect, size)
            }
        }
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

class Analyzer {
    @available(iOS 18.0, *)
    func analyzeImageNative(img: Data, callback: @escaping (ComposeApp.Rect, ComposeApp.Size) -> ()) {
//        let options = FaceDetectorOptions()
//        options.performanceMode = .fast
//        options.landmarkMode = .none
//        options.classificationMode = .none
//        
//        do {
//            if let image = UIImage(data: img) {
//                let vizImg = VisionImage(image: image)
////                UIImageWriteToSavedPhotosAlbum(image, self, nil, nil)
//                vizImg.orientation = Analyzer.imageOrientation(deviceOrientation: .portrait, cameraPosition: .front)
//                
//                let faceDetector = FaceDetector.faceDetector(options: options)
//                faceDetector.process(vizImg) { faces, err in
//                    
//                    if err == nil {
//                        if let faces {
//                            print("faces not nil: \(faces.count)")
//                            for face in faces {
//                                callback(Rect(top: Float(face.frame.minY), left: Float(face.frame.minX), bottom: Float(face.frame.maxY), right: Float(face.frame.maxX)), Size(width: Float(image.size.width), height: Float(image.size.height)))
//                                
//                            }
//                        } else {
//                            print("faces nil")
//                        }
//                    } else {
//                        print("skipping frame - error processing image: \(err)")
//                    }
//                }
//                
//                
//            }
//        } catch {
//            print("skipping frame - error processing image: \(error)")
//        }
        if let image = UIImage(data: img) {
            UIImageWriteToSavedPhotosAlbum(image, self, nil, nil)
            let faceDetectionRequest = DetectFaceRectanglesRequest()
            Task {
                do {
                    let res = try await faceDetectionRequest.perform(on: CIImage(image: image)!)
                    res.forEach { face in
                        
                        var newSize = face.boundingBox.toImageCoordinates(image.size, origin: .upperLeft)
                        print("FACE RESULT @ \(newSize)")
                        callback(Rect(top: Float(newSize.minY), left: Float(newSize.minX), bottom: 0, right: 0), Size(width: Float(image.size.width), height: Float(image.size.height)))
                    }
                } catch {
                    print("error processing image: \(error)")
                }
            }
        }
        
        
    }
    static func imageOrientation(
        deviceOrientation: UIDeviceOrientation,
        cameraPosition: AVCaptureDevice.Position
      ) -> UIImage.Orientation {
        switch deviceOrientation {
        case .portrait:
          return cameraPosition == .front ? .leftMirrored : .right
        case .landscapeLeft:
          return cameraPosition == .front ? .downMirrored : .up
        case .portraitUpsideDown:
          return cameraPosition == .front ? .rightMirrored : .left
        case .landscapeRight:
          return cameraPosition == .front ? .upMirrored : .down
        case .faceDown, .faceUp, .unknown:
          return .up
        }
      }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}



