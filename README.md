
# react-native-audio-streaming

## Features

- Background audio streaming of remote stream
- Control via sticky notification on android and media center on iOS
- Shoutcast/Icy meta data support
- Simple UI player component (if needed, an api to control the sound is available)

![Demo iOS](https://raw.githubusercontent.com/tlenclos/react-native-audio-streaming/master/demo_ios.gif)
![Demo android](https://raw.githubusercontent.com/tlenclos/react-native-audio-streaming/master/demo_android.gif)

## Getting started

`$ npm install react-native-audio-streaming --save`

### Mostly automatic installation

`$ react-native link react-native-audio-streaming`

Go to `node_modules` ➜ `react-native-audio-streaming` => `Pods` and drag/drop `Pods.xcodeproj` to the Libraries folder in your XCode project.

In XCode, in the project navigator, select your project. Add `libReactNativeAudioStreaming.a` and `libStreamingKit.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-audio-streaming` => `ios`
   - run `pod install` to download StreamingKit dependency
   - add `ReactNativeAudioStreaming.xcodeproj` to the Libraries folder in your XCode project
   - add `Pods/Pods.xcodeproj` to the Libraries folder in your XCode project
3. In XCode, in the project navigator, select your project. Add `libReactNativeAudioStreaming.a` and `libStreamingKit.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.ReactNativeAudioStreamingPackage;` to the imports at the top of the file
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

## Usage

### Playing sound (similar code used by the player UI)

```javascript
import { ReactNativeAudioStreaming } from 'react-native-audio-streaming';

const url = "http://lacavewebradio.chickenkiller.com:8000/stream.mp3";
ReactNativeAudioStreaming.pause();
ReactNativeAudioStreaming.resume();
ReactNativeAudioStreaming.play(url);
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

## TODO

- [ ] Allow to specify custom style for the android notification (maybe a custom view ?)
- [ ] Allow to specify custom styles for the player
- [ ] Handle artwork of artist
- [ ] Add tests

## Credits

- Android version based on the work of @EstebanFuentealba https://github.com/EstebanFuentealba/react-native-android-audio-streaming-aac
- iOS version based on the work of @jhabdas https://github.com/jhabdas/lumpen-radio

See also the list of [contributors](https://github.com/tlenclos/react-native-audio-streaming/graphs/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the LICENSE file for details
