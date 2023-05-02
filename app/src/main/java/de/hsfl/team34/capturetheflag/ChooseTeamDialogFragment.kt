package de.hsfl.team34.capturetheflag

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class ChooseTeamDialogFragment : DialogFragment() {

    val viewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val navController = findNavController()

        return activity?.let {
            var team: Int = 0
            val builder = AlertDialog.Builder(it)

            builder.setTitle(R.string.chooseTeam)
            builder.setSingleChoiceItems(
                R.array.chooseTeamArray,
                0,
                DialogInterface.OnClickListener { dialogInterface, id ->
                    when (id) {
                        0 -> team = 0
                        1 -> team = 1
                        2 -> team = 2
                    }
                })

            builder.apply {
                setPositiveButton(R.string.join,
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.setTeam(team)
                        viewModel.joinGame()
                    }
                )
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialogInterface, id ->
                        navController.navigate(R.id.action_chooseTeamDialogFragment_to_joinFragment)
                    }
                )
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}