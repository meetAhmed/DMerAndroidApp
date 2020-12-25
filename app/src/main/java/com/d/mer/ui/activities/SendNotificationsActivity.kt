package com.d.mer.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.d.mer.R
import com.d.mer.data.models.UserModel
import com.d.mer.data.repositories.volley.VolleyRequestResponse
import com.d.mer.ui.common.Constants
import com.d.mer.ui.common.Dialogs
import com.d.mer.ui.common.Logger
import com.d.mer.ui.viewModels.SendNotificationsActivityViewModel
import org.json.JSONObject

class SendNotificationsActivity : BaseActivity() {

    private val viewModel: SendNotificationsActivityViewModel by lazy {
        ViewModelProvider(this).get(SendNotificationsActivityViewModel::class.java)
    }

    private val listOfTokens = ArrayList<String>()

    private lateinit var messageEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_notifications)

        supportActionBar?.title = getString(R.string.send_notifications)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.getUsers().observe(this, {
            getModels(it, UserModel::class.java)?.let { users ->
                users.forEach { user ->
                    if (!listOfTokens.contains(user.token.trim())) {
                        listOfTokens.add(user.token.trim())
                    }
                }
            }
        })

        messageEditText = findViewById(R.id.messageEditText)
    }

    fun sendBtnClicked(view: View) {
        val messageStr = messageEditText.text.toString().trim()
        if (messageStr.isEmpty()) {
            messageEditText.error = getString(R.string.message_required)
            messageEditText.requestFocus()
        } else {
            if (listOfTokens.isEmpty()) {
                Dialogs.showMessage(this, getString(R.string.no_tokens))
            } else {
                val loadingDialog = Dialogs.loader(
                    this,
                    getString(R.string.sending_notifications_loading_dialog_message)
                )

                viewModel.sendNotifications(
                    getNotificationData(messageStr),
                    listOfTokens,
                    object : VolleyRequestResponse {
                        override fun success() {
                            if(loadingDialog.isShowing){
                                loadingDialog.dismiss()
                            }
                            Dialogs.showToast(
                                this@SendNotificationsActivity,
                                getString(R.string.send_notifications_success)
                            )
                            finish()
                        }// success ends here

                        override fun failure(exception: Exception) {
                            if(loadingDialog.isShowing){
                                loadingDialog.dismiss()
                            }
                            exception.message?.let {
                                Dialogs.showMessage(this@SendNotificationsActivity, it)
                            }
                        }// failure ends here
                    }// VolleyRequestResponse block ends here
                )// viewModel.sendNotifications() ends here

            }// inner else ends here
        }// outer else ends here
    }// fun ends here

    private fun getNotificationData(message: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("title", getString(R.string.notification_title))
            jsonObject.put("message", message)
            jsonObject.put("type", Constants.TO_ALL)
            jsonObject.put("time", System.currentTimeMillis().toString())
        } catch (e: Exception) {
            Logger.info("getNotificationData() - $e")
        }
        return jsonObject
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}