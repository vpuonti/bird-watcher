package fi.valtteri.birdwatcher.ui.observations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Observation

class ObservationsAdapter : RecyclerView.Adapter<ObservationsAdapter.ObservationViewHolder>() {

    private val items: MutableList<Observation> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_viewholder, parent, false)
        return ObservationViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) = holder.bind(items[position])

    fun setItems(observations: List<Observation>) {
        items.clear()
        items.addAll(observations)
    }

    class ObservationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(observation: Observation) {
            //bind data to VH
        }
    }
}