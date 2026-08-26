package com.smartpantry.chef.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.smartpantry.chef.ui.screens.AddRecipeScreen
import com.smartpantry.chef.ui.screens.ExploreScreen
import com.smartpantry.chef.ui.screens.HomeScreen
import com.smartpantry.chef.ui.screens.PantryScreen
import com.smartpantry.chef.ui.screens.ProfileScreen

@Composable
fun MainScreen() {

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Ana Sayfa"
                        )
                    },
                    label = {
                        Text("Ana Sayfa")
                    }
                )

                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Keşfet"
                        )
                    },
                    label = {
                        Text("Keşfet")
                    }
                )

                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = { selectedIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tarif Ekle"
                        )
                    },
                    label = {
                        Text("Tarif Ekle")
                    }
                )

                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = { selectedIndex = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = "Buzdolabım"
                        )
                    },
                    label = {
                        Text("Buzdolabım")
                    }
                )

                NavigationBarItem(
                    selected = selectedIndex == 4,
                    onClick = { selectedIndex = 4 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil"
                        )
                    },
                    label = {
                        Text("Profil")
                    }
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (selectedIndex) {
                0 -> HomeScreen()
                1 -> ExploreScreen()
                2 -> AddRecipeScreen()
                3 -> PantryScreen()
                4 -> ProfileScreen()
            }
        }
    }
}