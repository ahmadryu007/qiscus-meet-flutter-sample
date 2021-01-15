import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:meet_flutter/system_event_request.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  static const MethodChannel _channel = const MethodChannel('qiscusmeet_plugin');

  @override
  void initState() {
    super.initState();
    _permissionHandler();
  }

  Future<void> _permissionHandler() async {
    Map<Permission, PermissionStatus> statuses = await [
      Permission.camera,
      Permission.storage,
      Permission.microphone,
      Permission.calendar
    ].request();
    print(statuses);
  }

  void _launchMeetSdk() {
    //_channel.invokeMapMethod("video_call");
    _callAction(true);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'Meet SDK Qiscuss, User 2 Sample Call',
            )
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _launchMeetSdk,
        child: Icon(Icons.video_call),
      ),
    );
  }

  Future<void> _callAction(bool isVideo) async {
    try {
      SystemEventRequest data = SystemEventRequest(
          systemEventType: "custom",
          message: "Call Incoming",
          subjectEmail: "call@dwidasa.com",
          roomId: "33443516",
          payload: Payload(
              type: "call",
              callEvent: "incoming",
              callRoomId: "33443516",
              callIsVideo: isVideo,
              callCaller: CallCalle(
                  avatar: "https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png",
                  name: "User 2 Sample Call",
                  username: "User 2 Sample Call"),
              callCallee: CallCalle(
                  avatar: "https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png",
                  name: "User 1 Sample Call",
                  username: "User 1 Sample Call")));
      Response response = await Dio().post(
          "https://api.qiscus.com/api/v2.1/rest/post_system_event_message",
          data: data.toJson(),
          options: Options(headers: {
            'QISCUS-SDK-APP-ID': 'kawan-seh-g857ffuuw9b',
            'QISCUS_SDK_SECRET': 'c7f3ab87acc3843a1b81d77c2b4d6b0c'
          }));
      print(response);

      _channel.invokeMethod("video_call");

    } catch (e) {
      print(e);
    }
  }

}
