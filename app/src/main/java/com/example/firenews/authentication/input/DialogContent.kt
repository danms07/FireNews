package com.example.firenews.authentication.input

import android.text.InputType
import android.view.View
import com.example.firenews.R

sealed class DialogContent(val iconId:Int, val instructionsId:Int, val inputType:Int,val inputHint:Int){
    val autoFillHints=when(inputType){
        InputType.TYPE_CLASS_PHONE -> View.AUTOFILL_HINT_PHONE
        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS-> View.AUTOFILL_HINT_EMAIL_ADDRESS
        else ->null
    }

    class PhoneNumberContent(): DialogContent(
        R.drawable.baseline_local_phone_64,
        R.string.phone_number_instructions,
        InputType.TYPE_CLASS_PHONE,
        R.string.phone_number_hint
    )

    class PhoneCodeContent(val verificationId:String): DialogContent(
        R.drawable.baseline_sms_64,
        R.string.phone_code_instructions,
        InputType.TYPE_CLASS_NUMBER,
        R.string.verification_code_hint
    )

    class EmailContent(): DialogContent(
        R.drawable.baseline_email_64,
        R.string.phone_code_instructions,
        InputType.TYPE_CLASS_NUMBER,
        R.string.verification_code_hint
    )
}

