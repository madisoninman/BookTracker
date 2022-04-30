package com.example.booktracker

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.booktracker.database.AppDatabase
import com.example.booktracker.databinding.ActivityViewBookBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ViewBookActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private lateinit var binding: ActivityViewBookBinding
    private var purpose: String? = ""
    private var bookId: Long = -1
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )
        bookId = intent.getLongExtra(
            getString(R.string.intent_key_book_id), -1
        )
        title = "${purpose} Book"
        displayContents()

        binding.readToggleButton.setOnCheckedChangeListener(this)
        binding.previewButton.setOnClickListener {
            if (url.isNotBlank()) {
                val uri = Uri.parse(url)
                val intentUrl = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intentUrl)
            } else {
                val builder = AlertDialog.Builder(binding.root.context)
                builder
                    .setTitle(R.string.no_url_title)
                    .setMessage(R.string.no_url_msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    private fun displayContents() {
        CoroutineScope(Dispatchers.IO).launch {
            val book = AppDatabase.getDatabase(applicationContext)
                .bookDao()
                .getBook(bookId)

            // preview link
            url = book.link

            // cover
            var cover = book.image
            cover = cover.replace("http", "https")
            cover = cover.replace("&edge=curl", "")
            cover += ".jpeg"
            var bitmap: Bitmap? = null
            if (isNetworkAvailable()) {
                try {
                    val imgUrl = URL(cover)
                    val imgConnection: HttpURLConnection = imgUrl.openConnection() as HttpURLConnection

                    try {
                        imgConnection.inputStream.use { stream ->
                            bitmap = BitmapFactory.decodeStream(stream)
                        }
                    } finally {
                        imgConnection.disconnect()
                    }
                } catch (e: IOException) {
                    cover = "ERROR"
                }
            } else { cover = "ERROR" }


            withContext(Dispatchers.Main) {
                binding.bookTitleTextView.text = book.title
                binding.bookAuthorTextView.text = book.author
                binding.bookDescriptionTextView.text = book.description

                binding.readToggleButton.isChecked = book.read != 0

                if (cover == "ERROR") { binding.bookCoverImageView.setImageResource(R.drawable.error) }
                else { binding.bookCoverImageView.setImageBitmap(bitmap) }
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val bookDao = AppDatabase.getDatabase(applicationContext)
                .bookDao()

            val book = bookDao.getBook(bookId)

            var bool = 0
            if (checked) { bool = 1 }
            book.read = bool

            bookDao.updateBook(book)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        var available = false

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.run {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // code for newer versions
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    ) {
                        available = true
                    }
                }

            } else {

                // code for older versions
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_MOBILE
                        || type == ConnectivityManager.TYPE_WIFI
                        || type == ConnectivityManager.TYPE_VPN
                    ) {
                        available = true
                    }
                }

            }
        }

        return available
    }
}
