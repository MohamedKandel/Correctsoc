package com.correct.correctsoc.ui.home

import android.content.Context
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
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.mappingNumbers


class AboutFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentAboutBinding
    private lateinit var helper: HelperClass
    private var sourceLayout = 0
    private lateinit var fragmentListener: FragmentChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.aboutFragment)

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

        val version = if (helper.getLang(requireContext()).equals("ar")) {
            helper.getAppVersion(requireContext()).mappingNumbers()
        } else {
            helper.getAppVersion(requireContext())
        }
        binding.txtVersion.append(" : $version")

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

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.aboutFragment)
    }
}