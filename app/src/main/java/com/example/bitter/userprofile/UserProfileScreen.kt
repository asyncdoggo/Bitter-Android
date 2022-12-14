package com.example.bitter.userprofile

import Bitter.R
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bitter.data.Routes
import com.example.bitter.home.PostCard
import com.example.bitter.postUrl
import com.example.bitter.ui.theme.buttonColor

@Composable
fun UserProfileScreen(
    outerNavController: NavController,
    username: String
) {
     Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ){
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { outerNavController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "back"
                            )
                        }
                    },
                    title = {
                        Text(text = username)
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp
                )
            }
        )
         {
            UserData(it,username,outerNavController)
        }
    }

    BackHandler {
        outerNavController.popBackStack()
    }
}


@Composable
fun UserData(
    it: PaddingValues,
    uname: String,
    outerNavController: NavController,
    viewModel: UserProfileViewModel = viewModel()
    ) {
    val keyPref = LocalContext.current.getSharedPreferences("authkey", Context.MODE_PRIVATE)
    val username = keyPref.getString("uname",null)
    val token = keyPref.getString("token", null)
    val toast = Toast.makeText(LocalContext.current,"Cannot connect, please check your network connection",Toast.LENGTH_LONG)
    val fullname = viewModel.fullname.collectAsState()
    val bio = viewModel.bio.collectAsState()
    val posts = viewModel.getPosts(uname)?.observeAsState(listOf())

    val reversed by remember(posts?.value) {
        derivedStateOf {
            posts?.value?.reversed() ?: emptyList()
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(it),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, bottom = 10.dp, top = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                AsyncImage(
                    model = "$postUrl/images/$uname",
                    contentDescription = "image",
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = fullname.value,
                    modifier = Modifier.padding(30.dp),
                    fontSize = 16.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = bio.value,
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                if(username == uname) {
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 25.dp, end = 25.dp)
                            .size(35.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.buttonColor,
                            contentColor = Color.White
                        ),
                        onClick = { outerNavController.navigate(Routes.EditUserProfileScreen.route) }
                    ) {
                        Text(text = "Edit profile", fontSize = 14.sp)
                    }
                }
            }
        }

        item{
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
                    .size(1.dp)
            )
        }

        items(reversed) { item ->
            PostCard(
                item,
                token = token ?: "",
                navController = null
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
                    .size(1.dp)
            )

        }
    }
    LaunchedEffect(key1 = true){
        try {
            viewModel.getName(token,uname)
//                viewModel.updateLikes(token)
        }
        catch (e:Exception){
            toast.show()
        }
    }
}