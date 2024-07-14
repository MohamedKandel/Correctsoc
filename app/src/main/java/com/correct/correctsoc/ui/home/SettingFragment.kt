package com.correct.correctsoc.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentSettingBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass


class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSettingBinding
    private var sourceFragment = 0
    private lateinit var helper: HelperClass
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
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.settingFragment)

        /*if (arguments != null) {
            sourceFragment = requireArguments().getInt(SOURCE, 0)
            if (sourceFragment != 0) {
                binding.btnBack.setOnClickListener {
                    findNavController().navigate(sourceFragment, requireArguments())
                }
            }
        } else {
            sourceFragment = R.id.homeFragment
            binding.btnBack.setOnClickListener {
                findNavController().navigate(sourceFragment)
            }
        }
        if (sourceFragment != R.id.homeFragment) {
//            helper.onBackPressed(sourceFragment, requireArguments())
            helper.onBackPressed(this) {
                findNavController().navigate(sourceFragment, requireArguments())
            }
        } else {
            helper.onBackPressed(this) {
                findNavController().navigate(R.id.homeFragment)
            }
            //onBackPressed(R.id.homeFragment, null)
        }*/

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.detailsLayout.setOnClickListener {
            findNavController().navigate(R.id.editInfoFragment)
        }

        binding.passwordLayout.setOnClickListener {
            findNavController().navigate(R.id.updatePasswordFragment)
        }

        binding.aboutLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.settingFragment)
            findNavController().navigate(R.id.aboutFragment, bundle)
        }

        binding.languageLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.settingFragment)
            findNavController().navigate(R.id.langFragment, bundle)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    /*private fun onBackPressed(layoutID: Int, bundle: Bundle?) {
        (activity as AppCompatActivity).supportFragmentManager
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity() /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back is pressed... Finishing the activity
                    findNavController().navigate(layoutID, bundle)
                }
            })
    }*/
    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.settingFragment)
    }

}