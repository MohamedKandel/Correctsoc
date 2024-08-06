package com.correct.correctsoc.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.data.auth.forget.ForgotResponse
import com.correct.correctsoc.databinding.FragmentSettingBinding
import com.correct.correctsoc.helper.Constants.SOURCE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.buildDialog
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.ui.auth.AuthViewModel
import kotlinx.coroutines.launch


class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentSettingBinding
    private lateinit var helper: HelperClass
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var viewModel: AuthViewModel
    private lateinit var usersDB: UsersDB

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
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        usersDB = UsersDB.getDBInstance(requireContext())

        fragmentListener.onFragmentChangedListener(R.id.settingFragment)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.detailsLayout.setOnClickListener {
            findNavController().navigate(R.id.editInfoFragment)
        }

        binding.passwordLayout.setOnClickListener {
            findNavController().navigate(R.id.updatePasswordFragment)
        }

        binding.aboutLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.settingFragment)
            findNavController().navigate(R.id.aboutFragment, bundle)
        }

        binding.privacyLayout.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment)
        }

        binding.languageLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(SOURCE, R.id.settingFragment)
            findNavController().navigate(R.id.langFragment, bundle)
        }

        binding.notificationLayout.setOnClickListener {
            openSetting()
        }

        binding.shareLayout.setOnClickListener {
            shareApp()
        }
        binding.feedbackLayout.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${requireContext().packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    )
                )
            }
        }

        /*binding.privacyLayout.setOnClickListener {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.not_supported),
                Toast.LENGTH_SHORT
            )
                .show()
        }*/

        binding.deleteLayout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .buildDialog(title = resources.getString(R.string.confirmation),
                    msg = resources.getString(R.string.confirmation_msg),
                    positiveButton = resources.getString(R.string.delete),
                    negativeButton = resources.getString(R.string.cancel),
                    positiveButtonFunction = {
                        lifecycleScope.launch {
                            val id = usersDB.dao().getUserID() ?: ""
                            if (id.isNotEmpty()) {
                                deleteAccount(id, helper.getToken(requireContext()))
                            }
                        }
                    },
                    negativeButtonFunction = {

                    })
        }

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.settingFragment)
    }

    private fun openSetting() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // For Android 8 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().packageName)
        } else {
            // For Android 5-7
            intent.putExtra("app_package", requireContext().packageName)
            intent.putExtra("app_uid", requireContext().applicationInfo.uid)
        }
        startActivity(intent)
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
        val shareMessage = "https://play.google.com/store/apps/details?id=com.correct.correctsoc"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        requireActivity()
            .startActivity(Intent.createChooser(intent, resources.getString(R.string.share_with)))
    }

    private fun deleteAccount(userID: String, token: String) {
        viewModel.deleteAccount(userID, token)
        val observer = object : Observer<ForgotResponse> {
            override fun onChanged(value: ForgotResponse) {
                if (value.isSuccess) {
                    lifecycleScope.launch {
                        usersDB.dao().deleteUser(userID)
                        findNavController().navigate(R.id.registerFragment)
                    }
                } else {
                    Toast.makeText(requireContext(), value.errorMessages, Toast.LENGTH_SHORT).show()
                }
                viewModel.deleteAccountResponse.removeObserver(this)
            }
        }
        viewModel.deleteAccountResponse.observe(viewLifecycleOwner, observer)
    }

}