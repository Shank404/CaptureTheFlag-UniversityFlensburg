package de.hsfl.team34.capturetheflag

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.hsfl.team34.capturetheflag.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {

    val viewModel: MainViewModel by activityViewModels()
    val handler : Handler = Handler()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity as MainActivity).startLocation()

        val binding = FragmentJoinBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val name = binding.joinNameEditText
        val gameID = binding.joinGameIdEditText
        val joinGame = binding.joinJoinBtn
        val cancel = binding.joinCancelBtn
        var isClicked = false
        var nextScreen = false

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.gamesAdapter = GamesAdapter(mutableListOf(), viewModel)
        binding.recyclerViewJoinGames?.adapter = viewModel.gamesAdapter
        binding.recyclerViewJoinGames?.layoutManager = LinearLayoutManager(context)
        viewModel.getAllGames()

        joinGame.setOnClickListener {
            isClicked = true
            if (!viewModel.isFieldEmpty("name") && !viewModel.isFieldEmpty("gameID")) {
                navController.navigate(R.id.action_joinFragment_to_chooseTeamDialogFragment)
            } else {
                if (viewModel.isFieldEmpty("name")) name.error = "Bitte geben sie einen Namen ein!"
                if (viewModel.isFieldEmpty("gameID")) gameID.error = "Bitte geben sie eine GameID ein!"
            }
        }

        cancel.setOnClickListener {
            stopRefreshGamesList()
            navController.navigate(R.id.action_joinFragment_to_startFragment)
        }

        viewModel.getNextScreen().observe(viewLifecycleOwner) {
            if(viewModel.getNextScreen().value!!){
                viewModel.setNextScreen(false)
                nextScreen = true
                stopRefreshGamesList()
                (activity as MainActivity)?.activateBluetoothOrSearchForDevices()
                navController.navigate(R.id.action_chooseTeamDialogFragment_to_lobbyFragment)
            } else {
                if(isClicked && !nextScreen){
                    isClicked = false
                    nextScreen = false
                    viewModel.createErrorToast()
                }
            }
        }

        refreshGamesList(1000)
        return binding.root
    }

    private fun refreshGamesList(interval : Long) {
        handler.post(object : Runnable {
            override fun run() {
                viewModel.getAllGames()
                handler.postDelayed(this, interval)
            }
        })
    }

    private fun stopRefreshGamesList(){
        handler.removeCallbacksAndMessages(null)
    }
}