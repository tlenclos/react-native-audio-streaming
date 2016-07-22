
# react-native-react-native-android-streaming

## Features

- Background audio streaming of remote stream
- Control via sticky notification on android and media center on iOS
- Shoutcast/Icy meta data support
- Simple UI player component

![Demo iOS](https://raw.githubusercontent.com/tlenclos/react-native-audio-streaming/master/demo_ios.gif)

## Getting started

`$ npm install react-native-react-native-android-streaming --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-android-streaming`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-android-streaming` and add `RNReactNativeAndroidStreaming.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeAndroidStreaming.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeAndroidStreamingPackage;` to the imports at the top of the file
  - Add `new RNReactNativeAndroidStreamingPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-android-streaming'
  	project(':react-native-react-native-android-streaming').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-android-streaming/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-android-streaming')
  	```


## Usage
```javascript
import RNReactNativeAndroidStreaming from 'react-native-react-native-android-streaming';

// TODO: What do with the module?
RNReactNativeAndroidStreaming;
```
  
 
## TODO

- [ ] Improve installation of the libs
- [ ] Allow to specify custom styles for the player
- [ ] Handle artwork of artist
- [ ] Add tests
