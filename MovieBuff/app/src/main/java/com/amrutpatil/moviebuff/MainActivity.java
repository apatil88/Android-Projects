package com.amrutpatil.moviebuff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback  {

    private boolean mTwoPane;

    private MovieInfo mMovieInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Converting the image into a bitmap
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.film);
        Palette palette = Palette.from(img).generate();

        Palette.from(img).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

            }
        });
        // Getting the different types of colors from the Image
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        float[] vibrant = vibrantSwatch.getHsl();

        Palette.Swatch vibrantDarkSwatch = palette.getDarkVibrantSwatch();
        float[] vibrantdark = vibrantDarkSwatch.getHsl();

        // Changing the background color of the toolbar to Vibrant Light Swatch
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.HSVToColor(vibrant)));

        if (Build.VERSION.SDK_INT >= 21) { //  setStatusBarColor only works above API 21!
            getWindow().setStatusBarColor(Color.HSVToColor(vibrantdark));
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(),
                                DetailActivityFragment.TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(MovieInfo movieInfo) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailActivityFragment.DETAIL_MOVIE, movieInfo);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DetailActivityFragment.TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivityFragment.DETAIL_MOVIE, movieInfo);
            startActivity(intent);
        }
    }
}
