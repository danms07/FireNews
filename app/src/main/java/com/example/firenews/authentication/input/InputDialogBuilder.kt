package com.example.firenews.authentication.input

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.firenews.databinding.InputDialogBinding

class InputDialogBuilder(context: Context, private val dialogContent: DialogContent) {

    private val builder=AlertDialog.Builder(context)
    private val inflater: LayoutInflater =LayoutInflater.from(context)

    private val binding=InputDialogBinding.inflate(inflater).apply {
        iconImageView.setImageResource(dialogContent.iconId)
        instructionsTextView.setText(dialogContent.instructionsId)
        dataInputEditText.inputType=dialogContent.inputType
        dataInputEditText.setAutofillHints(dialogContent.autoFillHints)
        dataInputEditText.setHint(dialogContent.inputHint)
    }
    private var onDataInputListener: OnDataInputListener?=null

    private var onInputCancelledListener: OnInputCancelledListener?=null


    /*fun setIcon(resourceId: Int=R.mipmap.app_icon){
        binding.iconImageView.setImageResource(resourceId)
        binding.iconImageView.visibility= View.VISIBLE
    }

    fun setInstructionMessage(resourceId:Int){
        binding.instructionsTextView.setText(resourceId)
    }

    fun setInputType(inputType:Int=InputType.TYPE_CLASS_TEXT){
        binding.dataInputEditText.inputType=inputType

        val autoFillHints=when(inputType){
            InputType.TYPE_CLASS_PHONE ->View.AUTOFILL_HINT_PHONE
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS->View.AUTOFILL_HINT_EMAIL_ADDRESS
            else ->null
        }
        autoFillHints?.let {
            binding.dataInputEditText.setAutofillHints(it)
        }
    }

    fun setInputHint(resourceId:Int){
        binding.dataInputEditText.setHint(resourceId)
    }
*/
    fun build():AlertDialog{
        val dialog=builder
            .setView(binding.root)
            .setCancelable(false)
            .create()

        binding.confirmInputButton.setOnClickListener {
            val input=binding.dataInputEditText.text.toString()
            onDataInputListener?.onDataInserted(input)
            dialog.dismiss()
        }

        binding.cancelInputButton.setOnClickListener {
            onInputCancelledListener?.onInputCancelled()
            dialog.dismiss()
        }
        return dialog
    }

    fun setOnDataInputListener(listener:(String)->Unit): InputDialogBuilder {
        onDataInputListener=object: OnDataInputListener {
            override fun onDataInserted(input: String) {
                listener(input)

            }
        }
        return this
    }

    fun setOnInputCancelledListener(listener:()->Unit): InputDialogBuilder {
        onInputCancelledListener=object: OnInputCancelledListener {
            override fun onInputCancelled() {
                listener()
            }
        }
        return this
    }

    interface OnDataInputListener{
        fun onDataInserted(input:String)
    }

    interface OnInputCancelledListener{
        fun onInputCancelled()
    }

}