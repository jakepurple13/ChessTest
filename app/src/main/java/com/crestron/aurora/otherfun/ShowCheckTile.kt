package com.crestron.aurora.otherfun

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.crestron.aurora.Loged
import com.crestron.aurora.utilities.Utility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowCheckTile : TileService() {

    fun updateCheck() {
        if (Utility.isNetworkToast(this@ShowCheckTile))
            GlobalScope.launch {
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.updateTile()
                val showCheck = Intent(this@ShowCheckTile, ShowCheckIntentService::class.java)
                startService(showCheck)
                delay(1000)
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.updateTile()
            }
    }

    override fun onClick() {
        super.onClick()
        Loged.wtf("Click")
        updateCheck()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        // Do something when the user removes the Tile
        Loged.wtf("Removed")
    }

    override fun onTileAdded() {
        super.onTileAdded()
        // Do something when the user add the Tile
        //qsTile.state = Tile.STATE_INACTIVE
        //qsTile.updateTile() // you need to call this method to apply changes
        Loged.wtf("Added")
    }

    override fun onStartListening() {
        super.onStartListening()
        // Called when the Tile becomes visible
        //val tile = qsTile // this is getQsTile() method form java, used in Kotlin as a property
        //tile.state = Tile.STATE_ACTIVE
        //tile.updateTile() // you need to call this method to apply changes
        Loged.wtf("Start Listening")
    }

    override fun onStopListening() {
        super.onStopListening()
        Loged.wtf("Stop Listening")
        // Called when the tile is no longer visible
    }

}