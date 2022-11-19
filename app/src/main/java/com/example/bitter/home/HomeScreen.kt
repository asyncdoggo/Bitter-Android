package com.example.bitter.home

import Bitter.R
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bitter.data.Routes
import com.example.bitter.postUrl
import io.ktor.utils.io.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    outerNavController: NavController,
    innerNavController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyPref = context.getSharedPreferences("authkey", Context.MODE_PRIVATE)
    val uname = keyPref.getString("uname", null)
    val token = keyPref.getString("token", null)
    val editor = keyPref.edit()
    viewModel.uname = uname
    viewModel.token = token
    viewModel.editor = editor
    viewModel.navController = outerNavController
    var isRefreshing by remember{
        mutableStateOf(false)
    }

    var expanded by remember {
        mutableStateOf(false)
    }
    val listState = rememberLazyListState()
    var showFloatingAction by remember {
        mutableStateOf(true)
    }
    val toast = Toast.makeText(
        LocalContext.current,
        "Cannot connect, please check your network connection",
        Toast.LENGTH_LONG
    )

    val posts = viewModel.updatePosts(context).observeAsState(listOf())
    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        isRefreshing = true
        try {
            viewModel.updateLikes(context, token)
            viewModel.fetchNewPosts(context, latestPost = keyPref.getString("post", "0") ?: "0")
        } catch (e: Exception) {
            toast.show()
        }
//        delay(1000)
        isRefreshing = false
    }

    val state = rememberPullRefreshState(isRefreshing,::refresh)

    Scaffold(
        scaffoldState = rememberScaffoldState(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "B-itter")
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                actions = {
                    Box(
                        Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { expanded = true }) {
                            AsyncImage(
                                model = "$postUrl/images/$uname",
                                contentDescription = "icon",
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(50.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            try {
                                viewModel.logout(context)
                            } catch (e: Exception) {
                                toast.show()
                            }
                        }) {
                            Text(text = "Logout")
                        }
                    }
                },
                elevation = AppBarDefaults.TopAppBarElevation,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showFloatingAction, enter = scaleIn(), exit = scaleOut()) {
                FloatingActionButton(
                    onClick = {
                        outerNavController.navigate(Routes.NewPostScreen.route)
                    }
                ) {
                    Icon(Icons.Filled.Add, "New Post")
                }
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxWidth()
            .fillMaxHeight(0.9f)

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state)
        ) {

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                )
                {
                    if (!isRefreshing) {
                        items(posts.value.asReversed()) { item ->
                            PostCard(
                                content = item.content,
                                lc = item.lc,
                                dlc = item.dlc,
                                token = token ?: "",
                                postId = item.postId,
                                isLiked = item.isliked,
                                isDisliked = item.isdisliked,
                                byUser = item.byuser,
                                datetime = item.datetime,
                                navController = innerNavController
                            )
                        }
                    }
                }
                showFloatingAction = listState.isScrollingDown()
            PullRefreshIndicator(isRefreshing, state, Modifier.align(Alignment.TopCenter))
        }

    }

    LaunchedEffect(key1 = true) {
        try {
            viewModel.updateLikes(context, token)
        } catch (e: Exception) {
            toast.show()
        }
    }

    var backPressedTime: Long = 0
    BackHandler {
        try {
            val t = System.currentTimeMillis()

            if (t - backPressedTime > 2000) {
                backPressedTime = t
                Toast.makeText(context, "Press back again to logout", Toast.LENGTH_SHORT).show()
            } else viewModel.logout(context)
        } catch (e: Exception) {
            toast.show()
        }
    }
}

@Composable
private fun LazyListState.isScrollingDown(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}