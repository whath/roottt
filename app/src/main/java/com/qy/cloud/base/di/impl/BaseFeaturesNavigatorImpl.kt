package com.qy.cloud.base.di.impl

import com.qy.cloud.MainActivity
import com.qy.cloud.base.BaseFeaturesNavigator

class BaseFeaturesNavigatorImpl : BaseFeaturesNavigator {
    override fun getAccessActivityClass(): Class<*> = MainActivity::class.java
}
