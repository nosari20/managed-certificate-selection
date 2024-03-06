package com.nosari20.managedcertificateselection

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nosari20.managedcertificateselection.ui.theme.ManagedCertificateSelectionTheme


class MainActivity : ComponentActivity() {

    companion object {
        private val TAG: String = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Events
        val sharedPref = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)

        val events: ArrayList<Event>? = sharedPref.getString("logs", "")?.let {
            Event.deserialize(it)
        }

        // Managed Configuration
        val myRestrictionsMgr = getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        val appRestrictions: Bundle = myRestrictionsMgr.applicationRestrictions

        val autoDeny: Boolean = appRestrictions.getBoolean("auto_deny")
        val mappings: Array<out Parcelable>? = appRestrictions.getParcelableArray("cert_mapping")

        var appConfig = arrayListOf(
            "auto_deny: $autoDeny",
        )

        if (mappings?.isNotEmpty() == true) {
            mappings.map { it as Bundle }.forEach { bundle ->
                appConfig.add("mapping: " + bundle.getString("appid").toString() + "/" + bundle.getString("uri").toString() + " : " + bundle.getString("certalias").toString())
            }
        }


        // Delegated scopes
        val dpm: DevicePolicyManager = this.getSystemService(
            DevicePolicyManager::class.java
        )

        val scopes = dpm.getDelegatedScopes(null, packageName).toList()

        setContent {
            ManagedCertificateSelectionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ManagedCertificateSelectionTheme {

                        if(dpm.activeAdmins != null) {

                            Column(
                                modifier = Modifier
                                    .size(100.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text("Managed Configuration")
                                if (appConfig.isNotEmpty()) {
                                    AppConfig(appConfig)
                                } else {
                                    Text(
                                        fontSize = 10.sp,
                                        lineHeight = 10.sp,
                                        text = "No managed configuration found for mapping",
                                        color = Color.Yellow

                                    )
                                }
                                Text("Delegated scopes")
                                if (scopes.isNotEmpty()) {
                                    DelegatedScope(scopes)
                                    if (!scopes.contains("delegation-cert-selection")) {
                                        Text(
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            text = "Certificate selection delegation missing",
                                            color = Color.Red
                                        )
                                    }
                                } else {
                                    Text(
                                        fontSize = 10.sp,
                                        lineHeight = 10.sp,
                                        text = "No scope delegated (certificate delegation required)",
                                        color = Color.Red

                                    )
                                }
                                Text("Logs")
                                if (events != null) {
                                    Logs(events = events)
                                }
                            }
                        }else{
                            Text(
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                text = "No MDM found, app must be deployed on devices enrolled into an mobile device management solution.",
                                color = Color.Red

                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DelegatedScope(scopes: List<String>) {

     for (scope in scopes){
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            text = scope

        )
    }

}


@Composable
fun AppConfig(configs: List<String>) {

    for (config in configs){
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            text = config

        )
    }

}

@Composable
fun Logs(events: ArrayList<Event>) {

    for (event in events){
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            text = (
                    (event.appID ?: "unknown")
                    + " : " +
                    (event.message ?: "-")
                    )

        )
    }

}


@Preview(showBackground = true, apiLevel = 33)
@Composable
fun ActivityPreview() {

    val sampleEvents = arrayListOf<Event>(
        Event("com.sample.app", "blocked"),
        Event("com.sample.app", "TLS_DUMMYCERT_"),
    )

    val sampleAppConfig = arrayListOf(
        "auto_deny: false",
        "mapping: com.sample.app=TLS_DUMMYCERT_"
    )


    val sampleDelegatedScopes = arrayListOf(
        "certificates"
    )

    ManagedCertificateSelectionTheme {
        Column {
            Text("Managed Configuration")
            AppConfig(sampleAppConfig)
            Text("Delegated scopes")
            DelegatedScope(sampleDelegatedScopes)
            Text("Logs")
            Logs(sampleEvents)
        }
    }
}