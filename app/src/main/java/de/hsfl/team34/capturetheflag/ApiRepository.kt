package de.hsfl.team34.capturetheflag

import android.app.Application
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ApiRepository(app: Application) {
    companion object {
        // Singleton Pattern
        private var instance: ApiRepository? = null
        fun getInstance(app: Application): ApiRepository {
            instance = instance ?: ApiRepository(app)
            return instance!!
        }
    }

    private val requestQueue = Volley.newRequestQueue(app)

    fun postCreateGame(name: String, list: MutableList<JSONObject>?, callback: (JSONObject) -> (Unit), errorCallback: (VolleyError) -> (Unit)) {
        val json = JSONObject(mapOf("name" to name, "points" to list))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/game/register", json,
            {
                callback(it)
            },
            {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postJoinGame(name : String, gameID : String, team : Int, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val json = JSONObject(mapOf("game" to gameID, "name" to name, "team" to team))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/game/join", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postLobbyGame(gameID: String, name: String, token: String, callback: (JSONObject) -> Unit, errorCallback: (VolleyError) -> Unit){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/players", json,
            {
                callback(it)
            },
            {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun getAllGames(callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val json = JSONObject(mapOf("game" to "game", "auth" to "auth"))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/games", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postLeaveGame(gameID: String, name: String, token: String, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "name" to name, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/player/remove", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postGameGetPoints(gameID: String, name: String, token: String, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/points", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postChangePlayer(player : JSONObject, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to player.getString("name"), "token" to player.getString("token")))
        val json = JSONObject(mapOf("game" to player.getString("game"), "name" to player.getString("name"), "addr" to player.getString("addr"),"auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/player/change", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postChangePlayerTeam(player : JSONObject, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to player.getString("name"), "token" to player.getString("token")))
        val json = JSONObject(mapOf("game" to player.getString("game"), "name" to player.getString("name"), "team" to player.getString("team"), "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/player/change", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun postConquer(gameID: String, name: String, team: Int, token: String, point : Int, callback: (JSONObject) -> Unit, errorCallback: (VolleyError) -> Unit){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "point" to point, "team" to team, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/point/conquer", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }
    fun startGame(gameID: String, name: String, token: String, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/game/start", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

    fun endGame(gameID: String, name: String, token: String, callback: (JSONObject) -> (Unit), errorCallback : (VolleyError) -> (Unit)){
        val auth = JSONObject(mapOf("name" to name, "token" to token))
        val json = JSONObject(mapOf("game" to gameID, "auth" to auth))
        val request = JsonObjectRequest(Request.Method.POST, "https://ctf.letorbi.de/game/end", json,
            {
                callback(it)
            }, {
                errorCallback(it)
            }
        )
        requestQueue.add(request)
    }

}