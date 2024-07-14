package com.correct.correctsoc.ui.onBoarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.MainActivity
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentLangBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.google.android.material.bottomsheet.BottomSheetDialog


class LangFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private lateinit var binding: FragmentLangBinding
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
        binding = FragmentLangBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()

        fragmentListener.onFragmentChangedListener(R.id.langFragment)
        helper.onBackPressed(this) {
            if (arguments != null) {
                val source = requireArguments().getInt(SOURCE, 0)
                if (source != 0) {
                    findNavController().navigate(source)
                } else {
                    requireActivity().finish()
                }
            } else {
                // Back is pressed... Finishing the activity
                requireActivity().finish()
            }
        }

        //Start animation
        binding.circleImg.startAnimation(helper.circularAnimation(1000))

        displayBtmSheet()

        return binding.root
    }

    private fun displayBtmSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.language_dialog_layout, null, false)
        val btn_english = view.findViewById<Button>(R.id.btn_english)
        val btn_arabic = view.findViewById<Button>(R.id.btn_arabic)

        btn_english.setOnClickListener {
            helper.setLang("en", requireContext())
            dialog.cancel()
            dialog.dismiss()

            val intent = Intent(context, MainActivity::class.java)
            requireActivity().finish()
            startActivity(intent)
        }

        btn_arabic.setOnClickListener {
            helper.setLang("ar",requireContext())
            dialog.cancel()
            dialog.dismiss()

            val intent = Intent(context, MainActivity::class.java)
            requireActivity().finish()
            startActivity(intent)
        }
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.langFragment)
    }
}