package com.suraksha.setu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class TrustedContact(
    val id: String = "",
    val name: String = "",
    val phone: String = ""
)

class TrustCircleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter

    private val db = FirebaseFirestore.getInstance()
    private val contacts = mutableListOf<TrustedContact>()

    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trust_circle)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val userId = currentUser.uid

        // Views
        recyclerView = findViewById(R.id.rvTrustCircle)

        val etName = findViewById<EditText>(R.id.etCircleName)
        val etPhone = findViewById<EditText>(R.id.etCirclePhone)

        val btnAdd = findViewById<Button>(R.id.btnAddCircleContact)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // RecyclerView
        adapter = ContactsAdapter(contacts) { contact ->

            val callIntent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${contact.phone}")
            )

            startActivity(callIntent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        // Load Contacts
        loadContacts(userId)

        // Add Contact
        btnAdd.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter name and phone number",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (phone.length < 10) {

                Toast.makeText(
                    this,
                    "Enter valid phone number",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            addContact(userId, name, phone)

            etName.text.clear()
            etPhone.text.clear()
        }
    }

    private fun loadContacts(userId: String) {

        listenerRegistration =
            db.collection("users")
                .document(userId)
                .collection("contacts")
                .orderBy(
                    "addedAt",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .addSnapshotListener { snapshots, error ->

                    if (error != null) {

                        Toast.makeText(
                            this,
                            "Firestore Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    contacts.clear()

                    snapshots?.forEach { doc ->

                        val contact = TrustedContact(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: ""
                        )

                        contacts.add(contact)
                    }

                    adapter.notifyDataSetChanged()
                }
    }

    private fun addContact(
        userId: String,
        name: String,
        phone: String
    ) {

        val contact = hashMapOf(
            "name" to name,
            "phone" to phone,
            "addedAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("contacts")
            .add(contact)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Contact Added",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Failed: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        listenerRegistration?.remove()
    }

    inner class ContactsAdapter(
        private val list: List<TrustedContact>,
        private val onCall: (TrustedContact) -> Unit
    ) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

        inner class ViewHolder(view: android.view.View) :
            RecyclerView.ViewHolder(view) {

            val avatar: TextView =
                view.findViewById(R.id.tvAvatar)

            val name: TextView =
                view.findViewById(R.id.tvContactName)

            val phone: TextView =
                view.findViewById(R.id.tvContactPhone)

            val callBtn: ImageButton =
                view.findViewById(R.id.ibCall)
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): ViewHolder {

            val view = layoutInflater.inflate(
                R.layout.item_contact,
                parent,
                false
            )

            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {

            val contact = list[position]

            holder.name.text = contact.name
            holder.phone.text = contact.phone

            holder.avatar.text =
                contact.name.firstOrNull()?.uppercase() ?: "?"

            holder.callBtn.setOnClickListener {
                onCall(contact)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}