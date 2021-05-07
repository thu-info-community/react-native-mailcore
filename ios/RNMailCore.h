#import <MailCore/MailCore.h>
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif

@interface RNMailCore : NSObject <RCTBridgeModule>
    @property (strong, nonatomic) MCOSMTPSession *smtpObject;
    @property (strong, nonatomic) MCOIMAPSession *imapSession;

    - (instancetype)init:(MCOSMTPSession *)smtpObject;
    - (instancetype)init:(MCOSMTPSession *)imapObject;            

    - (MCOSMTPSession *) getSmtpObject;
    - (MCOSMTPSession *) getImapObject;
@end
  
