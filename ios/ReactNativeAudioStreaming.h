// AudioManager.h
// From https://github.com/jhabdas/lumpen-radio/blob/master/iOS/Classes/AudioManager.h

#import "RCTBridgeModule.h"
#import "STKAudioPlayer.h"

@interface ReactNativeAudioStreaming : NSObject <RCTBridgeModule, STKAudioPlayerDelegate>

@property (nonatomic, strong) STKAudioPlayer *audioPlayer;
@property (nonatomic, readwrite) BOOL isPlayingWithOthers;
@property (nonatomic, readwrite) NSString *lastUrlString;
@property (nonatomic, retain) NSString *currentSong;

- (void)play;
- (void)pause;

@end
