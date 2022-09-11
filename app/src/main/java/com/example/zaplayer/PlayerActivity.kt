package com.example.zaplayer

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.zaplayer.databinding.ActivityPlayerBinding
import com.example.zaplayer.databinding.BoosterBinding
import com.example.zaplayer.databinding.MoreFeatureBinding
import com.example.zaplayer.databinding.SpeedDialogBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {
    companion object{
        private var timer: Timer? = null
        lateinit var player: ExoPlayer
        lateinit var playerlist:ArrayList<Video>
        var position: Int = -1
        var repeat :Boolean = false
        var isfullscreen:Boolean = false
        private var issubtitle:Boolean = true
        var islocked:Boolean = false
        private lateinit var trackselector: DefaultTrackSelector
        private lateinit var laudnessenocar: LoudnessEnhancer
        private var speed:Float = 1.0f
        var pipStatus: Int = 0
        var nowPlayingId:String = ""
    }
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runable:Runnable
    private var moreTime:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        binding = ActivityPlayerBinding.inflate(layoutInflater)

        setTheme(R.style.Forplayer)
        val view = binding.root

        setContentView(view)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        WindowInsetsControllerCompat(window,binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE



        }
        initilizeview()
        initializebinding()
        binding.forwdfl.setOnClickListener(DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.exoplayer.showController()
                binding.forwardbtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition + 10000)
                moreTime = 0
            }
        }))
        binding.rewindfl.setOnClickListener(DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.exoplayer.showController()
                binding.rewindbtn.visibility = View.VISIBLE
                player.seekTo(player.currentPosition - 10000)
                moreTime = 0
            }
        }))


    }
    private fun nextprevbtn(isnext:Boolean = true){
        if (isnext) setposition()
        else setposition(isincrement = false)
        createplayer()



    }
    private fun setposition(isincrement:Boolean = true){
        if (!repeat){
            if (isincrement){
                if (playerlist.size - 1 == position){
                    position= 0
                }else
                    ++position

            }else{
                if (position == 0){
                    position= playerlist.size - 1
                }else
                    --position

            }
        }
    }

    @SuppressLint("PrivateResource")
    private fun initilizeview() {

        when(intent.getStringExtra("class")){
            "AllVideos" -> {
                playerlist = ArrayList()
                playerlist.addAll(MainActivity.videoList)
                createplayer()
            }
            "FolderActivity" -> {
                playerlist = ArrayList()
                playerlist.addAll(FolderActivity.currentfoldervideos)
                createplayer()
            }
            "SearchVideos" -> {
                playerlist = ArrayList()
                playerlist.addAll(MainActivity.searchList)
                createplayer()

            }
            "NowPlaying" -> {
                speed = 1.0f
                binding.videotitle.text = playerlist[position].title
                binding.videotitle.isSelected = true
                binding.exoplayer.player = player
                playvideo()
                playinfullscreen(isfullscreen)
                setvisibility()

            }
        }
        if (repeat) binding.repeatbtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
        else binding.repeatbtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PrivateResource", "SetTextI18n", "ObsoleteSdkInt")
    private fun initializebinding(){

        binding.backbtn.setOnClickListener {
            finish()
        }
        binding.playpause.setOnClickListener {
            if (player.isPlaying)
            {
                pausevideo()
            }else{
                playvideo()
            }

        }
        binding.next.setOnClickListener { nextprevbtn() }
        binding.prev.setOnClickListener { nextprevbtn(false) }
        binding.repeatbtn.setOnClickListener {
            if (repeat){
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatbtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
            }else{
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.repeatbtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)

            }

        }
        binding.fullscreenbtn.setOnClickListener {
            if (isfullscreen){
                isfullscreen = false
                playinfullscreen(false)
            }else{
                isfullscreen = true
                playinfullscreen(true)
            }
        }
        binding.lockbtn.setOnClickListener {
            if (!islocked){
                islocked = true
                binding.exoplayer.hideController()
                binding.exoplayer.useController = false
                binding.lockbtn.setImageResource(R.drawable.ic_baseline_lock_24)
            }else{
                islocked = false
                binding.exoplayer.useController = true
                binding.exoplayer.showController()

                binding.lockbtn.setImageResource(R.drawable.lock)
            }
        }
        binding.menu.setOnClickListener {
            pausevideo()
            val customdialog = LayoutInflater.from(this).inflate(R.layout.more_feature,binding.root,false)
            val bindingMF = MoreFeatureBinding.bind(customdialog)
            val diaolog = MaterialAlertDialogBuilder(this).setView(customdialog)
                .setOnCancelListener { playvideo() }
                .setBackground(ColorDrawable(0x80018786.toInt()))
                .create()
            diaolog.show()
            bindingMF.audiotrack.setOnClickListener {
                diaolog.dismiss()
                playvideo()

                val audiotrack = ArrayList<String>()
                for (i in 0 until player.currentTrackGroups.length){
                    if (player.currentTrackGroups.get(i).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
                        audiotrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }
                val temptracks = audiotrack.toArray(arrayOfNulls<CharSequence>(audiotrack.size))

                MaterialAlertDialogBuilder(this,R.style.allertdialog)
                    .setTitle("Sellect Language...")
                    .setOnCancelListener { playvideo() }
                    .setBackground(ColorDrawable(0x80018786.toInt()))
                    .setItems(temptracks){_, position ->
                        Toast.makeText(this,audiotrack[position] + "Sellected...",Toast.LENGTH_SHORT ).show()
                        trackselector.setParameters(trackselector.buildUponParameters().setPreferredAudioLanguage(audiotrack[position]))


                    }
                    .create()
                    .show()

            }
            bindingMF.subtitles.setOnClickListener {
                if (issubtitle){
                    trackselector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(C.TRACK_TYPE_VIDEO,true).build()
                    Toast.makeText(this,"Subtitles off...",Toast.LENGTH_SHORT ).show()
                    issubtitle = false

                }else{
                    trackselector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(C.TRACK_TYPE_VIDEO,false).build()
                    Toast.makeText(this,"Subtitles on...",Toast.LENGTH_SHORT ).show()
                    issubtitle = true
                }
                diaolog.dismiss()
                playvideo()
            }
            bindingMF.audiobooster.setOnClickListener {
                diaolog.dismiss()
                val customdialogb = LayoutInflater.from(this).inflate(R.layout.booster,binding.root,false)
                val bindingb = BoosterBinding.bind(customdialogb)
                val diaologb = MaterialAlertDialogBuilder(this).setView(customdialogb)
                    .setOnCancelListener { playvideo() }
                    .setPositiveButton("ok"){self, _ ->
                        laudnessenocar.setTargetGain(bindingb.verticalbar.progress * 100)
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x80018786.toInt()))
                    .create()
                diaologb.show()
                bindingb.verticalbar.progress = laudnessenocar.targetGain.toInt()/100
                bindingb.progresstext.text = "Audio Boost\n\n${laudnessenocar.targetGain.toInt()/10} %"
                bindingb.verticalbar.setOnProgressChangeListener {
                    bindingb.progresstext.text = "Audio Boost\n\n${it*10} %"
                }

            }
            bindingMF.speedbtn.setOnClickListener {
                diaolog.dismiss()
                playvideo()
                val customdialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                val bindinS = SpeedDialogBinding.bind(customdialogS)
                val diaologS = MaterialAlertDialogBuilder(this).setView(customdialogS)
                    .setCancelable(false)
                    .setPositiveButton("ok"){self, _ ->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x80018786.toInt()))
                    .create()
                diaologS.show()
                bindinS.speedtxt.text = "${DecimalFormat("#.##").format(speed)} X"
                bindinS.minusbtn.setOnClickListener {
                    changeSpeed(false)
                    bindinS.speedtxt.text = "${DecimalFormat("#.##").format(speed)} X"
                }
                bindinS.plusbtn.setOnClickListener {
                    changeSpeed(true)
                    bindinS.speedtxt.text = "${DecimalFormat("#.##").format(speed)} X"
                }

            }
            bindingMF.timerbtn.setOnClickListener {
                diaolog.dismiss()
                if (timer!=null) Toast.makeText(this,"Timer Already set...",Toast.LENGTH_SHORT).show()
                else{
                    var sleepTime = 1
                    val customdialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                    val bindinS = SpeedDialogBinding.bind(customdialogS)
                    val diaologS = MaterialAlertDialogBuilder(this).setView(customdialogS)
                        .setCancelable(false)
                        .setPositiveButton("ok"){self, _ ->
                            timer = Timer()
                            val task = object : TimerTask(){
                                override fun run() {
                                    moveTaskToBack(true)
                                    exitProcess(1)
                                }

                            }
                            timer!!.schedule(task,sleepTime*60*1000.toLong())
                            self.dismiss()
                            playvideo()
                        }
                        .setBackground(ColorDrawable(0x80018786.toInt()))
                        .create()
                    diaologS.show()
                    bindinS.speedtxt.text = "$sleepTime Min"
                    bindinS.minusbtn.setOnClickListener {
                        if (sleepTime > 1)sleepTime -=1
                        bindinS.speedtxt.text = "$sleepTime Min"
                    }
                    bindinS.plusbtn.setOnClickListener {
                        if (sleepTime < 10)sleepTime += 1
                        bindinS.speedtxt.text = "$sleepTime Min"
                    }
                }


            }
            bindingMF.pipbtn.setOnClickListener {
                val appoPs = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ appoPs.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(),packageName)== AppOpsManager.MODE_ALLOWED
                } else {
                    false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if (status) {

                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        diaolog.dismiss()
                        binding.exoplayer.hideController()
                        playvideo()
                        pipStatus = 0

                    }else{
                        val intent = Intent("andriod.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.parse("package:$packageName"))
                        startActivity(intent)

                    }


                } else{
                    Toast.makeText(this, "Feature not supported!!", Toast.LENGTH_SHORT).show()
                    diaolog.dismiss()
                    playvideo()
                }


            }

        }
    }

    private fun createplayer(){
        try { player.release() }catch (e:Exception){}
        speed = 1.0f
        trackselector = DefaultTrackSelector(this)
        binding.videotitle.text = playerlist[position].title
        binding.videotitle.isSelected = true
        player = ExoPlayer.Builder(this).setTrackSelector(trackselector).build()
        binding.exoplayer.player = player
        val mediaItem = MediaItem.fromUri(playerlist[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playvideo()
        player.addListener(object: Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) nextprevbtn()
            }

        })
        playinfullscreen(isfullscreen)
        setvisibility()
        laudnessenocar = LoudnessEnhancer(player.audioSessionId)
        laudnessenocar.enabled = true
        nowPlayingId = playerlist[position].id

    }
    private fun playinfullscreen(isenable:Boolean){
        if (isenable){
            binding.exoplayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullscreenbtn.setImageResource(R.drawable.ic_baseline_fullscreen_exit_24)
        }else{
            binding.exoplayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullscreenbtn.setImageResource(R.drawable.fullscreen_icon)
        }

    }
    private fun setvisibility(){
        runable = Runnable {
            if (binding.exoplayer.isControllerVisible) changevisibility(View.VISIBLE)
            else changevisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runable,300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runable,0)
    }
    private fun changevisibility(visibility:Int){
        binding.topcontroler.visibility = visibility
        binding.bottomcontroler.visibility = visibility
        binding.playpause.visibility = visibility

        if (islocked){
            binding.lockbtn.visibility = View.VISIBLE
        }else binding.lockbtn.visibility = visibility
        if (moreTime == 2){
            binding.forwardbtn.visibility = View.GONE
            binding.rewindbtn.visibility = View.GONE

        }else ++moreTime

    }

    override fun onDestroy() {
        super.onDestroy()
        player.pause()
    }
    private fun playvideo(){
        binding.playpause.setImageResource(R.drawable.pause)
        player.play()
    }
    private fun pausevideo(){
        binding.playpause.setImageResource(R.drawable.playicon)
        player.pause()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        if (pipStatus != 0){
            finish()
            val intent = Intent(this,PlayerActivity::class.java)
            when(pipStatus){
                1 -> intent.putExtra("class","FolderActivity")
                2 -> intent.putExtra("class","SearchVideos")
                3 -> intent.putExtra("class","AllVideos")
            }
            startActivity(intent)
        }
    }
    private fun changeSpeed(isincrement: Boolean){
        if (isincrement){
            if (speed <= 2.9f){
                speed += 0.10f
            }
        }else{
            if (speed > 0.20f){
                speed -= 0.10f
            }


        }
        player.setPlaybackSpeed(speed)
    }
}

