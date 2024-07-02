package com.correct.correctsoc.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentAboutBinding
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.HelperClass


class AboutFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentAboutBinding
    private lateinit var helper: HelperClass
    private var sourceLayout = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        binding.btnBack.setOnClickListener {
            if (arguments != null) {
                val source = requireArguments().getInt(Constants.SOURCE, 0)
                if (source != 0) {
                    findNavController().navigate(source, requireArguments())
                    sourceLayout = source
                } else {
                    findNavController().navigate(R.id.settingFragment)
                }
            }
        }

        binding.txtVersion.append(" : ${helper.getAppVersion(requireContext())}")

        if (sourceLayout != 0) {
            onBackPressed(sourceLayout, requireArguments())
        } else {
            onBackPressed(R.id.settingFragment, null)
        }

        val url = ""
        val facebook = ""
        val insta = ""

        binding.btnFacebook.setOnClickListener {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(facebook)
            )
            startActivity(urlIntent)
        }

        binding.btnInsta.setOnClickListener {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(insta)
            )
            startActivity(urlIntent)
        }

        binding.btnInternet.setOnClickListener {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            startActivity(urlIntent)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    private fun onBackPressed(layoutRes: Int, bundle: Bundle?) {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (arguments != null) {
                        val source = requireArguments().getInt(Constants.SOURCE, 0)
                        if (source != 0) {
                            findNavController().navigate(layoutRes, bundle)
                        } else {
                            findNavController().navigate(R.id.settingFragment)
                        }
                    }
                }
            })
    }
}