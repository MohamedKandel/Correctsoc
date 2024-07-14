package com.correct.correctsoc.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.ResetPasswordBody
import com.correct.correctsoc.databinding.FragmentResetPasswordBinding

import com.correct.correctsoc.helper.Constants.TAG
import com.correct.correctsoc.helper.Constants.TOKEN_KEY
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.isContainsSpecialCharacter
import com.correct.correctsoc.helper.isContainsNumbers
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.updateRequirements
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentResetPasswordBinding
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

    private lateinit var viewModel: AuthViewModel
    private lateinit var usersDB: UsersDB
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
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())

        fragmentListener.onFragmentChangedListener(R.id.resetPasswordFragment)

        binding.progress.visibility = View.GONE
        binding.placeholder.visibility = View.GONE

        binding.layoutPassInstructionsNew.root.visibility = View.GONE
        binding.layoutPassInstructionsConfirm.root.visibility = View.GONE

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

        binding.txtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    binding.layoutPassInstructionsNew.root.visibility = View.VISIBLE
                } else {
                    binding.layoutPassInstructionsNew.root.visibility = View.GONE
                }
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
                        )
                ) {
                    binding.layoutPassInstructionsNew.root.visibility = View.GONE
                }
            }
        })

        binding.txtConfirmpassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

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


        helper.onBackPressed(this) {
            findNavController().navigate(R.id.OTPFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.OTPFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.doneBtn.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            binding.placeholder.visibility = View.VISIBLE

            val newPassword = binding.txtPassword.text.toString()
            val confirm = binding.txtConfirmpassword.text.toString()
            if (newPassword.equals(confirm) && (newPassword.isNotEmpty() || confirm.isNotEmpty())) {
                if (arguments != null) {
                    val token = requireArguments().getString(TOKEN_KEY, "") ?: ""
                    lifecycleScope.launch {
                        val id = usersDB.dao().getUserID() ?: ""
                        val phone = usersDB.dao().getUserPhone(id) ?: ""
                        val body = ResetPasswordBody(newPassword, token, phone)
                        resetPassword(body, helper.getToken(requireContext()))
                    }
                }
            } else {
                binding.progress.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.not_match),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        return binding.root
    }

    private fun resetPassword(body: ResetPasswordBody, token: String) {
        viewModel.resetPassword(body, token)
        viewModel.resetPasswordResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.progress.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                Log.v(TAG, "${it.statusCode}")
                Log.v(TAG, "password reseted")
                Log.v(TAG, body.newPassword)
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    usersDB.dao().updatePassword(id, body.newPassword)
                    findNavController().navigate(R.id.loginFragment)
                }
            } else {
                binding.progress.visibility = View.GONE
                binding.placeholder.visibility = View.GONE
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.resetPasswordFragment)
    }

}