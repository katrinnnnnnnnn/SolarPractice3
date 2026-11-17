package ua.kpi.solarpractice3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.kpi.solarpractice3.ui.SolarApp
import ua.kpi.solarpractice3.viewmodel.SolarViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface { SolarApp(viewModel<SolarViewModel>()) } } }
    }
}
