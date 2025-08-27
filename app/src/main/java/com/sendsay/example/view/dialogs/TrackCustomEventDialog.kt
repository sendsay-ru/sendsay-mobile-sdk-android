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
import com.sendsay.sdk.models.PropertiesList


class TrackCustomEventDialog : DialogFragment() {

    private lateinit var onConfirmed: (eventName: String, properties: PropertiesList) -> Unit
    private val propsMap = hashMapOf(
        "property" to "some value" as Any,
        "cce" to """{ "cce-key": "test-value-CCE" }""".trimIndent() as Any
    )
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

        const val TAG = "TrackCustomEventDialog"

        fun show(
            fragmentManager: FragmentManager,
            callback: (eventName: String, properties: PropertiesList) -> (Unit)
        ) {
            val fragment = fragmentManager.findFragmentByTag(TAG)
                    as? TrackCustomEventDialog
                ?: TrackCustomEventDialog()

            fragment.onConfirmed = callback
            fragment.show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context, R.style.MyDialogTheme)
        val inflate = LayoutInflater.from(context)
        val view = inflate.inflate(R.layout.dialog_track_custom_event, null, false)
        builder.setView(view)
        initListeners(view)
        return builder.create()
    }

    private fun initListeners(view: View) {
        val eventName: EditText = view.findViewById(R.id.editTextEventName)
        val propsTextView: TextView = view.findViewById(R.id.textViewProperties)
        val propName: EditText = view.findViewById(R.id.editTextPropName)
        val propValue: EditText = view.findViewById(R.id.editTextValue)

        val spinnerMode: Spinner = view.findViewById(R.id.spinner)
        val switchLayout: LinearLayout = view.findViewById(R.id.switchLayout)
        val switchWidget: SwitchCompat = view.findViewById(R.id.switchWidget)

        val buttonAddProperty: Button = view.findViewById<Button>(R.id.buttonAddProperty)
        val buttonTrack: Button = view.findViewById<Button>(R.id.buttonTrack)

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

        propsTextView.text = propsMap.asJson()

        buttonAddProperty.setOnClickListener {
            if (!propValue.text.isEmpty() && !propName.text.isEmpty()) {
                val isCopy = if (switchWidget.isChecked) ".copy" else ""
                datakey.add(
                    listOf<String>(
                        propName.text.toString(),
                        "${spinnerMode.selectedItem}$isCopy",
                        propValue.text.toString()
                    )
                )
                Log.d("DATAKEY", datakey.joinToString())
                member_set["datakey"] = datakey
                propsMap["member_set"] = member_set
                Log.d(TAG, propsMap.toString())
                propsTextView.text = propsMap.asJson()
            }
        }

        buttonTrack.setOnClickListener {
            val name = eventName.text.toString()
            val properties = PropertiesList(propsMap)
            onConfirmed(name, properties)
            dismiss()
        }
    }
}
