package com.d.mer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.d.mer.R
import com.d.mer.ui.common.Constants
import com.d.mer.ui.common.Dialogs
import com.d.mer.ui.common.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        supportActionBar?.title = getString(R.string.register_now)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fullNameEditText = findViewById(R.id.fullName)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)

    }

    fun registerBtnClicked(view: View) {
        val fullNameStr = fullNameEditText.text.toString().trim()
        val emailStr = emailEditText.text.toString().trim()
        val passwordStr = passwordEditText.text.toString().trim()
        var isError = false

        val pattern: Pattern = Pattern.compile("^[a-zA-Z\\s]*$")

        if (passwordStr.isEmpty()) {
            passwordEditText.error = getString(R.string.valid_password_required)
            passwordEditText.requestFocus()
            isError = true
        }

        if (emailStr.isEmpty() ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()
        ) {
            emailEditText.error = getString(R.string.valid_email_address_required)
            emailEditText.requestFocus()
            isError = true
        }

        if (fullNameStr.isEmpty() || !pattern.matcher(fullNameStr).matches()) {
            fullNameEditText.error = getString(R.string.valid_name_required)
            fullNameEditText.requestFocus()
            isError = true
        }

        if (!isError) {
            createUserAccount(fullNameStr, emailStr, passwordStr)
        }

    }

    private fun createUserAccount(fullName: String, email: String, password: String) {
        val loadingDialog =
            Dialogs.loader(this, getString(R.string.register_loading_dialog_message))
        val firebaseAuth = FirebaseAuth.getInstance()
        val fireStore = FirebaseFirestore.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {

            val user = hashMapOf(
                "name" to fullName,
                "email" to email,
                "password" to password
            )

            firebaseAuth.currentUser?.uid?.let { uid ->
                fireStore.collection(Constants.COLLECTION_USERS).document(uid)
                    .set(user)
                    .addOnSuccessListener { Logger.info("DocumentSnapshot - $user - successfully written") }
                    .addOnFailureListener { e -> Logger.info("Error writing document - $user - $e") }
            }

            loadingDialog.dismiss()
            Dialogs.showToast(
                this@CreateAccountActivity,
                getString(R.string.registered_successfully)
            )
            startActivity(Intent(applicationContext, MainActivity::class.java))
            LoginActivity.loginActivityObj?.finish()
            finish()

        }.addOnFailureListener { e ->
            loadingDialog.dismiss()
            Logger.info("Exception: $e")
            e.localizedMessage?.let {
                Dialogs.showMessage(this@CreateAccountActivity, it)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}