package com.mobdeve.chuachingdytocstamaria.mco.pomopet.viewholders

import android.text.Editable
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ItemTodoBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class TodoViewHolder(val binding: ItemTodoBinding): ViewHolder(binding.root) {

    // Binds ToDo data to the ViewHolder's layout
    fun bindData(todo:ToDo){
        binding.todoItemET.hint = "Add to do list item here"
        binding.todoItemET.setText(todo.label)

    }

    // Clears the data displayed in the ViewHolder's layout
    fun clearData(){
        binding.todoItemET.setText("")
    }
}