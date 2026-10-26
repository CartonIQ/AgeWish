package com.agewish.calculator.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    // Official Google AdMob Test Ad Unit IDs
    const val BANNER_TEST_ID = "ca-app-pub-9771662959477951/5625491997"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-9771662959477951/3481624513"
    const val APP_OPEN_TEST_ID = "ca-app-pub-9771662959477951/4770655456"

    private const val PREFS_NAME = "agewish_ad_prefs"
    private const val KEY_LAST_APP_OPEN_TIME = "last_app_open_time"
    private const val KEY_PDF_GENERATED_COUNT = "pdf_ad_counter"
    private const val ONE_HOUR_MILLIS = 3600000L

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    fun loadBanner(adView: AdView?) {
        adView?.let {
            val adRequest = AdRequest.Builder().build()
            it.loadAd(adRequest)
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialIfDue(activity: Activity, onAction: () -> Unit) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(KEY_PDF_GENERATED_COUNT, 0) + 1
        prefs.edit().putInt(KEY_PDF_GENERATED_COUNT, currentCount).apply()

        // Show interstitial every 2nd "Generate PDF" tap
        val isDue = (currentCount % 2 == 0)

        if (isDue && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAction()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAction()
                }
            }
            interstitialAd?.show(activity)
        } else {
            // Not due or ad not ready yet
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
            onAction()
        }
    }

    fun showAppOpenOnColdStart(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastShown = prefs.getLong(KEY_LAST_APP_OPEN_TIME, 0L)
        val now = System.currentTimeMillis()

        if (now - lastShown < ONE_HOUR_MILLIS) {
            return
        }

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            APP_OPEN_TEST_ID,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    prefs.edit().putLong(KEY_LAST_APP_OPEN_TIME, System.currentTimeMillis()).apply()
                    appOpenAd.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Fail silently
                }
            }
        )
    }
}
