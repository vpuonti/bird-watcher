package fi.valtteri.birdwatcher.ui.observations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Observation
import kotlinx.android.synthetic.main.observation_viewholder.view.*
import org.joda.time.format.DateTimeFormat

class ObservationsAdapter : RecyclerView.Adapter<ObservationsAdapter.ObservationViewHolder>() {

    private val items: MutableList<Observation> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_viewholder, parent, false)
        return ObservationViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) = holder.bind(items[position])

    fun setItems(observations: List<Observation>) {
        items.clear()
        items.addAll(observations)
        notifyDataSetChanged()
    }

    class ObservationViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        fun bind(observation: Observation) {
            itemView.card_timestamp.text = observation.timeStamp.toString(DateTimeFormat.mediumDateTime())
            observation.picUri?.let {
                Glide.with(context).load(it).into(itemView.card_image)
            }
            itemView.card_title.text = observation.description
        }
    }
}