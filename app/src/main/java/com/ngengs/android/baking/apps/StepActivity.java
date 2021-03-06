/*******************************************************************************
 * Copyright (c) 2017 Rizky Kharisma (@ngengs)
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package com.ngengs.android.baking.apps;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngengs.android.baking.apps.data.Step;
import com.ngengs.android.baking.apps.fragments.StepFragment;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class StepActivity extends AppCompatActivity {
    private FrameLayout mFragmentStepLayout;
    private ImageView mButtonStepPrev;
    private TextView mStepIndicator;
    private ImageView mButtonStepNext;
    private View mToolsStep;


    private List<Step> mData;
    private int mActivePosition;
    private StepFragment mStepFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_step);
        mToolsStep = findViewById(R.id.tools_step);
        mButtonStepNext = findViewById(R.id.button_step_next);
        mButtonStepPrev = findViewById(R.id.button_step_prev);
        mStepIndicator = findViewById(R.id.step_indicator);
        mFragmentStepLayout = findViewById(R.id.fragment_step);

        mButtonStepPrev.setOnClickListener(this::onViewClicked);
        mButtonStepNext.setOnClickListener(this::onViewClicked);

        mFragmentManager = getSupportFragmentManager();
        mActivePosition = 0;
        mData = new ArrayList<>();

        if (mFragmentStepLayout != null) {
            if (savedInstanceState == null) {
                if (getIntent().getExtras() != null) {
                    List<Step> temp = getIntent().getExtras().getParcelableArrayList("DATA");
                    if (temp != null) {
                        mData.addAll(temp);
                    }
                    mActivePosition = getIntent().getExtras().getInt("POSITION");
                }
                mStepFragment = StepFragment.newInstance(mData.get(mActivePosition),
                                                         isFullScreen());
                mFragmentManager.beginTransaction()
                                .add(mFragmentStepLayout.getId(), mStepFragment)
                                .commit();
            } else {
                List<Step> temp = savedInstanceState.getParcelableArrayList("DATA");
                if (temp != null) {
                    mData.addAll(temp);
                }
                mActivePosition = savedInstanceState.getInt("POSITION");
                mStepFragment = (StepFragment) mFragmentManager.findFragmentById(
                        mFragmentStepLayout.getId());
                mStepFragment.setFullScreen(isFullScreen());
            }
        }

        changeStatusPrevNextButton();
        changeStep(mActivePosition, false, true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("DATA", new ArrayList<Parcelable>(mData));
        outState.putInt("POSITION", mActivePosition);
        super.onSaveInstanceState(outState);
    }

    private void changeLayoutToFullscreen(boolean fullScreen) {
        if (fullScreen) {
            Timber.d("changeLayoutToFullscreen: %s", "landscape");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else {
            Timber.d("changeLayoutToFullscreen: %s", "portrait");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }
        mToolsStep.setVisibility(fullScreen ? View.GONE : View.VISIBLE);
    }

    private void changeStatusPrevNextButton() {
        Timber.d("changeStatusPrevNextButton: %s %s", mActivePosition, mData.size());
        boolean activePrev = mActivePosition > 0;
        mButtonStepPrev.setClickable(activePrev);
        mButtonStepPrev.setVisibility((activePrev) ? View.VISIBLE : View.INVISIBLE);
        boolean activeNext = mActivePosition < (mData.size() - 1);
        mButtonStepNext.setClickable(activeNext);
        mButtonStepNext.setVisibility((activeNext) ? View.VISIBLE : View.INVISIBLE);
    }

    private void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_step_prev:
                if (mActivePosition > 0) {
                    changeStep(mActivePosition - 1, false, false);
                }
                break;
            case R.id.button_step_next:
                if (mActivePosition < (mData.size() - 1)) {
                    changeStep(mActivePosition + 1, false, false);
                }
                break;
            default:
                Timber.e("onViewClicked: %s", "The view id in clicked view is not known");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeTitle(@NonNull String title) {
        setTitle(title);
    }

    private boolean isFullScreen() {
        boolean fullScreen = (!getResources().getBoolean(R.bool.isTablet)
                              && (getResources().getConfiguration().orientation
                                  == Configuration.ORIENTATION_LANDSCAPE)
                              && !TextUtils.isEmpty(mData.get(mActivePosition).getVideoURL()));
        Timber.d("isFullScreen() returned: %s", fullScreen);
        return fullScreen;
    }

    private void changeStep(int positionNow, boolean forceExitFullScreen, boolean fromStart) {
        Timber.d("changeStep() called with: positionNow = [%s], forceExitFullScreen = [%s], "
                 + "fromStart = [%s]", positionNow, forceExitFullScreen, fromStart);
        mActivePosition = positionNow;
        mStepIndicator.setText(
                String.format("%s/%s", mActivePosition, (mData.size() - 1)));
        changeTitle(mData.get(mActivePosition).getShortDescription());
        changeStatusPrevNextButton();

        boolean fullScreen = !forceExitFullScreen && isFullScreen();

        if (!fromStart) {
            mStepFragment = StepFragment.newInstance(mData.get(mActivePosition), fullScreen);
            mFragmentManager.beginTransaction()
                            .replace(mFragmentStepLayout.getId(), mStepFragment)
                            .commit();
        }
        if (!TextUtils.isEmpty(mData.get(mActivePosition).getVideoURL())) {
            changeLayoutToFullscreen(fullScreen);
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen() && mStepFragment.isFullScreen()) {
            changeStep(mActivePosition, true, false);
        } else {
            super.onBackPressed();
        }
    }
}
