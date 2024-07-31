package com.correct.correctsoc.ui.lock

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentPasswordBinding
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.room.App
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.launch

class PasswordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentPasswordBinding
    private lateinit var helper: HelperClass
    private var index = 0
    private lateinit var stringBuilder: StringBuilder
    private val DELAY = 70L
    private lateinit var usersDB: UsersDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())

        if (arguments != null) {
            helper.onBackPressed(this) {
                findNavController().navigate(R.id.appsFragment, requireArguments())
            }

            binding.btnBack.setOnClickListener {
                findNavController().navigate(R.id.appsFragment, requireArguments())
            }
            val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getParcelable(ITEM, App::class.java)
            } else {
                requireArguments().getParcelable(ITEM)
            }
            if (item != null) {
                Log.i("Package name mohamed", item.packageName)
            }

            stringBuilder = StringBuilder()

            binding.apply {
                txtOne.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "1")
                        addText("1")
                        index++
                    }
                }

                txtTwo.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "2")
                        addText("2")
                        index++
                    }
                }

                txtThree.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "3")
                        addText("3")
                        index++
                    }
                }

                txtFour.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "4")
                        addText("4")
                        index++
                    }
                }

                txtFive.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "5")
                        addText("5")
                        index++
                    }
                }

                txtSix.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "6")
                        addText("6")
                        index++
                    }
                }

                txtSeven.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "7")
                        addText("7")
                        index++
                    }
                }

                txtEight.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "8")
                        addText("8")
                        index++
                    }
                }

                txtNine.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "9")
                        addText("9")
                        index++
                    }
                }

                txtZero.setOnClickListener {
                    if (index < 4) {
                        stringBuilder.insert(index, "0")
                        addText("0")
                        index++
                    }
                }

                deleteIcon.setOnClickListener {
                    if (index >= 1) {
                        index--
                        stringBuilder.deleteCharAt(index)
                    }
                    addText("")
                }

                doneBtn.setOnClickListener {
                    if (stringBuilder.isNotEmpty() && stringBuilder.length == 4) {
                        if (item != null) {
                            val app =
                                App(item.packageName, item.appName, stringBuilder.toString(), 0)
                            lifecycleScope.launch {
                                usersDB.appDao().insert(app)
                                findNavController().navigate(R.id.appsFragment, requireArguments())
                                Toast.makeText(
                                    requireContext(),
                                    resources.getString(R.string.app_locked_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        if (stringBuilder.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.password_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (stringBuilder.length < 4) {
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.less_password),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun addText(number: String) {
        binding.apply {
            when (index) {
                0 -> {
                    txtFirstDigit.text = number
                }

                1 -> {
                    txtSecondDigit.text = number
                }

                2 -> {
                    txtThirdDigit.text = number
                }

                3 -> {
                    txtFourthDigit.text = number
                }
            }
        }
    }
}