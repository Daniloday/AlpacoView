package com.missclick.alpacoview

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File

class AlpacoView(context : Context) : WebView(context) {

    private var file : ValueCallback<Array<Uri>>? = null
    private var uri : Uri? = null


     init {
         settings.allowFileAccess = true
         settings.javaScriptCanOpenWindowsAutomatically = true
         settings.allowContentAccess = true
         CookieManager.getInstance().setAcceptCookie(true)
         settings.databaseEnabled = true
         settings.allowUniversalAccessFromFileURLs = true
         CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
         settings.javaScriptEnabled = true
         settings.useWideViewPort = true
         layoutParams = ViewGroup.LayoutParams(
             ViewGroup.LayoutParams.MATCH_PARENT,
             ViewGroup.LayoutParams.MATCH_PARENT
         )
         settings.domStorageEnabled = true
         settings.allowFileAccessFromFileURLs = true
         settings.mixedContentMode = 0
         settings.cacheMode = WebSettings.LOAD_DEFAULT
         settings.loadWithOverviewMode = true
         webViewClient = object : WebViewClient(){
             override fun shouldOverrideUrlLoading(
                 view: WebView?,
                 request: WebResourceRequest?
             ): Boolean {
                 val url = request?.url.toString()
                 if (url.startsWith("http://") || url.startsWith("https://")) {
                     return false
                 } else {
                     try {
                         val intent: Intent
                         if (url.startsWith("intent:")) {
                             intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                             intent.addCategory(Intent.CATEGORY_BROWSABLE)
                        if (intent.action == "com.google.firebase.dynamiclinks.VIEW_DYNAMIC_LINK"){
                            intent.extras?.getString("browser_fallback_url")
                                ?.let { view?.loadUrl(it) }
                            return true
                        }
                         } else {
                             intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         }
                         view!!.context.startActivity(intent)

                     } catch (_: Exception) {
                     }
                     return true
                 }

             }
         }


     }

    fun setPermission(activity : Activity){
        webChromeClient = object : WebChromeClient(){
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                file = filePathCallback
                activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), 643)
                return true
            }
        }
    }

    fun setPermission(activity : ActivityResultLauncher<String>){
        webChromeClient = object : WebChromeClient(){
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                file = filePathCallback
                activity.launch(Manifest.permission.CAMERA)
                return true
            }
        }
    }

    fun setPermission(activity :  ManagedActivityResultLauncher<String, Boolean>){
        webChromeClient = object : WebChromeClient(){
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                file = filePathCallback
                activity.launch(Manifest.permission.CAMERA)
                return true
            }
        }
    }


    fun permissionResult(it : Boolean, activity: Activity){
        if (!it){
            file?.onReceiveValue(null)
        }else{
            //files
            val getContent = Intent(Intent.ACTION_GET_CONTENT)
            getContent.type = "*/*"
            getContent.addCategory(Intent.CATEGORY_OPENABLE)

            val fileTemp = File.createTempFile("image", ".jpg", context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES))
            uri =  FileProvider.getUriForFile(context,
                context.packageName,fileTemp)
            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri)

            val putter = Intent(Intent.ACTION_CHOOSER)
            putter.putExtra(Intent.EXTRA_INTENT, getContent)
            putter.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(camIntent))
            activity.startActivityForResult(putter, 72)
        }
    }

    fun permissionResult(it : Boolean, activity:  ManagedActivityResultLauncher<Intent, ActivityResult>){
        if (!it){
            file?.onReceiveValue(null)
        }else{
            //files
            val getContent = Intent(Intent.ACTION_GET_CONTENT)
            getContent.type = "*/*"
            getContent.addCategory(Intent.CATEGORY_OPENABLE)

            val fileTemp = File.createTempFile("image", ".jpg", context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES))
            uri =  FileProvider.getUriForFile(context,
                context.packageName,fileTemp)
            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri)

            val putter = Intent(Intent.ACTION_CHOOSER)
            putter.putExtra(Intent.EXTRA_INTENT, getContent)
            putter.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(camIntent))
            activity.launch(putter)
        }
    }

    fun permissionResult(it : Boolean, activity: ActivityResultLauncher<Intent>){
        if (!it){
            file?.onReceiveValue(null)
        }else{
            //files
            val getContent = Intent(Intent.ACTION_GET_CONTENT)
            getContent.type = "*/*"
            getContent.addCategory(Intent.CATEGORY_OPENABLE)

            val fileTemp = File.createTempFile("image", ".jpg", context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES))
            uri =  FileProvider.getUriForFile(context,
                context.packageName,fileTemp)
            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri)

            val putter = Intent(Intent.ACTION_CHOOSER)
            putter.putExtra(Intent.EXTRA_INTENT, getContent)
            putter.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(camIntent))
            activity.launch(putter)
        }
    }

    fun onActivityResult(it : ActivityResult){
        if (it.resultCode == -1){
            if (it.data?.data == null){
                if (uri != null){
                    file?.onReceiveValue(arrayOf(uri!!))
                }else{
                    file?.onReceiveValue(null)
                }
            }else{
                file?.onReceiveValue(arrayOf(it.data!!.data!!))
            }
        }else{
            file?.onReceiveValue(null)
        }
    }

    fun onActivityResult(resultCode : Int, data : Intent?){
        if (resultCode == -1){
            if (data?.data == null){
                if (uri != null){
                    file?.onReceiveValue(arrayOf(uri!!))
                }else{
                    file?.onReceiveValue(null)
                }
            }else{
                file?.onReceiveValue(arrayOf(data.data!!))
            }
        }else{
            file?.onReceiveValue(null)
        }
    }

    fun back(){
        println("back 2")
        if (canGoBack()) goBack()
    }



}