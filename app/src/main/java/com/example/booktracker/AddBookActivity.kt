package com.example.booktracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    }

    inner class SearchListener : View.OnClickListener {
        override fun onClick(view: View?) {

        }
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