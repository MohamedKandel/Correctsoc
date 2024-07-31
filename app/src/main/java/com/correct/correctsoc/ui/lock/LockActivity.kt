package com.correct.correctsoc.ui.lock

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.ActivityLockBinding
import com.correct.correctsoc.helper.Constants.PACKAGE
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.room.App
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private lateinit var helper: HelperClass
    private var index = 0
    private lateinit var stringBuilder: StringBuilder
    private var isPasswordVisible = false
    private lateinit var usersDB: UsersDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLockBinding.inflate(layoutInflater)
        helper = HelperClass.getInstance()
        setLocale(helper.getLang(this))
        setContentView(binding.root)

        usersDB = UsersDB.getDBInstance(this)

        if (helper.getLang(this).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        if (intent.extras != null) {
            val pkg = intent.extras?.getString(PACKAGE) ?: "empty"
            lifecycleScope.launch {
                val app = usersDB.appDao().getAppData(pkg)

                stringBuilder = StringBuilder()

                binding.apply {
                    passwordToggleIcon.setOnClickListener {
                        if (!isPasswordVisible) {
                            showPassword()
                        } else {
                            hidePassword()
                        }
                        isPasswordVisible = !isPasswordVisible
                    }
                    txtOne.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "1")
                            if (isPasswordVisible) {
                                addText("1")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtTwo.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "2")
                            if (isPasswordVisible) {
                                addText("2")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtThree.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "3")
                            if (isPasswordVisible) {
                                addText("3")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtFour.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "4")
                            if (isPasswordVisible) {
                                addText("4")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtFive.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "5")
                            if (isPasswordVisible) {
                                addText("5")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtSix.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "6")
                            if (isPasswordVisible) {
                                addText("6")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtSeven.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "7")
                            if (isPasswordVisible) {
                                addText("7")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtEight.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "8")
                            if (isPasswordVisible) {
                                addText("8")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtNine.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "9")
                            if (isPasswordVisible) {
                                addText("9")
                            } else {
                                addText("*")
                            }
                            index++
                        }
                    }

                    txtZero.setOnClickListener {
                        if (index < 4) {
                            stringBuilder.insert(index, "0")
                            if (isPasswordVisible) {
                                addText("0")
                            } else {
                                addText("*")
                            }
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
                            // check password
                            val enteredPassword = stringBuilder.toString()
                            if (app!= null) {
                                val password = app.password
                                if (password == enteredPassword) {
                                    lifecycleScope.launch {
                                        usersDB.appDao().allowForApp(pkg)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            finish()
                                        }
                                    }
                                } else {
                                    Toast.makeText(this@LockActivity,
                                        resources.getString(R.string.incorrect_password),Toast.LENGTH_SHORT).show()
                                }
                            } else {

                            }
                        } else {
                            if (stringBuilder.isEmpty()) {
                                Toast.makeText(
                                    this@LockActivity,
                                    resources.getString(R.string.password_empty),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (stringBuilder.length < 4) {
                                Toast.makeText(
                                    this@LockActivity,
                                    resources.getString(R.string.less_password),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hidePassword() {
        binding.passwordToggleIcon.setImageResource(R.drawable.show_password)
        val length = stringBuilder.length
        when (length) {
            1 -> {
                binding.txtFirstDigit.text = "*"
            }

            2 -> {
                binding.txtFirstDigit.text = "*"
                binding.txtSecondDigit.text = "*"
            }

            3 -> {
                binding.txtFirstDigit.text = "*"
                binding.txtSecondDigit.text = "*"
                binding.txtThirdDigit.text = "*"
            }

            4 -> {
                binding.txtFirstDigit.text = "*"
                binding.txtSecondDigit.text = "*"
                binding.txtThirdDigit.text = "*"
                binding.txtFourthDigit.text = "*"
            }
        }
    }

    private fun showPassword() {
        when (stringBuilder.length) {
            1 -> {
                binding.txtFirstDigit.text = stringBuilder[0].toString()
            }

            2 -> {
                binding.txtFirstDigit.text = stringBuilder[0].toString()
                binding.txtSecondDigit.text = stringBuilder[1].toString()
            }

            3 -> {
                binding.txtFirstDigit.text = stringBuilder[0].toString()
                binding.txtSecondDigit.text = stringBuilder[1].toString()
                binding.txtThirdDigit.text = stringBuilder[2].toString()
            }

            4 -> {
                binding.txtFirstDigit.text = stringBuilder[0].toString()
                binding.txtSecondDigit.text = stringBuilder[1].toString()
                binding.txtThirdDigit.text = stringBuilder[2].toString()
                binding.txtFourthDigit.text = stringBuilder[3].toString()
            }
        }

        binding.passwordToggleIcon.setImageResource(R.drawable.hide_password)
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

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val resources: Resources = resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        createConfigurationContext(configuration)
    }
}