package com.udacity.project4.locationreminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemindersBinding

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.POST_NOTIFICATIONS] == false)
            Toast.makeText(this, "Notifications is denied, You can enable it from the setting", Toast.LENGTH_SHORT).show()

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//          setContentView(R.layout.activity_reminders)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)

        if (Build.VERSION.SDK_INT >= 33) {
            if (!isPostNotificationPermissionGranted()) {
                requestNotificationPermission.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
//                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(33)
    private fun isPostNotificationPermissionGranted():Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
