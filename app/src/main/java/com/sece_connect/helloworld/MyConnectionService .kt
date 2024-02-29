package com.sece_connect.helloworld

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle

class MyConnectionService: ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        // Handle incoming call
        // Create a new Connection object and return it
        val connection = MyConnection()
        // Customize the connection properties if needed
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        // Handle outgoing call
        // Create a new Connection object and return it
        val connection = MyConnection()
        // Customize the connection properties if needed
        return connection
    }

    // Override other methods to manage call lifecycle events
    // and handle other functionality as needed
}