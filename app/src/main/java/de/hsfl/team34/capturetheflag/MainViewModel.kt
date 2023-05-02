package de.hsfl.team34.capturetheflag

import android.app.Application
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.lokibt.bluetooth.BluetoothDevice
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val apiRepository = ApiRepository.getInstance(app)
    private val bluetoothRepository = BluetoothRepository.getInstance().apply {
        discoveryCallback = {
            devices.value = (devices.value ?: listOf()) + listOf(it)
        }
        serverCallback = {
            isServerActive.postValue(it)
        }
    }

    private val tlLatitude = 54.778514
    private val tlLongitude = 9.442749
    private val brLatitude = 54.769009
    private val brLongitude = 9.464722

    private val appContext: Application = app
    val handler : Handler = Handler()

    private val name: MutableLiveData<String> = MutableLiveData()
    private val gameID: MutableLiveData<String> = MutableLiveData()
    private val token: MutableLiveData<String> = MutableLiveData()
    private val bluetoothAddress: MutableLiveData<String> = MutableLiveData()
    private val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val team: MutableLiveData<Int> = MutableLiveData()
    private val playerID: MutableLiveData<Int> = MutableLiveData()
    private val location: MutableLiveData<JSONObject> = MutableLiveData()
    private val locationPoint: MutableLiveData<JSONObject> = MutableLiveData()
    private val gesturePoint: MutableLiveData<JSONObject> = MutableLiveData()
    private val pointsCollection: MutableLiveData<MutableList<JSONObject>> = MutableLiveData(mutableListOf())
    private val pointsCollectionFromAPI: MutableLiveData<MutableList<JSONObject>> = MutableLiveData(mutableListOf())
    private var mapViewSize : JSONObject = JSONObject()
    private val nextScreen: MutableLiveData<Boolean> = MutableLiveData()
    private val isServerActive: MutableLiveData<Boolean> = MutableLiveData()
    private val lastMessage: MutableLiveData<String> = MutableLiveData()
    private val lastError: MutableLiveData<Error> = MutableLiveData()
    private val devices: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    private val playerList: MutableList<JSONObject> = mutableListOf()
    private val isHost: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isKicked: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isGameStarted: MutableLiveData<Boolean> = MutableLiveData()
    private val hasGameEnded: MutableLiveData<Boolean> = MutableLiveData()
    private val pointsTeam1: MutableLiveData<Int> = MutableLiveData()
    private val pointsTeam2: MutableLiveData<Int> = MutableLiveData()
    private val winnerTeam: MutableLiveData<String> = MutableLiveData()

    lateinit var playerAdapter: PlayerAdapter
    lateinit var gamesAdapter: GamesAdapter



    fun getNameFromPlayer(): MutableLiveData<String> = name
    fun getGameID(): MutableLiveData<String> = gameID
    fun getToken(): LiveData<String> = token
    fun getBluetoothAddress(): LiveData<String> = bluetoothAddress
    fun getErrorMessage(): LiveData<String> = errorMessage
    fun getTeam(): LiveData<Int> = team
    fun getPlayerID(): LiveData<Int> = playerID
    fun getNextScreen(): LiveData<Boolean> = nextScreen
    fun getLocationPosition(): LiveData<JSONObject> = location
    fun getPointsCollection(): MutableLiveData<MutableList<JSONObject>> = pointsCollection
    fun getPointsCollectionServer(): MutableLiveData<MutableList<JSONObject>> = pointsCollectionFromAPI
    fun getIsServerActive(): LiveData<Boolean> = isServerActive
    fun getLastMessage(): LiveData<String> = lastMessage
    fun getLastError(): LiveData<Error> = lastError
    fun getDevices(): LiveData<List<BluetoothDevice>> = devices
    fun getIsHost(): LiveData<Boolean> = isHost
    fun getIsKicked(): LiveData<Boolean> = isKicked
    fun getIsGameStarted(): LiveData<Boolean> = isGameStarted
    fun getHasGameEnded(): LiveData<Boolean> = hasGameEnded
    fun getPointsTeam1(): LiveData<Int> = pointsTeam1
    fun getPointsTeam2(): LiveData<Int> = pointsTeam2
    fun getWinnerTeam(): LiveData<String> = winnerTeam

    fun searchForDevices() {
        devices.value = listOf()
        bluetoothRepository.searchDevices(getApplication())
    }

    fun sendMessage(device: BluetoothDevice) {
        bluetoothRepository.sendMessage(
            device,
            gameID.value!!,
            name.value!!,
            token.value!!,
            getApplication(),
            { lastMessage.postValue(it) },
            { lastError.postValue(it) })
    }

    fun startService() {
        bluetoothRepository.startServer(
            gameID.value!!,
            {
                if (!playerList.contains(it)) {
                    if (playerList.size == 0) {
                        addServerToList(it)
                    }
                    playerList.add(it)
                    val croppedJSON = JSONObject(
                        mapOf(
                            "game" to it.getString("game"),
                            "name" to it.getString("name"),
                            "addr" to it.getString("addr"),
                            "token" to it.getString("token")
                        )
                    )
                    changePlayerInAPI(croppedJSON)
                }
            },
            { lastError.postValue(it) })
    }

    private fun addServerToList(obj: JSONObject) {
        val json = JSONObject(
            mapOf(
                "game" to gameID.value,
                "name" to name.value,
                "addr" to obj.getString("serverAddr"),
                "token" to token.value
            )
        )
        playerList.add(json)
        changePlayerInAPI(json)
    }



    fun stopServer() {
        bluetoothRepository.stopServer()
    }

    fun setToken(newToken: String) {
        token.value = newToken
    }

    fun setNameFromPlayer(newName: String) {
        name.value = newName
    }

    fun setGameID(newGameID: String) {
        gameID.value = newGameID
    }

    private fun setBluetoothAddress(newBluetoothAddress: String) {
        bluetoothAddress.value = newBluetoothAddress
    }

    private fun setErrorMessage(err: VolleyError) {
        errorMessage.value = err.toString()
    }

    private fun setErrorMessageEmpty() {
        errorMessage.value = ""
    }

    fun setTeam(value: Int) {
        team.value = value
    }

    fun setPlayerID(value: Int) {
        playerID.value = value
    }

    fun setLocation(latitude: Double, longitude: Double) {
        val long = (longitude - tlLongitude) / (brLongitude - tlLongitude)
        val lat = (latitude - tlLatitude) / (brLatitude - tlLatitude)
        val json = JSONObject(mapOf("lat" to lat, "long" to long))
        location.value = json
    }

    private fun setLocationPoint() {
        val json = JSONObject(
            mapOf(
                "lat" to location.value?.getDouble("lat"),
                "long" to location.value?.getDouble("long")
            )
        )
        locationPoint.value = json
    }

    fun setGesturePoint(value: JSONObject) {
        gesturePoint.value = value
    }

    fun setPointsCollection() {
        setLocationPoint()
        val json = JSONObject(
            mapOf(
                "lat" to location.value!!.getDouble("lat"),
                "long" to location.value!!.getDouble("long"),
                "calculate" to true
            )
        )
        addOrRemovePoint(json)
    }

    fun addGesturePoint() {
        val json = JSONObject(
            mapOf(
                "lat" to gesturePoint.value!!.getDouble("lat"),
                "long" to gesturePoint.value!!.getDouble("long"),
                "calculate" to false
            )
        )
        addOrRemovePoint(json)
    }

    private fun addOrRemovePoint(point : JSONObject){
        var pointPadding = 30.0
        if(pointsCollection.value!!.size > 0){
            var exist = false
            var index = -1
            for(i in 0 until pointsCollection.value!!.size){
                val currentLatPoint = pointsCollection.value!![i].getDouble("lat")
                val currentLongPoint = pointsCollection.value!![i].getDouble("long")
                if (currentLatPoint <= point.getDouble("lat") + pointPadding && currentLatPoint >= point.getDouble("lat") - pointPadding
                    && currentLongPoint <= point.getDouble("long") + pointPadding && currentLongPoint >= point.getDouble("long") - pointPadding){
                    exist = true
                    index = i
                    break
                }
            }
            if(exist){
                pointsCollection.value!!.removeAt(index)
            } else {
                pointsCollection.value!!.add(point)
            }
        } else {
            pointsCollection.value!!.add(point)
        }
    }

    fun setMapViewSize(value : JSONObject){
        mapViewSize = value
    }

    fun setNextScreen(bool: Boolean) {
        nextScreen.value = bool
    }

    fun createErrorToast() {
        val error = errorMessage.value.toString()
        Toast.makeText(appContext, error, Toast.LENGTH_LONG).show()
    }

    private fun fillPlayerCollection(collection: JSONArray) {
        playerAdapter.deleteAllPlayers()
        for (i in 0 until collection.length()) {
            val name = collection.getJSONObject(i).getString("name")
            val team = collection.getJSONObject(i).getString("team")
            val bluetoothAddress = collection.getJSONObject(i).getString("addr")
            val newPlayer = Player(0, name, team.toInt(), bluetoothAddress)
            playerAdapter.addPlayer(newPlayer)
        }
    }

    private fun setPointsCollectionServer(collection: JSONArray) {
        pointsCollectionFromAPI.value?.clear()
        setPointsTeam1(0)
        setPointsTeam2(0)
        for (i in 0 until collection.length()) { //-1
            val id = collection.getJSONObject(i).getString("id")
            val team = collection.getJSONObject(i).getString("team")
            val lat = collection.getJSONObject(i).getDouble("lat")
            val long = collection.getJSONObject(i).getDouble("long")
            val json = JSONObject(mapOf("id" to id, "team" to team, "lat" to lat, "long" to long))
            if(team == "1"){
                setPointsTeam1(getPointsTeam1().value!! + 1)
            } else if (team == "2"){
                setPointsTeam2(getPointsTeam2().value!! + 1)
            }
            if(getPointsTeam1().value == collection.length()){
                setWinnerTeam("Red")
                endGame()
            }
            if(getPointsTeam2().value == collection.length()){
                setWinnerTeam("Blue")
                endGame()
            }

            pointsCollectionFromAPI.value?.add(json)
        }
    }

    fun getGamePoints() {
        apiRepository.postGameGetPoints(gameID.value!!, name.value!!, token.value!!,
            {
                setPointsCollectionServer(it.getJSONArray("points"))
            },
            {
                setErrorMessage(it)
            }
        )
    }

    private fun fillGamesCollection(collection: JSONArray) {
        gamesAdapter.deleteAllGames()
        for (i in 0 until collection.length()) {
            val id = collection.getJSONObject(i).getInt("id")
            val game = Game(id)
            gamesAdapter.addGame(game)
        }
    }

    fun deleteItemsInPointsCollection() {
        pointsCollection.value?.clear()
    }

    fun isFieldEmpty(field: String): Boolean {
        var empty: Boolean = true
        when (field) {
            "name" -> {
                name.value?.let {
                    empty = false
                }
            }
            "gameID" -> {
                gameID.value?.let {
                    empty = false
                }
            }
            "team" -> {
                team.value?.let {
                    empty = false
                }
            }
        }
        return empty
    }

    fun registerGame() {
        name.value?.let {
            apiRepository.postCreateGame(it, pointsCollection.value,
                {
                    setToken(it.getString("token"))
                    setGameID(it.getString("game"))
                    setNameFromPlayer(it.getString("name"))
                    setTeam(1)
                    setIsHost(true)
                    setNextScreen(true)
                },
                {
                    setErrorMessage(it)
                    setNextScreen(false)
                }
            )
        }
    }

    fun joinGame() {
        setBluetoothAddress("")
        if (name.value != null && gameID.value != null && team.value != null)
            apiRepository.postJoinGame(name.value!!, gameID.value!!, team.value!!,
                {
                    setToken(it.getString("token"))
                    setGameID(it.getString("game"))
                    setNameFromPlayer(it.getString("name"))
                    setTeam(it.getInt("team"))
                    setNextScreen(true)
                    setErrorMessageEmpty()
                },
                {
                    setNextScreen(false)
                    setErrorMessage(it)
                }
            )
    }

    fun leaveGame() {
        apiRepository.postLeaveGame(gameID.value!!, name.value!!, token.value!!,
            {

            },
            {
                setErrorMessage(it)
            }
        )
    }
    private fun kickPlayer(collection: JSONArray, index : Int) {
        var player = collection.getJSONObject(index)

        apiRepository.postLeaveGame(gameID.value!!, player.getString("name"), player.getString("token"),
            {

            },
            {
                setErrorMessage(it)
            }
        )
    }


    fun autoWeightFunction() {
        if (name.value != null && gameID.value != null && team.value != null) {
            apiRepository.postLobbyGame(gameID.value!!, name.value!!, token.value!!,
                {
                    var players = it.getJSONArray("players")
                    var team1 = 0
                    var team2 = 0

                    for(i in 0 until players.length()){
                        if(players.getJSONObject(i).getString("team") == "1"){
                            team1++;
                        } else {
                            team2++;
                        }
                    }
                    if(team1 > team2+1 || team2 > team1+1){
                        if(team1 > team2+1){
                            var difference = (team1 - team2) / 2
                                for(i in 0 until players.length()){
                                    if(difference > 0){
                                        if(players.getJSONObject(i).getString("team") == "1"){
                                            val json = JSONObject(
                                                mapOf(
                                                    "game" to gameID.value,
                                                    "name" to players.getJSONObject(i).getString("name"),
                                                    "team" to "2",
                                                    "token" to players.getJSONObject(i).getString("token")
                                                )
                                            )
                                            changePlayerTeamInAPI(json)
                                            difference--
                                        }
                                    }
                                }
                        } else {
                            var difference = (team2 - team1) / 2
                                for(i in 0 until players.length()){
                                    if(difference > 0){
                                        if(players.getJSONObject(i).getString("team") == "2"){
                                            val json = JSONObject(
                                                mapOf(
                                                    "game" to gameID.value,
                                                    "name" to players.getJSONObject(i).getString("name"),
                                                    "team" to "1",
                                                    "token" to players.getJSONObject(i).getString("token")
                                                )
                                            )
                                            changePlayerTeamInAPI(json)
                                            difference--
                                        }
                                    }
                            }
                        }
                    }
                },
                {
                    setErrorMessage(it)
                })
        }
    }




    fun gameLobby() {
        if (name.value != null && gameID.value != null && team.value != null) {
            apiRepository.postLobbyGame(gameID.value!!, name.value!!, token.value!!,
                {

                    fillPlayerCollection(it.getJSONArray("players"))


                    playerAdapter.setIsHost(isHost.value!!)

                    if(playerAdapter.getPlayerID().value == null){
                        playerAdapter.setPlayerID(playerAdapter.itemCount)
                    }

                    if(playerAdapter.getPlayerToKick().value != null){
                        kickPlayer(it.getJSONArray("players"), playerAdapter.getPlayerToKick().value!!)
                        playerAdapter.setPlayerToKickNull()
                    }

                    if(playerAdapter.getPlayerIndex().value != null){
                        val player = it.getJSONArray("players")
                        var playerToken : String = ""
                        if(isHost.value!!){
                            playerToken = player.getJSONObject(playerAdapter.getPlayerIndex().value!!).getString("token")
                        } else {
                            playerToken = token.value!!
                        }

                        val json = JSONObject(
                            mapOf(
                                "game" to gameID.value,
                                "name" to player.getJSONObject(playerAdapter.getPlayerIndex().value!!).getString("name"),
                                "team" to playerAdapter.getPlayerTeamChange().value,
                                "token" to playerToken
                            )
                        )
                        changePlayerTeamInAPI(json)
                        playerAdapter.setPlayerIndex()
                    }

                    if(it.getString("state") == "0"){
                        isGameStarted.value = false
                        hasGameEnded.value = false
                    } else if(it.getString("state") == "1"){
                        playerAdapter.setIsLobbyActive(false)
                        isGameStarted.value = true
                    } else if(it.getString("state") == "2"){
                        playerAdapter.setIsLobbyActive(true)
                        hasGameEnded.value = true
                    }
                    setNextScreen(true)
                },
                {
                    setNextScreen(false)
                    setIsKicked(true)
                    setErrorMessage(it)
                }
            )
        }
    }

    private fun changePlayerInAPI(player: JSONObject) {
        apiRepository.postChangePlayer(player, {}, { setErrorMessage(it) })
    }

    private fun changePlayerTeamInAPI(player: JSONObject) {
        apiRepository.postChangePlayerTeam(player, {}, { setErrorMessage(it) })
    }

    fun getAllGames() {
        apiRepository.getAllGames(
            {
                fillGamesCollection(it.getJSONArray("games"))
            },
            {
                setErrorMessage(it)
            })
    }

    fun conquerPoint(point: Int, team : Int, callback: (JSONObject) -> Unit, errorCallback: (VolleyError) -> Unit) {
        apiRepository.postConquer(gameID.value!!, name.value!!, team, token.value!!, point,
            {
                callback(it)
            },
            {
                errorCallback(it)
            })
    }

    fun setIsHost(value: Boolean){
        isHost.value = value
    }
    fun setIsKicked(value: Boolean){
        isKicked.value = value
    }
    fun setIsGameStarted(value: Boolean) {
        isGameStarted.value = value
    }
    fun setHasGameEnded(value: Boolean) {
        hasGameEnded.value = value
    }

    fun setPointsTeam1(value: Int){
        pointsTeam1.value = value
    }

    fun setPointsTeam2(value: Int){
        pointsTeam2.value = value
    }

    fun setWinnerTeam(value: String){
        winnerTeam.value = value
    }

    fun startGame() {
        apiRepository.startGame(gameID.value!!, name.value!!, token.value!!,
            {

            },
            {
                setErrorMessage(it)
            }
        )
    }

    fun endGame(){
        apiRepository.endGame(gameID.value!!, name.value!!, token.value!!,
            {
                playerAdapter.setIsLobbyActive(true)
            },
            {
                setErrorMessage(it)
            }
        )
    }

    fun setTeamAtPoint(id : Int, team : Int){
        var counter = 0
        for(i in pointsCollectionFromAPI.value!!){
            if(i.getInt("id") == id){
                val json = JSONObject(mapOf("id" to i.getInt("id"), "team" to team, "lat" to i.getDouble("lat"), "long" to i.getDouble("long")))
                pointsCollectionFromAPI.value!![counter] = json
            }
            counter++
        }
    }

    fun refreshPlayerList(interval : Long) {
        handler.post(object : Runnable {
            override fun run() {
                gameLobby()
                handler.postDelayed(this, interval)
            }
        })
    }

    fun stopRefreshPlayerList(){
        handler.removeCallbacksAndMessages(null)
    }
}