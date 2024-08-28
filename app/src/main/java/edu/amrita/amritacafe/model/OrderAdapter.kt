package edu.amrita.amritacafe.model

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import edu.amrita.amritacafe.databinding.ItemOrderBinding
import edu.amrita.amritacafe.menu.MenuItemUS
import edu.amrita.amritacafe.menu.RegularOrderItem

class OrderAdapter(context: Context) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val orderList: MutableList<RegularOrderItem> = mutableListOf()

    val orderItems: List<RegularOrderItem> = orderList

    var orderChanged: () -> Unit = {} // Replaced by correct callback in mainactivity

    override fun getCount(): Int {
        return orderList.size
    }

    override fun getItem(position: Int): Any {
        return orderList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun remove(item: RegularOrderItem) {
        orderList.removeAll { it == item }
        updateAll()
    }

    fun add(addItem: MenuItemUS, weight: Float) {
        var found = false
        for (existingItem in orderList) {
            if(addItem.malaylamName == existingItem.menuItem.malaylamName && existingItem.comment == "") {
                existingItem.increment(weight)
                println("increasing! ${existingItem.quantity}")
                found = true
                break
            }
        }
        if(!found) {
            orderList.add(RegularOrderItem(addItem, quantity = weight))
        }
        updateAll()
    }

    fun clear() {
        orderList.clear()
        updateAll()
    }

    fun lastItemCostMultiplier(percentCost: Float) {
        if (orderList.size > 0) {
            val lastOrder = orderList.last()
            if (percentCost == -1f)
                lastOrder.comment = "Refund"
            else
                lastOrder.comment = "Discount ${Math.round(100 - percentCost * 100)}%"
            lastOrder.priceMultiplier = percentCost
            updateAll()
        }
    }

    private fun updateAll() {
        notifyDataSetChanged()
        orderChanged()
    }

    private fun reuseOrInflate(view: View?, parent: ViewGroup): ItemOrderBinding =
        if (view == null) {
            ItemOrderBinding.inflate(inflater, parent, false)
        } else {
            ItemOrderBinding.bind(view)
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = reuseOrInflate(convertView, parent)
        binding.root.tag = position

        orderList[position].let { orderItem ->
            binding.label.text = orderItem.menuItem.englishName
            binding.amountTV.text = orderItem.quantity.toString()
            binding.priceTV.text = orderItem.finalPrice.toString()
            binding.commentET.setText(orderItem.comment)
            binding.commentET.visibility =
                if (orderItem.comment.isNotBlank()) View.VISIBLE
                else View.GONE

            binding.amountTV.setOnClickListener {
                println("increasing amount of bla bla")
                orderList[position].increment()
                notifyDataSetChanged()
                orderChanged()
            }

            binding.root.setOnClickListener {
                binding.commentET.run {
                    visibility = View.VISIBLE
                    requestFocus()
                    context.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
                        (it as InputMethodManager).showSoftInput(this, 0)
                    }
                }
            }

            binding.cancelButton.setOnClickListener {
                remove(orderList[position])
            }

            binding.commentET.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    orderList[position] = orderItems[position].editComment(p0.toString())
                }
            })
        }

        return binding.root
    }
}
