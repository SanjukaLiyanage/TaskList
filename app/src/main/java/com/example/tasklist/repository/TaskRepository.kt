package com.example.tasklist.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tasklist.database.TaskDatabase
import com.example.tasklist.models.Task
import com.example.tasklist.utils.Resource
import com.example.tasklist.utils.StatusResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskRepository(application: Application) {

    private val taskDao = TaskDatabase.getInstance(application).taskDao

    private val _taskStateFlow = MutableStateFlow<Resource<Flow<List<Task>>>>(Resource.Loading())
    val taskStateFlow: StateFlow<Resource<Flow<List<Task>>>>
        get() = _taskStateFlow

    private val _statusLiveData = MutableLiveData<Resource<StatusResult>>()
    val statusLiveData: LiveData<Resource<StatusResult>>
        get() = _statusLiveData

    private val _sortByLiveData = MutableLiveData<Pair<String,Boolean>>().apply {
        postValue(Pair("title",true))
    }
    val sortByLiveData: LiveData<Pair<String,Boolean>>
        get() = _sortByLiveData

    fun setSortBy(sort:Pair<String,Boolean>){
        _sortByLiveData.postValue(sort)
    }

    fun getTaskList(isAsc : Boolean, sortByName:String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _taskStateFlow.emit(Resource.Loading())
                delay(500)
                val result = if (sortByName == "title"){
                    taskDao.getTaskListSortByTaskTitle(isAsc)
                }else{
                    taskDao.getTaskListSortByTaskDate(isAsc)
                }
                _taskStateFlow.emit(Resource.Success("loading", result))
            } catch (e: Exception) {
                _taskStateFlow.emit(Resource.Error(e.message.toString()))
            }
        }
    }

    fun insertTask(task: Task) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.insertTask(task)
                handleResult(result.toInt(), "Inserted Task Successfully", StatusResult.Added)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Resource.Error(e.message.toString()))
        }
    }

    fun deleteTask(task: Task) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.deleteTask(task)
                handleResult(result, "Deleted Task Successfully", StatusResult.Deleted)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Resource.Error(e.message.toString()))
        }
    }

    fun deleteTaskUsingId(taskId: String) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.deleteTaskUsingId(taskId)
                handleResult(result, "Deleted Task Successfully", StatusResult.Deleted)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Resource.Error(e.message.toString()))
        }
    }

    fun updateTask(task: Task) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.updateTask(task)
                handleResult(result, "Updated Task Successfully", StatusResult.Updated)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Resource.Error(e.message.toString()))
        }
    }

    fun updateTaskPaticularField(taskId: String, title: String, description: String) {
        try {
            _statusLiveData.postValue(Resource.Loading())
            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.updateTaskPaticularField(taskId, title, description)
                handleResult(result, "Updated Task Successfully", StatusResult.Updated)
            }
        } catch (e: Exception) {
            _statusLiveData.postValue(Resource.Error(e.message.toString()))
        }
    }

    private fun handleResult(result: Int, message: String, statusResult: StatusResult) {
        if (result != -1) {
            _statusLiveData.postValue(Resource.Success(message, statusResult))
        } else {
            _statusLiveData.postValue(Resource.Error("Something Went Wrong", statusResult))
        }
    }
}
