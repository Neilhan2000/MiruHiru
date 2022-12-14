package com.neil.miruhiru.task

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemTaskTaskBinding
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class TaskAdapter(viewModel: TaskViewModel) : ListAdapter<Task, TaskAdapter.ViewHolder>(DiffCallBack()) {

    private lateinit var context: Context
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private val viewModel = viewModel

    inner class ViewHolder(private val binding: ItemTaskTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            Glide.with(binding.challengeImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
            ).into(binding.challengeImage)
            binding.challengeTitle.text = item.name
            binding.challengeStage.text = item.stage.toString()
            startTrackingDistance(item.location)

            var distance = 0

            binding.startTaskButton.setOnClickListener {
                if (binding.challengeDistance.text != context.getString(R.string.no_gps)) {
                    distance = binding.challengeDistance.text.toString().filter { it.isDigit() }.toInt()
                } else {
                    distance = 0
                }
                val locationInfo = LocationInfo(
                    item.name, distance,
                    item.introduction, item.image, item.question, item.answer)
                itemView.findNavController().navigate(NavGraphDirections.actionGlobalTaskDetailFragment(locationInfo))
            }
            if (item.stage >= viewModel.currentStage) {
                binding.cardTaskSuccessIcon.visibility = View.GONE
            } else {
                binding.startTaskButton.text = context.getString(R.string.task_success)
                binding.startTaskButton.backgroundTintList = (ColorStateList.valueOf(itemView.context.resources.getColor(R.color.grey)))
                binding.startTaskButton.setOnClickListener(null)
            }
        }

        private fun startTrackingDistance(destination: GeoPoint) {
            scope.launch {
                while(true) {
                    calculateAndShowDistance(destination)
                    delay(1000)
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun calculateAndShowDistance(destination: GeoPoint) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            var currentPoint: Point? = null
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    location?.let {
                        currentPoint = Point.fromLngLat(
                            location.longitude,
                            location.latitude
                        )
                    }
                    val distance = calculateDistance(currentPoint, destination)
                    if (distance.roundToInt() != 0) {
                        binding.challengeDistance.text = "${distance.roundToInt()} Ms"
                    }
                }
        }

        private fun calculateDistance(currentPoint: Point?, destinationPoint: GeoPoint): Float {
            val current = Location("current")
            currentPoint?.let {
                current.latitude = currentPoint.latitude()
                current.longitude = currentPoint.latitude()
            }

            val destination = Location("current")
            destination.latitude = destinationPoint.latitude
            destination.longitude = destinationPoint.longitude

            val result =  floatArrayOf(0.0F)
            val distance = currentPoint?.let {
                Location.distanceBetween(
                    currentPoint.latitude(), currentPoint.longitude(),
                    destinationPoint.latitude, destinationPoint.longitude, result)
            }

            return result[0]
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_task, parent, false)
        context = parent.context
        if (this.itemCount == 1) {
            view.layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
        }
        return ViewHolder(binding = ItemTaskTaskBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TaskAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Task
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}