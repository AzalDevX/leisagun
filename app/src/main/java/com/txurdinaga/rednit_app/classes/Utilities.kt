package com.txurdinaga.rednit_app.classes
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class Utilities : Application(){
    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        context.createConfigurationContext(configuration)
        context.resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}