package com.billin.com.playground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.billin.com.playground.databinding.FragmentHomeBinding
import com.billin.com.playground.databinding.ItemTextBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var data: List<TextItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = listOf(
            TextItem(
                getString(R.string.fragment_name_crash),
                nav_graph.action.home_to_crash
            ),
            TextItem(
                getString(R.string.fragment_name_bottom_sheet_behavior),
                nav_graph.action.home_to_bottomSheetBehavior
            )
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = Adapter()
        }
    }

    private inner class Adapter : RecyclerView.Adapter<TextVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextVH {
            return LayoutInflater.from(context).inflate(
                R.layout.item_text,
                parent,
                false
            ).let { view ->
                TextVH(view).apply {
                    onItemClickListener = { pos ->
                        itemView.findNavController().navigate(data[pos].jumpAction)
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: TextVH, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount() = data.size
    }
}

private class TextVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemTextBinding.bind(itemView)
    var onItemClickListener: ((pos: Int) -> Unit)? = null

    init {
        binding.root.setOnClickListener {
            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) onItemClickListener?.invoke(pos)
        }
    }

    fun bind(item: TextItem) {
        binding.tv.text = item.name
    }
}

private data class TextItem(
    val name: String,
    val jumpAction: Int
)