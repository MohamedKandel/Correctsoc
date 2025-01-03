package com.correct.correctsoc.ui.lock

import android.app.AppOpsManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.adapter.AppsAdapter
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.databinding.FragmentAppsBinding
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants
import com.correct.correctsoc.helper.Constants.ISLOCKED
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.Constants.PACKAGE
import com.correct.correctsoc.helper.FragmentChangedListener
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.helper.displayDialog
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.isLightweightVersion
import com.correct.correctsoc.helper.show
import com.correct.correctsoc.room.App
import com.correct.correctsoc.room.UsersDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppsFragment : Fragment(), ClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentAppsBinding
    private lateinit var helper: HelperClass
    private lateinit var adapter: AppsAdapter
    private lateinit var list: MutableList<App>
    private lateinit var fragmentListener: FragmentChangedListener
    private lateinit var usersDB: UsersDB
    private var isUsageAccepted = false
    private var isDisplayAccepted = false

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
        binding = FragmentAppsBinding.inflate(inflater, container, false)
        helper = HelperClass.getInstance()
        usersDB = UsersDB.getDBInstance(requireContext())

        list = mutableListOf()

        fragmentListener.onFragmentChangedListener(R.id.appsFragment)

        if (helper.getLang(requireContext()).equals("ar")) {
            binding.btnBack.rotation = 180f
        }

        helper.onBackPressed(this) {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        lifecycleScope.launch {
            val apps = usersDB.appDao().getLockedApp()
            if (apps != null) {
                for (app in apps) {
                    Log.i("app found mohamed", app.packageName)
                    Log.i("app found mohamed", app.appName)
                    Log.i("app found mohamed", app.password)
                    Log.i("app found mohamed", "${app.isAllowed}")
                    Log.i("app found mohamed", "======================================")
                }
            }
            adapter = AppsAdapter(requireContext(), list, this@AppsFragment, apps, helper)
            binding.applicationsRecyclerView.adapter = adapter

            if (arguments != null) {
                list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireArguments().getParcelableArrayList(LIST, App::class.java)!!
                } else {
                    requireArguments().getParcelableArrayList(LIST)!!
                }
            }
            adapter.updateAdapter(list)

            binding.txtSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val keyword = s.toString()
                    if (keyword.isNotEmpty()) {
                        var filtered = search(keyword)
                        if (filtered.size == 0) {
                            Toast.makeText(
                                requireContext(), resources.getText(R.string.app_search_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                            filtered = list
                        }
                        adapter.updateAdapter(filtered)
                    } else {
                        adapter.updateAdapter(list)
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

        }
        return binding.root
    }

    private fun search(keyWord: String): MutableList<App> {
        val filteredList = mutableListOf<App>()
        for (app in list) {
            if (app.appName.contains(keyWord, true)) {
                filteredList.add(app)
            }
        }
        return filteredList
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.appsFragment)
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), context.packageName
            )
        }
        val granted = mode == AppOpsManager.MODE_ALLOWED
        return granted
    }

    private fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun isDisplayOverAppsEnabled(): Boolean {
        return Settings.canDrawOverlays(requireContext())
    }


    override fun onItemClickListener(position: Int, extras: Bundle?) {
        Log.v("Package name mohamed", list[position].packageName)
        if (extras != null) {
            val tag = extras.getString(ISLOCKED, "") ?: ""
            val pkg = extras.getString(PACKAGE, "") ?: ""
            if (tag.isNotEmpty()) {
                if (tag == resources.getString(R.string.unlock)) {
                    isUsageAccepted = hasUsageStatsPermission(requireContext())
                    if (requireContext().isLightweightVersion()) {
                        isDisplayAccepted = false
                    } else {
                        isDisplayAccepted = isDisplayOverAppsEnabled()
                    }
                    /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                        isDisplayAccepted = isDisplayOverAppsEnabled()
                    } else {
                        isDisplayAccepted = isDisplayOverAppsEnabled()
                    }*/
                    Log.v("ISDisplayed mohamed", "$isDisplayAccepted")
                    if (isUsageAccepted && isDisplayAccepted) {
                        val bundle = Bundle()
                        bundle.putParcelable(ITEM, list[position])
                        bundle.putParcelableArrayList(LIST, ArrayList(list))
                        findNavController().navigate(R.id.passwordFragment, bundle)
                    } else {
                        val dialog = Dialog(requireContext()).displayDialog(
                            R.layout.lock_permissions_dialog,
                            requireContext()
                        )

                        val btn_usage = dialog.findViewById<RelativeLayout>(R.id.btn_usage_access)
                        val btn_display =
                            dialog.findViewById<RelativeLayout>(R.id.btn_draw_over_apps)
                        val usage_permission_status =
                            dialog.findViewById<ImageView>(R.id.usage_permission_icon)
                        val draw_permission_status =
                            dialog.findViewById<ImageView>(R.id.display_permission_icon)

                        btn_usage.setOnClickListener {
                            dialog.dismiss()
                            dialog.cancel()
                            requestUsageStatsPermission(requireContext())
                        }

                        btn_display.setOnClickListener {
                            dialog.dismiss()
                            dialog.cancel()
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${requireContext().packageName}")
                            )
                            startActivity(intent)
                        }

                        /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                            btn_display.hide()
                            draw_permission_status.show()
                            draw_permission_status.setImageResource(R.drawable.clear_icon)
                            draw_permission_status.setColorFilter(
                                resources.getColor(
                                    R.color.danger_color,
                                    context?.theme
                                )
                            )
                        }*/
                        if (requireContext().isLightweightVersion()) {
                            btn_display.hide()
                            draw_permission_status.show()
                            draw_permission_status.setImageResource(R.drawable.clear_icon)
                            draw_permission_status.setColorFilter(
                                resources.getColor(
                                    R.color.danger_color,
                                    context?.theme
                                )
                            )
                        }

                        if (isDisplayAccepted) {
                            btn_display.hide()
                            draw_permission_status.show()
                            draw_permission_status.setImageResource(R.drawable.check_icon)
                            draw_permission_status.setColorFilter(
                                resources.getColor(
                                    R.color.safe_color,
                                    context?.theme
                                )
                            )
                        } else {
                            //btn_display.show()
                            //draw_permission_status.hide()
                            if (!requireContext().isLightweightVersion()) {
                                btn_display.show()
                                draw_permission_status.hide()
                            } else {
                                btn_display.hide()
                                draw_permission_status.show()
                                draw_permission_status.setImageResource(R.drawable.clear_icon)
                                draw_permission_status.setColorFilter(
                                    resources.getColor(
                                        R.color.danger_color,
                                        context?.theme
                                    )
                                )
                            }
                        }

                        if (isUsageAccepted) {
                            btn_usage.hide()
                            usage_permission_status.show()
                            usage_permission_status.setImageResource(R.drawable.check_icon)
                            usage_permission_status.setColorFilter(
                                resources.getColor(
                                    R.color.safe_color,
                                    context?.theme
                                )
                            )
                        } else {
                            btn_usage.show()
                            usage_permission_status.hide()
                        }
                    }
                } else {
                    // switch to unlock
                    lifecycleScope.launch {
                        usersDB.appDao().unLockApp(pkg)
                        CoroutineScope(Dispatchers.Main).launch {
                            val viewHolder =
                                binding.applicationsRecyclerView.findViewHolderForAdapterPosition(
                                    position
                                ) as AppsAdapter.ViewHolder?
                            viewHolder?.let {
                                it.lock_icon.setImageResource(R.drawable.lock_open_icon)
                                it.lock_icon.tag = resources.getString(R.string.unlock)
                                it.txt_status.text = resources.getString(R.string.unlock)
                            }
                        }
                    }
                }
            }
        } else {
            val bundle = Bundle()
            bundle.putParcelable(ITEM, list[position])
            bundle.putParcelableArrayList(LIST, ArrayList(list))
            findNavController().navigate(R.id.passwordFragment, bundle)
        }
    }

    override fun onLongItemClickListener(position: Int, extras: Bundle?) {

    }
}