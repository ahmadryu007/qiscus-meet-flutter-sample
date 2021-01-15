//
//  QiscusViewController.swift
//  Runner
//
//  Created by Ahmad Krisman Ryuzaki on 4/1/21.
//

import UIKit
import QiscusMeet

class QiscusViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        QiscusMeet.shared.QiscusMeetDelegate = self
        QiscusMeet.call(isVideo: true, isMicMuted: false, room: "33443516", avatarUrl: "https://upload.wikimedia.org/wikipedia/en/8/86/Avatar_Aang.png", displayName: "User 2 Sample Call", onSuccess: { (vc) in
                
            vc.modalPresentationStyle = .fullScreen
            self.navigationController?.present(vc, animated: true, completion: {
                })
            }) { (error) in
            print("meet error =\(error)")
        }
    }
}

extension QiscusViewController: QiscusMeetDelegate {
    func conferenceTerminated() {
        print("conferenceTerminated")
    }
    
    func conferenceJoined() {
        print("conferenceJoined")
    }
}
