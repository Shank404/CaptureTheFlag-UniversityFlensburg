package de.hsfl.team34.capturetheflag

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.hsfl.team34.capturetheflag.databinding.FragmentCreateBinding


class CreateFragment : Fragment() {

    val viewModel : MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity as MainActivity).startLocation()

        val binding = FragmentCreateBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val input : EditText = binding.editTextTextPersonName
        val createGame : Button = binding.buttonCreateGameCreate
        val cancel : Button = binding.buttonCancelCreate
        val setPosition : Button = binding.buttonSetFlagAtPositionCreate
        val map = binding.mapView1
        var isClicked = false
        var nextScreenLocal = false

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        map.drawPlayerPointer = true

        val mDetector : GestureDetector = GestureDetector(context, map.createGestureListener())
        map.setOnTouchListener() { _, event -> mDetector.onTouchEvent(event)}

        createGame.setOnClickListener {
            isClicked = true
            if(!viewModel.isFieldEmpty("name") && viewModel.getNameFromPlayer().value!! != ""){
                viewModel.registerGame()
            } else {
                input.error = "Bitte geben Sie einen Namen ein!"
            }
        }

        viewModel.getNextScreen().observe(viewLifecycleOwner) {
            if(viewModel.getNextScreen().value!!){
                viewModel.setNextScreen(false)
                nextScreenLocal = true
                (activity as MainActivity)?.startServer()
                navController.navigate(R.id.action_createFragment_to_lobbyFragment)
            } else {
                if(isClicked && !nextScreenLocal){
                    isClicked = false
                    nextScreenLocal = false
                    viewModel.createErrorToast()
                }
            }
        }

        viewModel.getLocationPosition().observe(viewLifecycleOwner) {
            map.locationPlayer = it
        }

        viewModel.getPointsCollection().observe(viewLifecycleOwner) {
            map.pointsCollection = it
        }

        map.gesturePoint.observe(viewLifecycleOwner){
            viewModel.setGesturePoint(it)
            viewModel.addGesturePoint()
        }

        map.mapViewSize.observe(viewLifecycleOwner){
            viewModel.setMapViewSize(it)
        }

        cancel.setOnClickListener {
            viewModel.stopServer()
            navController.navigate(R.id.action_createFragment_to_startFragment)
        }

        setPosition.setOnClickListener {
            viewModel.setPointsCollection()
        }

        return binding.root
    }
}