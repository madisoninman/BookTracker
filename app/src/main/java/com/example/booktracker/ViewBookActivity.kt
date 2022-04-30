package com.example.booktracker

import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.booktracker.databinding.ActivityViewBookBinding

class ViewBookActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private lateinit var binding: ActivityViewBookBinding
    private var purpose: String? = ""
    private var bookId: Long = -1
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.readToggleButton.setOnCheckedChangeListener(this)
        binding.previewButton.setOnClickListener { }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {

    }
}
