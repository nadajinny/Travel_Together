package com.example.myapplication.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityChatMainBinding
import com.example.myapplication.databinding.FragmentMessageBinding

class ActivityChatMainFragment : Fragment(){
    private lateinit var MessageViewModel : MessageViewModel
    private var _binding: ActivityChatMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityChatMainBinding.inflate(inflater, container, false)
        return binding.root
    }


}