package com.example.mydialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import timber.log.Timber
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        searchEditText = findViewById(R.id.et_search)
        searchButton = findViewById(R.id.btn_search)
        recyclerView = findViewById(R.id.rView)

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter()
        recyclerView.adapter = adapter

        // Load data from JSON link and parse it into Contact objects
        GlobalScope.launch(Dispatchers.Main) {
            val contacts = fetchData("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
            adapter.setData(contacts)
        }

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString().trim()
            if (searchText.isEmpty()) {
                // Show all elements if search field is empty
                GlobalScope.launch(Dispatchers.Main) {
                    val allContacts = fetchData("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
                    adapter.setData(allContacts)
                }
            } else {
                // Filter elements based on search text
                GlobalScope.launch(Dispatchers.Main) {
                    val allContacts = fetchData("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
                    val filteredContacts = allContacts.filter { contact ->
                        contact.name.contains(searchText, true) ||
                                contact.phone.contains(searchText, true) ||
                                contact.type.contains(searchText, true)
                    }
                    adapter.setData(filteredContacts)
                }
            }
        }
    }

    private suspend fun fetchData(url: String): List<Contact> {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            val inputStreamReader = InputStreamReader(connection.inputStream)
            val json = inputStreamReader.readText()
            inputStreamReader.close()
            connection.disconnect()

            parseJsonData(json)
        }
    }

    private fun parseJsonData(json: String): List<Contact> {
        val jsonArray = JSONArray(json)
        val contacts = mutableListOf<Contact>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("name")
            val phone = jsonObject.getString("phone")
            val type = jsonObject.getString("type")

            contacts.add(Contact(name, phone, type))
        }

        return contacts
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)

class ContactAdapter : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts: List<Contact> = emptyList()

    fun setData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textName)
        private val phoneTextView: TextView = itemView.findViewById(R.id.textPhone)
        private val typeTextView: TextView = itemView.findViewById(R.id.textType)

        fun bind(contact: Contact) {
            nameTextView.text = contact.name
            phoneTextView.text = contact.phone
            typeTextView.text = contact.type
        }
    }
}