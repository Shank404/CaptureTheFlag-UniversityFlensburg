package de.hsfl.team34.capturetheflag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.player_item.view.*


class PlayerAdapter(private val players: MutableList<Player>) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>(){

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false))
    }

    var isHost: Boolean = false
    var isLobbyActive: MutableLiveData<Boolean> = MutableLiveData(true)
    var playerID: MutableLiveData<Int> = MutableLiveData()
    var playerTeamChange: MutableLiveData<Int> = MutableLiveData()
    var playerIndex: MutableLiveData<Int> = MutableLiveData()

    private val playerToKick: MutableLiveData<Int> = MutableLiveData()


    fun addPlayer(player: Player){
        players.add(player)
        notifyItemInserted(players.size + 1)
    }

    fun deleteAllPlayers(){
        players.clear()
        notifyDataSetChanged()
    }

    fun setIsHost(value : Boolean){
        isHost = value
    }

    fun setIsLobbyActive(value : Boolean){
        isLobbyActive.value = value
    }

    fun setPlayerIndex(){
        playerIndex.value = null
    }

    fun setPlayerID(value : Int){
        playerID.value = value
    }
    fun getPlayerID() : LiveData<Int> = playerID

    fun getPlayerToKick() : LiveData<Int> = playerToKick

    fun getPlayerTeamChange() : LiveData<Int> = playerTeamChange

    fun getPlayerIndex() : LiveData<Int> = playerIndex

    fun setPlayerToKickNull(){
        playerToKick.value = null
    }


    override fun onBindViewHolder(holder: PlayerViewHolder, index: Int) {
        val currentPlayer = players[index]
        holder.itemView.apply {
            player_item_textViewPlayerName.text = currentPlayer.name
            player_item_textViewPlayerTeam.text = currentPlayer.team.toString()

            val changeTeamButton : Button = player_item_team
            changeTeamButton.isVisible = false

            if((playerID.value!! + index) == (itemCount) || isHost){
                changeTeamButton.isVisible = isLobbyActive.value!!
                changeTeamButton.setOnClickListener {
                    playerIndex.value = index
                    if(currentPlayer.team == 1){
                        playerTeamChange.value = 2
                    } else {
                        playerTeamChange.value = 1
                    }
                }
            }

            if(index == players.size-1){
                player_kick_btn.isVisible = false
            } else {
                player_kick_btn.isVisible = isHost && isLobbyActive.value!!
                player_kick_btn.setOnClickListener {
                    playerToKick.value = index
                }
            }


            if(currentPlayer.bluetoothAddress == "null"){
                player_item_imageViewBluetoothStatus.setImageResource(android.R.drawable.btn_star_big_off)
            } else {
                player_item_imageViewBluetoothStatus.setImageResource(android.R.drawable.btn_star_big_on)
            }

        }
    }


    override fun getItemCount(): Int {
        return players.size
    }
}