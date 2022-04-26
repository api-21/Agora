package com.example.agoratesting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.databinding.DataBindingUtil
import com.example.agoratesting.databinding.ActivityVideoCallScreenBinding
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE
import io.agora.rtc.video.VideoEncoderConfiguration.VD_640x480


class VideoCallScreen : AppCompatActivity() {

    lateinit var rtcEngine: RtcEngine
    lateinit var binding: ActivityVideoCallScreenBinding
    private val PERMISSION_REQ_ID = 22


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call_screen)

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initAgoraEngine();
        }


    }

    // Handle SDK Events
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            setUpRemoteVideoStream(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            onRemoteUserLeft(uid)
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            onRemoteUserToggled(uid, state)

        }

    }

    private fun onRemoteUserToggled(uid: Int, state: Int) {
        onRemoteUserToggle(uid, state)
    }

    private fun onRemoteUserLeft(uid: Int) {
        removeVideo(binding.bgVideoContainer.id)
    }

    private fun setUpRemoteVideoStream(uid: Int) {

    }

    private fun initAgoraEngine() {
        try {
            rtcEngine = RtcEngine.create(applicationContext, "Agora Key", mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("AgoraInitageEngine", "${e.message}")
        }

        setUpSession()

    }

    private fun setUpSession() {
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine.enableVideo()
        rtcEngine.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1, STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun setUpLocalVideoFeed() {

        val videoSurface: SurfaceView = RtcEngine.CreateRendererView(applicationContext)
        videoSurface.setZOrderMediaOverlay(true)
        binding.bgVideoContainer.addView(videoSurface)
        rtcEngine.setupLocalVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideoStream(uid: Int) {

        if (binding.bgVideoContainer.childCount >= 1) {
            return
        }
        val videoSurface: SurfaceView = RtcEngine.CreateRendererView(applicationContext)
        rtcEngine.setupRemoteVideo(VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid))
        rtcEngine.setRemoteSubscribeFallbackOption(Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY)
    }

    fun muteCall(view: View) {

//        if (binding.btnMute.isSelected){
//            binding.btnMute.isSelected = false
//
//        }

        rtcEngine.muteLocalAudioStream(binding.btnMute.isSelected)

    }

    fun endCall(view: View) {

        leaveChannel()
    }

    fun flipCamera(view: View) {


    }

    private fun leaveChannel() {
        rtcEngine.leaveChannel()
        Toast.makeText(this, "Channel Exiting.....", Toast.LENGTH_SHORT).show()
    }

    private fun onJoinChannelClicked() {
        rtcEngine.joinChannel(null, "test-channel", "XpressCure", 0)
        setUpLocalVideoFeed()
    }

    private fun removeVideo(containerId: Int) {
        binding.bgVideoContainer.removeAllViews()
    }

    private fun onRemoteUserToggle(uid: Int, state: Int) {
        val videoSurface: SurfaceView = binding.bgVideoContainer.getChildAt(0) as SurfaceView
        videoSurface.visibility = if (state === 0) View.GONE else View.VISIBLE

        if (state == 0) {
            Toast.makeText(this, "Video Disable", Toast.LENGTH_SHORT).show()
        } else {
            binding.bgVideoContainer.getChildAt(1) as ImageView
        }
    }

    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.e("LOG_TAG", "checkSelfPermission: $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                REQUESTED_PERMISSIONS,
                requestCode
            )
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i("", "onRequestPermissionsResult: ${grantResults[0]}, $requestCode")
        when (requestCode) {
            PERMISSION_REQ_ID -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(
                        "Need permissions",
                        "onRequestPermissionsResult: ${Manifest.permission.RECORD_AUDIO}"
                    )

                }
                initAgoraEngine()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()

    }


}