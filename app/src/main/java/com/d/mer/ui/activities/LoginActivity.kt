package com.d.mer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.d.mer.R
import com.d.mer.ui.common.Dialogs
import com.d.mer.ui.common.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        var loginActivityObj: LoginActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginActivityObj?.finish()
        loginActivityObj = this

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

    }

    fun signInBtnClicked(view: View) {
        val emailStr = emailEditText.text.toString().trim()
        val passwordStr = passwordEditText.text.toString().trim()
        var isError = false

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

        if (!isError) {
            accessUserAccount(emailStr, passwordStr)
        }
    }

    private fun accessUserAccount(email: String, password: String) {
        val loadingDialog = Dialogs.loader(this, getString(R.string.login_loading_dialog_message))

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            loadingDialog.dismiss()
            Dialogs.showToast(this@LoginActivity, getString(R.string.sign_in_successful))
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }.addOnFailureListener { e ->
            loadingDialog.dismiss()
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    passwordEditText.error = getString(R.string.invalid_password)
                    passwordEditText.requestFocus()
                    emailEditText.error = ""
                }
                is FirebaseAuthInvalidUserException -> {
                    emailEditText.error = getString(R.string.no_user_found)
                    emailEditText.requestFocus()
                    passwordEditText.error = ""
                }
                else -> {
                    Logger.info("Exception: $e")
                    e.localizedMessage?.let {
                        Dialogs.showMessage(this@LoginActivity,it)
                    }
                }
            }
        }

    }

    fun openRegisterActivityClicked(view: View) {
        startActivity(Intent(applicationContext, CreateAccountActivity::class.java))
    }

}