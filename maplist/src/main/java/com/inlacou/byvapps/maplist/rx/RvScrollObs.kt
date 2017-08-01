package com.inlacou.byvapps.galdakao.rx

import android.support.v7.widget.RecyclerView

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

class RvScrollObs private constructor(private val recyclerView: RecyclerView) : ObservableOnSubscribe<RvScrollObs.Result> {

    @Throws(Exception::class)
    override fun subscribe(subscriber: ObservableEmitter<Result>) {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                subscriber.onNext(Result(recyclerView, dx, dy))
            }
        }

        recyclerView.addOnScrollListener(scrollListener)

        subscriber.setCancellable { recyclerView.removeOnScrollListener(scrollListener) }
    }

    companion object {
        fun create(recyclerView: RecyclerView?): Observable<Result> {
            if (recyclerView == null) {
                throw NullPointerException()
            }
            return Observable.create(RvScrollObs(recyclerView))
        }
    }

	class Result(val recyclerView: RecyclerView?, val dx: Int, val dy: Int)

}