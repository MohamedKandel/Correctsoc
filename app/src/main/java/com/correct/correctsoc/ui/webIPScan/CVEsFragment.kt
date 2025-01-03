package com.correct.correctsoc.ui.webIPScan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.CVEAdapter
import com.correct.correctsoc.data.openPorts.CvEs
import com.correct.correctsoc.databinding.FragmentCVEsBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.DEVICE_NAME
import com.correct.correctsoc.helper.Constants.TYPE
import com.correct.correctsoc.helper.Constants.URL
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass

class CVEsFragment : Fragment(), ClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentCVEsBinding
    private lateinit var list: MutableList<CvEs>
    private lateinit var adapter: CVEAdapter
    private lateinit var helper: HelperClass
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
        binding = FragmentCVEsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        list = mutableListOf()
        adapter = CVEAdapter(list, this)
        binding.recyclerView.adapter = adapter
        fragmentListener.onFragmentChangedListener(R.id.CVEsFragment)
        binding.txtTitle.text = binding.txtTitle.text.toString().replace(":","").trim()

        if (arguments != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list = requireArguments().getParcelableArrayList(Constants.LIST, CvEs::class.java)!!
            } else {
                list = requireArguments().getParcelableArrayList(Constants.LIST)!!
            }
            adapter.updateAdapter(list)
            println(requireArguments().getString(TYPE))

            binding.btnBack.setOnClickListener {
                findNavController().navigate(R.id.webScanFragment, requireArguments())
            }
        }

        helper.onBackPressed(this) {
            Log.i(
                "device name mohamed", "handleOnBackPressed: ${
                    requireArguments()
                        .getString(DEVICE_NAME)
                }"
            )
            findNavController().navigate(R.id.webScanFragment, requireArguments())
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    override fun onItemClickListener(position: Int, extras: Bundle?) {
//        TODO("Not yet implemented")
        val url = extras?.getString(URL, "") ?: ""
        if (url.isNotEmpty()) {
            println(url)
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            startActivity(urlIntent)
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {
//        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.CVEsFragment)
    }
}