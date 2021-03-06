package com.example.booktracker

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booktracker.database.AppDatabase
import com.example.booktracker.database.Book
import com.example.booktracker.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    private val books = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // recyclerview
        val layoutManager = LinearLayoutManager(this)
        binding.mainRecyclerview.layoutManager = layoutManager
        val dividerItemDecoration =
            DividerItemDecoration(applicationContext, layoutManager.orientation)
        binding.mainRecyclerview.addItemDecoration(dividerItemDecoration)
        adapter = MyAdapter()
        binding.mainRecyclerview.adapter = adapter

        loadAllBooks("")
    }

    // DATABASE
    private fun loadAllBooks(sort: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.bookDao()

            val results = when (sort) {
                getString(R.string.sort_title) -> {
                    dao.getAllBooksByTitle()
                }
                getString(R.string.sort_author) -> {
                    dao.getAllBooksByAuthor()
                }
                getString(R.string.sort_read) -> {
                    dao.getAllBooksByRead()
                }
                else -> {
                    dao.getAllBooks()
                }
            }

            withContext(Dispatchers.Main) {
                books.clear()
                books.addAll(results)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun addNewBook() {
        val intent = Intent(applicationContext, AddBookActivity::class.java)
        intent.putExtra(
            getString(R.string.intent_purpose_key),
            getString(R.string.intent_purpose_add_book)
        )
        startForAddResult.launch(intent)
    }

    private fun deleteAllBooks() {
        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_all_msg))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { dialogInterface, whichButton ->

                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getDatabase(applicationContext)
                        .bookDao()
                        .deleteAllBooks(books)

                    loadAllBooks("")
                }
            }
        builder.show()
    }


    private val startForAddResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                loadAllBooks("")
            }
        }

    private val startForViewResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                loadAllBooks("")
            }
        }


    // RECYCLERVIEW
    inner class MyViewHolder(val view: TextView) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener, View.OnLongClickListener {

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(view: View?) {
            val intent = Intent(applicationContext, ViewBookActivity::class.java)

            intent.putExtra(
                getString(R.string.intent_purpose_key),
                getString(R.string.intent_purpose_view_book)
            )

            val book = books[adapterPosition]
            intent.putExtra(
                getString(R.string.intent_key_book_id),
                book.id
            )

            startForViewResult.launch(intent)
        }

        override fun onLongClick(view: View?): Boolean {
            val book = books[adapterPosition]

            val builder = AlertDialog.Builder(view!!.context)
                .setTitle(getString(R.string.delete_dialog_title))

                .setMessage(getString(R.string.delete_dialog_msg) + "${book.title}\"?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { dialogInterface, whichButton ->

                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getDatabase(applicationContext)
                            .bookDao()
                            .deleteBook(book)
                        loadAllBooks("")
                    }
                }
            builder.show()
            return true
        }
    }

    inner class MyAdapter :
        RecyclerView.Adapter<MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view, parent, false) as TextView
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.view.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(books[position].toStringMain(), Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(books[position].toStringMain())
            }
        }

        override fun getItemCount(): Int {
            return books.size
        }
    }

    // OPTIONS MENU
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add -> {
                addNewBook()
                return true
            }
            R.id.menu_item_sort_title -> {
                loadAllBooks(getString(R.string.sort_title))
                return true
            }
            R.id.menu_item_sort_author -> {
                loadAllBooks(getString(R.string.sort_author))
                return true
            }
            R.id.menu_item_sort_read -> {
                loadAllBooks(getString(R.string.sort_read))
                return true
            }
            R.id.menu_item_delete -> {
                deleteAllBooks()
                return true
            }
            R.id.menu_item_about -> {
                val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.menu_item_about_title))
                    .setMessage(getString(R.string.menu_item_about_msg))
                    .setPositiveButton(android.R.string.ok, null)
                builder.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}