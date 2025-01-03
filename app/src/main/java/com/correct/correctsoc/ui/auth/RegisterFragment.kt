package com.correct.correctsoc.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentRegisterBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass

class RegisterFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0
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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.registerFragment)

/*        if (helper.getLang(requireContext()).equals("en")) {
//            startIndx = 23
//            endIndx = 30
//        } else {
//            startIndx = 20
//            endIndx = 32
//        }
//
//        val span = helper.setSpannable(
//            startIndx, endIndx, resources.getString(R.string.already_have_account),
//            resources.getColor(R.color.white, context?.theme)
//        ) {
//            val bundle = Bundle()
//            bundle.putInt(SOURCE, R.id.registerFragment)
//            findNavController().navigate(R.id.loginFragment, bundle)
//        }
//
//        binding.txtLogin.text = span
        binding.txtLogin.movementMethod = LinkMovementMethod.getInstance()*/

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }

        binding.btnLogin.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.registerFragment)
            findNavController().navigate(R.id.loginFragment, bundle)
        }

        helper.onBackPressed(this) {
            requireActivity().finish()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.registerFragment)
    }
}