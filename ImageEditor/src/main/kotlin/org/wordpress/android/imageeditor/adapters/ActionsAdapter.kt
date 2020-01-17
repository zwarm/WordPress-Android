package org.wordpress.android.imageeditor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import org.wordpress.android.imageeditor.R
import org.wordpress.android.imageeditor.adapters.ActionsAdapter.ActionViewHolder

class ActionsAdapter(
    private val actions: Array<String>
) : Adapter<ActionViewHolder>() {
    class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var actionTextView: TextView = view.findViewById(R.id.action_text_view)
        init {
            actionTextView.setOnClickListener {}
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActionViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.action_list_item, parent, false)

        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.actionTextView.text = actions[position]
    }

    override fun getItemCount() = actions.size
}
