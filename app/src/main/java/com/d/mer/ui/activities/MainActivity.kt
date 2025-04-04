package com.d.mer.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.beloo.widget.chipslayoutmanager.layouter.breaker.IRowBreaker
import com.d.mer.R
import com.d.mer.data.firestore.FireStoreReferences
import com.d.mer.data.firestore.Resource
import com.d.mer.data.models.CategoryModel
import com.d.mer.data.models.ImageModel
import com.d.mer.data.models.SharedImageModel
import com.d.mer.data.models.UserModel
import com.d.mer.ui.adapters.CategoriesAdapter
import com.d.mer.ui.adapters.ImagesAdapter
import com.d.mer.ui.common.*
import com.d.mer.ui.interfaces.AlertDialogInterface
import com.d.mer.ui.interfaces.CategoryClickListener
import com.d.mer.ui.interfaces.ImagesClickListener
import com.d.mer.ui.interfaces.TimerEndedForImageListener
import com.d.mer.ui.viewModels.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivity() {

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

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }

    private var showImageToWinnerNodeAddress = ""

    companion object {
        var mainActivityObject: MainActivity? = null
    }

    /**
     * onCreate() Method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivityObject?.finish()
        mainActivityObject = this

        showImageToWinnerNodeAddress = intent.getStringExtra("imageNodeAddress") ?: ""

        if (showImageToWinnerNodeAddress.trim().isNotEmpty()) {
            val intent = Intent(applicationContext, WinnerActivity::class.java)
            intent.putExtra("nodeAddress", showImageToWinnerNodeAddress)
            startActivity(intent)
        }

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

                override fun endTimer(imageModel: ImageModel) {
                    endTimerForImage(imageModel.nodeAddress)
                }
            })

        FireStoreReferences.getLoggedUserId()?.let { uid ->
            viewModel.getUserData(uid).observe(this, { results ->
                getModel(results, UserModel::class.java)?.let { userModel ->
                    listOfCategoriesForFilter.clear()
                    listOfCategoriesForFilter.addAll(getSelectedCategoriesForFilter(userModel.filteredCategories))
                    categoriesAdapter.notifyDataSetChanged()
                }
            })
        }

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

        viewModel.getCategories().observe(this, { results ->
            getModels(results, CategoryModel::class.java)?.let {
                listOfCategories.clear()
                listOfCategories.addAll(it)
                categoriesAdapter.notifyDataSetChanged()
            }
        })

        viewModel.getImages().observe(this, { results ->
            getModels(results, ImageModel::class.java)?.let { list ->
                listOfImages.clear()
                list.forEach { imageModel ->
                    if (imageModel.skip == 0) {
                        listOfImages.add(imageModel)
                        getCategoriesForImage(imageModel.nodeAddress)
                    }
                }
                imagesAdapter.notifyDataSetChanged()
                imagesAdapter.applyFilter(listOfCategoriesForFilter)
            }
        })

        getFirebaseToken()

    }

    /**
     * getFirebaseToken() Method
     */
    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FireStoreReferences.uploadUserToken(it)
        }
    }

    /**
     * handleImageShareRequest() Method
     */
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

    /**
     * startImageShareIntent() Method
     */
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

    /**
     * saveSharedImage() Method
     */
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

    /**
     * applyFilterClicked() Method
     */
    fun applyFilterClicked(view: View) {
        imagesAdapter.applyFilter(listOfCategoriesForFilter)
        filterView.visibility = View.GONE
        saveFilteredCategories(Gson().toJson(listOfCategoriesForFilter))
    }

    /**
     * onCreateOptionsMenu() Method
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * onOptionsItemSelected() Method
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter -> {
                if (filterView.visibility == View.VISIBLE) {
                    filterView.visibility = View.GONE
                    hideKeyboard()
                } else {
                    filterView.visibility = View.VISIBLE
                }
            }
            R.id.shared_images -> {
                startActivity(Intent(applicationContext, SharedImagesActivity::class.java))
            }
            R.id.send_notifications -> {
                startActivity(Intent(applicationContext, SendNotificationsActivity::class.java))
            }
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
            R.id.end_all_timers -> {
                Dialogs.showMessage(
                    this@MainActivity,
                    getString(R.string.end_all_timers_check),
                    getString(R.string.yes),
                    getString(R.string.no),
                    object : AlertDialogInterface {
                        override fun positiveButtonClick() {
                            endAllTimers()
                        }

                        override fun negativeButtonClick() {}
                    }
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * getCategoriesForImage() Method
     */
    private fun getCategoriesForImage(nodeAddress: String) {
        viewModel.categoriesForImageCollection(nodeAddress)
            .observe(this, { innerResults ->
                if (innerResults is Resource.Success) {
                    innerResults.data?.let { result ->
                        val categories = ArrayList<Long>()
                        for (document in result) {
                            document.data["id"]?.let { id ->
                                categories.add(id as Long)

                                for (i in listOfImages.indices) {
                                    if (listOfImages[i].nodeAddress.trim()
                                            .equals(nodeAddress, false)
                                    ) {
                                        listOfImages[i].categories = categories
                                        imagesAdapter.notifyDataSetChanged()
                                        imagesAdapter.applyFilter(listOfCategoriesForFilter)
                                        break
                                    }
                                }

                            }
                        }
                    }
                }
            })
    }

    /**
     * saveFilteredCategories() Method
     */
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

    /**
     * getSelectedCategoriesForFilter() Method
     */
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

    /**
     * isAllPermissionsAllowed() Method
     */
    private fun isAllPermissionsAllowed(): Boolean {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }

    /**
     * requestPermissions() Method
     */
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

    /**
     * onRequestPermissionsResult() Method
     */
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

    /**
     * endTimerForImage() Method
     */
    private fun endTimerForImage(
        nodeAddress: String,
        listener: TimerEndedForImageListener? = null
    ) {
        Logger.info("endTimerForImage() - $nodeAddress")
        val sharedImagesList = ArrayList<SharedImageModel>()
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .document(nodeAddress)
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
                        sendNotificationToWinner(sharedImagesList[0].user, nodeAddress)
                        sharedImagesList[0].username
                    } else {
                        val random = Random().nextInt(sharedImagesList.size)
                        sendNotificationToWinner(sharedImagesList[random].user, nodeAddress)
                        sharedImagesList[random].username
                    }
                }

                val imageData = HashMap<String, Any>()
                imageData["winner"] = winner

                fireStore.collection(Constants.COLLECTION_IMAGES)
                    .document(nodeAddress)
                    .update(imageData)
                    .addOnSuccessListener {
                        listener?.ended()
                        Logger.info("DocumentSnapshot - $imageData - successfully written")
                    }.addOnFailureListener { e ->
                        listener?.ended()
                        Logger.info("Error writing document - $imageData - $e")
                    }
            }
    }

    /**
     * startTimer() Method
     */
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

    /**
     * onResume() Method
     */
    override fun onResume() {
        super.onResume()

        startTimer()
    }

    /**
     * onPause() Method
     */
    override fun onPause() {
        timer?.cancel()
        timer = null

        super.onPause()
    }

    /**
     * endAllTimers() Method
     */
    private fun endAllTimers() {
        if (listOfImages.isEmpty()) {
            Dialogs.showToast(applicationContext, getString(R.string.no_images))
        } else {
            val listOfNodeAddresses = ArrayList<String>()
            listOfImages.forEach {
                if (it.winner.trim().isEmpty()) {
                    listOfNodeAddresses.add(it.nodeAddress)
                }
            }
            val loadingDialog = Dialogs.loader(
                this,
                getString(R.string.end_timers_loading_dialog_message)
            )
            Thread {
                endTimerForImage(listOfNodeAddresses, loadingDialog)
            }.start()
        }
    }

    /**
     * endTimerForImage() Method
     */
    private fun endTimerForImage(listOfNodeAddresses: ArrayList<String>, loadingDialog: Dialog) {
        Logger.info("endTimerForImage() $listOfNodeAddresses")
        if (listOfNodeAddresses.isNotEmpty()) {
            val nodeAddress = listOfNodeAddresses[0]
            endTimerForImage(
                nodeAddress,
                object : TimerEndedForImageListener {
                    override fun ended() {
                        if (listOfNodeAddresses.isNotEmpty()) {
                            listOfNodeAddresses.removeAt(0)
                        }
                        endTimerForImage(listOfNodeAddresses, loadingDialog)
                    }
                })
        } else {
            runOnUiThread {
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
                Dialogs.showToast(
                    applicationContext,
                    getString(R.string.end_all_timers_success)
                )
            }
        }
    }

    /**
     * sendNotificationToWinner() Method
     */
    private fun sendNotificationToWinner(user: String, imageNodeAddress: String) {
        fireStore.collection(Constants.COLLECTION_USERS).document(user).get().addOnSuccessListener {
            it.toObject(UserModel::class.java)?.let { userModel ->
                Logger.info("sendNotificationToWinner() $userModel")
                viewModel.sendNotifications(
                    getNotificationData(imageNodeAddress),
                    userModel.token
                )
            }
        }.addOnFailureListener {
            Logger.info("sendNotificationToWinner() $it")
        }
    }

    /**
     * getNotificationData() Method
     */
    private fun getNotificationData(imageNodeAddress: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("title", getString(R.string.notification_title))
            jsonObject.put("message", getString(R.string.notification_message_for_winner))
            jsonObject.put("type", Constants.TO_WINNER)
            jsonObject.put("imageNodeAddress", imageNodeAddress)
            jsonObject.put("time", System.currentTimeMillis().toString())
        } catch (e: Exception) {
            Logger.info("getNotificationData() - $e")
        }
        return jsonObject
    }

}