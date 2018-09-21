package com.openlocate.android.core

import com.firebase.jobdispatcher.JobTrigger
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.RetryStrategy.RETRY_POLICY_EXPONENTIAL
import com.firebase.jobdispatcher.Trigger
import com.google.android.gms.location.LocationRequest
import java.util.concurrent.TimeUnit

/**
 * Created by feromakovi <feromakovi@gmail.com> on 21/09/2018.
 */
class Config {

    companion object {
       @JvmStatic val locationRequest: LocationRequest by lazy {
            LocationRequest().apply {
                interval = TimeUnit.MINUTES.toMillis(1)
                fastestInterval = TimeUnit.SECONDS.toMillis(30)
                maxWaitTime = TimeUnit.MINUTES.toMillis(5)
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            }
        }

        @JvmStatic val trigger: JobTrigger by lazy { Trigger.executionWindow(TimeUnit.HOURS.toSeconds(1).toInt(), TimeUnit.HOURS.toSeconds(2).toInt()) }
    }
}