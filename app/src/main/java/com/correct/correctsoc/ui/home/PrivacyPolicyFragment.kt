package com.correct.correctsoc.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentPrivacyPolicyBinding
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.readFile

class PrivacyPolicyFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyPolicyBinding
    private lateinit var helper: HelperClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
            binding.txtPrivacy.text = requireContext().readFile(R.raw.policy_ar)
        } else {
            binding.txtPrivacy.text = requireContext().readFile(R.raw.policy_en)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.settingFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.settingFragment)
        }

        return binding.root
    }
}