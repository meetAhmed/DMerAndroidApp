package com.d.mer.data.firestore

import com.d.mer.ui.common.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FireStoreReferences {

    private val fireStore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    val categoriesCollection by lazy {
        fireStore.collection(Constants.COLLECTION_CATEGORIES)
            .asSnapshotLiveData()
    }

    val imagesCollection by lazy {
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .asSnapshotLiveData()
    }

    fun categoriesForImageCollection(nodeAddress: String) =
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .document(nodeAddress)
            .collection(Constants.COLLECTION_CATEGORIES)
            .asSnapshotLiveData()

    fun getUserData(nodeAddress: String) =
        fireStore.collection(Constants.COLLECTION_USERS)
            .document(nodeAddress)
            .asSnapshotLiveData()

    fun userSharedImagesCollection(nodeAddress: String) =
        fireStore.collection(Constants.COLLECTION_IMAGES)
            .document(nodeAddress)
            .collection(Constants.COLLECTION_USERS_SHARED_IMAGES)
            .asSnapshotLiveData()

    fun getLoggedUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

}

fun Query.asSnapshotLiveData(): FireStoreQueryLiveData {
    return FireStoreQueryLiveData(this)
}

fun DocumentReference.asSnapshotLiveData(): FireStoreDocumentSnapshotLiveData {
    return FireStoreDocumentSnapshotLiveData(this)
}