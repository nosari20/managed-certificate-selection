package com.nosari20.managedcertificateselection

import android.app.admin.DelegatedAdminReceiver
import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.security.KeyChain
import android.util.Log


class MyDelegatedAdminReceiver: DelegatedAdminReceiver() {

    private lateinit var context: Context

    companion object {
        private val TAG: String = "MyDelegatedAdminReceiver"
    }

     override fun onChoosePrivateKeyAlias(
        context: Context,
        intent: Intent,
        uid: Int,
        rawUri: Uri?,
        alias: String?
    ): String? {



         // Store context
         this.context = context

         // Get appID of requesting app
         val packageManager: PackageManager = context.packageManager
         val source = packageManager.getNameForUid(uid)
         val uri = Uri.decode(rawUri.toString()).replace("/","")

         // Retrieve managed configuration
         val myRestrictionsMgr = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
         val appRestrictions: Bundle = myRestrictionsMgr.applicationRestrictions

         val autoDeny: Boolean = appRestrictions.getBoolean("auto_deny")
         val mappings: Array<out Parcelable>? = appRestrictions.getParcelableArray("cert_mapping")


         if (source != null) {
             log(source,"receiving request $alias/$uri")
         }else{
             log("unknown","receiving unknown request for $alias")
             return null
         }

        if(source != null){

            if (mappings?.isNotEmpty() == true) {
                mappings.map { it as Bundle }.forEach { bundle ->


                    if(source == bundle.getString("appid").toString()){
                        if(uri == bundle.getString("uri").toString() || bundle.getString("uri").toString() == "*" || bundle.getString("uri").toString() == "") {
                            log(source,"preselected: " +bundle.getString("certalias").toString())
                            return bundle.getString("certalias").toString()
                        }
                    }
                }
            }
        }


        if(autoDeny){
            log(source,"denied")
            return KeyChain.KEY_ALIAS_SELECTION_DENIED
        }else{
            log(source,"default")
            return null

        }

    }


    private fun log(appID: String, action: String) {
        val sharedPref = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)

        sharedPref.getString("logs", "")?.let {
            Event.deserialize(it)
        }?.let {
            it.add(Event(appID, action))
            with(sharedPref.edit()) {
                putString("logs",Event.serialize(it))
                apply()
            }
        }

        Log.d(TAG, "$appID: $action")
    }

}