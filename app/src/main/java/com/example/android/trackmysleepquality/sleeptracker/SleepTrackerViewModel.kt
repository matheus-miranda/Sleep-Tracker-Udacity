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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao, application: Application
) : AndroidViewModel(application) {

    // Create a job to handle Coroutines in the ViewModel
    private var viewModelJob = Job()

    // Get a Coroutine scope for jobs to run on Main thread because results affects the UI
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Hold the current night
    private var tonight = MutableLiveData<SleepNight?>()

    // Get all of the Sleep data
    private val nights = database.getAllNights()
    // Transform all the nights into a formatted string every time nights receives new data from DB
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources) // resources needed to access String resources
    }

    // Define when navigation to SleepQuality should occur
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight> get() = _navigateToSleepQuality

    // Transformations map to define button visibility
    // When tonight is null Start Button should be visible
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }
    // If tonight has value, Stop should be visible
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }
    // If list has a value, Clear should be visible
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    init {
        initializeTonight()
    }

    /**
     * Set value to null after navigating fragments
     */
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    /**
     * Initialize tonight while not blocking main UI
     */
    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    /**
     * Return tonight from Database
     */
    private suspend fun getTonightFromDatabase(): SleepNight? {
        // Create coroutine
        return withContext(Dispatchers.IO) {
            var night = database.getTonight() // Returns latest night saved

            // If start time and end time are the same we are continuing an existing night
            // Else there is no night started so return null
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    /**
     * Click handler for the Start Button. Create a new night, insert to the database and
     * set it to tonight's value
     */
    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight() // New sleep night captures current time as start time
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    /**
     * Inserts into the Database
     */
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    /**
     * Click handler for the Stop button
     */
    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight // trigger the navigation
        }
    }

    /**
     * Update the Database when user stops tracking
     */
    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    /**
     * Click handler for the Clear Button
     */
    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    /**
     * Remove all data from Database
     */
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    /**
     * Cancel any running jobs when onCleared is called
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel() // Cancel all jobs when ViewModel is destroyed
    }
}

