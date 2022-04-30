package com.example.booktracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booktracker.database.AppDatabase
import com.example.booktracker.database.Book
import com.example.booktracker.databinding.ActivityAddBookBinding
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class AddBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookBinding
    private var purpose: String? = ""
    val searchResults = mutableListOf<Book>()
    private var job: Job? = null
    private var bookId : Long = -1
    private var key : String = "AIzaSyCRcxq0dIr5qW0JFcSkYWSNs6xDXJXqd4E"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )
        setTitle("${purpose} Book")

        searchResults.clear()
        binding.searchTermsEditText.getText().clear()
        binding.searchButton.setOnClickListener(SearchListener())
    }

    inner class SearchListener : View.OnClickListener {
        override fun onClick(view: View?) {
            searchResults.clear()
            val terms = binding.searchTermsEditText.text.toString().trim()

            if (terms.isBlank()) {
                Toast.makeText(applicationContext, R.string.no_input_toast, Toast.LENGTH_LONG)
                    .show()
            } else {
                if (isNetworkAvailable()) {
                    if (job?.isActive != true)
                        callAPIs(terms)
                } else {
                    Toast.makeText(applicationContext, R.string.no_network_toast, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun callAPIs(terms: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val url = getApiUrl(terms)
            val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

            var jsonResults = ""
            try {
                jsonResults = connection.getInputStream()
                    .bufferedReader().use(BufferedReader::readText)
            } finally { connection.disconnect() }

            val json = JSONObject(jsonResults)
            var total = json.getInt("totalItems")

            if (total == 0) {
                // No results
                withContext(Dispatchers.Main) {
                    val builder = AlertDialog.Builder(binding.root.context)
                    builder
                        .setTitle(R.string.no_results_title)
                        .setMessage(R.string.no_results_msg)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
                searchResults.clear()
            } else {
                val itemNums = json.getJSONArray("items")

                // Ensures only 10 objects are shown
                var loopLength = 0
                if (total > 10) { loopLength = 10 }
                else { loopLength = total }

                for (i in 0 until loopLength) {
                    var title = ""
                    var description = ""
                    var preview = ""
                    var author = ""
                    var image = ""

                    try {
                        val item = itemNums.getJSONObject(i)
                        val volumeInfo = item.getJSONObject("volumeInfo")

                        title = volumeInfo.getString("title")
                        description = volumeInfo.getString("description")
                        preview = volumeInfo.getString("previewLink")

                        val authors = volumeInfo.getJSONArray("authors")
                        author = authors.getString(0)

                        val images = volumeInfo.getJSONObject("imageLinks")
                        image = images.getString("thumbnail")

                        val book = Book(0, title, author, description, preview, image, 0)
                        searchResults.add(book)
                    }
                    catch (e: JSONException) {
                        // error check
                        if (title.isBlank()) { title = getString(R.string.unknown) }
                        if (description.isBlank()) { description = getString(R.string.unknown) }
                        if (preview.isBlank()) { preview = getString(R.string.unknown) }
                        if (author.isBlank()) { author = getString(R.string.unknown) }
                        if (image.isBlank()) { image = getString(R.string.unknown) }
                    }
                }

                // RecyclerView
                withContext(Dispatchers.Main) {
                    val layoutManager = LinearLayoutManager(applicationContext)
                    binding.searchRecyclerView.layoutManager = layoutManager

                    binding.searchRecyclerView.setHasFixedSize(true)

                    val divider = DividerItemDecoration(applicationContext, layoutManager.orientation)
                    binding.searchRecyclerView.addItemDecoration(divider)

                    val adapter = MyAddAdapter()
                    binding.searchRecyclerView.adapter = adapter
                }
            }
        }
    }

    private fun getApiUrl(terms : String) : URL {
        var searchTerms = terms.replace(" ", "-")
        searchTerms = "\"" + searchTerms + "\""

        var path = ""
        if (terms.isNotBlank()) {
            path = "https://www.googleapis.com/books/v1/volumes?q=$searchTerms&key=$key"
        }
        return URL(path)
    }

    fun isNetworkAvailable(): Boolean {
        var available = false
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    ) {
                        available = true
                    }
                }
            } else {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_MOBILE ||
                        type == ConnectivityManager.TYPE_WIFI ||
                        type == ConnectivityManager.TYPE_VPN
                    ) {
                        available = true
                    }
                }
            }
        }
        return available
    }

    private val startForViewResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) { super.onBackPressed() }
        }

    // RECYCLERVIEW
    inner class MyAddViewHolder(val view: TextView) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getDatabase(applicationContext)
                    .bookDao()

                bookId = dao.addBook(searchResults[position])

                withContext(Dispatchers.Main) {
                    setResult(RESULT_OK, intent)
                    val intent = Intent(applicationContext, ViewBookActivity::class.java)

                    intent.putExtra(
                        getString(R.string.intent_purpose_key),
                        getString(R.string.intent_purpose_view_book)
                    )
                    intent.putExtra(
                        getString(R.string.intent_key_book_id),
                        bookId
                    )

                    searchResults.clear()
                    binding.searchTermsEditText.getText().clear()
                    startForViewResult.launch(intent)
                }
            }
        }
    }

    inner class MyAddAdapter :
        RecyclerView.Adapter<AddBookActivity.MyAddViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAddViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_item_view, parent, false) as TextView
            return MyAddViewHolder(view)
        }

        override fun onBindViewHolder(holder: AddBookActivity.MyAddViewHolder, position: Int) {
            holder.view.text = searchResults[position].toString()
        }

        override fun getItemCount(): Int {
            return searchResults.size
        }
    }
}