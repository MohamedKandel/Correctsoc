package com.correct.correctsoc.ui.pay

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.DistributorsAdapter
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.data.pay.DistributorsModel
import com.correct.correctsoc.data.pay.SubscribeCodeBody
import com.correct.correctsoc.databinding.FragmentDistributorsBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.PHONE_NUMBER
import com.correct.correctsoc.helper.Constants.WA_NUMBER
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.NextStepListener
import com.correct.correctsoc.room.UsersDB
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.launch

class DistributorsFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentDistributorsBinding
    private lateinit var helper: HelperClass
    private lateinit var list: MutableList<DistributorsModel>
    private lateinit var adapter: DistributorsAdapter
    private var phoneNumber: String? = null
    private lateinit var fragmentListener: FragmentChangedListener
    private var deletePressed = false
    private lateinit var viewModel: PayViewModel
    private lateinit var usersDB: UsersDB
    private lateinit var listener: NextStepListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is NextStepListener) {
            listener = parentFragment as NextStepListener
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 1) {
                    childFragmentManager.popBackStack()
                    return
                }
                val parentPayFragment = this@DistributorsFragment.parentFragment as ParentPayFragment
                parentPayFragment.changeSteps(2)
                parentPayFragment.displayHeader(true)
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }
    //private var swippedPosition: Int ?= null

    private val arl: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // permission granted
        if (isGranted) {
            phoneNumber?.let { makeCall(it) }
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", "com.correct.correctsoc", null)
            intent.data = uri
            startActivity(intent)
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
        binding = FragmentDistributorsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.distributorsFragment)
        list = mutableListOf()
        adapter = DistributorsAdapter(requireContext(), list, this)
        binding.contactsRecyclerView.adapter = adapter
        viewModel = ViewModelProvider(this)[PayViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())

        /*if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }*/

        fillList()

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // action for swiped item
                val position = viewHolder.bindingAdapterPosition
                //swippedPosition = position
                val bundle = Bundle()
                bundle.putString("name", list[position].contact_name)
                bundle.putString("phone", list[position].phone_number)
                onItemClickListener(position, bundle)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.barlow_semi_condensed_semibold)

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.safe_color
                        )
                    )
                    .addSwipeRightActionIcon(R.drawable.phone_icon)
                    .setSwipeRightActionIconTint(Color.rgb(255, 255, 255))
                    .addSwipeRightLabel(resources.getString(R.string.call))
                    .setSwipeRightLabelColor(Color.rgb(255, 255, 255))
                    .setSwipeRightLabelTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    .setSwipeRightLabelTypeface(typeface)
                    .create()
                    .decorate()

                super.onChildDraw(
                    c, recyclerView,
                    viewHolder, dX, dY,
                    actionState, isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.contactsRecyclerView)

        val hyphenPositions = intArrayOf(8, 13, 18, 23)

        binding.txtActivationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deletePressed = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Code mohamed","${s.toString().length}")
                if (s.toString().length == 36) {
                    Log.d("Code mohamed","Code complete")
                    val code = s.toString()
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

                }
            }

            override fun afterTextChanged(s: Editable?) {
                binding.txtActivationCode.removeTextChangedListener(this) // Remove listener to prevent infinite loop
                s?.let {
                    val upperCaseText = it.toString().uppercase()
                    if (upperCaseText != it.toString()) {
                        binding.txtActivationCode.setText(upperCaseText)
                        binding.txtActivationCode.setSelection(upperCaseText.length) // Set cursor to end of text
                    }
                }
                binding.txtActivationCode.addTextChangedListener(this) // Re-attach listener

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
        fragmentListener.onFragmentChangedListener(R.id.distributorsFragment)
        resetSwipedItem()
    }

    // fill list with dummy data
    private fun fillList() {
        list.add(
            DistributorsModel(
                "Ahmed mohamed", "01234567890",
                "0123456789", false, false, true,
                true
            )
        )

        list.add(
            DistributorsModel(
                "Ibrahim Adel", "01111111111",
                "01111111111", false, true, false,
                true
            )
        )

        list.add(
            DistributorsModel(
                "Islam Elwy", "01000000000",
                "01000000000", true, false, false,
                false
            )
        )

        list.add(
            DistributorsModel(
                "Mostafa Ahmed", "01276200486",
                "01068411302", true, false, true,
                true
            )
        )

        adapter.updateAdapter(list)
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
        if (extras != null) {
            Log.v("Contacts mohamed", extras.getString("name") ?: "")
            Log.v("Contacts mohamed", extras.getString("phone") ?: "")
            if (extras.getString("phone") != null) {
                // make call
                phoneNumber = extras.getString("phone")
                arl.launch(Manifest.permission.CALL_PHONE)
            }
            val whatsapp = extras.getString(WA_NUMBER, "")
            val phone = extras.getString(PHONE_NUMBER, "")
            if (whatsapp.isNotEmpty()) {
                val url = "https://api.whatsapp.com/send?phone=$whatsapp"
                try {
                    val pm = requireContext().packageManager
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Whatsapp app not installed in your phone",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
            if (phone.isNotEmpty()) {
                val u = Uri.parse("tel:$phone")

                val intent = Intent(Intent.ACTION_DIAL, u)
                try {
                    startActivity(intent)
                } catch (s: SecurityException) {
                    Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }

    private fun makeCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$phoneNumber")
        startActivity(callIntent)
    }

    private fun resetSwipedItem() {
        for (i in 0..<list.size) {
            adapter.notifyItemChanged(i)
        }
    }

    private fun subscribe(body: SubscribeCodeBody) {
        viewModel.subscribeWithCode(body)
        val observer = object : Observer<ForgotResponse> {
            override fun onChanged(value: ForgotResponse) {
                if (value.isSuccess) {
                    //Log.v("subscription", "Subscribed successfully")
                    (parentFragment as? ParentPayFragment)?.replaceFragment(
                        PaymentSuccessFragment()
                    )
                    listener.onNextStepListener(2)
                } else {
                        Toast.makeText(
                            requireContext(),
                            value.errorMessages,
                            Toast.LENGTH_SHORT
                        ).show()
                }
                viewModel.subscribeWithCodeResponse.removeObserver(this)
            }
        }
        viewModel.subscribeWithCodeResponse.observe(viewLifecycleOwner, observer)
    }

}