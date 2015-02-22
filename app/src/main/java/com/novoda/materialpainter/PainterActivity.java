package com.novoda.materialpainter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.materialpainter.view.PaletteView;
import com.novoda.notils.caster.Views;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PainterActivity extends ActionBarActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final int ANIMATION_START_DELAY = 300;
    private static final int ANIMATION_DURATION = 400;
    private static final float TENSION = 1.f;

    private TextView startingText;
    private PaletteView paletteView;
    private ImageButton selectImage;
    private ImageView imageView;
    private Toolbar toolbar;

    private int fabHideTranslationY;
    private int toolbarHideTranslationY;

    private boolean viewsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painter);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        fabHideTranslationY = 2 * getResources().getDimensionPixelOffset(R.dimen.fab_min_size);
        toolbarHideTranslationY = -2 * getResources().getDimensionPixelOffset(R.dimen.toolbar_min_size);

        initViews();
        setListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                showImage(uri);
            }
        }
    }

    private void initViews() {
        startingText = Views.findById(this, R.id.starting_text);
        paletteView = Views.findById(this, R.id.palette);
        imageView = Views.findById(this, R.id.show_image);
        selectImage = Views.findById(this, R.id.fab_select_image);
    }

    private void setListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewsVisible) {
                    hideViews();
                } else {
                    showViews();
                }
            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTranslateAnimationWithEndAction(selectImage, fabHideTranslationY, performImageSearchRunnable);
            }
        });
    }

    private Runnable performImageSearchRunnable =
            new Runnable() {
                @Override
                public void run() {
                    performFileSearch();
                }
            };

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void showImage(Uri uri) {
        try {
            Bitmap image = parcelImage(uri);
            generatePalette(image);
            hideViews();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Bitmap parcelImage(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor;
        parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        imageView.setImageBitmap(image);
        parcelFileDescriptor.close();
        return image;
    }

    private void generatePalette(Bitmap image) {
        Palette.generateAsync(image, new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                if (palette != null) {
                    paletteView.updateWith(palette);
                }
            }
        });
    }

    private void showViews() {
        runTranslateAnimation(selectImage, 0);
        runTranslateAnimation(toolbar, 0);
        viewsVisible = true;
    }

    private void hideViews() {
        runTranslateAnimation(selectImage, fabHideTranslationY);
        runTranslateAnimation(toolbar, toolbarHideTranslationY);
        startingText.setVisibility(View.GONE);
        viewsVisible = false;
    }

    private void runTranslateAnimation(View view, int translateY) {
        view.animate()
                .translationY(translateY)
                .setInterpolator(new OvershootInterpolator(TENSION))
                .setStartDelay(ANIMATION_START_DELAY)
                .setDuration(ANIMATION_DURATION)
                .start();

    }

    private void runTranslateAnimationWithEndAction(View view, int translateY, Runnable runnable) {
        view.animate()
                .translationY(translateY)
                .setInterpolator(new OvershootInterpolator(TENSION))
                .setStartDelay(ANIMATION_START_DELAY)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(runnable)
                .start();

    }
}
