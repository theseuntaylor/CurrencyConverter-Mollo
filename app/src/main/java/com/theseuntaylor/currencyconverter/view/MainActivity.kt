package com.theseuntaylor.currencyconverter.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.google.android.material.snackbar.Snackbar
import com.theseuntaylor.currencyconverter.R
import com.theseuntaylor.currencyconverter.utils.Resource
import com.theseuntaylor.currencyconverter.utils.Utils
import com.theseuntaylor.currencyconverter.viewModel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    val tag: String = "MainActivity TAG"

    lateinit var viewModel: MainActivityViewModel

    private lateinit var toEditText: EditText
    private lateinit var toSpinner: Spinner
    private lateinit var toTextView: TextView
    private lateinit var detailsTextView: TextView

    private lateinit var fromEditText: EditText
    private lateinit var fromSpinner: Spinner
    private lateinit var fromTextView: TextView

    private lateinit var swapButton: ImageButton
    private lateinit var convertButton: CircularProgressButton

    private var bottomCurrency = ""
    private var topCurrency = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this@MainActivity).get(MainActivityViewModel::class.java)

        findViews()
        setViews()

        if (viewModel.currencies.isEmpty()) {
            viewModel.getCurrencies()?.observe(
                this@MainActivity
            ) {
                viewModel.currencies.addAll(it)
                viewModel.currencies.sort()

                setUpSpinners()
            }

        } else setUpSpinners()

        swapViews(swapButton, fromSpinner, toSpinner)

        convertButton.setOnClickListener {

            if (topCurrency.isEmpty() || bottomCurrency.isEmpty()) {

                val snackBar =
                    Snackbar.make(it, "Just hold on a bit. :)", Snackbar.LENGTH_LONG)
                val snackBarView = snackBar.view

                val textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.background_color))
                snackBar.show()

                return@setOnClickListener
            }

            val fromValue = fromEditText.text.toString()

            if (fromValue.isEmpty()) {

                val snackBar =
                    Snackbar.make(it, "Please put in a value (e.g. 1.6180)", Snackbar.LENGTH_LONG)

                val snackBarView = snackBar.view

                val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)

                textView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.background_color))

                snackBar.show()


            } else {
                convertButton.startMorphAnimation()
                convertCurrencies(fromValue)
            }
        }
    }


    private fun setUpSpinners() {

        val currencyAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@MainActivity,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.currencies
        )

        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fromSpinner.adapter = currencyAdapter
        toSpinner.adapter = currencyAdapter

    }

    private fun setViews() {
        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                toTextView.text = ""
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val text = parent?.getItemAtPosition(position).toString()
                toTextView.text = text
                bottomCurrency = text
            }
        }

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                fromTextView.text = ""
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val text = parent?.getItemAtPosition(position).toString()
                fromTextView.text = text
                topCurrency = text


                Log.e(tag, "onItemSelected: item selected is $text", null)
                try {
                    Log.e(
                        tag,
                        "onItemSelected: item selected number is ${parent?.getPositionForView(view)}",
                        null
                    )
                } catch (e: Exception) {
                    Log.e(tag, "onItemSelected: $e", null)
                }
            }
        }
    }

    private fun convertCurrencies(fromValue: String) {

        val fromValueToDouble = fromValue.toDouble()

        viewModel
            .getConvertedCurrencies(topCurrency, bottomCurrency, fromValueToDouble)
            .observe(this@MainActivity) {

                when (it.status) {
                    Resource.Status.LOADING -> convertButton.startMorphAnimation()

                    Resource.Status.SUCCESS -> {
                        convertButton.revertAnimation()
                        toEditText.setText(it.data?.result.toString())
                        detailsTextView.text = getString(
                            R.string.concat_conversion_details,
                            it.data?.info?.rate.toString(),
                            it.data?.query?.toCurrency,
                            it.data?.query?.fromCurrency
                        )
                    }

                    Resource.Status.ERROR -> {
                        convertButton.revertAnimation()
                        Utils.showToast("${it.data?.success}", this@MainActivity)
                    }
                }
            }
    }

    private fun swapViews(swapButtons: ImageButton, fromSpinner: Spinner, toSpinner: Spinner) {

        swapButtons.setOnClickListener {
            val item1: Int = fromSpinner.selectedItemPosition
            val item2: Int = toSpinner.selectedItemPosition

            toSpinner.setSelection(item1)

            fromSpinner.setSelection(item2)

        }
    }

    private fun findViews() {
        detailsTextView = findViewById(R.id.conversion_details_textView)
        toEditText = findViewById(R.id.currency_to_editText)
        fromEditText = findViewById(R.id.currency_from_editText)
        convertButton = findViewById(R.id.convert_button)
        toSpinner = findViewById(R.id.toSpinner)
        toTextView = findViewById(R.id.currency_to_textView)
        fromSpinner = findViewById(R.id.fromSpinner)
        fromTextView = findViewById(R.id.currency_from_textView)
        swapButton = findViewById(R.id.swap_spinners_button)
    }

}