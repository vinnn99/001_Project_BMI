package com.example.projectbmi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = com.example.projectbmi.R.drawable.person_placeholder), contentDescription = "illustration", modifier = Modifier.size(120.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("BMI Checker", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Calculate and monitor your body mass index", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(18.dp))

                Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), onClick = { navController.navigate("calculator") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start Calculate")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(onClick = { navController.navigate("about") }, modifier = Modifier.fillMaxWidth()) {
                    Text("About App")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { navController.navigate("history") }, modifier = Modifier.fillMaxWidth()) {
                    Text("History")
                }
            }
        }
    }
}