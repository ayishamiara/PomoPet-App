package com.mobdeve.chuachingdytocstamaria.mco.pomopet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.adapters.TodoAdapter
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ActivityMainBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var todoListRV: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.todoListRV = binding.todoListRV
        this.todoListRV.adapter = TodoAdapter(arrayListOf(ToDo("Add todo list item here")))
        this.todoListRV.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,
            false)

    }
}