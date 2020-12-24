package com.d.mer.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.d.mer.R
import com.d.mer.interfaces.AlertDialogInterface

object Dialogs {

    fun showMessage(
        activity: Activity,
        message: String,
        positiveButtonText: String,
        listener: AlertDialogInterface
    ) {

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialogInterface, i ->
            listener.positiveButtonClick()
        }

        val alert = builder.create()
        alert.show()
    }

    fun showMessage(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        listener: AlertDialogInterface
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialogInterface, i ->
            listener.positiveButtonClick()
        }
        builder.setNegativeButton(negativeButtonText) { dialogInterface, i ->
            listener.negativeButtonClick()
        }

        val alert = builder.create()
        alert.show()
    }

    fun showMessage(
        activity: Activity,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        listener: AlertDialogInterface
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialogInterface, i ->
            listener.positiveButtonClick()
        }
        builder.setNegativeButton(negativeButtonText) { dialogInterface, i ->
            listener.negativeButtonClick()
        }

        val alert = builder.create()
        alert.show()
    }

    fun showMessage(
        context: Context,
        message: String
    ) {

        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            dialogInterface.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showToast(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
    }

    fun showMessage(
        activity: Activity,
        message: String
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            dialogInterface.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    fun loader(activity: Activity, message: String): AlertDialog {
        val layoutInflater = LayoutInflater.from(activity)
        val customView = layoutInflater.inflate(R.layout.loading_dialog_layout, null)
        val myBox: AlertDialog.Builder = AlertDialog.Builder(activity)
        myBox.setView(customView)
        val messageTextView: TextView = customView.findViewById(R.id.message)
        if (message.trim().isNotEmpty()) {
            messageTextView.text = message
        }
        val customProgressDialog = myBox.create()
        customProgressDialog.setCancelable(false)
        customProgressDialog.show()
        return customProgressDialog
    }

    fun loader(context: Context, message: String): AlertDialog {
        val layoutInflater = LayoutInflater.from(context)
        val customView = layoutInflater.inflate(R.layout.loading_dialog_layout, null)
        val myBox: AlertDialog.Builder = AlertDialog.Builder(context)
        myBox.setView(customView)
        val messageTextView: TextView = customView.findViewById(R.id.message)
        if (message.trim().isNotEmpty()) {
            messageTextView.text = message
        }
        val customProgressDialog = myBox.create()
        customProgressDialog.setCancelable(false)
        customProgressDialog.show()
        return customProgressDialog
    }

}