package fi.valtteri.birdwatcher.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

open class CoroutineViewModel : ViewModel() {

    private val viewModelJob: Job = Job()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        viewModelJob.cancelChildren()
        super.onCleared()
    }

}