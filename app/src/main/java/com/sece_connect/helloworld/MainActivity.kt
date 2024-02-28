package com.sece_connect.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import org.linphone.core.*
import org.linphone.core.tools.Log



class MainActivity : AppCompatActivity() {
    private lateinit var core: Core

    private val coreListener = object: CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {
            // If account has been configured correctly, we will go through Progress and Ok states
            // Otherwise, we will be Failed.
            findViewById<TextView>(R.id.registration_status).text = message

            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                findViewById<Button>(R.id.connect).isEnabled = true
            } else if (state == RegistrationState.Ok) {
                findViewById<LinearLayout>(R.id.register_layout).visibility =
                    View.GONE
                findViewById<TextView>(R.id.push_info).text = account.params.contactUriParameters
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
    }

    private fun login() {
        val username = findViewById<EditText>(R.id.username).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        val domain = findViewById<EditText>(R.id.domain).text.toString()
        // Get the transport protocol to use.
        // TLS is strongly recommended
        // Only use UDP if you don't have the choice
        val transportType = when (findViewById<RadioGroup>(R.id.transport).checkedRadioButtonId) {
            R.id.udp -> TransportType.Udp
            R.id.tcp -> TransportType.Tcp
            else -> TransportType.Tls
        }

        // To configure a SIP account, we need an Account object and an AuthInfo object
        // The first one is how to connect to the proxy server, the second one stores the credentials

        // The auth info can be created from the Factory as it's only a data class
        // userID is set to null as it's the same as the username in our case
        // ha1 is set to null as we are using the clear text password. Upon first register, the hash will be computed automatically.
        // The realm will be determined automatically from the first register, as well as the algorithm
        val authInfo = Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)

        // Account object replaces deprecated ProxyConfig object
        val params = core.createAccountParams()
        // Account object is configured through an AccountParams object that we can obtain from the Core

        // A SIP account is identified by an identity address that we can construct from the username and domain
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        params.identityAddress = identity

        // We also need to configure where the proxy server is located
        val address = Factory.instance().createAddress("sip:$domain")
        // We use the Address object to easily set the transport protocol
        address?.transport = transportType
        params.serverAddress=address
        // And we ensure the account will start the registration process
//        params.registerEnabled = true

        // Now that our AccountParams is configured, we can create the Account object
        params.pushNotificationAllowed=true

        core.addAuthInfo(authInfo)
        val account = core.createAccount(params)

        // Now let's add our objects to the Core

        core.addAccount(account)

        // Also set the newly added account as default
        core.defaultAccount = account
        core.addListener(coreListener)
        core.start()

        if (!core.isPushNotificationAvailable) {
            Toast.makeText(this, "Something is wrong with the push setup!", Toast.LENGTH_LONG).show()
        }
    }
}