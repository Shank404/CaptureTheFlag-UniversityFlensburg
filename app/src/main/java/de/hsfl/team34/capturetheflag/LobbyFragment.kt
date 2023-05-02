package de.hsfl.team34.capturetheflag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lokibt.bluetooth.BluetoothDevice
import de.hsfl.team34.capturetheflag.databinding.FragmentLobbyBinding


class LobbyFragment : Fragment() {

    val viewModel : MainViewModel by activityViewModels()


    private val devices = mutableListOf<BluetoothDevice>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity as MainActivity).startLocation()

        val binding = FragmentLobbyBinding.inflate(inflater,container,false)
        val navController = findNavController()
        val coordinator = binding.coordinatorLayoutBar!!
        val start : Button = binding.lobbyStartBtn
        val leave : Button = binding.lobbyLeaveBtn
        val autoWeight : Button? = binding.lobbyAutoWeightBtn

        autoWeight?.setOnClickListener {
            viewModel.autoWeightFunction()
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.playerAdapter = PlayerAdapter(mutableListOf())
        binding.recyclerViewLobbyPlayers?.adapter = viewModel.playerAdapter
        binding.recyclerViewLobbyPlayers?.layoutManager = LinearLayoutManager(context)

        start.isVisible = viewModel.getIsHost().value!!
        autoWeight?.isVisible = viewModel.getIsHost().value!!

        viewModel.getDevices().observe(viewLifecycleOwner) {
            devices.clear()
            devices.addAll(it)
            for (device : BluetoothDevice in devices){
                viewModel.sendMessage(device)
            }
        }

        viewModel.getIsGameStarted().observe(viewLifecycleOwner){
            if(viewModel.getIsGameStarted().value!!){
                viewModel.setNextScreen(false)
                navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
            }
        }

        viewModel.getIsKicked().observe(viewLifecycleOwner){
            if(viewModel.getIsKicked().value!!){
                viewModel.setNextScreen(false)
                viewModel.setIsKicked(false)
                navController.navigate(R.id.action_lobbyFragment_to_startFragment)
            }
        }

        start.setOnClickListener {
            if(viewModel.getNextScreen().value!!) {
                viewModel.startGame()
                viewModel.setNextScreen(false)
                navController.navigate(R.id.action_lobbyFragment_to_gameFragment)
            } else {
                viewModel.createErrorToast()
            }
        }

        leave.setOnClickListener {
            Snackbar.make(coordinator, R.string.snackbar_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_quit) {
                    viewModel.leaveGame()
                    if(viewModel.getNextScreen().value!!) {
                        viewModel.setNextScreen(false)
                        navController.navigate(R.id.action_lobbyFragment_to_startFragment)
                    } else {
                        viewModel.createErrorToast()
                    }
                }
                .show()
        }

        viewModel.refreshPlayerList(500)
        return binding.root
    }




}