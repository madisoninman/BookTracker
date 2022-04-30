package com.example.booktracker.database

import androidx.room.*

@Dao
interface BookDao {

    @Insert
    fun addBook(book : Book) : Long

    @Delete
    fun deleteBook(book : Book)

    @Delete
    fun deleteAllBooks(books: List<Book>)

    @Update
    fun updateBook(book : Book)

    @Query("SELECT * FROM book WHERE id = :bookId")
    fun getBook(bookId : Long) : Book

    @Query("SELECT * FROM book")
    fun getAllBooks(): MutableList<Book>

    @Query("SELECT * FROM book ORDER BY title")
    fun getAllBooksByTitle() : List<Book>

    @Query("SELECT * FROM book ORDER BY author")
    fun getAllBooksByAuthor() : List<Book>

    @Query("SELECT * FROM book ORDER BY read DESC, title")
    fun getAllBooksByRead() : List<Book>
}