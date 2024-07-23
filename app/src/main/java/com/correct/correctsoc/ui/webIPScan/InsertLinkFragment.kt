package com.correct.correctsoc.ui.webIPScan

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentInsertLinkBinding
import com.correct.correctsoc.helper.ConnectionManager
import com.correct.correctsoc.helper.ConnectivityListener
import com.correct.correctsoc.helper.Constants.IP_ADDRESS
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.buildDialog

class InsertLinkFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentInsertLinkBinding
    private lateinit var helper: HelperClass
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var connectionManager: ConnectionManager
    private var isConnected = MutableLiveData<Boolean>()

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
        binding = FragmentInsertLinkBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.insertLinkFragment)
        connectionManager = ConnectionManager(requireContext())

        isConnected.postValue(true)
        connectionManager.observe()
        connectionManager.statusLiveData.observe(viewLifecycleOwner) {
            when(it) {
                ConnectivityListener.Status.AVAILABLE -> {
                    isConnected.postValue(true)
                }
                ConnectivityListener.Status.UNAVAILABLE -> {
                    isConnected.postValue(false)
                }
                ConnectivityListener.Status.LOST -> {
                    isConnected.postValue(false)
                }
                ConnectivityListener.Status.LOSING -> {
                    isConnected.postValue(false)
                }
            }
        }


        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (arguments != null) {
            val source = requireArguments().getString(TYPE)
            if (source.equals(IP_ADDRESS)) {
                binding.txtTitle.text = resources.getString(R.string.ip_scan)
                binding.txtLink.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ip_scan_icon, 0, 0, 0
                )
                binding.txtLink.hint = resources.getString(R.string.enter_ip)
                binding.btnScan.setOnClickListener {
                    isConnected.observe(viewLifecycleOwner) {
                        if (it) {
                            scanWithArgs()
                        } else {
                            Log.v("Scan mohamed","btn scan with args")
                            noInternet()
                        }
                    }
                }

                binding.txtLink.setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        isConnected.observe(viewLifecycleOwner) {
                            if (it) {
                                scanWithArgs()
                            } else {
                                Log.v("Scan mohamed","txt scan with args")
                                noInternet()
                            }
                        }
                        true
                    } else if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {
                        isConnected.observe(viewLifecycleOwner) {
                            if (it) {
                                scanWithArgs()
                            } else {
                                Log.v("Scan mohamed","txt scan with args")
                                noInternet()
                            }
                        }
                        true
                    } else if (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        isConnected.observe(viewLifecycleOwner) {
                            if (it) {
                                scanWithArgs()
                            } else {
                                Log.v("Scan mohamed","txt scan with args")
                                noInternet()
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        } else {
            // web scan
            binding.txtLink.inputType = InputType.TYPE_CLASS_TEXT
            binding.txtLink.imeOptions = EditorInfo.IME_ACTION_DONE

            binding.txtLink.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    TODO("Not yet implemented")
                    if (!Patterns.WEB_URL.matcher(binding.txtLink.text.toString()).matches()) {
                        binding.txtLink.error = resources.getString(R.string.invalid_link)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
//                    TODO("Not yet implemented")
                }

            })

            binding.txtLink.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.link_icon, 0, 0, 0
            )
            binding.txtLink.hint = resources.getString(R.string.enter_link)
            binding.btnScan.setOnClickListener {
                isConnected.observe(viewLifecycleOwner) {
                    if (it) {
                        scanWithoutArgs()
                    } else {
                        Log.v("Scan mohamed","btn scan without args")
                        noInternet()
                    }
                }
            }

            binding.txtLink.setOnEditorActionListener { v, actionId, event ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE -> {
//                        scanWithoutArgs()
//                        true
//                    }
//
//                    EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT -> {
//                        scanWithoutArgs()
//                        true
//                    }
//
//                    else -> false
//                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    isConnected.observe(viewLifecycleOwner) {
                        if (it) {
                            scanWithoutArgs()
                        } else {
                            Log.v("Scan mohamed","txt scan without args")
                            noInternet()
                        }
                    }
                    true
                } else if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO) {
                    isConnected.observe(viewLifecycleOwner) {
                        if (it) {
                            scanWithoutArgs()
                        } else {
                            Log.v("Scan mohamed","txt scan without args")
                            noInternet()
                        }
                    }
                    true
                } else if (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    isConnected.observe(viewLifecycleOwner) {
                        if (it) {
                            scanWithoutArgs()
                        } else {
                            Log.v("Scan mohamed","txt scan without args")
                            noInternet()
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun scanWithArgs() {
        val input = binding.txtLink.text.toString()
        if (input.isNotEmpty()) {
            val bundle = Bundle()
            val ip = input.trim()
            bundle.putString(IP_ADDRESS, ip)
            bundle.putInt(SOURCE, R.id.insertLinkFragment)
            bundle.putString(TYPE, IP_ADDRESS)
            findNavController().navigate(R.id.webScanFragment, bundle)
        } else {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.invalid_ip),
                Toast.LENGTH_SHORT
            )
                .show()
        }
        hideKeyboard(binding.txtLink)
        Log.i("IP mohamed", "onCreateView: $input")
    }

    private fun scanWithoutArgs() {
        val input = binding.txtLink.text.toString()
        if (input.isNotEmpty()) {
            val bundle = Bundle()
            val link = input.trim()
            bundle.putString(IP_ADDRESS, link)
            bundle.putInt(SOURCE, R.id.insertLinkFragment)
            findNavController().navigate(R.id.webScanFragment, bundle)
        } else {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.invalid_link),
                Toast.LENGTH_SHORT
            )
                .show()
        }
        hideKeyboard(binding.txtLink)
    }

    private fun noInternet() {
        AlertDialog.Builder(requireContext())
            .buildDialog(title = resources.getString(R.string.warning),
                msg = resources.getString(R.string.no_internet_connection),
                icon = R.drawable.no_internet_icon,
                positiveButton = resources.getString(R.string.ok),
                negativeButton = resources.getString(R.string.cancel),
                positiveButtonFunction = {
                    //findNavController().navigate(R.id.detectingFragment)
                },
                negativeButtonFunction = {
                    //findNavController().navigate(R.id.detectingFragment)
                })
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.insertLinkFragment)
    }
}