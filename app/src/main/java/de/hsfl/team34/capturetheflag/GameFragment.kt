package de.hsfl.team34.capturetheflag

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lokibt.bluetooth.BluetoothDevice
import de.hsfl.team34.capturetheflag.databinding.FragmentGameBinding
import org.json.JSONObject



class GameFragment : Fragment() {

    private val devices = mutableListOf<BluetoothDevice>()

    val viewModel : MainViewModel by activityViewModels()
    val handler : Handler = Handler()
    val areaForCapturing = 0.02

    var playerLatitude : Double = 0.0
    var playerLongitude : Double = 0.0
    var pointCollectionServer : MutableList<JSONObject> = mutableListOf()
    var pointID : Int = 0
    var lastTeam : Int = 0
    var timerStarted = false
    var lastPosLat = playerLatitude
    var lastPosLong = playerLongitude


    val timer = object : CountDownTimer(10000, 1000){
        override fun onTick(p0: Long) {
            if(devices.size > 0){
                timerStarted = false
                viewModel.conquerPoint(pointID, lastTeam, {
                    Toast.makeText(context, "Enemy has defended this point!", Toast.LENGTH_LONG)
                }, {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG)
                })
                viewModel.setTeamAtPoint(pointID, lastTeam)
                stopTimer()
            }
        }
        override fun onFinish() {
            timerStarted = false
            lastTeam = viewModel.getTeam().value!!
            viewModel.conquerPoint(pointID, viewModel.getTeam().value!!,
                {
                    Toast.makeText(context, "You have conquered this point!", Toast.LENGTH_LONG)
                }, {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_LONG)
                })
            viewModel.setTeamAtPoint(pointID, viewModel.getTeam().value!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity as MainActivity).startLocation()
        (activity as MainActivity)?.setDiscoverableGameMode()

        val binding = FragmentGameBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val gameToStartBtn = binding.gameLeaveBtn
        val map = binding.mapView
        val coordinator = binding.coordinatorLayoutBar!!

        map.drawPlayer = true
        map.drawPointCollection = true
        map.drawPointCollectionSever = true

        viewModel.stopRefreshPlayerList()

        viewModel.playerAdapter = PlayerAdapter(mutableListOf())
        binding.recyclerViewGamePlayers?.adapter = viewModel.playerAdapter
        binding.recyclerViewGamePlayers?.layoutManager = LinearLayoutManager(context)
        viewModel.searchForDevices()

        gameToStartBtn.setOnClickListener {
            Snackbar.make(coordinator, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_quit) {

                    if(viewModel.getNextScreen().value!!) {
                        stopRefreshGamePoints()
                        viewModel.setNextScreen(false)
                        viewModel.stopServer()
                        viewModel.leaveGame()
                        navController.navigate(R.id.action_gameFragment_to_startFragment)

                    } else {
                        viewModel.createErrorToast()
                    }
                }
                .show()
        }

        viewModel.getHasGameEnded().observe(viewLifecycleOwner){
            if(viewModel.getHasGameEnded().value!!){
                stopRefreshGamePoints()
                dialog(navController, viewModel.getWinnerTeam().value!!)
            }
        }

        viewModel.getPointsCollectionServer().observe(viewLifecycleOwner) {
            map.pointsCollectionServer = it
            pointCollectionServer = it
        }

        viewModel.getLocationPosition().observe(viewLifecycleOwner) {
            map.locationPlayer = it
            if(lastPosLat != it.getDouble("lat") && lastPosLong != it.getDouble("long")){
                checkCollision()
            }
            playerLatitude = it.getDouble("lat")
            playerLongitude = it.getDouble("long")
        }

        map.mapViewSize.observe(viewLifecycleOwner){
            viewModel.setMapViewSize(it)
        }

        viewModel.getDevices().observe(viewLifecycleOwner) {
            devices.clear()
            devices.addAll(it)
        }

        refreshGamePoints(4000, viewModel)
        return binding.root
    }

    private fun checkCollision(){
        for(point in pointCollectionServer){
            if (playerLatitude <= point.getDouble("lat") + areaForCapturing && playerLatitude >= point.getDouble("lat") - areaForCapturing
                && playerLongitude <= point.getDouble("long") + areaForCapturing && playerLongitude >= point.getDouble("long") - areaForCapturing)
            {
                if(!timerStarted){
                    lastTeam = point.getInt("team")
                    if(lastTeam != viewModel.getTeam().value){
                        timerStarted = true
                        pointID = point.getInt("id")
                        viewModel.conquerPoint(pointID, -1,
                            {
                                timer.start()
                            },
                            {
                                Toast.makeText(context, it.toString(), Toast.LENGTH_LONG)
                            })
                    }
                }
            }
        }
    }

    fun refreshGamePoints(interval : Long, viewModel : MainViewModel) {
        handler.post(object : Runnable {
            override fun run() {
                viewModel.getGamePoints()
                viewModel.gameLobby()
                handler.postDelayed(this, interval)
            }
        })
    }

    fun stopTimer(){
        timer.cancel()
    }

    fun stopRefreshGamePoints(){
        handler.removeCallbacksAndMessages(null)
    }

    fun dialog(navController : NavController, team : String){
        var builder = AlertDialog.Builder(activity)
        builder.setTitle("Game End")
        builder.setMessage("Team ${team} Won")
        builder.setPositiveButton("Leave", DialogInterface.OnClickListener{ dialog, id ->
            viewModel.leaveGame()
            navController.navigate(R.id.action_gameFragment_to_startFragment)
            dialog.cancel()
        })
        var alert = builder.create()
        alert.show()

    }

}