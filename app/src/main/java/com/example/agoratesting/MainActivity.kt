package com.example.agoratesting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.agoratesting.databinding.ActivityMainBinding
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE
import io.agora.rtc.video.VideoEncoderConfiguration.VD_640x360

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var mEndCall = false
    private var mMuted = false
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null
    private var rtcEngine: RtcEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

       // requestForPermission()
        initRtcEngine()

        binding.joinRoom.setOnClickListener {
            startActivity(Intent(this, VideoCallScreen::class.java))
        }

//        binding.buttonCall.setOnClickListener {
//            if (mEndCall) {
//                startCall()
//                mEndCall = false
//                binding.buttonCall.setImageResource(R.drawable.ic_end_call)
//                binding.buttonMute.visibility = VISIBLE
//                binding.buttonSwitchCamera.visibility = VISIBLE
//
//            } else {
//                endCall()
//                mEndCall = true
//                binding.buttonCall.setImageResource(R.drawable.ic_baseline_local_phone_24)
//                binding.buttonMute.visibility = INVISIBLE
//                binding.buttonSwitchCamera.visibility = INVISIBLE
//            }
//        }
//
//        binding.buttonSwitchCamera.setOnClickListener {
//            rtcEngine?.switchCamera()
//        }
//
//        binding.buttonMute.setOnClickListener {
//            mMuted = !mMuted
//            rtcEngine?.muteLocalAudioStream(mMuted)
//            val res: Int = if (mMuted) {
//                R.drawable.ic_mute
//            } else {
//                R.drawable.ic_unmute
//            }
//
//            binding.buttonMute.setImageResource(res)
//        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onConnectionLost() {
            super.onConnectionLost()
            Toast.makeText(this@MainActivity, "Poor Connection", Toast.LENGTH_SHORT).show()
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Joined Channel Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /*
         * Listen for the onFirstRemoteVideoDecoded callback.
         * This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
         * You can call the setupRemoteVideoView method in this callback to set up the remote video view.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread {
                setupRemoteVideoView(uid)
            }
        }

        /*
        * Listen for the onUserOffline callback.
        * This callback occurs when the remote user leaves the channel or drops offline.
        */
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                onRemoteUserLeft()
            }
        }
    }

    private fun setupSession() {
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine?.enableVideo()
        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun initAndJoinChannel() {

        // This is our usual steps for joining
        // a channel and starting a call.
        initRtcEngine()
        setupVideoConfig()
        setupLocalVideoView()
        joinChannel()
        // setupSession()
    }

    // Initialize the RtcEngine object.
    private fun initRtcEngine() {
        try {
            Log.e("InitRtcEngine", "InitRtcEngine: Successful")
            rtcEngine = RtcEngine.create(baseContext, getString(R.string.APP_ID), mRtcEventHandler)
        } catch (e: Exception) {
            Log.d("InitRtcEngine", "initRtcEngine: $e")
        }
    }

    private fun setupLocalVideoView() {

        Log.e("setupVideoConfig", "setupLocalVideoView: Successfull ")

        localView = RtcEngine.CreateRendererView(baseContext)
        localView!!.setZOrderMediaOverlay(true)
//        binding.localVideoView.addView(localView)

        // Set the local video view.
        rtcEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun setupRemoteVideoView(uid: Int) {

        Toast.makeText(this, "Setup Remote Video View", Toast.LENGTH_SHORT).show()

//        if (binding.remoteVideoView.childCount > 1) {
//            return
//        }
//        remoteView = RtcEngine.CreateRendererView(applicationContext)
//        remoteView?.setZOrderMediaOverlay(true)
//        binding.remoteVideoView.addView(remoteView)
//        rtcEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun setupVideoConfig() {

        Log.e("setupVideoConfig", "setupVideoConfig: Successful")

        rtcEngine?.enableVideo()

        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1,
                STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun joinChannel() {
        // Join a channel with a token.
        rtcEngine?.joinChannel(getString(R.string.TOKEN), "ChannelOne", "Extra Optional Data", 0)
    }

    private fun startCall() {
        setupLocalVideoView()
        joinChannel()
        initAndJoinChannel()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        leaveChannel()
    }

    private fun removeLocalVideo() {
//        if (localView != null) {
//            binding.localVideoView.removeView(localView)
//        }
        localView = null
    }

    private fun removeRemoteVideo() {
        Toast.makeText(this, "User End The Call", Toast.LENGTH_SHORT).show()
        // binding.remoteVideoView.removeView(remoteView)
//        if (remoteView != null) {
//            binding.remoteVideoView.removeView(remoteView)
//        }
        remoteView = null
    }

    private fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        removeRemoteVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mEndCall) {
            leaveChannel()
        }
        RtcEngine.destroy()
    }


    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasGallaryPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasChangeAudioSettings() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED


    private fun hasRecordAudio() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetooth() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasNetworkState() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_NETWORK_STATE
    ) == PackageManager.PERMISSION_GRANTED


    private fun requestForPermission() {

        val permissionList = mutableListOf<String>()

        if (!hasCameraPermission()) {
            permissionList.add(Manifest.permission.CAMERA)
        }

        if (!hasChangeAudioSettings()) {
            permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        if (!hasGallaryPermission()) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!hasNetworkState()) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (!hasBluetooth()) {
            permissionList.add(Manifest.permission.BLUETOOTH)
        }
        if (!hasRecordAudio()) {
            permissionList.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionList.isNotEmpty()) {

            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                     Here we continue only if all permissions are granted.
                    // The permissions can also be granted in the system settings manually.
                    initAndJoinChannel()

                } else {
                    Toast.makeText(this, "$i Permission Not Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}