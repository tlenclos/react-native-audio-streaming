import React, { Component } from 'react';
import {
    NativeModules,
    StyleSheet,
    Text,
    View,
    TouchableOpacity,
    DeviceEventEmitter,
    ActivityIndicator
} from 'react-native';

const { RNReactNativeAudioStreaming } = NativeModules;

// Possibles states
const PLAYING = 'PLAYING';
const PAUSED = 'PAUSED';
const STOPPED = 'STOPPED';
const ERROR = 'ERROR';
const METADATA_UPDATED = 'METADATA_UPDATED';
const BUFFERING = 'BUFFERING';
const START_PREPARING = 'START_PREPARING'; // Android only
const BUFFERING_START = 'BUFFERING_START'; // Android only

// UI
const iconSize = 60;

class Player extends Component {
    constructor(props) {
        super(props);
        this._onPress = this._onPress.bind(this);
        this.state = {
            status: STOPPED,
            song: ''
        };
    }

    componentDidMount() {
        this.subscription = DeviceEventEmitter.addListener(
            'AudioBridgeEvent', (evt) => {
                // We just want meta update for song name
                if (evt.status === METADATA_UPDATED && evt.key === 'StreamTitle') {
                    this.setState({song: evt.value});
                } else if (evt.status != METADATA_UPDATED) {
                    this.setState(evt);
                }
            }
        );

        RNReactNativeAudioStreaming.getStatus((error, status) => {
            (error) ? console.log(error) : this.setState(status)
        });
    }

    _onPress() {
        switch (this.state.status) {
            case PLAYING:
                RNReactNativeAudioStreaming.pause();
                break;
            case PAUSED:
                RNReactNativeAudioStreaming.resume();
                break;
            case STOPPED:
            case ERROR:
                RNReactNativeAudioStreaming.play(this.props.url);
                break;
            case BUFFERING:
                RNReactNativeAudioStreaming.stop();
                break;
        }
    }

    render() {
        let icon = null;
        switch (this.state.status) {
            case PLAYING:
                icon = <Text style={styles.icon}>॥</Text>;
                break;
            case PAUSED:
            case STOPPED:
            case ERROR:
                icon = <Text style={styles.icon}>▸</Text>;
                break;
            case BUFFERING:
            case BUFFERING_START:
            case START_PREPARING:
                icon = <ActivityIndicator
                    animating={true}
                    style={{height: 80}}
                    size="large"
                />;
                break;
        }

        return (
            <TouchableOpacity style={styles.container} onPress={this._onPress}>
                {icon}
                <View style={styles.textContainer}>
                    <Text style={styles.songName}>{this.state.song}</Text>
                </View>
            </TouchableOpacity>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        position: 'absolute',
        bottom: 0,
        left: 0,
        right: 0,
        alignItems: 'center',
        flexDirection: 'row',
        height: 80,
        paddingLeft: 10,
        paddingRight: 10,
        borderColor: '#000033',
        borderWidth: 1,
    },
    icon: {
        color: '#000',
        fontSize: 26,
        borderColor: '#000033',
        borderWidth: 1,
        borderRadius: iconSize / 2,
        width: iconSize,
        height: iconSize,
        textAlign: 'center',
        paddingTop: 8
    },
    textContainer: {
        flexDirection: 'column',
        margin: 10
    },
    textLive: {
        color: '#000',
        marginBottom: 5
    },
    songName: {
        fontSize: 20,
        textAlign: 'center',
        color: '#000'
    }
});

Player.propTypes = {
    url: React.PropTypes.string.isRequired
};

export { Player, RNReactNativeAudioStreaming }
