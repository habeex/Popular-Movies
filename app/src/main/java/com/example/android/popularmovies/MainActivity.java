package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.Movies;
import com.example.android.popularmovies.utils.APICallback;
import com.example.android.popularmovies.utils.MoviesAdapter;
import com.example.android.popularmovies.utils.MoviesSyncService;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements APICallback, MoviesAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    public static final String[] MAIN_MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_PLOT,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_POSTER_URL,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_TITLE
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_PLOT = 1;
    public static final int INDEX_MOVIE_FAVORITE = 2;
    public static final int INDEX_MOVIE_POSTER_URL = 3;
    public static final int INDEX_MOVIE_RATING = 4;
    public static final int INDEX_MOVIE_RELEASE_DATE = 5;
    public static final int INDEX_MOVIE_TITLE = 6;

    private RecyclerView mMoviesList;
    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager mLayoutManager;
    private ProgressDialog mProgressDialog;
    private TextView mErrorTextView;

    private static final String POPULAR_MOVIES = "popular";

    private static final String MOST_RATED_MOVIES = "top_rated";

    private static final int MOVIE_LOADER_ID = 778;

//    private ImageView mErrorImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbar();
//        setSupportActionBar(toolbar);

        mMoviesList    = (RecyclerView) findViewById(R.id.movies_list);

        mErrorTextView = (TextView) findViewById(R.id.error_textview);
//        mErrorImage    = (ImageView) findViewById(R.id.error_image);

        mMoviesList.setHasFixedSize(true);


        Intent intent = new Intent(this, MoviesSyncService.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.movie_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_popular) {
            requestData(POPULAR_MOVIES);
            return true;
        } else if (id == R.id.action_highest_rated) {
            requestData(MOST_RATED_MOVIES);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestData(String type) {

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
//
//        Context context = getApplicationContext();
////        GetMovies getMovies = new GetMovies(this, context);
//
//        URL apiDetails = APIDetails.makeResourceUrl(type);
//
////        getMovies.execute(apiDetails);
    }

    @Override
    public void handleData(JSONObject object) throws JSONException {

        mProgressDialog.cancel();

        if (object.has("error")) {

            mMoviesList.setVisibility(View.INVISIBLE);

//            mErrorImage.setVisibility(View.VISIBLE);

            String message = object.getString("error");

            mErrorTextView.setVisibility(View.VISIBLE);
            mErrorTextView.setText(message);


        } else {

            mMoviesAdapter = new MoviesAdapter(this, this);

            setUpRecyclerView();


        }


//        for (Movies movie : movies) {
//
//            mDisplayDetails.append(movie.plot + "/n/n/n");
//        }
    }

    private  int convertDPtoPixel(int dp) {
        Resources resources = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }

    private void setUpRecyclerView() {

//        mErrorImage.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);

        int dpToPixel = convertDPtoPixel(10);
        boolean includeEdge = true;

        GridSpacing gridSpacing = new GridSpacing(2, dpToPixel, includeEdge);
        mLayoutManager = new GridLayoutManager(this, 2);

        if (mMoviesAdapter != null) {
            mMoviesList.setLayoutManager(mLayoutManager);
            mMoviesList.addItemDecoration(gridSpacing);
            mMoviesList.setItemAnimator(new DefaultItemAnimator());
            mMoviesList.setAdapter(mMoviesAdapter);
        }
    }

    @Override
    public void onListItemClick(int itemIndex) {

        Class detailActivity = MovieDetail.class;

        Intent intent = new Intent(this, detailActivity);

        Uri uriForMovieClicked = MovieContract.MovieEntry.buildUriWithId(itemIndex);

        intent.setData(uriForMovieClicked);

        startActivity(intent);
    }

    /*
          Initializing collapsing toolbar
          Will show and hide the toolbar title on scroll
         */
    private void collapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar is expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {

            case MOVIE_LOADER_ID:

                Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;

                return new CursorLoader(
                        this,
                        moviesUri,
                        MAIN_MOVIE_PROJECTION,
                        null,
                        null,
                        null
                );

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mProgressDialog.cancel();
        mMoviesAdapter = new MoviesAdapter(this, this);

        setUpRecyclerView();

        mMoviesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesAdapter.swapCursor(null);
    }
}
