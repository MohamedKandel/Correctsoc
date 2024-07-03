package com.correct.correctsoc.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.databinding.FragmentSignUpBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.HelperClass

class SignUpFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0
    private var source = -1
    private lateinit var usersDB: UsersDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        val text = resources
            .getString(R.string.already_have_account)
            .replace("\n", " ")

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
        }

        if (helper.getLang(requireContext()).equals("en")) {
            startIndx = 23
            //endIndx = 30
        } else {
            startIndx = 20
            //endIndx = 32
        }
        endIndx = text.length

        val span = helper.setSpannable(
            startIndx,
            endIndx,
            text,
            resources.getColor(R.color.white, context?.theme)
        ) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.signUpFragment)
            findNavController().navigate(R.id.loginFragment, bundle)
        }

        binding.txtLogin.text = span
        binding.txtLogin.movementMethod = LinkMovementMethod.getInstance()

        // accept + in first on phone number only
        binding.txtPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789+")
                } else {
                    if (s.toString().startsWith("+")) {
                        binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
                    } else {
                        binding.txtPhone.keyListener = DigitsKeyListener.getInstance("0123456789")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.registerBtn.setOnClickListener {
            if (helper.isEmpty(
                    binding.txtPhone,
                    binding.txtPassword,
                    binding.txtCode,
                    binding.txtName
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.required_data),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // send data to server here

            }
        }

        helper.onBackPressed(this) {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.registerFragment)
            }
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            if (source != -1) {
                findNavController().navigate(source)
            } else {
                findNavController().navigate(R.id.registerFragment)
            }
        }

        return binding.root
    }
}