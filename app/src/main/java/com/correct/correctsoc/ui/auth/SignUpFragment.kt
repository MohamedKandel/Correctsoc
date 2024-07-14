package com.correct.correctsoc.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.RegisterBody
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.databinding.FragmentSignUpBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TAG
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.setSpannable
import com.correct.correctsoc.helper.updateRequirements
import com.correct.correctsoc.room.User
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

import okhttp3.RequestBody.Companion.toRequestBody

class SignUpFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var helper: HelperClass
    private var startIndx = 0
    private var endIndx = 0
    private var source = -1
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
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())

        fragmentListener.onFragmentChangedListener(R.id.signUpFragment)

        binding.placeholder.visibility = View.GONE
        binding.progress.visibility = View.GONE

        val text = resources
            .getString(R.string.already_have_account)
            .replace("\n", " ")

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
        }

        if (helper.getLang(requireContext()).equals("en")) {
            startIndx = 23
        } else {
            startIndx = 20
        }
        endIndx = text.length

        binding.txtLogin.setSpannable(startIndx,
            endIndx,
            text,
            resources.getColor(R.color.white, context?.theme)) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.signUpFragment)
            findNavController().navigate(R.id.loginFragment, bundle)
        }
        binding.txtLogin.movementMethod = LinkMovementMethod.getInstance()

        binding.layoutPassInstructions.root.visibility = View.GONE

        if (helper.getLang(requireContext()).equals("en")) {
            binding.layoutPassInstructions.txtInstructionConfirm.text =
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
            binding.layoutPassInstructions.txtInstructionConfirm.text =
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
                    binding.layoutPassInstructions.root.visibility = View.VISIBLE
                } else {
                    binding.layoutPassInstructions.root.visibility = View.GONE
                }
                //updateRequirements(s.toString())
                if (binding.layoutPassInstructions.txtInstructionConfirm
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
                    binding.layoutPassInstructions.root.visibility = View.GONE
                }
            }
        })

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
                binding.placeholder.visibility = View.VISIBLE
                binding.progress.visibility = View.VISIBLE
                // send data to server here
                val name = binding.txtName.text.toString()
                val phone = binding.txtPhone.text.toString()
                val password = binding.txtPassword.text.toString()
                val body = RegisterBody(
                    name = name, phone = phone,
                    password = password
                )
                registerUser(body)
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

        binding.txtCode.setText(helper.generateFirstCode())

        return binding.root
    }

    private fun registerUser(body: RegisterBody) {
        viewModel.registerUser(body)
        viewModel.registerResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.placeholder.visibility = View.GONE
                binding.progress.visibility = View.GONE
                Log.v(TAG, "${it.statusCode}")
                val user = User("1", body.name, body.password, body.phone, "0")
                lifecycleScope.launch {
                    usersDB.dao().insert(user)
                    helper.setRemember(requireContext(), true)
                    val bundle = Bundle()
                    bundle.putInt(SOURCE, R.id.signUpFragment)
                    findNavController().navigate(R.id.OTPFragment, bundle)
                }
                if (it.errorMessages != null) {
                    Log.v(TAG, "${it.errorMessages}")
                }
                if (it.result != null) {
                    Log.v(TAG, "${it.result.name}")
                    Log.v(TAG, "${it.result.userid}")
                    Log.v(TAG, "${it.result.token}")
                }
            } else {
                binding.placeholder.visibility = View.GONE
                binding.progress.visibility = View.GONE
                Log.v(TAG, "${it.errorMessages}")
                Toast.makeText(requireContext(), "${it.errorMessages}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.signUpFragment)
    }
}