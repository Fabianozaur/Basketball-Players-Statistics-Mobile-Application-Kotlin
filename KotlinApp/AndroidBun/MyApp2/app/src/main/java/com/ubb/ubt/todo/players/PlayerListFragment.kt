package com.ubb.ubt.todo.players

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.ubb.ubt.R
import com.ubb.ubt.auth.data.AuthRepository
import com.ubb.ubt.core.TAG
import com.ubb.ubt.databinding.FragmentPlayerListBinding

class PlayerListFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentPlayerListBinding? = null
    private lateinit var playerListAdapter: PlayerListAdapter
    private lateinit var playersModel: PlayersListViewModel
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData
    private val binding get() = _binding!!

    private var lightSeen = true
    private lateinit var sensorManager: SensorManager
    private var light: Sensor? = null
    private var lightLevel = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager = activity?.getSystemService(android.net.ConnectivityManager::class.java)!!
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(this, {
            Log.d(TAG, "connectivityLiveData $it")
        })

        // Get sensor manager
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Get the default sensor of specified type
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        showAllSensors()
        checkSensor()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "isOnline ${isOnline()}")
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isOnline(): Boolean {
        val connMgr = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    private val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "The default network is now: $network")
            playersModel.syncDataWithServer()
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "The application no longer has a default network. The last default network was $network")
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "The default network changed capabilities: $networkCapabilities")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.d(TAG, "The default network changed link properties: $linkProperties")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")
        _binding = FragmentPlayerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.FragmentLogin)
            return;
        }
        setupPlayerList()
        binding.fab.setOnClickListener {
            Log.v(TAG, "add new player")
            findNavController().navigate(R.id.PlayerEditFragment)
        }

        binding.button2.setOnClickListener {
            Log.v(TAG, "second button pressed")
            runAnimation()
            startJob()
        }
    }

    private fun runAnimation() {
        if (!lightSeen) {
            binding.lightLevel.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(2000)
                    .setListener(null)
            }
        }
        else{
            binding.lightLevel.apply {
                alpha = 1f
                visibility = View.VISIBLE
                animate()
                    .alpha(0f)
                    .setDuration(2000)
                    .setListener(null)
            }
        }

        lightSeen = !lightSeen
    }

    private fun setupPlayerList() {
        playerListAdapter = PlayerListAdapter(this)
        binding.itemList.adapter = playerListAdapter
        playersModel = ViewModelProvider(this).get(PlayersListViewModel::class.java)
        playersModel.players.observe(viewLifecycleOwner, { value ->
            Log.i(TAG, "update players")
            playerListAdapter.players = value
        })
        playersModel.loading.observe(viewLifecycleOwner, { loading ->
            Log.i(TAG, "update loading")
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        })
        playersModel.loadingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        })
        playersModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView")
        _binding = null
    }

    private fun startJob() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putString("light", lightLevel.toString())
            .build()
//        val myWork = PeriodicWorkRequestBuilder<ExampleWorker>(1, TimeUnit.MINUTES)
        val myWork = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workId = myWork.id
        WorkManager.getInstance().apply {
            // enqueue Work
            enqueue(myWork)
            // observe work status
            getWorkInfoByIdLiveData(workId)
        }
    }

    private fun showAllSensors() {
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        Log.d(TAG, "showAllSensors");
        deviceSensors.forEach {
            Log.d(TAG, it.name + " " + it.vendor + " " + it.version);
        }
    }

    private fun checkSensor() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GRAVITY)
            val sensor = gravSensors.firstOrNull { it.vendor.contains("AOSP") && it.version == 3 }
            sensor?.let { Log.d(TAG, "Check sensor " + it.name) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged $accuracy");
    }

    override fun onSensorChanged(event: SensorEvent) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        val lux = event.values[0]
        lightLevel = lux.toDouble()
        // Do something with this sensor value.
        Log.d(TAG, "onSensorChanged $lux");
        binding.lightLevel.setText("Light level: $lux")
    }

    override fun onResume() {
        super.onResume()
        light?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}