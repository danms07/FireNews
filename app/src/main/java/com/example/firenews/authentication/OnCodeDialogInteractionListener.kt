package com.example.firenews.authentication

import android.content.DialogInterface

interface OnCodeDialogInteractionListener {
    fun onCodeInput(verificationId:String, verificationCode:String, dialogInterface:DialogInterface)
    fun onPhoneVerificationCancelled()
}