package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.github.ajalt.timberkt.Timber.d
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminder: ReminderDataItem
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var geofence: Geofence
    private lateinit var geofencingRequest: GeofencingRequest
    private lateinit var pendingIntent: PendingIntent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext());
        geofenceHelper = GeofenceHelper(requireContext());
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminder = ReminderDataItem(title, description, location, latitude, longitude)

            val LatLng = LatLng(latitude!!, longitude!!)
            addGeofence(LatLng, _viewModel.GEOFENCE_RADIUS, reminder.id)
            _viewModel.validateAndSaveReminder(reminder)
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float, geoId: String) {
        geofence = geofenceHelper.getGeofence(
            geoId,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER
        )
        geofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
        pendingIntent = geofenceHelper.getPendingsIntent()!!



        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener { d { "onSuccess: Geofence Added..." } }
            .addOnFailureListener { e ->
                val errorMessage = geofenceHelper.getErrorString(e)
                d { "onFailure: $errorMessage" }
            }

    }

    private fun removeGeofences() {

        geofencingClient.removeGeofences(pendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                d { "removed geofences" }
                Toast.makeText(requireContext(), "remove geofences", Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                d { "falied to remove geofences" }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
