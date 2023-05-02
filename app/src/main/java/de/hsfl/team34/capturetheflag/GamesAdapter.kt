package de.hsfl.team34.capturetheflag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.game_item.view.*

class GamesAdapter(val games: MutableList<Game>, val viewModel : MainViewModel) : RecyclerView.Adapter<GamesAdapter.GamesViewHolder>(){

    class GamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        return GamesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false))
    }

    fun addGame(game: Game){
        games.add(game)
        notifyItemInserted(games.size - 1)
    }

    fun deleteAllGames(){
        games.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GamesViewHolder, index: Int) {
        val currentGame = games[index]
        holder.itemView.apply {
            game_item_game_item_btn.text = currentGame.game.toString()
            game_item_game_item_btn.setOnClickListener() {
                viewModel.setGameID(game_item_game_item_btn.text.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return games.size
    }
}