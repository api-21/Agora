package com.example.agoratesting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
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
import java.lang.Exception


class VideoCallScreen : AppCompatActivity() {

    private var rtcEngine: RtcEngine? = null
    lateinit var binding: ActivityVideoCallScreenBinding
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call_screen)

        /* Requesting Permissison*/
        requestPermission()


        /* Initalize Agora*/
        initAgoraEngineAndJoinChannel()


    }

    private fun initAgoraEngineAndJoinChannel() {

        joinChannel()
        setVideoConfiguration()
        initRtcEngine()
        setLocalVideo()

    }

    private fun setLocalVideo() {

        localView = RtcEngine.CreateRendererView(baseContext)
        localView!!.setZOrderMediaOverlay(true)
        binding.localVideoViewContainer.addView(localView)

        rtcEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    private fun setRemoteVideo(uid: Int) {

        remoteView = RtcEngine.CreateRendererView(baseContext)
        remoteView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteView)

        rtcEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FILL, uid))
    }

    private fun joinChannel() {

        rtcEngine?.joinChannel(getString(R.string.TOKEN), "AgoraDemo", "Optional Data", 0)
    }

    private fun setVideoConfiguration() {
        rtcEngine?.enableVideo()
        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1,
                STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun initRtcEngine() {

        rtcEngine = RtcEngine.create(this, getString(R.string.APP_ID), mIrtcEventHandler)
    }


    val mIrtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)

            runOnUiThread {
                Toast.makeText(
                    this@VideoCallScreen,
                    "Channel Join Success: $channel, uid: $uid",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                setRemoteVideo(uid)
            }
        }

//        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
//            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
//            runOnUiThread {
//                setRemoteVideo(uid)
//            }
//
//        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                onRemoteUserLeft()
            }
        }

    }

    private fun onRemoteUserLeft() {

        if (remoteView != null) {
            binding.remoteVideoViewContainer.removeAllViews()
        }
    }

    private fun removeLocalVideo() {

        if (localView != null) {
            binding.localVideoViewContainer.removeAllViews()
        }
    }

    private fun leaveChannel() {
        rtcEngine!!.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
    }

    /*Permission Functions For Agora Engine */

    /*
    *
    * MODIFY_AUDIO_SETTINGS
    * ACCESS_NETWORK_STATE
    * CAMERA
    * BLUETOOTH
    * WRITE_EXTERNAL_STORAGE
    * RECORD_AUDIO
    * INTERNET
    * */
    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasRecordAudioPermission() = ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetoothPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteExternalStoragePermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasNetworkStatePermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_NETWORK_STATE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasModifiAudioSettionsPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        val permissionList = mutableListOf<String>()
        if (!hasBluetoothPermission()) {
            permissionList.add(Manifest.permission.BLUETOOTH)
        }
        if (!hasCameraPermission()) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (!hasNetworkStatePermission()) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (!hasRecordAudioPermission()) {
            permissionList.add(Manifest.permission.RECORD_AUDIO)
        }
        if (!hasWriteExternalStoragePermission()) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!hasModifiAudioSettionsPermission()) {
            permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }
        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                }
            }
        }
    }

    fun onSwitchCameraClicked(view: View) {
        rtcEngine?.switchCamera()
    }
    fun onLocalAudioMuteClicked(view: View) {
        rtcEngine?.muteLocalAudioStream(true)
    }
    fun onEndCallClicked(view: View) {
        removeLocalVideo()
        rtcEngine?.leaveChannel()
    }

    fun onStartCall(view: View) {
        initAgoraEngineAndJoinChannel()
    }

    /*
    *
    * Implementing Agora From Here
    *
    * */


}