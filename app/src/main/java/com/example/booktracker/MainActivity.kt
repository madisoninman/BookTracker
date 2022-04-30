package com.example.booktracker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booktracker.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    //private val books = mutableListOf<Book>()
    private val books = mutableListOf<String>()

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

        }

        override fun onLongClick(view: View?): Boolean {
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
            holder.view.text = books[position].toString()
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
                return true
            }
            R.id.menu_item_sort_title -> {
                return true
            }
            R.id.menu_item_sort_author -> {
                return true
            }
            R.id.menu_item_sort_read -> {
                return true
            }
            R.id.menu_item_delete -> {
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