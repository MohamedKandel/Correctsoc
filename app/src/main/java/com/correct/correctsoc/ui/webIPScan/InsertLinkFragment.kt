package com.correct.correctsoc.ui.webIPScan

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentInsertLinkBinding
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.HelperClass

class InsertLinkFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentInsertLinkBinding
    private lateinit var helper: HelperClass

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentInsertLinkBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (arguments != null) {
            val source = requireArguments().getString(TYPE)
            if (source.equals(IP_ADDRESS)) {
                binding.txtTitle.text = resources.getString(R.string.ip_scan)
                binding.txtLink.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ip_scan_icon, 0, 0, 0
                )
                binding.txtLink.hint = resources.getString(R.string.enter_ip)
                binding.btnScan.setOnClickListener {
                    val input = binding.txtLink.text.toString()
                    if (input.isNotEmpty()) {
                        val bundle = Bundle()
                        val ip = input.trim()
                        bundle.putString(IP_ADDRESS, ip)
                        bundle.putInt(SOURCE, R.id.insertLinkFragment)
                        bundle.putString(TYPE, IP_ADDRESS)
                        findNavController().navigate(R.id.webScanFragment, bundle)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.invalid_ip),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Log.i("IP mohamed", "onCreateView: $input")
                }
            }
        } else {
            // web scan
            binding.txtLink.inputType = InputType.TYPE_CLASS_TEXT
            binding.txtLink.imeOptions = EditorInfo.IME_ACTION_DONE

            binding.txtLink.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    TODO("Not yet implemented")
                    if (!Patterns.WEB_URL.matcher(binding.txtLink.text.toString()).matches()) {
                        binding.txtLink.error = resources.getString(R.string.invalid_link)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
//                    TODO("Not yet implemented")
                }

            })

            binding.txtLink.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.link_icon, 0, 0, 0
            )
            binding.txtLink.hint = resources.getString(R.string.enter_link)
            binding.btnScan.setOnClickListener {
                val input = binding.txtLink.text.toString()
                if (input.isNotEmpty()) {
                    val bundle = Bundle()
                    val link = input.trim()
                    bundle.putString(IP_ADDRESS, link)
                    bundle.putInt(SOURCE, R.id.insertLinkFragment)
                    findNavController().navigate(R.id.webScanFragment, bundle)
                } else {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.invalid_link),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    /*private fun onBackPressed() {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeFragment)
                }
            })
    }*/
}