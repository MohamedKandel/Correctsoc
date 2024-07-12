package com.correct.correctsoc.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.UpdatePasswordBody
import com.correct.correctsoc.databinding.FragmentUpdatePasswordBinding
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.add
import com.correct.correctsoc.helper.clear
import com.correct.correctsoc.helper.colorfulTextView
import com.correct.correctsoc.helper.contains
import com.correct.correctsoc.helper.isContainsLowerCase
import com.correct.correctsoc.helper.isContainsNumbers
import com.correct.correctsoc.helper.isContainsSpecialCharacter
import com.correct.correctsoc.helper.isContainsUpperCase
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.remove
import com.correct.correctsoc.helper.updateRequirements
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class UpdatePasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentUpdatePasswordBinding
    private lateinit var helper: HelperClass
    private var lenStrtIndx = 0
    private var lenEndIndx = 0
    private var upperStrtIndx = 0
    private var upperEndIndx = 0
    private var numberStrtIndx = 0
    private var numberEndIndx = 0
    private var lowerStrtIndx = 0
    private var lowerEndIndx = 0
    private var specialStrtIndx = 0
    private var specialEndIndx = 0
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.layoutPassInstructionsNew.root.visibility = View.GONE

        if (helper.getLang(requireContext()).equals("en")) {
            binding.layoutPassInstructionsNew.txtInstructionConfirm.text =
                resources.getString(R.string.password_instruction)

            upperStrtIndx = 68
            upperEndIndx = 88

            lowerStrtIndx = 91
            lowerEndIndx = 111

            numberStrtIndx = 56
            numberEndIndx = 66

            specialStrtIndx = 116
            specialEndIndx = resources.getString(R.string.password_instruction).length

            lenStrtIndx = 31
            lenEndIndx = 43
        } else {
            binding.layoutPassInstructionsNew.txtInstructionConfirm.text =
                resources.getString(R.string.password_instruction).mappingNumbers()

            upperStrtIndx = 58
            upperEndIndx = 66

            lowerStrtIndx = 68
            lowerEndIndx = 77

            numberStrtIndx = 53
            numberEndIndx = 56

            specialStrtIndx = 79
            specialEndIndx = resources.getString(R.string.password_instruction).length

            lenStrtIndx = 34
            lenEndIndx = 40
        }

        binding.txtNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    binding.layoutPassInstructionsNew.root.visibility = View.VISIBLE
                } else {
                    binding.layoutPassInstructionsNew.root.visibility = View.GONE
                }
                //updateRequirements(s.toString())
                if (binding.layoutPassInstructionsNew.txtInstructionConfirm
                    .updateRequirements(
                        s.toString(), arrayOf(
                            lenStrtIndx, numberStrtIndx,
                            upperStrtIndx, lowerStrtIndx, specialStrtIndx
                        ),
                        arrayOf(
                            lenEndIndx, numberEndIndx,
                            upperEndIndx, lowerEndIndx,
                            specialEndIndx
                        ),
                        resources.getString(R.string.password_instruction),
                        resources.getColor(R.color.safe_color, context?.theme),
                        resources.getColor(R.color.black, context?.theme)
                    )) {
                    binding.layoutPassInstructionsNew.root.visibility = View.GONE
                }
            }
        })

        binding.txtConfirmpassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    binding.layoutPassInstructionsConfirm.root.visibility = View.VISIBLE
                } else {
                    binding.layoutPassInstructionsConfirm.root.visibility = View.GONE
                }
                if (binding.layoutPassInstructionsConfirm.txtInstructionConfirm
                        .updateRequirements(
                            s.toString(), arrayOf(
                                lenStrtIndx, numberStrtIndx,
                                upperStrtIndx, lowerStrtIndx, specialStrtIndx
                            ),
                            arrayOf(
                                lenEndIndx, numberEndIndx,
                                upperEndIndx, lowerEndIndx,
                                specialEndIndx
                            ),
                            resources.getString(R.string.password_instruction),
                            resources.getColor(R.color.safe_color, context?.theme),
                            resources.getColor(R.color.black, context?.theme)
                        )
                ) {
                    binding.layoutPassInstructionsConfirm.root.visibility = View.GONE
                }
            }
        })

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.settingFragment)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.settingFragment)
        }

        binding.updateBtn.setOnClickListener {
            val current_password = binding.txtPassword.text.toString()
            val new_password = binding.txtNewPassword.text.toString()
            val confirm_password = binding.txtConfirmpassword.text.toString()
            lifecycleScope.launch {
                val id = usersDB.dao().getUserID() ?: ""
                val password = usersDB.dao().getPassword(id) ?: ""
                if (password.equals(current_password)) {
                    if (new_password.equals(confirm_password)) {
                        val body = UpdatePasswordBody(new_password, current_password, id)
                        updatePassword(body, helper.getToken(requireContext()))
                    } else {
                        Toast.makeText(
                            requireContext(), resources.getString(R.string.not_match),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        return binding.root
    }

    private fun updatePassword(body: UpdatePasswordBody, token: String) {
        viewModel.updatePassword(body, token)
        viewModel.updatePasswordResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    usersDB.dao().updatePassword(id, body.newPassword)
                    Toast.makeText(
                        requireContext(), resources.getString(R.string.password_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.settingFragment)
                }
            } else {
                Toast.makeText(
                    requireContext(), it.errorMessages,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /*private fun updateRequirements(password: String) {
        val requirementsText = resources.getString(R.string.password_instruction)
        val spannableString = SpannableString(requirementsText)

        if (password.length >= 6) {
            val start = lenStrtIndx
            val end = lenEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.safe_color,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val start = lenStrtIndx
            val end = lenEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (password.any { it.isDigit() }) {
            val start = numberStrtIndx
            val end = numberEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.safe_color,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val start = numberStrtIndx
            val end = numberEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (password.any { it.isUpperCase() }) {
            val start = upperStrtIndx
            val end = upperEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.safe_color,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val start = upperStrtIndx
            val end = upperEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (password.any { it.isLowerCase() }) {
            val start = lowerStrtIndx
            val end = lowerEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.safe_color,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val start = lowerStrtIndx
            val end = lowerEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (password.any { !it.isLetterOrDigit() }) {
            val start = specialStrtIndx
            val end = specialEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.safe_color,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val start = specialStrtIndx
            val end = specialEndIndx
            spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black,context?.theme)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.layoutPassInstructionsNew.txtInstructionConfirm.text = spannableString
    }*/
}