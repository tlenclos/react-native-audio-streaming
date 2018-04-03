
# react-native-audio-streaming

## Features

- Background audio streaming of remote stream
- Control via sticky notification on android and media center on iOS
- Shoutcast/Icy meta data support
- Simple UI player component (if needed, an api to control the sound is available)

If you are only looking to play local audio file with app in foreground, please [see other audio libs](https://github.com/tlenclos/react-native-audio-streaming/blob/master/README.md#other-rn-audio-projects).

![Demo iOS](https://raw.githubusercontent.com/tlenclos/react-native-audio-streaming/master/demo_ios.gif)
![Demo android](https://raw.githubusercontent.com/tlenclos/react-native-audio-streaming/master/demo_android.gif)

## First installation step (applied for both iOS & Android)

`$ npm install react-native-audio-streaming --save`

### Next installation steps for iOS (choose one of three options below)

#### 1. Cocoapods installation

1. add `pod 'RNAudioStreaming', :path => '../node_modules/react-native-audio-streaming'` to `Podfile`
2. run `pod install`

#### 2. Mostly automatic installation

`$ react-native link react-native-audio-streaming`

Go to `node_modules` ➜ `react-native-audio-streaming` => `ios` => `Pods` and drag/drop `Pods.xcodeproj` to the Libraries folder in your XCode project.

In XCode, in the project navigator, select your project. Add `libReactNativeAudioStreaming.a` and `libStreamingKit.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`

#### 3. Manual installation

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-audio-streaming` => `ios`
   - run `pod install` to download StreamingKit dependency
   - add `ReactNativeAudioStreaming.xcodeproj` to the Libraries folder in your XCode project
   - add `Pods/Pods.xcodeproj` to the Libraries folder in your XCode project
3. In XCode, in the project navigator, select your project. Add `libReactNativeAudioStreaming.a` and `libStreamingKit.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

### Final steps for iOS (required after doing any of three options above)

1. Make sure `$(SRCROOT)/../node_modules/react-native-audio-streaming/ios` is added to your project's `Header Search Paths` within the `Build Settings` section.

2. Update Info.plist file of your Xcode project and add audio background mode

```xml
    <key>UIBackgroundModes</key>
    <array>
      <string>audio</string>
    </array>
```

### Next installation steps for Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.audioStreaming.ReactNativeAudioStreamingPackage;` to the imports at the top of the file
  - Add `new ReactNativeAudioStreamingPackage()` to the list returned by the `getPackages()` method
  If you're using Android 23 or above
  - Add `new ReactNativeAudioStreamingPackage(MainActivity.class)` to he list returned by the `getPackages()`method instead.
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-audio-streaming'
  	project(':react-native-audio-streaming').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-audio-streaming/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-audio-streaming')
  	```
4. If using Proguard then insert the following rules:
   ```
   -keep class com.spoledge.aacdecoder.** {
    *;
   }
   ```

   
   
## Usage

### Playing sound (similar code used by the player UI)

```javascript
import { ReactNativeAudioStreaming } from 'react-native-audio-streaming';

const url = "http://lacavewebradio.chickenkiller.com:8000/stream.mp3";
ReactNativeAudioStreaming.pause();
ReactNativeAudioStreaming.resume();
ReactNativeAudioStreaming.play(url, {showIniOSMediaCenter: true, showInAndroidNotifications: true});
ReactNativeAudioStreaming.stop();
```

For more information see the Example app.

### Player UI

```javascript
import { Player } from 'react-native-audio-streaming';

class PlayerUI extends Component {
  render() {
    return (
        <Player url={"http://lacavewebradio.chickenkiller.com:8000/stream.mp3"} />
    );
  }
}
```

### Android Custom Notification Style

To use custom notification style, you need to add a new layout file in your React Native Android Project with name `streaming_notification_player.xml`. In this view, atleast define three components
  - TextView with id `@+id/song_name_notification`
  - ImageButton with id `@+id/btn_streaming_notification_play`
  - ImageButton with id `@+id/btn_streaming_notification_stop`

  example

  ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ffffff"
        android:orientation="horizontal"
        android:padding="5dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentLeft="true"
            android:layout_alignParentStart="true"
            android:orientation="vertical">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginLeft="10dp"
                android:text="Your App Title"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/song_name_notification"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginLeft="10dp"
                android:textSize="14sp"
                android:textStyle="bold" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:orientation="horizontal">

                <ImageButton
                    android:id="@+id/btn_streaming_notification_play"
                    android:layout_width="45dp"
                    android:layout_height="45dp"
                    android:layout_gravity="center_vertical"
                    android:background="@android:color/white"
                    android:padding="10dp"
                    android:scaleType="fitCenter"
                    android:src="@drawable/ic_play" />

                <ImageButton
                    android:id="@+id/btn_streaming_notification_stop"
                    android:layout_width="45dp"
                    android:layout_height="45dp"
                    android:layout_gravity="center_vertical"
                    android:background="@android:color/white"
                    android:padding="10dp"
                    android:scaleType="fitCenter"
                    android:src="@drawable/ic_stop" />
            </LinearLayout>

        </LinearLayout>

        <ImageView
            android:id="@+id/streaming_icon"
            android:layout_width="100dp"
            android:layout_height="100dp"
            android:layout_alignParentEnd="true"
            android:layout_alignParentRight="true"
            android:src="@mimmap/ic_launcher" />
    </RelativeLayout>

  ```

### Android Notification Behaviour

The default behaviour for android notification is to persist on notification screen. If you want to make the notification dismissable and add custom song placeholder if the metadata is empty, you can pass additional parameter when playing the audio

```javascript
  ReactNativeAudioStreaming.play(url, {
      showIniOSMediaCenter: true,
      showInAndroidNotifications: true,
      persistNotification: false, // this will make notification dismissable
      audioTitlePlaceholder: 'Unknown'
    })

```

## TODO

- [ ] Allow to play local files
- [ ] Allow to specify custom style for the android notification (maybe a custom view ?)
- [ ] Allow to specify custom styles for the player
- [ ] Handle artwork of artist
- [ ] Add tests

## Other RN audio projects

- [jsierles/react-native-audio](https://github.com/jsierles/react-native-audio) to play local audio and record
- [zmxv/react-native-sound](https://github.com/zmxv/react-native-sound) to play local audio with more controls

## Credits

- Android version based on the work of @EstebanFuentealba https://github.com/EstebanFuentealba/react-native-android-audio-streaming-aac
- iOS version based on the work of @jhabdas https://github.com/jhabdas/lumpen-radio

See also the list of [contributors](https://github.com/tlenclos/react-native-audio-streaming/graphs/contributors) who participated in this project.

## Contribute

Since symlink support is [still lacking](https://github.com/facebook/react-native/issues/637) on React Native, I use the [wml](https://github.com/wix/wml) cli tool created by the nice folks at wix.

`wml add ~/react-native-audio-streaming ~/react-native-audio-streaming/Example/node_modules/react-native-audio-streaming`

## [Changelog](https://github.com/tlenclos/react-native-audio-streaming/blob/master/CHANGELOG.md)

## License

This project is licensed under the MIT License - see the LICENSE file for details
