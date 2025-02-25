package expo.modules.splashscreen

import android.app.Dialog
import android.view.View
import android.view.Window
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class SplashSCModule : Module() {
    private var splashDialog: Dialog? = null

    override fun definition() = ModuleDefinition {
        Name("SplashSC")

        OnCreate {
            showSplashScreen()
        }
        
        Function("show") {
            runOnUiThread {
                val activity = appContext.activityProvider?.currentActivity
                activity?.let {
                    if (splashDialog == null) {
                        splashDialog = Dialog(it, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                            requestWindowFeature(Window.FEATURE_NO_TITLE)
                            setCancelable(false)
                            setContentView(it.resources.getIdentifier("splash_screen_custom", "layout", it.packageName))
                        }
                    }
                    splashDialog?.show()
                }
            }
        }

        Function("hide") {
            runOnUiThread {
                splashDialog?.dismiss()
                splashDialog = null

                val activity = appContext.activityProvider?.currentActivity
                activity?.findViewById<View>(android.R.id.content)?.visibility = View.VISIBLE
            }
        }
    }

    private fun showSplashScreen() {
        runOnUiThread {
            val activity = appContext.activityProvider?.currentActivity
            activity?.let {
                if (splashDialog == null) {
                    splashDialog = Dialog(it, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        setCancelable(false)
                        setContentView(it.resources.getIdentifier("splash_screen_custom", "layout", it.packageName))
                    }
                }
                splashDialog?.show()
            }
        }
    }
    
    private fun runOnUiThread(action: () -> Unit) {
        appContext.activityProvider?.currentActivity?.runOnUiThread(action)
    }
}
