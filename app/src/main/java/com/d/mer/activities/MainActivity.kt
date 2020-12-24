package com.d.mer.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.beloo.widget.chipslayoutmanager.layouter.breaker.IRowBreaker
import com.d.mer.R
import com.d.mer.adapters.CategoriesAdapter
import com.d.mer.adapters.ImagesAdapter
import com.d.mer.common.*
import com.d.mer.dataModels.CategoryModel
import com.d.mer.dataModels.ImageModel
import com.d.mer.dataModels.SharedImageModel
import com.d.mer.dataModels.UserModel
import com.d.mer.interfaces.AlertDialogInterface
import com.d.mer.interfaces.CategoryClickListener
import com.d.mer.interfaces.ImagesClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private val fireStore = FirebaseFirestore.getInstance()

    private val listOfCategoriesForFilter = ArrayList<CategoryModel>()
    private val listOfCategories = ArrayList<CategoryModel>()
    private val listOfImages = ArrayList<ImageModel>()

    // filter view
    private lateinit var filterView: LinearLayout

    // images
    private lateinit var recViewForImages: RecyclerView
    private lateinit var imagesAdapter: ImagesAdapter

    // categories
    private lateinit var recViewForCategories: RecyclerView
    private val categoriesCountInSingleRow = 3
    private val categoriesAdapter by lazy {
        CategoriesAdapter(listOfCategories, listOfCategoriesForFilter)
    }

    private val permissionCode = 23

    private var imageModelForSharing: ImageModel? = null

    private var timer: Timer? = null
    private val oneSecond = 1000L

    private lateinit var searchBoxEdt: EditText
    private lateinit var cancel: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagesAdapter = ImagesAdapter(listOfImages,
            object : ImagesClickListener {
                override fun shareImage(imageModel: ImageModel) {
                    imageModelForSharing = imageModel

                    if (isAllPermissionsAllowed()) {
                        handleImageShareRequest()
                    } else {
                        requestPermissions()
                    }

                }

                override fun endTimer(imageModel: ImageModel, position: Int) {
                    endTimerClicked(imageModel, position)
                }
            })

        getUserData()

        cancel = findViewById(R.id.cancel)
        searchBoxEdt = findViewById(R.id.searchBoxEdt)
        filterView = findViewById(R.id.filterView)

        cancel.setOnClickListener {
            searchBoxEdt.setText("")
            searchBoxEdt.clearFocus()
            categoriesAdapter.searchFilter("")
            hideKeyboard()
        }

        searchBoxEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                categoriesAdapter.searchFilter(s)
            }
        })

        // images
        recViewForImages = findViewById(R.id.recViewForImages)
        // GridLayoutManager(  this, imagesCountInSingleRow, GridLayoutManager.VERTICAL, false   )
        recViewForImages.layoutManager = LinearLayoutManager(applicationContext)
        recViewForImages.adapter = imagesAdapter

        // categories
        recViewForCategories = findViewById(R.id.recViewForCategories)

        val chipsLayoutManager: ChipsLayoutManager =
            ChipsLayoutManager.newBuilder(applicationContext)
                .setChildGravity(Gravity.CENTER)
                .setScrollingEnabled(true)
                .setMaxViewsInRow(4)
                .setGravityResolver { Gravity.CENTER }
                .setRowBreaker(IRowBreaker { position ->
                    if (position >= 0 && position < listOfCategories.size) {
                        if (listOfCategories[position].name.trim().length >= 10) {
                            return@IRowBreaker true
                        }
                    }
                    false
                })
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_FILL_VIEW)
                .withLastRow(true)
                .build()

        /* val staggeredGridLayoutManager = StaggeredGridLayoutManager(
             categoriesCountInSingleRow,
             StaggeredGridLayoutManager.VERTICAL
         )
         staggeredGridLayoutManager.gapStrategy =
             StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS */

        recViewForCategories.layoutManager = chipsLayoutManager

        recViewForCategories.adapter = categoriesAdapter

        categoriesAdapter.attachListener(object : CategoryClickListener {
            override fun click(categoryModel: CategoryModel) {
                if (CategoryModel.isPresent(listOfCategoriesForFilter, categoryModel.id)) {
                    listOfCategoriesForFilter.remove(categoryModel)
                } else {
                    listOfCategoriesForFilter.add(categoryModel)
                }
                categoriesAdapter.notifyDataSetChanged()
            }
        })

        // load data
        getAllCategories()
        getAllImages()

    }

    private fun handleImageShareRequest() {
        imageModelForSharing?.let { imageModel ->

            val imageUriIfPresent =
                ImageUtils.getImageUriIfPresent(this@MainActivity, imageModel.nodeAddress)

            if (imageUriIfPresent == null) {
                val loadingDialog = Dialogs.loader(
                    this@MainActivity,
                    getString(R.string.downloading_image_dialog_message)
                )

                Picasso.get().load(imageModel.image_url)
                    .into(object : Target {
                        override fun onBitmapLoaded(
                            bitmap: Bitmap?,
                            from: Picasso.LoadedFrom?
                        ) {
                            if (loadingDialog.isShowing) {
                                loadingDialog.dismiss()
                            }

                            if (bitmap == null) {
                                Dialogs.showToast(
                                    applicationContext,
                                    getString(R.string.failed_to_download_image)
                                )
                            } else {

                                val imageUri =
                                    ImageUtils.saveImage(
                                        bitmap,
                                        this@MainActivity,
                                        imageModel.nodeAddress
                                    )
                                if (imageUri == null) {
                                    Dialogs.showToast(
                                        applicationContext,
                                        getString(R.string.failed_to_download_image)
                                    )
                                } else {
                                    startImageShareIntent(imageUri)
                                }

                            }

                        }

                        override fun onBitmapFailed(
                            e: java.lang.Exception?,
                            errorDrawable: Drawable?
                        ) {
                            if (loadingDialog.isShowing) {
                                loadingDialog.dismiss()
                            }

                            val message: String = e?.localizedMessage ?: ""
                            if (message.trim().isNotEmpty()) {
                                Dialogs.showMessage(this@MainActivity, message)
                            } else {
                                Dialogs.showToast(
                                    applicationContext,
                                    getString(R.string.failed_to_download_image)
                                )
                            }

                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    })
            } else {
                startImageShareIntent(imageUriIfPresent)
            }

        }
    }

    private fun startImageShareIntent(imageUri: Uri) {
        try {

            val shareIntent =
                Intent(Constants.INSTAGRAM_FEED_PACKAGE_NAME)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.setPackage(Constants.INSTAGRAM_PACKAGE_NAME)
            startActivity(shareIntent)

            saveSharedImage()

        } catch (e: Exception) {
            Utility.openAppInPlayStore(
                Constants.INSTAGRAM_PACKAGE_NAME,
                this@MainActivity
            )
        }
    }

    private fun saveSharedImage() {
        imageModelForSharing?.let { imageModel ->
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                fireStore.collection(Constants.COLLECTION_USERS).document(uid).get()
                    .addOnSuccessListener { result ->
                        val userModel = result.toObject(UserModel::class.java)
                        userModel?.let { user ->

                            val hashMap = HashMap<String, Any>()
                            hashMap["user"] = uid
                            hashMap["username"] = user.name
                            hashMap["date"] = System.currentTimeMillis()

                            fireStore
                                .collection(Constants.COLLECTION_IMAGES)
                                .document(imageModel.nodeAddress)
                                .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
                                .document(uid)
                                .set(hashMap)
                                .addOnSuccessListener { Logger.info("DocumentSnapshot - $hashMap - successfully written") }
                                .addOnFailureListener { e -> Logger.info("Error writing document - $hashMap - $e") }
                        }

                    }
            }
        }
    }

    fun applyFilterClicked(view: View) {
        imagesAdapter.applyFilter(listOfCategoriesForFilter)
        filterView.visibility = View.GONE
        saveFilteredCategories(Gson().toJson(listOfCategoriesForFilter))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                if (filterView.visibility == View.VISIBLE) {
                    filterView.visibility = View.GONE
                    hideKeyboard()
                } else {
                    filterView.visibility = View.VISIBLE
                    getUserData()
                }
            }
            R.id.shared_images -> {
                startActivity(Intent(applicationContext, SharedImagesActivity::class.java))
            }
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAllImages() {
        fireStore.collection(Constants.COLLECTION_IMAGES).get().addOnSuccessListener { result ->
            for (document in result) {
                val imageModel = document.toObject(ImageModel::class.java)

                if (imageModel.skip == 0) {
                    listOfImages.add(imageModel)
                    imagesAdapter.notifyDataSetChanged()
                    getCategoriesForImage(imageModel.nodeAddress)
                }

            }
            imagesAdapter.applyFilter(listOfCategoriesForFilter)
        }
    }

    private fun getCategoriesForImage(nodeAddress: String) {
        val categories = ArrayList<Long>()
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .document(nodeAddress)
            .collection(Constants.COLLECTION_CATEGORIES)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.data["id"]?.let { id ->
                        categories.add(id as Long)

                        for (i in listOfImages.indices) {
                            if (listOfImages[i].nodeAddress.trim().equals(nodeAddress, false)) {
                                listOfImages[i].categories = categories
                                imagesAdapter.notifyDataSetChanged()
                                imagesAdapter.applyFilter(listOfCategoriesForFilter)
                                Logger.info("${listOfImages[i]}")
                                break
                            }
                        }

                    }
                }
            }
    }

    private fun getAllCategories() {
        fireStore.collection(Constants.COLLECTION_CATEGORIES).get().addOnSuccessListener { result ->
            for (document in result) {
                val categoryModel = document.toObject(CategoryModel::class.java)
                listOfCategories.add(categoryModel)
                categoriesAdapter.notifyDataSetChanged()
                Logger.info("$categoryModel")
            }
        }
    }

    private fun getUserData() {
        listOfCategoriesForFilter.clear()
        categoriesAdapter.notifyDataSetChanged()
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            fireStore.collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener { result ->
                    val userModel = result.toObject(UserModel::class.java)
                    Logger.info("$userModel")

                    listOfCategoriesForFilter.addAll(getSelectedCategoriesForFilter(userModel?.filteredCategories))
                    categoriesAdapter.notifyDataSetChanged()
                }
        }
    }

    private fun saveFilteredCategories(listStr: String) {
        val user = HashMap<String, Any>()
        user["filteredCategories"] = listStr

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            fireStore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(user)
                .addOnSuccessListener { Logger.info("DocumentSnapshot - $user - successfully written") }
                .addOnFailureListener { e -> Logger.info("Error writing document - $user - $e") }
        }
    }

    private fun getSelectedCategoriesForFilter(listStr: String?): ArrayList<CategoryModel> {
        val listToReturn = ArrayList<CategoryModel>()
        listStr?.let { str ->
            if (str.trim().isNotEmpty()) {
                try {
                    val list = Gson().fromJson(str, Array<CategoryModel>::class.java).asList()
                    listToReturn.addAll(list)
                } catch (e: Exception) {
                    Logger.info("getSelectedCategories() - $listStr - $e")
                }
            }
        }
        return listToReturn
    }

    private fun isAllPermissionsAllowed(): Boolean {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            permissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    handleImageShareRequest()
                } else {
                    Dialogs.showMessage(
                        this,
                        getString(R.string.storage_permission_message),
                        getString(R.string.request_again),
                        getString(R.string.cancel),
                        object : AlertDialogInterface {
                            override fun positiveButtonClick() {
                                requestPermissions()
                            }

                            override fun negativeButtonClick() {}
                        })
                }
            }
        }
    }

    private fun endTimerClicked(imageModel: ImageModel, position: Int) {
        val sharedImagesList = ArrayList<SharedImageModel>()
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .document(imageModel.nodeAddress)
            .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
            .get()
            .addOnSuccessListener { sharedDataResult ->
                for (sharedDocument in sharedDataResult) {
                    sharedImagesList.add(sharedDocument.toObject(SharedImageModel::class.java))
                }
                val winner = if (sharedImagesList.isEmpty()) {
                    getString(R.string.no_image_shares)
                } else {
                    if (sharedImagesList.size == 1) {
                        sharedImagesList[0].username
                    } else {
                        val random = Random().nextInt(sharedImagesList.size)
                        sharedImagesList[random].username
                    }
                }

                val imageData = HashMap<String, Any>()
                imageData["winner"] = winner

                listOfImages[position].winner = winner
                imagesAdapter.notifyDataSetChanged()

                fireStore.collection(Constants.COLLECTION_IMAGES)
                    .document(imageModel.nodeAddress)
                    .update(imageData)
                    .addOnSuccessListener { Logger.info("DocumentSnapshot - $imageData - successfully written") }
                    .addOnFailureListener { e -> Logger.info("Error writing document - $imageData - $e") }
            }
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    imagesAdapter.currentTime = System.currentTimeMillis()
                    imagesAdapter.notifyDataSetChanged()
                }
            }
        }, 0L, oneSecond)
    }

    override fun onResume() {
        super.onResume()

        startTimer()
    }

    override fun onPause() {
        timer?.cancel()
        timer = null

        super.onPause()
    }

    private fun hideKeyboard() {
        currentFocus?.let { currentFocus ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.dispatchTouchEvent(ev)
    }

}