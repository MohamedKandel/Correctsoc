package com.correct.correctsoc.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.correct.correctsoc.R
import com.correct.correctsoc.databinding.FragmentLiveChatBinding
import com.correct.correctsoc.helper.Constants.CHAT_LINK
import com.correct.correctsoc.helper.HelperClass

class LiveChatFragment : Fragment() {

    private lateinit var binding: FragmentLiveChatBinding
    private lateinit var helper: HelperClass

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLiveChatBinding.inflate(inflater,container,false)
        helper = HelperClass.getInstance()

        binding.webChat.webViewClient = WebViewClient()

        binding.webChat.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                Log.d("web Progress mohamed", "$newProgress")
            }
        }

        binding.webChat.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.d("web mohamed", "${error?.errorCode}")
            }
        }

        binding.webChat.settings.javaScriptEnabled = true
        binding.webChat.settings.domStorageEnabled = true
        binding.webChat.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webChat.loadUrl(CHAT_LINK)

        helper.onBackPressed(this) {
            binding.webChat.clearCache(true)
            binding.webChat.clearHistory()
            binding.webChat.clearSslPreferences()
            findNavController().navigate(R.id.homeFragment)
        }

        return binding.root
    }
}