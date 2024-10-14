package com.correct.correctsoc.ui.auth

import AccountsAdapter
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.RegisterBody
import com.correct.correctsoc.databinding.FragmentSignUpBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TAG
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.mappingNumbers
import com.correct.correctsoc.helper.setSpannable
import com.correct.correctsoc.helper.show
import com.correct.correctsoc.helper.updateRequirements
import com.correct.correctsoc.room.User
import com.correct.correctsoc.room.UsersDB
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(), ClickListener {

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
    private lateinit var dialog: Dialog
    private lateinit var accountsList: MutableList<String>
    private lateinit var accountsAdapter: AccountsAdapter
    private var isChoosedMail = false
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                // granted
                getGoogleAccounts()
            } else {
                // declined
            }
        }

    /*private lateinit var gso: GoogleSignInOptions
    private lateinit var client: GoogleSignInClient
    private var isChoosedMail = false

    private val getMailIntent: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            task.getResult(ApiException::class.java)
            isChoosedMail = true
            returnToSignUp()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    private fun returnToSignUp() {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (account != null) {
                account.email?.let { Log.v("Google mail mohamed", it) }
                binding.txtMail.setText(account.email)
            }
    }*/

    private fun displayDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.pick_account_dialog)
        dialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.account_recyclerView)
        recyclerView.adapter = accountsAdapter

        checkAndRequestPermission()

        dialog.show()
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, retrieve Google accounts
                getGoogleAccounts()
            }

            else -> {
                // Request permission
                requestPermissionLauncher.launch(android.Manifest.permission.GET_ACCOUNTS)
            }
        }
    }

    private fun getGoogleAccounts() {
        val accountManager = AccountManager.get(requireContext())
        val accounts: Array<Account> = accountManager.getAccountsByType("com.google")

        for (account in accounts) {
            // add unique emails only
            if (!accountsList.contains(account.name)) {
                accountsList.add(account.name)
            }
            accountsAdapter.updateAdapter(accountsList)

            println("Google Account: ${account.name}")
            // Handle each Google account, e.g., display the name
        }
    }

    override fun onPause() {
        super.onPause()
        //client.signOut()
    }


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

        binding.placeholder.hide()
        binding.progress.hide()

        accountsList = mutableListOf()
        accountsAdapter = AccountsAdapter(accountsList, this)

        /*gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        client = GoogleSignIn.getClient(requireContext(), gso)*/

        val text = resources
            .getString(R.string.already_have_account)
            .replace("\n", " ")

        if (arguments != null) {
            source = requireArguments().getInt(SOURCE)
        }

        binding.txtMail.inputType = android.text.InputType.TYPE_NULL
        binding.txtMail.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val imm =
                    view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                displayDialog()
                //checkAndRequestPermission()

                /*if (isChoosedMail) {

                } else {
                    checkAndRequestPermission()
                }*/
                /*if (isChoosedMail) {
                    signOut()
                } else {
                    getMailIntent.launch(client.signInIntent)
                }*/
            }
        }

        binding.txtMail.setOnClickListener {
            val imm =
                view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

            displayDialog()
            //checkAndRequestPermission()

            /*if (isChoosedMail) {
                signOut()
            } else {
                getMailIntent.launch(client.signInIntent)
            }*/
        }

        if (helper.getLang(requireContext()).equals("en")) {
            startIndx = 23
        } else {
            startIndx = 20
        }
        endIndx = text.length

        binding.txtLogin.setSpannable(
            startIndx,
            endIndx,
            text,
            resources.getColor(R.color.white, context?.theme)
        ) {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.signUpFragment)
            findNavController().navigate(R.id.loginFragment, bundle)
        }
        binding.txtLogin.movementMethod = LinkMovementMethod.getInstance()

        binding.layoutPassInstructions.root.hide()

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
                    binding.layoutPassInstructions.root.show()
                } else {
                    binding.layoutPassInstructions.root.hide()
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
                        )
                ) {
                    binding.layoutPassInstructions.root.hide()
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
                    binding.txtMail,
                    binding.txtName
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.required_data),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.placeholder.show()
                binding.progress.show()
                // send data to server here
                val name = binding.txtName.text.toString()
                val phone = binding.txtPhone.text.toString()
                val password = binding.txtPassword.text.toString()
                val mail = binding.txtMail.text.toString()
                val body = RegisterBody(
                    name = name, phone = phone,
                    password = password,
                    mail = mail
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

    /*private fun signOut() {
        client.signOut().addOnCompleteListener {
            getMailIntent.launch(client.signInIntent)
        }
    }*/

    private fun registerUser(body: RegisterBody) {
        viewModel.registerUser(body)
        viewModel.registerResponse.observe(viewLifecycleOwner) {
            if (it.isSuccess) {
                binding.placeholder.hide()
                binding.progress.hide()
                Log.v(TAG, "${it.statusCode}")
                val user = User("1", body.name, body.password, body.phone, "0", body.mail)
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
                binding.placeholder.hide()
                binding.progress.hide()
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

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        Log.v("Account selected mohamed", accountsList[position])
        binding.txtMail.setText(accountsList[position])
        dialog.cancel()
        dialog.cancel()
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }
}