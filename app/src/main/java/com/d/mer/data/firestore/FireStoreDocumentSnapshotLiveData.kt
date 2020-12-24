package com.d.mer.data.firestore

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*

class FireStoreDocumentSnapshotLiveData(private val documentSnapshot: DocumentReference) :
    LiveData<Resource<DocumentSnapshot>>(),
    EventListener<DocumentSnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        value = if (e != null) {
            Resource.Error(e)
        } else {
            Resource.Success(snapshot)
        }
    }

    override fun onActive() {
        super.onActive()
        registration = documentSnapshot.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()

        registration?.let {
            it.remove()
            registration = null
        }
    }
}