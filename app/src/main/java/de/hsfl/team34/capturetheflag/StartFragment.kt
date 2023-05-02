package de.hsfl.team34.capturetheflag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.team34.capturetheflag.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    val viewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentStartBinding.inflate(inflater, container, false)
        val navController = findNavController()

        val joinGame = binding.startJoinGameBtn
        val hostGame : Button = binding.startHostGameBtn

        resetGameFields()

        joinGame.setOnClickListener {
            navController.navigate(R.id.action_startFragment_to_joinFragment)
        }

        hostGame.setOnClickListener {
            navController.navigate(R.id.action_startFragment_to_createFragment)
        }

        return binding.root
    }

    private fun resetGameFields(){
        viewModel.setNextScreen(false)

        viewModel.deleteItemsInPointsCollection()
        viewModel.setIsGameStarted(false)
        viewModel.setHasGameEnded(false)
        viewModel.setIsHost(false)
        viewModel.stopRefreshPlayerList()
        viewModel.setGameID("")
        viewModel.setNameFromPlayer("")
        viewModel.setToken("")
    }

}