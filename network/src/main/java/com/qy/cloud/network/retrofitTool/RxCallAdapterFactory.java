package com.qy.cloud.network.retrofitTool;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

/**
 * Created by xiaowen on 26/10/2017.
 */
public final class RxCallAdapterFactory extends CallAdapter.Factory {

    private final RxJava3CallAdapterFactory original;

    public static RxCallAdapterFactory create() {
        return new RxCallAdapterFactory();
    }

    private RxCallAdapterFactory() {
        original = RxJava3CallAdapterFactory.create();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        return new Rx2CallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static final class Rx2CallAdapterWrapper<R> implements CallAdapter<R, Object> {

        private final Retrofit retrofit;
        private final CallAdapter<R, Object> wrapped;

        Rx2CallAdapterWrapper(Retrofit retrofit, CallAdapter<R, Object> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object adapt(@NonNull Call<R> call) {
            Object adaptResult = wrapped.adapt(call);
            if (adaptResult instanceof Single) {
                return ((Single) adaptResult).onErrorResumeNext(new Function<Throwable, SingleSource>() {
                    @Override
                    public SingleSource apply(Throwable throwable) {
                        return Single.error(asRetrofitException(throwable));
                    }
                });
            }
            if (adaptResult instanceof Observable) {
                return ((Observable) adaptResult).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends Response<R>>>() {
                    @Override
                    public ObservableSource<? extends Response<R>> apply(Throwable throwable) {
                        return Observable.error(asRetrofitException(throwable));
                    }
                });
            }
            if (adaptResult instanceof Completable) {
                return ((Completable) adaptResult).onErrorResumeNext(new Function<Throwable, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Throwable throwable) {
                        return Completable.error(asRetrofitException(throwable));
                    }
                });
            }
            if (adaptResult instanceof Flowable) {
                return ((Flowable) adaptResult).onErrorResumeNext(new Function<Throwable, Publisher>() {
                    @Override
                    public Publisher apply(Throwable throwable) {
                        return Flowable.error(asRetrofitException(throwable));
                    }
                });
            }
            if (adaptResult instanceof Maybe) {
                return ((Maybe) adaptResult).onErrorResumeNext(new Function<Throwable, MaybeSource>() {
                    @Override
                    public MaybeSource apply(Throwable throwable) {
                        return Maybe.error(asRetrofitException(throwable));
                    }
                });
            }
            return adaptResult;
        }

        private RetrofitException asRetrofitException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                Response response = httpException.response();
                return RetrofitException.httpError(response.raw().request().url().toString(), response, retrofit);
            }
            // A json error happened
            if (throwable instanceof JsonProcessingException) {
                return RetrofitException.jsonError((JsonProcessingException) throwable);
            }
            // A network error happened
            if (throwable instanceof IOException) {
                return RetrofitException.networkError((IOException) throwable);
            }
            // We don't know what happened. We need to simply convert to an unknown error
            return RetrofitException.unexpectedError(throwable);
        }
    }
}