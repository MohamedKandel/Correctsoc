package com.correct.correctsoc.ui.selfScan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentSelfPenBinding
import com.correct.correctsoc.helper.ConnectionManager
import com.correct.correctsoc.helper.ConnectivityListener
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.helper.circularAnimation

class SelfPenFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSelfPenBinding
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
        binding = FragmentSelfPenBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        fragmentListener.onFragmentChangedListener(R.id.selfPenFragment)

        connectionManager = ConnectionManager(requireContext())
        connectionManager.observe()
        connectionManager.statusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                ConnectivityListener.Status.AVAILABLE -> isConnected.postValue(true)
                ConnectivityListener.Status.UNAVAILABLE -> isConnected.postValue(false)
                ConnectivityListener.Status.LOST -> isConnected.postValue(false)
                ConnectivityListener.Status.LOSING -> isConnected.postValue(false)
            }
        }



        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.progressLayout.setOnClickListener {
            // start scan
            isConnected.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(R.id.detectingFragment)
                } else {
                    noInternet()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
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
        fragmentListener.onFragmentChangedListener(R.id.selfPenFragment)
        //Start animation
        binding.progressCircular.startAnimation(circularAnimation(3000))
    }


}