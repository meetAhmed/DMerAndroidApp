package com.d.mer.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d.mer.R
import com.d.mer.adapters.SharedImagesAdapter
import com.d.mer.common.Constants
import com.d.mer.common.Dialogs
import com.d.mer.common.Logger
import com.d.mer.dataModels.ImageModel
import com.d.mer.dataModels.SharedImageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SharedImagesActivity : AppCompatActivity() {

    private val fireStore = FirebaseFirestore.getInstance()
    private val listOfSharedImages = ArrayList<SharedImageModel>()
    private lateinit var adapter: SharedImagesAdapter
    private val imagesCountInSingleRow = 3
    private lateinit var recView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_images)

        supportActionBar?.title = getString(R.string.shared_images)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = SharedImagesAdapter(listOfSharedImages, imagesCountInSingleRow)
        getAllImages()

        recView = findViewById(R.id.recView)
        recView.layoutManager = GridLayoutManager(
            this, imagesCountInSingleRow, GridLayoutManager.VERTICAL, false
        )
        recView.adapter = adapter

    }

    fun clearAllClicked(view: View) {
        val loadingDialog = Dialogs.loader(
            this@SharedImagesActivity,
            getString(R.string.clearing_images_dialog_message)
        )

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            fireStore.collection(Constants.COLLECTION_IMAGES).get().addOnSuccessListener { result ->
                for (document in result) {
                    val imageModel = document.toObject(ImageModel::class.java)
                    fireStore.collection(Constants.COLLECTION_IMAGES)
                        .document(imageModel.nodeAddress)
                        .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
                        .get()
                        .addOnSuccessListener { sharedDataResult ->
                            for (sharedDocument in sharedDataResult) {
                                val sharedImageModel =
                                    sharedDocument.toObject(SharedImageModel::class.java)
                                if (sharedImageModel.user.equals(uid, false)) {
                                    fireStore.collection(Constants.COLLECTION_IMAGES)
                                        .document(imageModel.nodeAddress)
                                        .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
                                        .document(uid)
                                        .delete()
                                }
                            }// inner for loop ends here

                            Dialogs.showToast(
                                applicationContext,
                                getString(R.string.deleted_successfully)
                            )
                            listOfSharedImages.clear()
                            adapter.notifyDataSetChanged()
                            loadingDialog.dismiss()

                        }// shared data collection ends here
                        .addOnFailureListener {
                            loadingDialog.dismiss()
                            Dialogs.showToast(
                                applicationContext,
                                getString(R.string.some_thing_went_wrong)
                            )
                        }
                }// outer for loop ends here
            }// collection images end here
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    Dialogs.showToast(
                        applicationContext,
                        getString(R.string.some_thing_went_wrong)
                    )
                }
        }// uid ends here
    }

    private fun getAllImages() {
        listOfSharedImages.clear()
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            fireStore.collection(Constants.COLLECTION_IMAGES).get().addOnSuccessListener { result ->
                for (document in result) {
                    val imageModel = document.toObject(ImageModel::class.java)
                    fireStore.collection(Constants.COLLECTION_IMAGES)
                        .document(imageModel.nodeAddress)
                        .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
                        .get()
                        .addOnSuccessListener { sharedDataResult ->
                            for (sharedDocument in sharedDataResult) {
                                val sharedImageModel =
                                    sharedDocument.toObject(SharedImageModel::class.java)
                                sharedImageModel.imageUrl = imageModel.image_url
                                if (sharedImageModel.user.equals(uid, false)) {
                                    listOfSharedImages.add(sharedImageModel)
                                    adapter.notifyDataSetChanged()
                                }
                            }// inner for loop ends here
                        }// shared data collection ends here
                }// outer for loop ends here
            }// collection images end here
        }// uid ends here
    }// method ends here

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

