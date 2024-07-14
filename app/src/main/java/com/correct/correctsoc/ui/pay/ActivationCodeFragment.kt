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
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentActivationCodeBinding
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.appendFilter

class ActivationCodeFragment : Fragment() {

    private lateinit var binding: FragmentActivationCodeBinding
    private lateinit var helper: HelperClass
    private var deletePressed = false
    private lateinit var fragmentListener: FragmentChangedListener

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

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.activationCodeFragment)
    }
}