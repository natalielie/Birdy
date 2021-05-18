package com.example.birdyapp

import android.app.Application
import com.example.birdyapp.db.BirdsDatabase
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.identity.CredentialsProviderImpl
import com.example.birdyapp.identity.KycProvider
import com.example.birdyapp.identity.KycProviderImpl
import com.example.birdyapp.util.ConnectivityInterceptor
import com.example.birdyapp.util.ToastManager
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class App: Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@App))

        //utils
        bind() from provider {ToastManager(instance())}

        //credentials on preferences
        bind<CredentialsProvider>() with provider { CredentialsProviderImpl(this@App) }
        bind<KycProvider>() with provider { KycProviderImpl(this@App) }

        //db
        bind() from provider { BirdsDatabase (instance()) }

        bind() from provider { instance<BirdsDatabase>().offlineBirdsInfo() }



        //network
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptor() }
    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

    }
}