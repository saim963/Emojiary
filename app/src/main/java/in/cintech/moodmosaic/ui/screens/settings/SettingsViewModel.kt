package `in`.cintech.moodmosaic.ui.screens.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.cintech.moodmosaic.utils.BillingManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel() {

    val products = billingManager.donationProducts

    fun purchaseDonation(activity: Activity, productDetails: com.android.billingclient.api.ProductDetails) {
        billingManager.launchBillingFlow(activity, productDetails)
    }
}