package com.mrugen_practicle.ui.search;

import android.annotation.SuppressLint;
import android.support.v7.widget.SearchView;
import android.util.Log;

import com.mrugen_practicle.Constants;
import com.mrugen_practicle.models.Example;
import com.mrugen_practicle.network.NetworkClient;
import com.mrugen_practicle.network.NetworkInterface;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public class SearchPresenter implements SearchPresenterInterface, Constants {

    private String TAG = "SearchPresenter";
    SearchViewInterface searchviewInterface;

    public SearchPresenter(SearchViewInterface searchViewInterface) {
        this.searchviewInterface = searchViewInterface;
    }


    @SuppressLint("CheckResult")
    @Override
    public void getResultsBasedOnQuery(SearchView searchView) {

        getObservableQuery(searchView)
                .filter(s -> {
                    if (s.equals("")) {
                        return false;
                    } else {
                        return true;
                    }
                })
                .debounce(2, TimeUnit.SECONDS)
                .distinctUntilChanged()
                .switchMap((Function<String, ObservableSource<Example>>) s -> NetworkClient.getRetrofit().create(NetworkInterface.class)
                        .getMoviesBasedOnQuery(GIFY_KEY, s))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver());


    }

    private Observable<String> getObservableQuery(SearchView searchView) {

        final PublishSubject<String> publishSubject = PublishSubject.create();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                publishSubject.onNext(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                publishSubject.onNext(newText);
                return true;
            }
        });

        return publishSubject;
    }

    public DisposableObserver<Example> getObserver() {
        return new DisposableObserver<Example>() {

            @Override
            public void onNext(@NonNull Example MovieResponse) {
                Log.d(TAG, "OnNext" + MovieResponse.getPagination().getTotalCount());
                searchviewInterface.displayResult(MovieResponse);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.d(TAG, "Error" + e);
                e.printStackTrace();
                searchviewInterface.displayError("Error fetching Movie Data");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Completed");
            }
        };
    }
}
