package io.horizontalsystems.bankwallet.modules.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.bankwallet.BaseActivity
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.modules.pin.lockscreen.LockScreenModule
import io.horizontalsystems.bankwallet.modules.pin.lockscreen.LockScreenPresenter
import io.horizontalsystems.bankwallet.modules.pin.lockscreen.LockScreenRouter
import kotlinx.android.synthetic.main.activity_pin_container.*


class LockScreenActivity : BaseActivity() {

    private lateinit var presenter: LockScreenPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_container)

        val pinFragment = PinFragment().apply {
            arguments = intent.extras
        }

        val showCancelButton = intent?.extras?.getBoolean(keyShowCancel) ?: true
        val showRates = intent?.extras?.getBoolean(keyShowRates) ?: false

        presenter = ViewModelProviders.of(this, LockScreenModule.Factory(showCancelButton)).get(LockScreenPresenter::class.java)
        val router = presenter.router as LockScreenRouter

        subscribeToRouterEvents(router)

        if (showRates) {
            viewPager.visibility = View.VISIBLE
            circleIndicator.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE

            val fragments = listOf(pinFragment, RatesFragment())

            viewPager.adapter = PinViewPagerAdapter(fragments, supportFragmentManager)

            circleIndicator.setViewPager(viewPager)
        } else {
            viewPager.visibility = View.GONE
            circleIndicator.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, pinFragment)
                        .commit()
            }
        }

    }

    private fun subscribeToRouterEvents(router: LockScreenRouter) {
        router.closeApplication.observe(this, Observer {
            finishAffinity()
        })

        router.closeActivity.observe(this, Observer {
            setResult(PinModule.RESULT_CANCELLED)
            finish()
        })
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    companion object {

        const val keyInteractionType = "interaction_type"
        const val keyShowCancel = "show_cancel"
        const val keyShowRates = "show_rates"

        fun startForResult(context: AppCompatActivity, interactionType: PinInteractionType, requestCode: Int = 0, showCancel: Boolean = true, showRates: Boolean = false) {
            val intent = Intent(context, LockScreenActivity::class.java)
            intent.putExtra(keyInteractionType, interactionType)
            intent.putExtra(keyShowCancel, showCancel)
            intent.putExtra(keyShowRates, showRates)

            context.startActivityForResult(intent, requestCode)
        }
    }
}