package edu.amrita.amritacafe.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.databinding.ItemHistoryBinding
import edu.amrita.amritacafe.model.Order

class HistoryAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    inner class HistoryHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.historyTimeTV.text = order.orderTime
            binding.historyOrderNrTV.text = order.orderNumber.toString()

            val orderText = StringBuilder()
            println("size: ${order.orderItems.size}")
            for (item in order.orderItems) {
                println("order: ${item.menuItem.malaylamName}")
                orderText.append(
                    "${item.quantity} ${item.menuItem.englishName}".padEnd(17) +
                            item.finalPrice.toString().padStart(3) +
                            if (item.comment.isBlank()) "\n"
                            else "\n * ${item.comment}\n"
                )
            }
            binding.historyOrderTV.text = orderText.toString()

            binding.historyItemSumTV.text = order.sum.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        println("creating new holder")
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryHolder(binding)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        println("Binding $position")
        val item = orders[position]
        holder.bind(item)
    }
}
