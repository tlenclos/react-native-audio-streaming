
import React, { Component } from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View
} from 'react-native';
import { RNReactNativeAudioStreaming, Player } from 'react-native-audio-streaming';

const url = 'http://www.stephaniequinn.com/Music/Canon.mp3';

export default class App extends Component {
    render() {
        return (
            <View style={styles.container}>
                <Text>Playing stream {url}</Text>

                <Player url={url} />
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});
