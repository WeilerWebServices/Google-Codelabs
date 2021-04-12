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

package com.example.android.codelabs.agera.step1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.codelabs.agera.R;
import com.google.android.agera.MutableRepository;
import com.google.android.agera.Repositories;
import com.google.android.agera.Updatable;

public class Step1ActivityFinal extends AppCompatActivity {

    private MutableRepository<String> mStringRepo;
    private Updatable mLoggerUpdatable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step1);

        // Create a MutableRepository
        mStringRepo = Repositories.mutableRepository("Initial value");

        // Create an Updatable
        mLoggerUpdatable = () -> Log.d("AGERA", mStringRepo.get());

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the dots:
        mStringRepo.addUpdatable(mLoggerUpdatable);

        // Change the repository's value
        mStringRepo.accept("Hello Agera!");
    }

    @Override
    protected void onStop() {
        mStringRepo.removeUpdatable(mLoggerUpdatable);
        super.onStop();
    }
}
