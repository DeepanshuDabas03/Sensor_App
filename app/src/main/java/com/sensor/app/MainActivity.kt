package com.sensor.app

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sensor.app.ui.theme.SensorAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = SensorViewModel(application = application)
        setContent {
            SensorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    SensorScreen(viewModel)
                }
            }
        }
    }

}


@Composable
fun SensorScreen(viewModel: SensorViewModel) {
    val orientation by viewModel.orientation.collectAsState()
    var exportHistory by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    if (!showHistory) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Orientation App",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                "X: ${orientation.xAngle}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                "Y: ${orientation.yAngle}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                "Z: ${orientation.zAngle}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.padding(10.dp))


            Button(onClick = { exportHistory = true }) {
                Text("Export History")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = { showHistory = true }) {
                Text("Show History")
            }

            if (exportHistory) {
                viewModel.exportHistory()
                exportHistory = false
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Start
            ) {

                Button(onClick = { showHistory = false }) {
                    Text("Back")
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(20.dp))
            Text(
                "History - Accelerometer Data",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(8.dp))
            HistoryScreen(viewModel)
        }

    }
}

@Composable
fun HistoryScreen(viewModel: SensorViewModel) {
    val historyData by viewModel.historyData.collectAsState()

    val xData = historyData.map { Entry(it.id.toFloat(), it.xAngle) }
    val yData = historyData.map { Entry(it.id.toFloat(), it.yAngle) }
    val zData = historyData.map { Entry(it.id.toFloat(), it.zAngle) }
    val configuration = LocalConfiguration.current
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LineChart(
            xData, "X vs Time",
            Modifier
                .weight(0.8f)
                .width(configuration.screenWidthDp.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        LineChart(
            yData, "Y vs Time",
            Modifier
                .weight(0.8f)
                .width(configuration.screenWidthDp.dp)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        LineChart(
            zData, "Z vs Time",
            Modifier
                .weight(0.8f)
                .width(configuration.screenWidthDp.dp)
        )
    }
}

@Composable
fun LineChart(data: List<Entry>, label: String, modifier: Modifier = Modifier) {
    val lineDataSet = LineDataSet(data, label).apply {
        color = Color.RED // Force a bright color
        lineWidth = 3f // Increase thickness
        setDrawCircles(false) // Don't draw data point circles
        setDrawValues(false) // Don't draw data point values
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }
    val lineData = LineData(lineDataSet)

    AndroidView(modifier = modifier, factory = { context ->
        LineChart(context).apply {
            this.data = lineData
            setBackgroundColor(Color.TRANSPARENT) // Make chart background transparent

            description.isEnabled = false // Disable the description text
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.textSize = 15f
        }
    }, update = { view ->
        view.data = lineData
    })
}


class SensorViewModel(private val application: Application) : ViewModel() {
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val _orientation = MutableStateFlow(OrientationData(0, 0.0f, 0.0f, 0.0f))
    val orientation: StateFlow<OrientationData> = _orientation.asStateFlow()

    private val database: AppDatabase? = AppDatabase.getDatabase(application)
    private val orientationDao = database?.orientationDao()

    private val _historyData =
        MutableStateFlow<List<OrientationData>>(emptyList()) // Introduce a MutableStateFlow
    val historyData: StateFlow<List<OrientationData>> = _historyData.asStateFlow()


    init {
        registerSensorListener()
        startSavingOrientation()
        viewModelScope.launch {
            orientationDao?.getAllOrientationData()?.collect {
                _historyData.value = it // Update the MutableStateFlow
            }
        }
    }


    fun exportHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            writeHistoryToMediaStore()
        }
    }

    fun getOrientationHistory(): Flow<List<OrientationData>>? =
        orientationDao?.getAllOrientationData()

    private suspend fun writeHistoryToMediaStore() {
        val contentResolver = application.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "orientation_history.txt")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val fileUri =
            contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                ?: return

        contentResolver.openOutputStream(fileUri, "w")?.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                orientationDao?.getAllOrientationData()?.collect { recordedData ->
                    recordedData.forEach { data ->
                        writer.write("${data.xAngle},${data.yAngle},${data.zAngle}\n")
                    }
                }
            }
        }

    }

    private fun startSavingOrientation() {
        viewModelScope.launch {
            while (isActive) { // Coroutine runs as long as ViewModel is active
                orientationDao?.insert(orientation.value)
                delay(1000L) //  Save every second
            }
        }
    }

    private fun registerSensorListener() {
        accelerometer?.let {
            sensorManager.registerListener(
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.values?.let { values ->
                            val timestamp = System.currentTimeMillis()
                            _orientation.value =
                                OrientationData(0, values[0], values[1], values[2])
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }, it, SensorManager.SENSOR_DELAY_UI // Change sensing interval as needed
            )
        }
    }

}




