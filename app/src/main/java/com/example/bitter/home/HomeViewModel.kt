package com.example.bitter.home

import android.content.SharedPreferences.Editor
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.bitter.data.PostDatabase
import com.example.bitter.data.PostItem
import com.example.bitter.data.PostRepository
import com.example.bitter.data.Routes
import com.example.bitter.util.ApiService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private var latestpost = 0

    fun getLatest() {
        viewModelScope.launch {
            latestpost = PostDatabase.instance?.postDao()?.let { PostRepository(it).getLatest() }?:0
        }
    }

    fun logout(editor: Editor, navController: NavController, token: String) {
        val postDao = PostDatabase.instance?.postDao()
        editor.clear()
        editor.apply()
        viewModelScope.launch {
            ApiService.logout(token)
            postDao?.let { PostRepository(it) }?.deleteAll()
            navController.navigate(Routes.LoginScreen.route)
        }
    }

    fun updatePosts(): LiveData<List<PostItem>>? {
        val postDao = PostDatabase.instance?.postDao()
        val repository = postDao?.let { PostRepository(it) }
        return repository?.getAllPosts
    }

    suspend fun fetchNewPosts(token: String) {
        val postDao = PostDatabase.instance?.postDao()
        val response = ApiService.getPosts(token, latestpost)
        if (response.status == "success") {
            val data = response.data
            if (data != null && data.isNotEmpty()) {
                for (i in data.keys) {
                    val item = data.getValue(i)
                    var datetime = item.jsonObject["datetime"]?.jsonPrimitive?.content.toString()
                    datetime = datetime.toDate()?.formatTo("dd MMM yyyy,  K:mm a") ?: ""
                    postDao?.let { PostRepository(it) }?.insert(
                        PostItem(
                            postId = i.toInt(),
                            content = item.jsonObject["content"]?.jsonPrimitive?.content.toString(),
                            lc = item.jsonObject["lc"]?.jsonPrimitive?.int ?: 0,
                            dlc = item.jsonObject["dlc"]?.jsonPrimitive?.int ?: 0,
                            isliked = item.jsonObject["islike"]?.jsonPrimitive?.int ?: 0,
                            isdisliked = item.jsonObject["isdislike"]?.jsonPrimitive?.int ?: 0,
                            byuser = item.jsonObject["uname"]?.jsonPrimitive?.content.toString(),
                            datetime = datetime
                        )
                    )
                }
            }
        }

    }

    suspend fun updateLikes(token: String?) {
        val postDao = PostDatabase.instance?.postDao()

        val response = ApiService.updateLikeData(token)
        if (response.status == "success") {
            val data = response.data
            if (data != null) {
                for (i in data.keys) {
                    val item = data.getValue(i)
                    postDao?.let { PostRepository(it) }?.update(
                        pid = i.toInt(),
                        lc = item.jsonObject["lc"]?.jsonPrimitive?.int ?: 0,
                        dlc = item.jsonObject["dlc"]?.jsonPrimitive?.int ?: 0,
                        isliked = item.jsonObject["islike"]?.jsonPrimitive?.int ?: 0,
                        isdisliked = item.jsonObject["isdislike"]?.jsonPrimitive?.int ?: 0,
                    )
                }
            }
        }

    }
}


fun String.toDate(
    dateFormat: String = "yyyy-MM-dd HH:mm:ss",
    timeZone: TimeZone = TimeZone.getTimeZone("UTC")
): Date? {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}

fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}