package com.scan.imagetotext.Utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.scan.imagetotext.R

class Fun(context: Context, activity: Activity) {

    private fun checkAds() {
        val prefs = Prefs(activity)
        removeAds = prefs.isRemoveAd
    }

    init {
        checkAds()
    }

    companion object {
        lateinit var context: Context
        var appurl = ""
        private var count = 0
        private const val countfc = 0
        private var mInterstitialAd: InterstitialAd? = null
        private var mRewardedAd: RewardedAd? = null
        private const val divider = 3
        private const val fc = 2
        var admobon = "0"
        lateinit var activity: Activity
        var adView: AdView? = null
        var sc = "8"
        var removeAds = false
        var nc = 15


        fun showBannerAds(adContainerView: FrameLayout, activity: Activity) {
            // Step 1 - Create an AdView and set the ad unit ID on it.
            adView = AdView(activity)
            adView?.setAdUnitId(activity.getString(R.string.admob_banner_id))
            adContainerView.addView(adView)
            if (removeAds) {
            } else {
                if (admobon != "0") {
                    loadBanner(activity)
                }
            }
        }

        fun rateApp() {
            val uri = Uri.parse("market://details?id=" + activity.getPackageName())
            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                activity.startActivity(myAppLinkToMarket)
            } catch (e: ActivityNotFoundException) {
                // Toast.makeText(activity, " unable to find market app", Toast.LENGTH_LONG).show();
            }
        }

        private fun loadBanner(activity: Activity) {
            // Create an ad request. Check your logcat output for the hashed device ID
            // to get test ads on a physical device, e.g.,
            // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
            // device."
            val adRequest = AdRequest.Builder()
                .build()
            val adSize = getAdSize(activity)
            // Step 4 - Set the adaptive ad size on the ad view.
            adView?.setAdSize(adSize)


            // Step 5 - Start loading the ad in the background.
            adView?.loadAd(adRequest)
        }

        private fun getAdSize(activity: Activity): AdSize {
            // Step 2 - Determine the screen width (less decorations) to use for the ad width.
            val display: Display = activity.getWindowManager().getDefaultDisplay()
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels: Float = outMetrics.widthPixels.toFloat()
            val density: Float = outMetrics.density
            val adWidth = (widthPixels / density).toInt()

            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }

        fun checkInternet(): Boolean {
            val connectivityManager: ConnectivityManager =
                activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info: NetworkInfo? = connectivityManager.getActiveNetworkInfo()
            return info != null && info.isConnected()
        }

        private const val ac = 0
        fun addShowreward() {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity, activity.getString(R.string.admob_reward_id),
                adRequest, object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error.
                        mRewardedAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        mRewardedAd = rewardedAd
                    }
                })
            if (mRewardedAd != null) {
                mRewardedAd?.show(activity, object : OnUserEarnedRewardListener {
                    override fun onUserEarnedReward(rewardItem: RewardItem) {
                        // Handle the reward.
                        val rewardAmount: Int = rewardItem.getAmount()
                        val rewardType: String = rewardItem.getType()
                    }
                })
            } else {
                addShowAdmob()
            }
        }

        fun addShowAdmob() {
            if (removeAds) {
            } else {
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(
                    activity, activity.getString(R.string.admob_insta_id), adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {

                            // an ad is loaded.
                            mInterstitialAd = interstitialAd
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show(activity)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.i("MainActivity", loadAdError.getMessage())
                            mInterstitialAd = null
                        }
                    })
            }
        }

        fun addShow() {
            count++
            if (removeAds) {
            } else {
                if (admobon != "0") {
                    if (count % divider == 0) {
                        addShowAdmob()
                    }
                    if (count % fc == 0) {
                        addShowreward()
                    }
                }
            }
        }

        fun copyItem(s: String?) {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipe: ClipData = ClipData.newPlainText("LINK", s)
            if (clipboard != null) {
                clipboard.setPrimaryClip(clipe)
                Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
            }
        }

        fun haveStoragePermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    //  Log.e("Permission error", "You have permission");
                    true
                } else {

                    //  Log.e("Permission error", "You have asked for permission");
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                    Toast.makeText(
                        activity,
                        "Need to Permission for Download",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            } else { //you dont need to worry about these stuff below api level 23
                //  Log.e("Permission error", "You already have the permission");
                true
            }
        }
    }
}
