// AudioManager.h
// From https://github.com/jhabdas/lumpen-radio/blob/master/iOS/Classes/AudioManager.h

#import "RCTBridgeModule.h"
#import "STKAudioPlayer.h"

@interface ReactNativeAudioStreaming : NSObject <RCTBridgeModule, STKAudioPlayerDelegate>

@property (nonatomic, strong) STKAudioPlayer *audioPlayer;
@property (nonatomic, readwrite) BOOL isPlayingWithOthers;
@property (nonatomic, readwrite) BOOL showNowPlayingInfo;
@property (nonatomic, readwrite) NSString *lastUrlString;
@property (nonatomic, retain) NSString *currentSong;
@property (nonatomic, retain) NSString *currentArtist;
@property (nonatomic, retain) NSString *currentArtwork;
@property (nonatomic, readwrite) BOOL fetchedAlbumArt;

- (void)play:(NSString *) streamUrl options:(NSDictionary *)options;
- (void)pause;

@end
