package fi.valtteri.birdwatcher.ui.observations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fi.valtteri.birdwatcher.R
import kotlinx.android.synthetic.main.observation_viewholder.view.*
import org.joda.time.format.DateTimeFormat

class ObservationCardDataAdapter : RecyclerView.Adapter<ObservationCardDataAdapter.ObservationViewHolder>() {

    private val items: MutableList<ObservationCardData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_viewholder, parent, false)
        return ObservationViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) = holder.bind(items[position])

    fun setItems(items: List<ObservationCardData>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class ObservationViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        fun bind(observation: ObservationCardData) {
            itemView.card_timestamp.text = observation.timeStamp.toString(DateTimeFormat.mediumDateTime())
            observation.pictureUri?.let {
                Glide.with(context).load(it).into(itemView.card_image)
            }
            itemView.card_title.text = observation.notes
        }
    }
}