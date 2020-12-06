package edu.amrita.amritacafe.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.amrita.amritacafe.BuildConfig
import edu.amrita.amritacafe.R
import edu.amrita.amritacafe.menu.BREAKFAST_FILE
import edu.amrita.amritacafe.menu.MenuItemUS
import edu.amrita.amritacafe.menu.createDefualtFilesIfNecessary
import edu.amrita.amritacafe.menu.getListOfMenu
import edu.amrita.amritacafe.model.MenuAdapter
import edu.amrita.amritacafe.model.Order
import edu.amrita.amritacafe.model.OrderAdapter
import edu.amrita.amritacafe.settings.Configuration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_history.view.*
import kotlinx.android.synthetic.main.dialog_login.view.*
import kotlinx.android.synthetic.main.dialog_payment.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

fun String.capitalizeWords(): String =
    split(" ").map { it.toLowerCase().capitalize() }.joinToString(" ")

class MainActivity : AppCompatActivity() {
    private lateinit var sheetsMenu: ArrayList<MenuItemUS>
    private var myToast: Toast? = null
    private lateinit var tabletName: String
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        modeAmritapuri = BuildConfig.FLAVOR == "amritapuri"
        println("Build flavor: ${BuildConfig.FLAVOR}, is amritapuri: $modeAmritapuri")

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.let { preferences ->
            configuration = Configuration(preferences)
        }

        supportActionBar?.hide()

//        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        val androidId =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        println("Unique device: $androidId")
        tabletName = when (androidId) {
            "f6ec19ab2b07a2f2" -> "Siva"
            "cb41899147fbbee7" -> "Amma"
            "3ebd272118138401" -> "Kali"
            "9dc83032a79a71a8" -> "Krishna"
            "c001d62ed3579582" -> "Shani"
            else -> androidId
        }
        user_TV.text = "$tabletName"

        orderAdapter = OrderAdapter(this)
        orderAdapter.orderChanged = {
            currentOrderSum = orderAdapter.orderItems.map { it.finalPrice }.sum()
            total_cost_TV.text = currentOrderSum.toString()
        }

        order_ListView.adapter = orderAdapter

        order_button.setOnClickListener {
            val order = Order(currentOrderNumber, orderAdapter.orderItems.toList())

            openPaymentDialog()
        }

        menuGridView.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            when (val menuItem = view.tag) {
                is MenuItemUS -> {
                    val (dialog, root) =
                        LayoutInflater.from(this).inflate(R.layout.dialog_enter_weight, null)
                            .let { view ->
                                AlertDialog.Builder(this)
                                    .setView(view)
                                    .setCancelable(true)
                                    .show()
                                    .to(view)
                            }

                    root.amritapuri_button.setOnClickListener {
                        try {
                            val toFloat = root.weight_ET.text.toString().toFloat()
                            orderAdapter.add(menuItem, toFloat)
                            dialog.dismiss()
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }

        println("STARTED?")

        setAmritapuriMode()
    }

    private val orderHistory = mutableListOf<Order>()

    private fun storeOrders(order: Order) {
        orderHistory.add(order)
    }

    private fun setMenuAdapter(menu: List<MenuItemUS>) {
        menuAdapter =
            MenuAdapter(menu, applicationContext) {
                runOnUiThread { menuAdapter.notifyDataSetChanged() }
            }
        runOnUiThread { menuGridView.adapter = menuAdapter }
    }

    private fun makeToast(text: String) {
        myToast?.cancel()
        myToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        myToast?.show()
    }

    private fun openPaymentDialog() {
        val (dialog, root) =
            LayoutInflater.from(this).inflate(R.layout.dialog_payment, null).let { view ->
                AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(true)
                    .show()
                    .to(view)
            }

        root.cash_received_button.setOnClickListener {
//            finishOrder(true)
            startNewOrder()
            dialog.dismiss()
        }
        root.credit_received_button.setOnClickListener {
//            finishOrder(false)
            dialog.dismiss()
        }

        received = 0f
        currentTotalCost = orderAdapter.orderItems.map { it.finalPrice }.sum()
        root.to_pay_TV.text = currentTotalCost.toString()
        val onClickRecivedListener = View.OnClickListener { billButton ->
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
            root.received_TV.text = received.toString()
            root.to_return_TV.text = (received - currentTotalCost).toString()
        }
        root.received_100_button.setOnClickListener(onClickRecivedListener)
        root.received_50_button.setOnClickListener(onClickRecivedListener)
        root.received_20_button.setOnClickListener(onClickRecivedListener)
        root.received_10_button.setOnClickListener(onClickRecivedListener)
        root.received_5_button.setOnClickListener(onClickRecivedListener)
        root.received_1_button.setOnClickListener(onClickRecivedListener)
        root.received_25cent_button.setOnClickListener(onClickRecivedListener)
        root.clear_received_button.setOnClickListener(onClickRecivedListener)
        root.credit_received_button.setOnClickListener(onClickRecivedListener)
    }

    override fun onResume() {
        super.onResume()
        loadAmritapuriMenu()
    }

    fun loadAmritapuriMenu() {
        val file = BREAKFAST_FILE
        val list = getListOfMenu(file)
        setMenuAdapter(list)
    }

    private fun setAmritapuriMode() {
        modeAmritapuri = true
        order_button.text = getString(R.string.order_string)
        user_TV.text = getString(R.string.amritapuri_at) + tabletName

        createDefualtFilesIfNecessary()
        loadAmritapuriMenu() //will load on resume

//        startNewOrder()
    }

    override fun onBackPressed() {
        println("dialog open: $dialogOpen")
        if (!dialogOpen) {
            finish()
        }
    }


    private fun isOnline(): Boolean {
        val cm =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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
                order_number_TV.text = currentOrderNumber.toString()
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
//                .setIcon(R.drawable.ic_print_black_24dp)
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
        val (dialog, root) =
            LayoutInflater.from(this).inflate(R.layout.dialog_history, null).let { view ->
                AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(true)
                    .show()
                    .to(view)
            }

        root.history_RV.layoutManager = LinearLayoutManager(this)
        val historyAdapter = HistoryAdapter(orderHistory)
        root.history_RV.adapter = historyAdapter
    }
}