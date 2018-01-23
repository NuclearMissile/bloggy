package nuclear.com.bloggy.Util

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import nuclear.com.bloggy.Entity.NoResultWrapper
import nuclear.com.bloggy.Entity.ResultWrapper

fun <T> Observable<T>.allIOSchedulers(): Observable<T> = subscribeOn(Schedulers.io()).observeOn(Schedulers.io())

fun <T> Observable<T>.defaultSchedulers(): Observable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.computeSchedulers(): Observable<T> = subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.allIOSchedulers(): Flowable<T> = subscribeOn(Schedulers.io()).observeOn(Schedulers.io())

fun <T> Flowable<T>.defaultSchedulers(): Flowable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.computeSchedulers(): Flowable<T> = subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun Flowable<NoResultWrapper>.checkApiErrorN(): Flowable<NoResultWrapper> {
    return this.map {
        if (it.isSuccess)
            it
        else
            throw Exception()
    }
}

fun <T> Flowable<ResultWrapper<T>>.checkApiError(): Flowable<T> {
    return this.map {
        if (it.isSuccess)
            it.result
        else
            throw Exception()
    }
}

fun <T> Single<T>.allIOSchedulers(): Single<T> = subscribeOn(Schedulers.io()).observeOn(Schedulers.io())

fun <T> Single<T>.defaultSchedulers(): Single<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.computeSchedulers(): Single<T> = subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.allIOSchedulers(): Maybe<T> = subscribeOn(Schedulers.io()).observeOn(Schedulers.io())

fun <T> Maybe<T>.defaultSchedulers(): Maybe<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.computeSchedulers(): Maybe<T> = subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
