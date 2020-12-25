package com.d.mer.ui.activities

import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.d.mer.R
import com.d.mer.data.firestore.Resource
import com.d.mer.ui.common.Dialogs
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

open class BaseActivity : AppCompatActivity() {

    /**
     * getModels() Method
     */
    fun <T> getModels(results: Resource<QuerySnapshot>, model: Class<T>): ArrayList<T>? {
        when (results) {
            is Resource.Error -> {
                showDataLoadingError(results.exception)
            }
            is Resource.Success -> {
                results.data?.let {
                    return models(it, model)
                }
            }
        }
        return null
    }

    /**
     * getModel() Method
     */
    fun <T> getModel(results: Resource<DocumentSnapshot>, model: Class<T>): T? {
        when (results) {
            is Resource.Error -> {
                showDataLoadingError(results.exception)
            }
            is Resource.Success -> {
                results.data?.let { document ->
                    return document.toObject(model)
                }
            }
        }
        return null
    }

    /**
     * showDataLoadingError() Method
     */
    private fun showDataLoadingError(exception: Exception?) {
        val errorMessage = exception?.message ?: getString(R.string.data_loading_error)
        Dialogs.showToast(applicationContext, errorMessage)
    }

    /**
     * models() Method
     */
    private fun <T> models(data: QuerySnapshot, model: Class<T>): ArrayList<T> {
        val list = ArrayList<T>()
        for (document in data) {
            list.add(document.toObject(model))
        }
        return list
    }

    /**
     * hideKeyboard() Method
     */
    fun hideKeyboard() {
        currentFocus?.let { currentFocus ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    /**
     * dispatchTouchEvent() Method
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.dispatchTouchEvent(ev)
    }
}