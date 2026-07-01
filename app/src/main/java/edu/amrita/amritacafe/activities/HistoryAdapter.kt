package edu.amrita.amritacafe.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.databinding.ItemHistoryBinding
import edu.amrita.amritacafe.model.Order

class HistoryAdapter(private val orders: MutableList<Order>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    class HistoryHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.historyTimeTV.text = order.orderTime
            binding.historyOrderNrTV.text = order.orderNumber.toString()

            val orderText = StringBuilder()
            order.orderItems.forEach { item ->
                val itemName = item.menuItem.englishName
                val qty = item.quantity
                val price = item.finalPrice
                
                orderText.append("${qty} ${itemName}".padEnd(17))
                orderText.append(price.toString().padStart(3))
                
                if (item.comment.isNotBlank()) {
                    orderText.append("\n * ${item.comment}\n")
                } else {
                    orderText.append("\n")
                }
            }
            binding.historyOrderTV.text = orderText.toString()
            binding.historyItemSumTV.text = order.sum.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryHolder(binding)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = orders[position]
        holder.bind(item)
    }
}
