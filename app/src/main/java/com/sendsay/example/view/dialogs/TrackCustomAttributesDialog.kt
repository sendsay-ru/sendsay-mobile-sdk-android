package com.sendsay.example.view.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.sendsay.example.R
import com.sendsay.example.utils.asJson

class TrackCustomAttributesDialog : DialogFragment() {

    private lateinit var onUpdate: (HashMap<String, Any>) -> Unit
    private val attributes = hashMapOf<String, Any>("cce" to """{test: test-CCE}""" as Any)
    private val member_set = mutableMapOf<String, Any>()
    private val datakey = arrayListOf<List<String>>()

    private val spinnerList = listOf<String>(
        "set",
        "update",
        "insert",
        "merge",
        "merge_update",
        "merge_insert",
        "push",
        "unshift",
        "delete"
    )

    companion object {
        const val TAG = "TrackCustomAttributesDialog"

        fun show(fragmentManager: FragmentManager, onUpdate: (HashMap<String, Any>) -> Unit) {
            val fragment = fragmentManager.findFragmentByTag(TAG)
                    as? TrackCustomAttributesDialog ?: TrackCustomAttributesDialog()

            fragment.onUpdate = onUpdate
            fragment.show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_track_custom_attributes, null, false)
        builder.setView(view)
        initListeners(view)
        return builder.create()
    }

    private fun initListeners(view: View) {
        val idsRegisteredView: EditText = view.findViewById(R.id.idsTextView)
        val valTextView: EditText = view.findViewById(R.id.valTextView)
        val propertiesView: TextView = view.findViewById<TextView>(R.id.textViewAttributes)

        val keyView: EditText = view.findViewById<EditText>(R.id.editTextPropName)
        val valueView: EditText = view.findViewById<EditText>(R.id.editTextValue)

        val spinnerMode: Spinner = view.findViewById(R.id.spinner)
        val switchLayout: LinearLayout = view.findViewById(R.id.switchLayout)
        val switchWidget: SwitchCompat = view.findViewById(R.id.switchWidget)

        val buttonAdd: Button = view.findViewById<Button>(R.id.buttonAddProperty)
        val buttonUpdate: Button = view.findViewById<Button>(R.id.buttonUpdate)

        idsRegisteredView.setText("registred")

        val spinnerAdapter: ArrayAdapter<String> =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                spinnerList
            )
        spinnerMode.adapter = spinnerAdapter

        switchLayout.setOnClickListener {
            switchWidget.isChecked = !switchWidget.isChecked
        }

        propertiesView.text = attributes.asJson()

        buttonAdd.setOnClickListener {
            if (!keyView.text.isEmpty() && !valueView.text.isEmpty()) {
                val isCopy = if (switchWidget.isChecked) ".copy" else ""
                datakey.add(
                    listOf<String>(
                        keyView.text.toString(),
                        "${spinnerMode.selectedItem}$isCopy",
                        valueView.text.toString()
                    )
                )
                Log.d("DATAKEY", datakey.joinToString())
                member_set["datakey"] = datakey
                attributes["member_set"] = member_set
                Log.d(TAG, attributes.toString())
                propertiesView.text = attributes.asJson()
            }
        }
//        buttonAdd.setOnClickListener {
//            if (!nameView.text.isEmpty() && !valueView.text.isEmpty()) {
//                attributes[nameView.text.toString()] = valueView.text.toString()
//                propertiesView.text = attributes.asJson()
//            }
//        }

        buttonUpdate.setOnClickListener {
            if (!idsRegisteredView.text.isEmpty() && !valTextView.text.isEmpty()) {
                val properties = attributes
                onUpdate(properties)
                dismiss()
            }
        }
    }
}
