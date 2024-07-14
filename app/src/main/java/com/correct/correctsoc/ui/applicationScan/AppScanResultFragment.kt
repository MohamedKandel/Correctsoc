package com.correct.correctsoc.ui.applicationScan


import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentAppScanResultBinding
import com.correct.correctsoc.helper.AudioUtils
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Locale


class AppScanResultFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentAppScanResultBinding
    private lateinit var helper: HelperClass
    private lateinit var audioUtils: AudioUtils
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
        binding = FragmentAppScanResultBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        audioUtils = AudioUtils.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.appScanResultFragment)
        if (helper.getLang(requireContext()).equals("en")) {
            audioUtils.playAudio(requireContext(),R.raw.scan_successful_en)
        } else {
            audioUtils.playAudio(requireContext(),R.raw.scan_successful_ar)
        }

        if (arguments != null) {
            binding.btnNext.setOnClickListener {
                audioUtils.releaseMedia()
                findNavController().navigate(R.id.APKsFragment, requireArguments())
            }
        }
        helper.onBackPressed(this) {
            findNavController().navigate(R.id.applicationScanFragment)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.appScanResultFragment)
    }

    override fun onDestroyView() {
//        audioUtils.releaseMedia()
        super.onDestroyView()
    }
}