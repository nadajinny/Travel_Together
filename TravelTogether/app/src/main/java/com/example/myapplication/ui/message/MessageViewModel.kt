package com.example.myapplication.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageViewModel : ViewModel(){

    private val _text = MutableLiveData<String>().apply {
        value = "This is message Fragmentd" +
                ""
    }
    val text: LiveData<String> = _text

    private lateinit var datas: MutableList<MutableMap<String, String>>
    private lateinit var simpleAdapter: SimpleAdapter
   fun chatListView(){
       datas= mutableListOf<MutableMap<String,String>>()
   }

}