package com.correct.correctsoc.ui.pay

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.DistributorsAdapter
import com.correct.correctsoc.data.pay.DistributorsModel
import com.correct.correctsoc.databinding.FragmentDistributorsBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class DistributorsFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentDistributorsBinding
    private lateinit var helper: HelperClass
    private lateinit var list: MutableList<DistributorsModel>
    private lateinit var adapter: DistributorsAdapter
    private var phoneNumber: String? = null
    private lateinit var fragmentListener: FragmentChangedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangedListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
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
        /*swippedPosition?.let {
            adapter.notifyItemChanged(it)
            swippedPosition = null
        }*/
        for (i in 0..<list.size) {
            adapter.notifyItemChanged(i)
        }
    }

}