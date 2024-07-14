package com.correct.correctsoc.ui.pay

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.SubscribeCodeBody
import com.correct.correctsoc.databinding.FragmentActivationCodeBinding
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.appendFilter
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class ActivationCodeFragment : Fragment() {

    private lateinit var binding: FragmentActivationCodeBinding
    private lateinit var helper: HelperClass
    private var deletePressed = false
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var viewModel: PayViewModel
    private lateinit var usersDB: UsersDB

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentActivationCodeBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[PayViewModel::class.java]

        fragmentListener.onFragmentChangedListener(R.id.activationCodeFragment)
        // make edit text accept uppercase letters only
        binding.txtActivationCode.appendFilter(InputFilter.AllCaps())

        val hyphenPositions = intArrayOf(8, 13, 18, 23)

        binding.txtActivationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deletePressed = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length in hyphenPositions) {
                    if (!deletePressed) {
                        // Append hyphen only if not deleting
                        binding.txtActivationCode.append("-")
                    } else {
                        // If user deletes a hyphen, re-insert it
                        val lastChar = s.toString().lastOrNull()
                        if (lastChar != null && lastChar != '-' && lastChar != ' ') {
                            // Delete the last character before appending hyphen
                            binding.txtActivationCode.text?.delete(s?.length?.minus(1)!!, s.length)
                        }
                    }
                }
            }
        })

        binding.btnDone.setOnClickListener {
            val code = binding.txtActivationCode.text.toString()
            if (code.isNotEmpty() && code.length == 39) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    if (phone.isNotEmpty()) {
                        val body = SubscribeCodeBody(
                            code = code,
                            deviceId = helper.getDeviceID(requireContext()),
                            phone = phone
                        )
                        subscribe(body)
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.invalid_code),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun subscribe(body: SubscribeCodeBody) {
        viewModel.subscribeWithCode(body)
        val observer = object : Observer<ForgotResponse> {
            override fun onChanged(value: ForgotResponse) {
                if (value.isSuccess) {
                    Log.v("subscription", "Subscribed successfully")
                }
                viewModel.subscribeWithCodeResponse.removeObserver(this)
            }
        }
        viewModel.subscribeWithCodeResponse.observe(viewLifecycleOwner, observer)
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.activationCodeFragment)
    }
}