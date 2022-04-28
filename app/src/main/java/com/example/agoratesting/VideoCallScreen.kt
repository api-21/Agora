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
        requestPermission()



        /* Initalize Agora*/
        initAgoraEngineAndJoinChannel()



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

    /*
    *
    * Implementing Agora From Here
    *
    * */



    private fun initAgoraEngineAndJoinChannel() {

        initalizeAgoraEngine()
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        rtcEngine?.setClientRole(0)
        setUpVideoProfile()
        setUpLocalVideo()
        joinChannel()
    }

    private fun joinChannel() {
        rtcEngine?.joinChannel(getString(R.string.TOKEN), "DemoAgora", "Optional Text", 0)
    }

    private fun setUpLocalVideo() {
        val surfaceView: SurfaceView = RtcEngine.CreateRendererView(this)
        surfaceView.setZOrderMediaOverlay(true)
        binding.localVideoViewContainer.addView(surfaceView)
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setUpVideoProfile() {
        rtcEngine?.enableVideo()
        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun initalizeAgoraEngine() {
        try {
            rtcEngine =
                RtcEngine.create(applicationContext, getString(R.string.APP_ID), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("Agoraengine", "${e.message}")
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                setUpRemoteVideo(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                onRemoteUserLeft()
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread {
                Toast.makeText(this@VideoCallScreen, "Join Channel Success", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /*
    * Calling From mRtcEventHandler
    * For Handle When User Join Or Leave The Room
    * */
    private fun setUpRemoteVideo(uid: Int) {

        if (binding.remoteVideoViewContainer.childCount >= 1) {
            return
        }

            val surfaceView = RtcEngine.CreateRendererView(this)
            rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
            surfaceView.tag = uid

    }

    private fun onRemoteUserLeft() {
            binding.remoteVideoViewContainer.removeAllViews()

    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine?.leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null

    }

    fun onEndCallClicked(view: View) {
        initAgoraEngineAndJoinChannel()
    }
    fun onSwitchCameraClicked(view: View) {
        rtcEngine?.switchCamera()
    }
    fun onLocalAudioMuteClicked(view: View) {
        rtcEngine?.muteLocalAudioStream(true)
    }


}