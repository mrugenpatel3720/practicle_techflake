package com.mrugen_practicle.ui.search;


import com.mrugen_practicle.models.Example;


public interface SearchViewInterface {

    void showToast(String str);
    void displayResult(Example movieResponse);
    void displayError(String s);
    void showProgressBar();
    void hideProgressBar();
}
