/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.agera.step3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.codelabs.agera.R;
import com.example.android.codelabs.agera.RepositorySeekBarListener;
import com.example.android.codelabs.agera.UiUtils;
import com.google.android.agera.MutableRepository;
import com.google.android.agera.Repositories;
import com.google.android.agera.Repository;
import com.google.android.agera.RepositoryConfig;
import com.google.android.agera.Result;
import com.google.android.agera.Updatable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorActivity extends AppCompatActivity {

    private static final String VALUE_2 = "value2";
    private static final String VALUE_1 = "value1";
    private static final String OPERATION_KEY = "operation_key";

    private MutableRepository<Integer> mValue1Repo = Repositories.mutableRepository(0);
    private MutableRepository<Integer> mValue2Repo = Repositories.mutableRepository(0);
    private Repository<Result<String>> mResultRepository;

    private ExecutorService mExecutor;
    private Updatable mValue1TVupdatable;
    private Updatable mValue2TVupdatable;
    private Updatable mResultUpdatable;
    private MutableRepository<Result<Integer>> mOperationSelector =
            Repositories.mutableRepository(Result.absent());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator);
        if (savedInstanceState != null && savedInstanceState.containsKey(VALUE_1)) {
            mValue1Repo.accept(savedInstanceState.getInt(VALUE_1));
            mValue2Repo.accept(savedInstanceState.getInt(VALUE_2));
            mOperationSelector.accept(Result.present(savedInstanceState.getInt(OPERATION_KEY)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mExecutor = Executors.newSingleThreadExecutor();

        // ACTION REQUIRED: Fix this repository
        mResultRepository = Repositories.repositoryWithInitialValue(Result.<String>absent())
                .observe(mValue1Repo, mValue2Repo, mOperationSelector)
                .onUpdatesPerLoop()
                .getFrom(() -> "Lambda all the things!")
                .thenTransform(input -> {
                    Result<Integer> operation = mOperationSelector.get();
                    if (operation.isPresent()) {
                        Integer result1;
                        int a = mValue1Repo.get();
                        int b = mValue2Repo.get();
                        switch (operation.get()) {
                            case R.id.radioButtonAdd:
                                result1 = a + b;
                                break;
                            case R.id.radioButtonSub:
                                result1 = a - b;
                                break;
                            case R.id.radioButtonMult:
                                result1 = a * b;
                                break;
                            case R.id.radioButtonDiv:
                                try {
                                    result1 = (a / b);
                                } catch (ArithmeticException e) {
                                    return Result.failure(e);
                                }
                                break;
                            default:
                                return Result.failure(
                                        new RuntimeException("Invalid operation"));
                        }
                        return Result.present(result1.toString());
                    } else {
                        return Result.absent();
                    }
                })
                .onConcurrentUpdate(RepositoryConfig.SEND_INTERRUPT)
                .compile();

        ((SeekBar) findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(
                new RepositorySeekBarListener(mValue1Repo));

        ((SeekBar) findViewById(R.id.seekBar2)).setOnSeekBarChangeListener(
                new RepositorySeekBarListener(mValue2Repo));

        mValue1TVupdatable = () -> ((TextView) findViewById(R.id.value1)).setText(
                mValue1Repo.get().toString());

        mValue2TVupdatable = () -> ((TextView) findViewById(R.id.value2)).setText(
                mValue2Repo.get().toString());

        TextView resultTextView = (TextView) findViewById(R.id.textViewResult);
        mResultUpdatable = () -> mResultRepository
                .get()
                .ifFailedSendTo(t -> Toast.makeText(this, t.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show())
                .ifFailedSendTo(t -> {
                    if (t instanceof ArithmeticException) {
                        resultTextView.setText(R.string.div_zero);
                    } else {
                        resultTextView.setText(getString(R.string.not_available));
                    }
                })
                .ifSucceededSendTo(resultTextView::setText);

        setUpdatables();
        UiUtils.startAnimation(findViewById(R.id.imageView));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mOperationSelector.get().isPresent()) {
            outState.putInt(VALUE_1, mValue1Repo.get());
            outState.putInt(VALUE_2, mValue2Repo.get());
            outState.putInt(OPERATION_KEY, mOperationSelector.get().get());
        }
        super.onSaveInstanceState(outState);
    }

    private void setUpdatables() {
        mValue1Repo.addUpdatable(mValue1TVupdatable);
        mValue2Repo.addUpdatable(mValue2TVupdatable);
        mResultRepository.addUpdatable(mResultUpdatable);

        mValue1TVupdatable.update();
        mValue2TVupdatable.update();
        mResultUpdatable.update();
    }

    @Override
    protected void onStop() {
        removeUpdatables();
        super.onStop();
    }

    private void removeUpdatables() {
        mValue1Repo.removeUpdatable(mValue1TVupdatable);
        mValue2Repo.removeUpdatable(mValue2TVupdatable);
        mResultRepository.removeUpdatable(mResultUpdatable);
    }

    public void onRadioButtonClicked(View view) {
        mOperationSelector.accept(Result.present(view.getId()));
    }
}
