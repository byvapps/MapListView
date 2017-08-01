package com.inlacou.byvapps.galdakao.rx

import android.support.annotation.CheckResult

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

class GoogleMapCameraMoveObs internal constructor(internal val view: GoogleMap) : ObservableOnSubscribe<LatLng> {

    @Throws(Exception::class)
    override fun subscribe(subscriber: ObservableEmitter<LatLng>) {
        view.setOnCameraMoveListener { subscriber.onNext(view.cameraPosition.target) }

        subscriber.setCancellable { view.setOnCameraMoveListener(null) }
    }

    companion object {
        @CheckResult
        fun init(view: GoogleMap): Observable<LatLng> {
            return Observable.create(GoogleMapCameraMoveObs(view))
        }
    }
}