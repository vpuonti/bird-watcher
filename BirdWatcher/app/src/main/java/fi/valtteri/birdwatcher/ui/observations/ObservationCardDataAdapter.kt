package fi.valtteri.birdwatcher.ui.observations

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import fi.valtteri.birdwatcher.R
import kotlinx.android.synthetic.main.observation_viewholder.view.*
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

class ObservationCardDataAdapter : RecyclerView.Adapter<ObservationCardDataAdapter.ObservationViewHolder>(), Filterable {
    private val items: MutableList<ObservationCardData> = mutableListOf()
    private var filterItems: List<ObservationCardData> = emptyList()
    private var onOpenLocationClick: ((ObservationCardData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_viewholder, parent, false)
        return ObservationViewHolder(view, parent.context, onOpenLocationClick)
    }

    override fun getItemCount(): Int = filterItems.size

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) = holder.bind(filterItems[position])

    fun setOnOpenLocationClickListener(listener: ((ObservationCardData) -> Unit)) {
        this.onOpenLocationClick = listener
    }

    fun setItems(items: List<ObservationCardData>) {
        this.items.clear()
        this.items.addAll(items)
        filterItems = items
        notifyDataSetChanged()
    }

    class ObservationViewHolder(
        view: View,
        private val context: Context,
        private val onOpenMapsClickListener: ((ObservationCardData) -> Unit)? ) : RecyclerView.ViewHolder(view) {

        fun bind(observation: ObservationCardData) {
            itemView.card_timestamp.text = observation.timeStamp.toString(DateTimeFormat.mediumDateTime())
            itemView.card_rarity.text = observation.rarity.toString()
            itemView.card_name.text = observation.speciesDisplayName
            val options = RequestOptions.centerCropTransform().circleCrop()
            if(observation.pictureUri == null) {
                Glide.with(context).load(R.drawable.bird_stock_photo).apply(options).into(itemView.card_image)
            } else {
                Glide.with(context).load(observation.pictureUri).apply(options).into(itemView.card_image)
            }

            itemView.card_notes_label.text = "Notes"
            itemView.card_notes.text = observation.notes

            if(observation.latitude != null && observation.longitude != null) {
                itemView.card_open_location_button.visibility = View.VISIBLE
                itemView.card_open_location_button.setOnClickListener {
                    onOpenMapsClickListener?.invoke(observation)
                }
            }




        }
    }

    override fun getFilter(): Filter {
        return ObservationCardFilter()
    }


    inner class ObservationCardFilter: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val query: String? = constraint?.toString()
            if(query == null || query.isBlank()){
                results.values = items
                results.count = items.size
            } else {
                val filterResults = items.filter {
                    val nameMatch = it.speciesDisplayName?.contains(query, true)
                    val descriptionMatch = it.notes.contains(query, ignoreCase = true)
                    if(nameMatch == null) {
                        return@filter descriptionMatch
                    } else {
                        return@filter (descriptionMatch.or(nameMatch))
                    }
                }
                Timber.d("QUERY: $query")
                results.values = filterResults
                results.count = filterResults.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val fList = results?.values as List<*>
            val fItems = fList.map { it as ObservationCardData }

            fItems.let {
                filterItems = it
                Timber.d("Filter items ${it.size}")
                notifyDataSetChanged()
            }

        }

    }


}