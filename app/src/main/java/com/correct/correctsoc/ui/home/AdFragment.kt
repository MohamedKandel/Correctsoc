package com.correct.correctsoc.ui.home

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.correct.correctsoc.R
import com.correct.correctsoc.data.AdsModel
import com.correct.correctsoc.databinding.FragmentAdBinding
import com.correct.correctsoc.helper.Constants.AD_OBJECT
import com.correct.correctsoc.helper.HelperClass

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
                arguments?.getParcelable(AD_OBJECT, AdsModel::class.java)!!
            } else {
                arguments?.getParcelable(AD_OBJECT)!!
            }
            Glide.with(requireContext())
                .load(model.img)
                .placeholder(R.drawable.correct)
                .into(binding.imgAd)

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