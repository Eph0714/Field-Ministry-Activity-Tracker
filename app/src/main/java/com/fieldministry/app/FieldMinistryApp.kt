package com.fieldministry.app

import android.app.Application
import com.fieldministry.app.di.ServiceLocator

class FieldMinistryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
