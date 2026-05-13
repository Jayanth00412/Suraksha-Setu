package com.suraksha.setu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VolunteerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val repository = EmergencyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        val switchMode = findViewById<Switch>(R.id.switchVolunteerMode)
        val statusText = findViewById<TextView>(R.id.tvVolunteerStatus)

        // Initial Status Load
        db.collection("volunteers").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                switchMode.isChecked = doc.getBoolean("isActive") ?: false
                updateStatusUI(switchMode.isChecked, statusText)
            }
        }

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            updateVolunteerStatus(userId, isChecked)
            updateStatusUI(isChecked, statusText)
        }

        val nameField = findViewById<EditText>(R.id.etContactName)
        val contactField = findViewById<EditText>(R.id.etContactPhone)
        val addContactBtn = findViewById<Button>(R.id.btnAddContact)

        addContactBtn.setOnClickListener {
            val name = nameField.text.toString().trim()
            val phone = contactField.text.toString().trim()
            if (name.isNotEmpty() && phone.isNotEmpty()) {
                addTrustedContact(userId, name, phone)
                nameField.text.clear()
                contactField.text.clear()
            } else {
                Toast.makeText(this, "Enter name and number", Toast.LENGTH_SHORT).show()
            }
        }

        setupContactsList(userId)
    }

    private fun setupContactsList(userId: String) {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvContactsList)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        db.collection("users").document(userId).collection("contacts")
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val contacts = snapshots?.map { doc ->
                    "${doc.getString("name")} - ${doc.getString("phone")}"
                } ?: emptyList()

                // Simple adapter-like behavior for demo purposes (using a basic text list)
                // In a production app, use a proper RecyclerView Adapter
                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<ContactViewHolder>() {
                    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ContactViewHolder {
                        val view = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                        return ContactViewHolder(view)
                    }
                    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
                        (holder.itemView as TextView).text = contacts[position]
                        (holder.itemView as TextView).setTextColor(android.graphics.Color.WHITE)
                        (holder.itemView as TextView).textSize = 14f
                    }
                    override fun getItemCount() = contacts.size
                }
                recyclerView.adapter = adapter
            }
    }

    class ContactViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    private fun addTrustedContact(userId: String, name: String, phone: String) {
        val contact = hashMapOf(
            "name" to name,
            "phone" to phone,
            "addedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId).collection("contacts").add(contact)
            .addOnSuccessListener {
                Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateVolunteerStatus(userId: String, isActive: Boolean) {
        val updates = hashMapOf<String, Any>(
            "isActive" to isActive,
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("volunteers").document(userId).update(updates)
            .addOnSuccessListener {
                val msg = if (isActive) "You are now active as a Responder" else "Volunteer mode disabled"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatusUI(isActive: Boolean, textView: TextView) {
        if (isActive) {
            textView.text = "ONLINE: Ready to respond"
            textView.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
        } else {
            textView.text = "OFFLINE: Not receiving alerts"
            textView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }
    }
}
