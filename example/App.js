
import React, { Component } from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    View,
    ListView,
    Platform,
    TouchableOpacity
} from 'react-native';
import { ReactNativeAudioStreaming, Player } from 'react-native-audio-streaming';

export default class App extends Component {
    constructor() {
        super();
        this.ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});
        this.urls = [
            {
                name: 'Shoutcast stream',
                url: 'http://lacavewebradio.chickenkiller.com:8000/stream.mp3'
            },
            {
                name: 'M4A stream',
                url: 'http://web.ist.utl.pt/antonio.afonso/www.aadsm.net/libraries/id3/music/02_Viandanze.m4a'
            },
            {
                name: 'MP3 stream with ID3 meta data',
                url: 'http://web.ist.utl.pt/antonio.afonso/www.aadsm.net/libraries/id3/music/Bruno_Walter_-_01_-_Beethoven_Symphony_No_1_Menuetto.mp3'
            },
            {
                name: 'MP3 stream',
                url: 'http://www.stephaniequinn.com/Music/Canon.mp3'
            }
        ];

        this.state = {
            dataSource: this.ds.cloneWithRows(this.urls),
            selectedSource: this.urls[0].url
        };
    }

    render() {
        return (
            <View style={styles.container}>
                <ListView
                    dataSource={this.state.dataSource}
                    renderRow={(rowData) =>
                        <TouchableOpacity onPress={() => {
                            this.setState({selectedSource: rowData.url, dataSource: this.ds.cloneWithRows(this.urls)});
                            ReactNativeAudioStreaming.play(rowData.url, {});
                        }}>
                            <View style={StyleSheet.flatten([
                                styles.row,
                                {backgroundColor: rowData.url == this.state.selectedSource ? '#3fb5ff' : 'white'}
                            ])}>
                                <Text style={styles.icon}>▸</Text>
                                <View style={styles.column}>
                                    <Text style={styles.name}>{rowData.name}</Text>
                                    <Text style={styles.url}>{rowData.url}</Text>
                                </View>
                            </View>
                        </TouchableOpacity>
                    }
                />

                <Player url={this.state.selectedSource} />
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
        paddingTop: Platform.OS === 'ios' ? 30 : 0
    },
    row: {
        flex: 1,
        flexDirection: 'row',
        padding: 5,
        borderBottomColor: 'grey',
        borderBottomWidth: 1
    },
    column: {
        flexDirection: 'column'
    },
    icon: {
        fontSize: 26,
        width: 30,
        textAlign: 'center'
    },
    name: {
        color: '#000'
    },
    url: {
        color: '#CCC'
    }
});
