package com.neil.miruhiru.verifydetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemVerifyTaskBinding

class VerifyTaskAdapter : ListAdapter<Task, VerifyTaskAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemVerifyTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            Glide.with(binding.verifyImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
            ).into(binding.verifyImage)
            binding.verifyStage.text = "第${item.stage}關"
            binding.verifyTaskName.text = item.name
            binding.verifyGuide.text = item.guide
            binding.placeIntroduction.text = item.introduction
            binding.verifyQuestion.text = item.question
            binding.verifyAnswer.text = item.answer

            // disable all gestures
            binding.mapView.gestures.pitchEnabled = false
            binding.mapView.gestures.scrollEnabled = false
            binding.mapView.gestures.rotateEnabled = false
            binding.mapView.gestures.pinchToZoomEnabled = false
            binding.mapView.gestures.doubleTapToZoomInEnabled = false
            binding.mapView.getMapboxMap().setCamera((CameraOptions.Builder().center(
                Point.fromLngLat(item.location.longitude, item.location.latitude)
            ).zoom(17.0).build()))
            addAnnotationToMap(item)
        }

        private fun addAnnotationToMap(task: Task) {
            // Create an instance of the Annotation API and get the PointAnnotationManager.
            bitmapFromDrawableRes(
                itemView.context,
                R.drawable.ic_red_location
            )?.let {
                val annotationApi = binding.mapView.annotations
                val pointAnnotationManager = annotationApi?.createPointAnnotationManager(binding.mapView)

                // Set options for the resulting symbol layer.
                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(
                        Point.fromLngLat(
                            task.location.longitude,
                            task.location.latitude
                        )
                    )
                    .withIconImage(it)
                    .withIconSize(2.0)
                // Add the resulting pointAnnotation to the map.
                pointAnnotationManager.create(pointAnnotationOptions)
            }
        }

        private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
            convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

        private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
            if (sourceDrawable == null) {
                return null
            }
            return if (sourceDrawable is BitmapDrawable) {
                sourceDrawable.bitmap
            } else {
                // copying drawable object to not manipulate on the same reference
                val constantState = sourceDrawable.constantState ?: return null
                val drawable = constantState.newDrawable().mutate()
                val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth, drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifyTaskAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_verify_task, parent, false)
        return ViewHolder(binding = ItemVerifyTaskBinding.bind(view))
    }

    override fun onBindViewHolder(holder: VerifyTaskAdapter.ViewHolder, position: Int) {
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