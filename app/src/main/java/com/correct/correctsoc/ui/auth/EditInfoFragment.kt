package com.correct.correctsoc.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.GenerateOTPBody
import com.correct.correctsoc.data.auth.UpdateUsernameBody
import com.correct.correctsoc.databinding.FragmentEditInfoBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class EditInfoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentEditInfoBinding
    private lateinit var helper: HelperClass
    private lateinit var usersDB: UsersDB
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.settingFragment)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.settingFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.saveBtn.setOnClickListener {
            val username = "${binding.txtFname.text} ${binding.txtLname.text}".trim()
            val phoneNumber = binding.txtPhone.text.toString().trim()
            if (username.isNotEmpty()) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    val name = usersDB.dao().getUsername(id) ?: ""
                    val phone = usersDB.dao().getUserPhone(id) ?: ""
                    //Toast.makeText(requireContext(), phone, Toast.LENGTH_SHORT).show()
                    if (!name.equals(username)) {
                        val body = UpdateUsernameBody(
                            phone = phone,
                            username = username
                        )
                        if (phone != phoneNumber && phoneNumber.isNotEmpty()) {
                            updateUsername(body, helper.getToken(requireContext()), true)
                        } else if (phone == phoneNumber || phoneNumber.isEmpty()) {
                            updateUsername(body, helper.getToken(requireContext()), false)
                        }
                    } else {
                        if (phoneNumber.isNotEmpty()) {
                            if (phone != phoneNumber) {
                                lifecycleScope.launch {
                                    val otpBody = GenerateOTPBody(
                                        newPhone = phoneNumber,
                                        userId = id
                                    )
                                    generateOTP(otpBody, helper.getToken(requireContext()))
                                }
                            }
                        }
                    }
                }
            } else {
                if (phoneNumber.isNotEmpty()) {
                    lifecycleScope.launch {
                        val id = usersDB.dao().getUserID() ?: ""
                        val phone = usersDB.dao().getUserPhone(id) ?: ""
                        if (phone != phoneNumber) {
                            lifecycleScope.launch {
                                val otpBody = GenerateOTPBody(
                                    newPhone = phoneNumber,
                                    userId = id
                                )
                                generateOTP(otpBody, helper.getToken(requireContext()))
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun updateUsername(body: UpdateUsernameBody, token: String, updatePhone: Boolean) {
        viewModel.updateUsername(body, token)
        viewModel.updateUsernameResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                lifecycleScope.launch {
                    val id = usersDB.dao().getUserID() ?: ""
                    usersDB.dao().updateUsername(body.username, id)

                    if (updatePhone) {
                        val otpBody = GenerateOTPBody(
                            newPhone = binding.txtPhone.text.toString(),
                            userId = id
                        )
                        generateOTP(otpBody, token)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.username_updated),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        findNavController().navigate(R.id.settingFragment)
                    }
                }
                // update phone too if it changed
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun generateOTP(body: GenerateOTPBody, token: String) {
        viewModel.generateOTP(body, token)
        viewModel.generateOTPResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                lifecycleScope.launch {
                    //val id = usersDB.dao().getUserID() ?: ""
                    usersDB.dao().updatePhone(body.userId, body.newPhone)

                    val bundle = Bundle()
                    bundle.putInt(SOURCE, R.id.editInfoFragment)
                    findNavController().navigate(R.id.OTPFragment, bundle)
                }
            } else {
                Toast.makeText(requireContext(), it.errorMessages, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}