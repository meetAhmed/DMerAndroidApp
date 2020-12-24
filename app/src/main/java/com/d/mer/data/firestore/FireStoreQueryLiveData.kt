package com.d.mer.data.firestore

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*

class FireStoreQueryLiveData(private val query: Query) : LiveData<Resource<QuerySnapshot>>(),
    EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        value = if (e != null) {
            Resource.Error(e)
        } else {
            Resource.Success(snapshots)
        }
    }

    override fun onActive() {
        super.onActive()
        registration = query.addSnapshotListener(this)
    }

    override fun onInactive() {
        super.onInactive()

        registration?.let {
            it.remove()
            registration = null
        }
    }
}