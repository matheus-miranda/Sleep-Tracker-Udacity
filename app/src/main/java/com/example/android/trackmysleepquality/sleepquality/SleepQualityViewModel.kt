/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

class SleepQualityViewModel(
    private val sleepNightKey: Long = 0L,
    private val database: SleepDatabaseDao
) : ViewModel() {

    // Create Job and Coroutine UI Scope
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Navigate back to SleepTracker
    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToTracker: LiveData<Boolean?> get() = _navigateToSleepTracker

    /**
     * Called when we navigate back to SleepTracker
     */
    fun doneNavigation() {
        _navigateToSleepTracker.value = null
    }

    /**
     * Click handler for the quality of sleep
     */
    fun onSetSleepQuality(quality: Int) {
        // Launch coroutine in the UI scope
        uiScope.launch {
            // Switch to the IO dispatcher
            withContext(Dispatchers.IO) {
                val tonight = database.get(sleepNightKey) ?: return@withContext
                tonight.sleepQuality = quality
                database.update(tonight)
            }
            _navigateToSleepTracker.value = true // trigger navigation
        }

    }


    /**
     * Cancel all executing coroutines when fragment is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}