package com.d.mer.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d.mer.R
import com.d.mer.data.firestore.FireStoreReferences
import com.d.mer.data.models.ImageModel
import com.d.mer.data.models.SharedImageModel
import com.d.mer.ui.adapters.SharedImagesAdapter
import com.d.mer.ui.common.Constants
import com.d.mer.ui.common.Dialogs
import com.d.mer.ui.common.Logger
import com.d.mer.ui.viewModels.SharedImagesActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SharedImagesActivity : BaseActivity() {

    private val fireStore = FirebaseFirestore.getInstance()
    private val listOfSharedImages = ArrayList<SharedImageModel>()
    private lateinit var adapter: SharedImagesAdapter
    private val imagesCountInSingleRow = 3
    private lateinit var recView: RecyclerView

    private val viewModel: SharedImagesActivityViewModel by lazy {
        ViewModelProvider(this).get(SharedImagesActivityViewModel::class.java)
    }

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
        FireStoreReferences.getLoggedUserId()?.let { uid ->
            viewModel.getImages().observe(this, { outResults ->
                getModels(outResults, ImageModel::class.java)?.let { imagesModel ->
                    imagesModel.forEach { imageModel ->
                        listOfSharedImages.clear()
                        adapter.notifyDataSetChanged()
                        viewModel.userSharedImagesCollection(imageModel.nodeAddress)
                            .observe(this, { innerResults ->
                                getModels(
                                    innerResults,
                                    SharedImageModel::class.java
                                )?.let { sharedModels ->

                                    val listIterator = listOfSharedImages.iterator()
                                    while (listIterator.hasNext()) {
                                        val listItem: SharedImageModel = listIterator.next()
                                        val modelImageAddress = listItem.imageNodeAddress.trim()
                                        if (imageModel.nodeAddress.trim()
                                                .equals(modelImageAddress, false)
                                        ) {
                                            listIterator.remove()
                                            adapter.notifyDataSetChanged()
                                        }
                                    }

                                    sharedModels.forEach { sharedImageModel ->
                                        sharedImageModel.imageUrl = imageModel.image_url
                                        sharedImageModel.imageNodeAddress = imageModel.nodeAddress
                                        Logger.info("$sharedImageModel")
                                        if (sharedImageModel.user.equals(uid, false)) {
                                            listOfSharedImages.add(sharedImageModel)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                            })
                    }
                }
            })
        }
    }// method ends here

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

