package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.alert
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.readableMessage
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.xboard.XBoardSyncManager
import java.text.DateFormat
import java.util.Date

class XBoardSyncActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_xboard_sync)

        supportActionBar?.title = getString(R.string.xboard_sync_title)

        val emailInput = findViewById<EditText>(R.id.xboard_email)
        val passwordInput = findViewById<EditText>(R.id.xboard_password)
        val syncButton = findViewById<Button>(R.id.xboard_sync_button)
        val refreshButton = findViewById<Button>(R.id.xboard_refresh_button)
        val openGroupButton = findViewById<Button>(R.id.xboard_open_group_button)
        val statusView = findViewById<TextView>(R.id.xboard_status)
        val trafficView = findViewById<TextView>(R.id.xboard_traffic)
        val expiryView = findViewById<TextView>(R.id.xboard_expiry)

        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        emailInput.setText(DataStore.xboardEmail)
        renderStatus(statusView, trafficView, expiryView)

        val hasLoginState = DataStore.xboardLastGroupId > 0 && DataStore.xboardEmail.isNotBlank()
        openGroupButton.visibility = if (hasLoginState) View.VISIBLE else View.GONE

        syncButton.setOnClickListener {
            val email = emailInput.text?.toString().orEmpty().trim()
            val password = passwordInput.text?.toString().orEmpty().trim()
            if (email.isBlank() || password.isBlank()) {
                alert(getString(R.string.xboard_sync_login_required)).show()
                return@setOnClickListener
            }
            doLoginAndSync(email, password, syncButton, refreshButton, openGroupButton, statusView, trafficView, expiryView)
        }

        refreshButton.setOnClickListener {
            val email = emailInput.text?.toString().orEmpty().trim()
            val password = passwordInput.text?.toString().orEmpty().trim()
            if (email.isBlank() || password.isBlank()) {
                renderStatus(statusView, trafficView, expiryView)
                return@setOnClickListener
            }
            doLoginAndSync(email, password, syncButton, refreshButton, openGroupButton, statusView, trafficView, expiryView)
        }

        openGroupButton.setOnClickListener {
            DataStore.selectedGroup = DataStore.xboardLastGroupId
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_XBOARD_HOME, true)
            })
            finish()
        }
    }

    private fun doLoginAndSync(
        email: String,
        password: String,
        syncButton: Button,
        refreshButton: Button,
        openGroupButton: Button,
        statusView: TextView,
        trafficView: TextView,
        expiryView: TextView
    ) {
        syncButton.isEnabled = false
        refreshButton.isEnabled = false
        statusView.text = getString(R.string.xboard_sync_running)
        runOnDefaultDispatcher {
            runCatching {
                val result = XBoardSyncManager.loginAndSync(email, password, "星隧互联")
                DataStore.xboardEmail = result.email
                DataStore.xboardPanelName = result.panelName
                DataStore.xboardLastGroupId = result.groupId
                DataStore.selectedGroup = result.groupId
                DataStore.xboardLastSyncAt = System.currentTimeMillis()
                DataStore.xboardTrafficUsed = result.usedTraffic
                DataStore.xboardTrafficTotal = result.totalTraffic
                DataStore.xboardExpireAt = result.expiredAt
                DataStore.xboardPlanName = result.planName
            }.onSuccess {
                onMainDispatcher {
                    syncButton.isEnabled = true
                    refreshButton.isEnabled = true
                    openGroupButton.visibility = View.VISIBLE
                    renderStatus(statusView, trafficView, expiryView)
                    startActivity(Intent(this@XBoardSyncActivity, MainActivity::class.java).apply {
                        putExtra(MainActivity.EXTRA_OPEN_XBOARD_HOME, true)
                    })
                    finish()
                }
            }.onFailure {
                onMainDispatcher {
                    syncButton.isEnabled = true
                    refreshButton.isEnabled = true
                    renderStatus(statusView, trafficView, expiryView)
                    alert(it.readableMessage).show()
                }
            }
        }
    }

    private fun formatBinaryTraffic(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${value.toLong()} ${units[unitIndex]}"
        } else {
            String.format(java.util.Locale.US, "%.2f %s", value, units[unitIndex])
        }
    }

    private fun renderStatus(statusView: TextView, trafficView: TextView, expiryView: TextView) {
        val last = DataStore.xboardLastSyncAt
        statusView.text = if (last > 0) {
            getString(R.string.xboard_sync_last_status, DateFormat.getDateTimeInstance().format(Date(last)))
        } else {
            getString(R.string.xboard_sync_never)
        }

        val used = DataStore.xboardTrafficUsed
        val total = DataStore.xboardTrafficTotal
        if (total > 0) {
            val remaining = (total - used).coerceAtLeast(0)
            trafficView.visibility = View.VISIBLE
            trafficView.text = getString(
                R.string.xboard_sync_traffic_status,
                formatBinaryTraffic(total),
                formatBinaryTraffic(used),
                formatBinaryTraffic(remaining)
            )
        } else {
            trafficView.visibility = View.GONE
        }

        val expireAt = DataStore.xboardExpireAt
        if (expireAt > 0) {
            expiryView.visibility = View.VISIBLE
            expiryView.text = getString(
                R.string.xboard_sync_expiry_status,
                DateFormat.getDateTimeInstance().format(Date(expireAt * 1000))
            )
        } else {
            expiryView.visibility = View.VISIBLE
            expiryView.text = "套餐到期时间：长期有效"
        }
    }
}
