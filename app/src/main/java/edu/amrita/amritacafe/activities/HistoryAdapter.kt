package edu.amrita.amritacafe.activities

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.model.Order
import kotlinx.android.synthetic.main.item_history.view.*

class HistoryAdapter(val orders: MutableList<Order>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {
    inner class HistoryHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(order: Order) {
            view.history_time_TV.text = order.orderTime
            view.history_order_nr_TV.text = order.orderNumber.toString()

            val orderText = StringBuffer()
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
            view.history_order_TV.text = orderText.toString()

            view.history_item_sum_TV.text = order.sum.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        println("creating new holder")
        val inflatedView = parent.inflate(R.layout.item_history, false)
        return HistoryHolder(inflatedView)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        println("Binding $position")
        val item = orders[position]
        holder.bind(item)
    }

}
