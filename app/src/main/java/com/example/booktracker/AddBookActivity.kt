package com.example.booktracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.booktracker.databinding.ActivityAddBookBinding
import kotlinx.coroutines.Job

class AddBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookBinding
    private var purpose: String? = ""
    //val searchResults = mutableListOf<Book>()
    val searchResults = mutableListOf<String>()
    private var job: Job? = null
    private var bookId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun callAPIs(terms: String) { }

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

    // RECYCLERVIEW
    inner class MyAddViewHolder(val view: TextView) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {

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