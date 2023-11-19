package com.mobdeve.chuachingdytocstamaria.mco.pomopet.viewholders

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ItemTodoBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class TodoViewHolder(val binding: ItemTodoBinding): ViewHolder(binding.root) {
    fun bindData(todo:ToDo){
        binding.todoItemET.hint = "Add to do list item here"
    }
    fun clearData(){
        binding.todoItemET.setText("")
    }
}