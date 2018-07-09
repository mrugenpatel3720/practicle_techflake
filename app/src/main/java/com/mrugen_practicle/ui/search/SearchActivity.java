package com.mrugen_practicle.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mrugen_practicle.MyApplication;
import com.mrugen_practicle.R;
import com.mrugen_practicle.adapters.SearchAdapter;
import com.mrugen_practicle.models.Example;
import com.mrugen_practicle.models.VideoData;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class SearchActivity extends AppCompatActivity implements SearchViewInterface {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.rvQueryResult)
    RecyclerView rvQueryResult;

    private SearchView searchView;
    SearchPresenter searchPresenter;
    RecyclerView.Adapter adapter;

    private BoxStore boxStore;
    private Box<VideoData> videoDataBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        boxStore = ((MyApplication) getApplication()).getBoxStore();
        videoDataBox = boxStore.boxFor(VideoData.class);

        setupViews();
        setupMVP();

    }

    private void setupViews() {

        setSupportActionBar(toolbar);
        rvQueryResult.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void setupMVP() {
        searchPresenter = new SearchPresenter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.search));

        searchPresenter.getResultsBasedOnQuery(searchView);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showToast(String str) {
        Toast.makeText(SearchActivity.this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void displayResult(Example movieResponse) {
        adapter = new SearchAdapter(movieResponse.getData(), SearchActivity.this, videoDataBox);
        rvQueryResult.setAdapter(adapter);
    }

    @Override
    public void displayError(String s) {
        showToast(s);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
