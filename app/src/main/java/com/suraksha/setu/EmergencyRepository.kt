package com.suraksha.setu

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class EmergencyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createAlert(lat: Double, lng: Double, audioUrl: String? = null, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val alert = hashMapOf(
            "userId" to userId,
            "lat" to lat,
            "lng" to lng,
            "audioUrl" to audioUrl,
            "timestamp" to System.currentTimeMillis(),
            "status" to "ACTIVE"
        )

        db.collection("alerts").add(alert)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun addTrustedContact(name: String, phone: String) {
        val userId = auth.currentUser?.uid ?: return
        val contact = hashMapOf("name" to name, "phone" to phone)
        db.collection("users").document(userId).collection("contacts").add(contact)
    }

    fun registerAsVolunteer(lat: Double, lng: Double) {
        val userId = auth.currentUser?.uid ?: return
        val volunteer = hashMapOf(
            "uid" to userId,
            "currentLat" to lat,
            "currentLng" to lng,
            "lastActive" to System.currentTimeMillis()
        )
        db.collection("volunteers").document(userId).set(volunteer)
    }
}
