package com.mobdeve.chuachingdytocstamaria.mco.pomopet.adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.databinding.ItemTodoBinding
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.db.ToDoDB
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.models.ToDo
import com.mobdeve.chuachingdytocstamaria.mco.pomopet.viewholders.TodoViewHolder

class TodoAdapter(private val data: ArrayList<ToDo>): Adapter<TodoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder{
        val view = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        val todoViewHolder = TodoViewHolder(view)
        val todoItemText =  view.todoItemET
        val todoCheckBtn = view.todoBtn
        var changed = false

        val todoDb = ToDoDB(parent.context)


        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Handle any necessary logic after text changes, if needed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Handle any necessary logic before text changes, if needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val position = todoViewHolder.adapterPosition
                if (s.isNullOrEmpty()) {
                    // If the text is empty, reset the "changed" flag
                    changed = false
                } else if (!changed) {
                    if (position < data.size) {
                        parent.post {
                            data[position].label = todoItemText.text.toString()
                        }
                    }

                    // Add a new ToDo object if needed
                    if (position == data.size - 1 && s.isNotEmpty()) {
                        parent.post {
                            data.add(ToDo())
                            notifyItemInserted(position + 1)
                        }
                    }

                }
            }
        }

        todoItemText.addTextChangedListener(textWatcher)
        todoCheckBtn.setOnClickListener{
            val position = todoViewHolder.adapterPosition
            if(todoItemText.text.isNotEmpty() && position < data.size){
                    data[position].isDone = true
                    todoDb.updateTodo(data[position])
                    data.remove(data[position])
                    notifyItemRemoved(position)
                    todoViewHolder.clearData()

            }
        }

        return todoViewHolder
    }
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}