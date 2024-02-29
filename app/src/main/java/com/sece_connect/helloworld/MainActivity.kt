package com.sece_connect.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import org.linphone.core.*

class MainActivity : AppCompatActivity()  {
    private lateinit var core: Core

    private val coreListener = object: CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {

            findViewById<TextView>(R.id.registration_status).text = message

            if (state == RegistrationState.Failed ) {
                findViewById<Button>(R.id.connect).isEnabled = true
            } else if (state == RegistrationState.Ok) {
                findViewById<LinearLayout>(R.id.register_layout).visibility =
                    View.GONE
                findViewById<RelativeLayout>(R.id.call_layout).visibility = View.VISIBLE
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ){
            findViewById<TextView>(R.id.call_status).text = message


            when (state) {
                Call.State.OutgoingInit -> {

                }
                Call.State.OutgoingProgress -> {

                }
                Call.State.OutgoingRinging -> {

                }
                Call.State.Connected -> {

                }
                Call.State.StreamsRunning -> {

                    findViewById<Button>(R.id.pause).isEnabled = true
                    findViewById<Button>(R.id.pause).text = "Pause"

                   //                    findViewById<Button>(R.id.toggle_camera).isEnabled = core.videoDevicesList.size > 2 && call.currentParams.videoEnabled()
                }
                Call.State.Paused -> {
                    // When you put a call in pause, it will became Paused
                    findViewById<Button>(R.id.pause).text = "Resume"

                }
                Call.State.PausedByRemote -> {

                }
                Call.State.Updating -> {

                }
                Call.State.UpdatedByRemote -> {

                }
                Call.State.Released -> {

                    findViewById<EditText>(R.id.remote_address).isEnabled = true
                    findViewById<Button>(R.id.call).isEnabled = true
                    findViewById<Button>(R.id.pause).isEnabled = false
                    findViewById<Button>(R.id.pause).text = "Pause"
                    findViewById<Button>(R.id.hang_up).isEnabled = false

                }
                Call.State.IncomingReceived -> {
                    findViewById<Button>(R.id.hang_up).isEnabled = true
//                    onShowIncomingCallUi()
                    findViewById<EditText>(R.id.remote_address).setText(call.remoteAddress.asStringUriOnly())
                }
                Call.State.Error -> {

                }

                else -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val factory = Factory.instance()
        factory.setDebugMode(true,"hello Linephone")
        core=factory.createCore(null,null,this)

        findViewById<Button>(R.id.connect).setOnClickListener {
            login()
            it.isEnabled = false
        }


        findViewById<Button>(R.id.call).setOnClickListener {
            outgoingCall()
            findViewById<EditText>(R.id.remote_address).isEnabled = false
            it.isEnabled = false
            findViewById<Button>(R.id.hang_up).isEnabled = true
        }

        findViewById<Button>(R.id.hang_up).setOnClickListener {
            hangUp()
        }

        findViewById<Button>(R.id.pause).setOnClickListener {
            pauseOrResume()
        }

        findViewById<Button>(R.id.pause).isEnabled = false
        findViewById<Button>(R.id.hang_up).isEnabled = false
    }

    private fun login() {
        val username = findViewById<EditText>(R.id.username).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        val domain = findViewById<EditText>(R.id.domain).text.toString()

        val transportType = when (findViewById<RadioGroup>(R.id.transport).checkedRadioButtonId) {
            R.id.udp -> TransportType.Udp
            R.id.tcp -> TransportType.Tcp
            else -> TransportType.Tls
        }

        val authInfo = Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)

        val params = core.createAccountParams()
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        params.identityAddress = identity
        val address = Factory.instance().createAddress("sip:$domain")

        address?.transport = transportType
        params.serverAddress=address

//        params.registerEnabled = true


        val account = core.createAccount(params)
        core.addAuthInfo(authInfo)
        core.addAccount(account)


        core.defaultAccount = account
        core.addListener(coreListener)
        core.start()

        if (packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }
    }

    private fun outgoingCall() {

        val remoteSipUri = findViewById<EditText>(R.id.remote_address).text.toString()
        val remoteAddress = Factory.instance().createAddress(remoteSipUri)
        remoteAddress ?: return


        val params = core.createCallParams(null)
        params ?: return

        params.mediaEncryption = MediaEncryption.None

        core.inviteAddressWithParams(remoteAddress, params)

    }

    private fun hangUp() {
        if (core.callsNb == 0) return
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        call.terminate()
    }

    private fun pauseOrResume() {
        if (core.callsNb == 0) return
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        if (call.state != Call.State.Paused && call.state != Call.State.Pausing) {
            call.pause()
        } else if (call.state != Call.State.Resuming) {
            call.resume()
        }
    }
}