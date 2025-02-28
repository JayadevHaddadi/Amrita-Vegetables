package edu.amrita.amritacafe.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.amrita.amritacafe.BuildConfig
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.databinding.ActivityMainBinding
import edu.amrita.amritacafe.databinding.DialogEnterWeightBinding
import edu.amrita.amritacafe.databinding.DialogHistoryBinding
import edu.amrita.amritacafe.databinding.DialogPaymentBinding
import edu.amrita.amritacafe.menu.MenuItemUS
import edu.amrita.amritacafe.menu.createDefualtFilesIfNecessary
import edu.amrita.amritacafe.menu.getListOfMenu
import edu.amrita.amritacafe.model.MenuAdapter
import edu.amrita.amritacafe.model.Order
import edu.amrita.amritacafe.model.OrderAdapter
import edu.amrita.amritacafe.settings.Configuration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

fun String.capitalizeWords(): String =
    split(" ").map { it.lowercase().replaceFirstChar { it.uppercase() } }.joinToString(" ")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sheetsMenu: ArrayList<MenuItemUS>
    private var myToast: Toast? = null
    private var modeAmritapuri: Boolean = false
    private lateinit var currentHistoryFile: File
    private var dialogOpen = false

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var configuration: Configuration
    private var currentOrderNumber = 0
    private var currentOrderSum = 0f
    var received = 0f
    var currentTotalCost = 0f

    private lateinit var BREAKFAST_FILE : File //breakfast file moved here.

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        modeAmritapuri = BuildConfig.FLAVOR == "amritapuri"
        println("Build flavor: ${BuildConfig.FLAVOR}, is amritapuri: $modeAmritapuri")

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        configuration = Configuration(pref)

        supportActionBar?.hide()

        orderAdapter = OrderAdapter(this)
        orderAdapter.orderChanged = {
            currentOrderSum = orderAdapter.orderItems.map { it.finalPrice }.sum()
            binding.totalCostTV.text = currentOrderSum.toString()
        }

        binding.orderListView.adapter = orderAdapter

        binding.orderButton.setOnClickListener {
            val order = Order(currentOrderNumber, orderAdapter.orderItems.toList())
            openPaymentDialog()
        }

        menuAdapter = MenuAdapter(emptyList(), applicationContext) { menuItem ->
            println("PRESSED2")
            openEnterWeightDialog(menuItem)
        }

        val columns = resources.getInteger(R.integer.columns)
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, columns)
        binding.menuRecyclerView.adapter = menuAdapter

        println("STARTED?")
        setAmritapuriMode()
    }

    private fun openEnterWeightDialog(menuItem: Any?) {
        println("PRESSED " + menuItem)
        when (menuItem) {
            is MenuItemUS -> {
                val (dialog, root) = LayoutInflater.from(this).inflate(R.layout.dialog_enter_weight, null)
                    .let { view ->
                        AlertDialog.Builder(this)
                            .setView(view)
                            .setCancelable(true)
                            .show()
                            .to(view)
                    }

                val enterWeightBinding = DialogEnterWeightBinding.bind(root)
                enterWeightBinding.amritapuriButton.setOnClickListener {
                    try {
                        val toFloat = enterWeightBinding.weightET.text.toString().toFloat()
                        orderAdapter.add(menuItem, toFloat)
                        dialog.dismiss()
                    } catch (e: Exception) {
                        // Handle the exception
                    }
                }
            }
        }
//        val (dialog, root) = LayoutInflater.from(this).inflate(R.layout.dialog_enter_weight, null)
//            .let { view ->
//                AlertDialog.Builder(this)
//                    .setView(view)
//                    .setCancelable(true)
//                    .show()
//                    .to(view)
//            }
//
//        val enterWeightBinding = DialogEnterWeightBinding.bind(root)
//        enterWeightBinding.amritapuriButton.setOnClickListener {
//            try {
//                val toFloat = enterWeightBinding.weightET.text.toString().toFloat()
//                orderAdapter.add(menuItem, toFloat)
//                dialog.dismiss()
//            } catch (e: Exception) {
//                // Handle the exception
//            }
//        }
    }


    private val orderHistory = mutableListOf<Order>()

    private fun storeOrders(order: Order) {
        orderHistory.add(order)
    }

    private fun setMenuAdapter(menu: List<MenuItemUS>) {
        // Update the menuAdapter with the new menu items
        menuAdapter = MenuAdapter(menu, applicationContext) { menuItem ->
            println("PRESSED2")
            openEnterWeightDialog(menuItem)
        }
//        menuAdapter = MenuAdapter(menu, applicationContext) {
//            runOnUiThread { menuAdapter.notifyDataSetChanged() }
//        }
        runOnUiThread { binding.menuRecyclerView.adapter = menuAdapter }
    }

    private fun makeToast(text: String) {
        myToast?.cancel()
        myToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        myToast?.show()
    }

    private fun openPaymentDialog() {
        val (dialog, root) = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null)
            .let { view ->
                AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(true)
                    .show()
                    .to(view)
            }

        val paymentBinding = DialogPaymentBinding.bind(root)
        paymentBinding.cashReceivedButton.setOnClickListener {
            startNewOrder()
            dialog.dismiss()
        }
        paymentBinding.creditReceivedButton.setOnClickListener {
            dialog.dismiss()
        }

        received = 0f
        currentTotalCost = orderAdapter.orderItems.map { it.finalPrice }.sum()
        paymentBinding.toPayTV.text = currentTotalCost.toString()

        val onClickReceivedListener = View.OnClickListener { billButton ->
            billButton as Button
            when (billButton.text) {
                "500₹" -> received += 500
                "200₹" -> received += 200
                "100₹" -> received += 100
                "50₹" -> received += 50
                "20₹" -> received += 20
                "10₹" -> received += 10
                "5₹" -> received += 5f
                "1₹" -> received += 1f
                "clear" -> received = 0f
            }
            paymentBinding.receivedTV.text = received.toString()
            paymentBinding.toReturnTV.text = (received - currentTotalCost).toString()
        }

        paymentBinding.received100Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received50Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received20Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received10Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received5Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received1Button.setOnClickListener(onClickReceivedListener)
        paymentBinding.received25centButton.setOnClickListener(onClickReceivedListener)
        paymentBinding.clearReceivedButton.setOnClickListener(onClickReceivedListener)
        paymentBinding.creditReceivedButton.setOnClickListener(onClickReceivedListener)
    }

    override fun onResume() {
        super.onResume()
        loadAmritapuriMenu()
    }

    fun loadAmritapuriMenu() {
        val list = getListOfMenu(BREAKFAST_FILE) //call the function with the file.
        setMenuAdapter(list)
    }

    private fun setAmritapuriMode() {
        modeAmritapuri = true
        binding.orderButton.text = getString(R.string.order_string)

        createDefualtFilesIfNecessary(this)

        val dir = File(filesDir, "Menus")
        BREAKFAST_FILE = File(dir, "Breakfast.txt") //initialize the file after context is available.

        loadAmritapuriMenu() // will load on resume
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        println("dialog open: $dialogOpen")
        if (!dialogOpen) {
            finish()
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun startNewOrder() {
        GlobalScope.launch {
            println("New order, printMode: $modeAmritapuri")
            currentOrderNumber++

            val order = Order(currentOrderNumber, orderAdapter.orderItems.toList())
            storeOrders(order)

            runOnUiThread {
                orderAdapter.clear()
                binding.orderNumberTV.text = currentOrderNumber.toString()
            }
        }
    }

    private fun lastItemCostMultiplier(percentCost: Float) {
        orderAdapter.lastItemCostMultiplier(percentCost)
    }

    @SuppressLint("InflateParams")
    private fun printerDialog() =
        LayoutInflater.from(this).inflate(R.layout.dialog_print, null).let { view ->
            AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .show()
                .apply {
                    setCanceledOnTouchOutside(false)
                }.to(view)
        }

    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun openHistoryDialog(historyButton: View) {
        makeToast("So many old orders: ${orderHistory.size}")
        val (dialog, root) = LayoutInflater.from(this).inflate(R.layout.dialog_history, null)
            .let { view ->
                AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(true)
                    .show()
                    .to(view)
            }

        val historyBinding = DialogHistoryBinding.bind(root)
        historyBinding.historyRV.layoutManager = LinearLayoutManager(this)
        val historyAdapter = HistoryAdapter(orderHistory)
        historyBinding.historyRV.adapter = historyAdapter
    }
}
