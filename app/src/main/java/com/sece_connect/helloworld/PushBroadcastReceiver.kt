package com.sece_connect.helloworld

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PushBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent){
        Toast.makeText(context, "Push received with app shut down", Toast.LENGTH_LONG).show()
    }
}