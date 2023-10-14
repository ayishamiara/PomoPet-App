package com.mobdeve.chuachingdytocstamaria.mco.pomopet.viewholders

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ItemTodoBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo

class TodoViewHolder(val binding: ItemTodoBinding): ViewHolder(binding.root) {
    fun bindData(todo:ToDo){
        binding.todoItemET.setHint(todo.label)
    }
    fun clearData(){
        binding.todoItemET.setText("")
    }
}