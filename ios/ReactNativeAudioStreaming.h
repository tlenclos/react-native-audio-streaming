// AudioManager.h
// From https://github.com/jhabdas/lumpen-radio/blob/master/iOS/Classes/AudioManager.h

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif

#import "STKAudioPlayer.h"

@interface ReactNativeAudioStreaming : NSObject <RCTBridgeModule, STKAudioPlayerDelegate>

@property (nonatomic, strong) STKAudioPlayer *audioPlayer;
@property (nonatomic, readwrite) BOOL isPlayingWithOthers;
@property (nonatomic, readwrite) BOOL showNowPlayingInfo;
@property (nonatomic, readwrite) NSString *lastUrlString;
@property (nonatomic, retain) NSString *currentSong;

- (void)play:(NSString *) streamUrl options:(NSDictionary *)options;
- (void)pause;

@end
