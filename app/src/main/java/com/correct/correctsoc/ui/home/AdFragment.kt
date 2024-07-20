package com.correct.correctsoc.ui.home

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.user.AdsResponse
import com.correct.correctsoc.data.user.AdsResult
import com.correct.correctsoc.databinding.FragmentAdBinding
import com.correct.correctsoc.helper.Constants.AD_OBJECT
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.parseBase64

class AdFragment : Fragment() {

    private lateinit var binding: FragmentAdBinding
    private lateinit var helper: HelperClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAdBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        if (arguments != null) {
            val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(AD_OBJECT, AdsResult::class.java)!!
            } else {
                arguments?.getParcelable(AD_OBJECT)!!
            }
            binding.imgAd.setImageBitmap(model.image.parseBase64())

            binding.txtAdDescription.text = model.description
            binding.txtAdTitle.text = model.title
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }
}