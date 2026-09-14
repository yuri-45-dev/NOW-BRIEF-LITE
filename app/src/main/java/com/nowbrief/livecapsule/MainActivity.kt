package com.nowbrief.livecapsule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nowbrief.livecapsule.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) armLiveCapsule()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        renderPreview()
        ensureNotificationPermissionThenArm()
    }

    override fun onResume() {
        super.onResume()
        // Cheap, synchronous — just re-reads the current period, no I/O.
        renderPreview()
    }

    private fun renderPreview() {
        val period = DayPeriod.forCalendar()
        binding.nowPillText.setText(period.labelResId)
        binding.nowPillRoot.setBackgroundResource(period.pillBackgroundRes())
    }

    private fun ensureNotificationPermissionThenArm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) armLiveCapsule() else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            armLiveCapsule()
        }
    }

    /** Posts the current notification once and arms the single next boundary alarm. */
    private fun armLiveCapsule() {
        NowBriefNotifier.updateNow(this)
        PeriodAlarmScheduler.scheduleNextBoundary(this)
    }
}
