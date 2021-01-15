package com.example.meet_flutter

import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.qiscus.meet.MeetJwtConfig
import com.qiscus.meet.MeetTerminatedConfEvent
import com.qiscus.meet.QiscusMeet
import com.qiscus.sdk.Qiscus
import com.qiscus.sdk.chat.core.data.model.QiscusComment
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject

class MainActivity: FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        if (Build.VERSION.SDK_INT > 9) {
            val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

//        Qiscus.setUser("user2_sample_call@example.com", "123")
//                .withUsername("User 2 Sample Call")
//                .save()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//
        //Qiscus.init(this.application, "kawan-seh-g857ffuuw9b")
        QiscusMeet.setup(this.application, "kawan-seh-g857ffuuw9b", "https://meet.qiscus.com")
        QiscusMeet.config()
                .setChat(true)
                .setVideoThumbnailsOn(true)
                .setOverflowMenu(true)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "qiscusmeet_plugin")
                .setMethodCallHandler { call, result ->

//                    Qiscus.getChatConfig().notificationBuilderInterceptor = QiscusNotificationBuilderInterceptor { notificationBuilder, qiscusComment ->
//                        if (qiscusComment.type == QiscusComment.Type.SYSTEM_EVENT) {
//                            false
//                        } else true
//                    }
//
//

                    if (!EventBus.getDefault().isRegistered(MultidexClass())) {
                        EventBus.getDefault().register(MultidexClass())
                    }

                    // create JWT before making call
                    val jwtConfig =  MeetJwtConfig();
                    jwtConfig.setEmail("user2_sample_call@example.com"); // need pass the userID must be unique for each user
                    jwtConfig.build();

                    // set builder jwt object to Qiscus Meet config before making call
                    QiscusMeet.config().setJwtConfig(jwtConfig);

                    if (call.method == "video_call"){
                        var member = QiscusRoomMember()
                        member.username = "User 1 Sample Call"
                        member.avatar = "https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png"

                        startVideoCall(member)
                    }
                }
    }

    private fun startVideoCall(target: QiscusRoomMember) {
        val request = JSONObject()
        val payload = JSONObject()
        val caller = JSONObject()
        val callee = JSONObject()
        val roomId: String = "33443516"
        try {
            request.put("system_event_type", "custom")
            request.put("room_id", roomId)
            request.put("subject_email", target.email)
            request.put("message", "User 2 Sample Call" + " call " + target.username)
            payload.put("type", "call")
            payload.put("call_event", "incoming")
            payload.put("call_room_id", roomId)
            payload.put("call_is_video", true)
            caller.put("username", "User 2 Sample Call")
            caller.put("name", "User 2 Sample Call")
            caller.put("avatar", "https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png")
            callee.put("username", target.email)
            callee.put("name", target.username)
            callee.put("avatar", target.avatar)
            payload.put("call_caller", caller)
            payload.put("call_callee", callee)
            request.put("payload", payload)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val httpConnection = AsyncHttpUrlConnection("POST", "/api/v2/rest/post_system_event_message", request.toString(), object : AsyncHttpUrlConnection.AsyncHttpEvents {
            override fun onHttpError(errorMessage: String) {

            }

            override fun onHttpComplete(response: String) {

                try {
                    val objStream = JSONObject(response)
                    if (objStream.getInt("status") == 200) {
                        callAction(roomId)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
        httpConnection.setContentType("application/json")
        httpConnection.send()
    }

    fun callAction(roomId: String){
        QiscusMeet.call()
                .setTypeCall(QiscusMeet.Type.VIDEO)
                .setRoomId(roomId)
                .setDisplayName("User 2 Sample Call")
                .build(this)
    }
}

class MultidexClass: MultiDexApplication() {
    @Subscribe
    fun onReceivedComment(event: QiscusCommentReceivedEvent) {
        if (event.qiscusComment.extraPayload != null && event.qiscusComment.extraPayload != "null") {
            handleCallPn(event.qiscusComment)
        }
    }

    @Subscribe
    fun onReceiveRoomEvent(roomEvent: QiscusChatRoomEvent) {
        when (roomEvent.event) {
            QiscusChatRoomEvent.Event.CUSTOM -> {
                //here, you can listen custom event
                roomEvent.roomId // this is the room id
                roomEvent.user // this is the sender's qiscus user id
                val json = roomEvent.eventData //event data (JSON)
                try {
                    val event = json.getString("event")
                    val roomId = json.getString("sender")
                    handleCustomEvent(roomId, event)
                } catch (ex: Exception) {
                    Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleCustomEvent(roomId: String, event: String) {
        if (event.equals("rejected", ignoreCase = true)) {
            QiscusMeet.event(QiscusMeet.QiscusMeetEvent.REJECTED, roomId)
        }
    }

    private fun handleCallPn(remoteMessage: QiscusComment) {
        val json: JSONObject
        try {
            json = JSONObject(remoteMessage.extraPayload)
            val payload = json.getJSONObject("payload")
            if (payload["type"] == "call" || payload["type"] == "webview_call") {
                val event = payload.getString("call_event")
                when (event.toLowerCase()) {
                    "incoming" -> {
                        val roomId = payload["call_room_id"].toString()
                        val caller = payload.getJSONObject("call_caller")
                        val caller_email = caller.getString("username")
                        val caller_name = caller.getString("name")
                        val caller_avatar = caller.getString("avatar")
                        val callee = payload.getJSONObject("call_callee")
                        val callee_email = callee.getString("username")
                        val callee_name = callee.getString("name")
                        val callee_avatar = callee.getString("avatar")
                        if (Qiscus.getQiscusAccount().email == callee_email) {
                            val handler = Handler()
                            handler.postDelayed({
                                val intent = Intent(applicationContext, IncomingCallActivity::class.java)
                                intent.putExtra("callerAvatar", caller_avatar)
                                intent.putExtra("callerDisplayName", caller_name)
                                intent.putExtra("roomId", roomId)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }, 2500)
                        }
                    }
                    else -> {
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Subscribe
    fun onTerminatedConf(event: MeetTerminatedConfEvent) {
        Log.e("debug event", event.roomId)
        endCall(event.roomId)
        val json = JSONObject()
        try {
            json.put("sender", event.roomId)
            json.put("event", "rejected")
            json.put("active", false)
            QiscusPusherApi.getInstance().setEvent(event.roomId!!.toLong(), json)
        } catch (ex: Exception) {
            Log.e("IncomingCallActivity", ex.message)
        }
    }

    private fun endCall(roomId: String?) {
        val request = JSONObject()
        val payload = JSONObject()
        val caller = JSONObject()
        val callee = JSONObject()
        try {
            request.put("system_event_type", "custom")
            request.put("room_id", roomId)
            request.put("message", Qiscus.getQiscusAccount().username + " endcall ")
            payload.put("type", "endcall")
            payload.put("call_event", "endcall")
            payload.put("call_room_id", roomId)
            payload.put("call_is_video", true)
            caller.put("username", Qiscus.getQiscusAccount().email)
            caller.put("name", Qiscus.getQiscusAccount().username)
            caller.put("avatar", Qiscus.getQiscusAccount().avatar)
            payload.put("call_caller", caller)
            payload.put("call_callee", callee)
            request.put("payload", payload)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val httpConnection = AsyncHttpUrlConnection("POST", "/api/v2/rest/post_system_event_message", request.toString(), object : AsyncHttpUrlConnection.AsyncHttpEvents {
            override fun onHttpError(errorMessage: String) {
                Log.e("TAG", "API connection error: $errorMessage")
            }

            override fun onHttpComplete(response: String) {
                Log.d("TAG", "API connection success: $response")
            }
        })
        httpConnection.setContentType("application/json")
        httpConnection.send()
    }
}
