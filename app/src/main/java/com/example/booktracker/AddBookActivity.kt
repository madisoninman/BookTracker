package com.example.booktracker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
}