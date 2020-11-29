package edu.amrita.amritacafe.printer.writer

import com.epson.epos2.printer.Printer
import edu.amrita.amritacafe.menu.RegularOrderItem
import edu.amrita.amritacafe.model.Order
import edu.amrita.amritacafe.settings.Configuration

class WorkOrderWriter(private val orders: List<Order>, private val configuration: Configuration) {

    init {
        require(orders.isNotEmpty()) { "An order needs at least one order item." }
    }

    private fun writeLine(orderItem: RegularOrderItem) =
        if (orderItem.quantity == 1f) {
            "  "
        } else {
            orderItem.quantity.toString().padEnd(2)
        } + orderItem.menuItem.englishName +
                if (orderItem.comment.isBlank()) {
                    ""
                } else {
                    "\n  * ${orderItem.comment}"
                }

    private fun writeTo(printer: Printer) {
        val (titleSize, textSize, lineFeed) = configuration.textConfig
        orders.forEach { (orderNumber, itemList, time) ->
            val itemsMap = itemList.groupBy { it.menuItem.category }.toSortedMap()
            val orderNumStr = orderNumber.toString().padStart(3, '0')

            val orderItemsText = itemsMap.map { (_, orderItems) ->
                orderItems.map(::writeLine)
            }.flatten().joinToString("\n")
            val itemCount =
                itemList.map { 1 }.sum()

            printer.addTextSize(titleSize, titleSize)
            printer.addText("$orderNumStr        $time")
            printer.addFeedLine(lineFeed)
            printer.addHLine(1, 2400, Printer.LINE_THICK_DOUBLE)

            printer.addFeedLine(lineFeed)

            printer.addTextSize(textSize, textSize)
            printer.addText(orderItemsText)
            printer.addFeedLine(lineFeed)
            printer.addFeedLine(lineFeed)
            printer.addFeedLine((6 - itemCount).let { if (it < 0) 0 else it })
            printer.addCut(Printer.CUT_FEED)
        }
    }

    companion object : ReceiptWriter {

        override fun writeToPrinter(
            orders: List<Order>,
            printer: Printer,
            configuration: Configuration
        ) {
            WorkOrderWriter(orders, configuration).writeTo(printer)
        }
    }
}