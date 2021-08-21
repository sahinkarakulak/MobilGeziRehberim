package com.mrcaracal.activity.contact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrcaracal.modul.UserAccountStore

class ContactViewModel : ViewModel() {

  var contactState: MutableLiveData<ContactViewState> = MutableLiveData<ContactViewState>()

  fun sendMessage(subject: String, message: String) {
    if (subject.isEmpty() || message.isEmpty()) {
      contactState.value = ContactViewState.ShowRequiredFieldsMessage
    } else {
      contactState.value = ContactViewState.OpenEmail(
        subject = subject,
        message = message,
        emails = UserAccountStore.adminAccountEmails
      )
    }
  }
}

sealed class ContactViewState {
  object ShowRequiredFieldsMessage : ContactViewState()
  data class OpenEmail(val subject: String, val message: String, val emails: ArrayList<String>) :
    ContactViewState()
}