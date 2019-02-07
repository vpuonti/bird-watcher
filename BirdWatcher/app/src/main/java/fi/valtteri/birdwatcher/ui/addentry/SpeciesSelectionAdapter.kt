package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.Filter
import android.widget.TextView
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Species
import timber.log.Timber

class SpeciesSelectionAdapter(context: Context,
                              private val resource: Int,
                              private var originalItems: MutableList<Species>) :
    ArrayAdapter<Species>(context, resource, originalItems) {


    private var filteredItems: List<Species> = originalItems.map{it}
    private var filter: SpeciesFilter? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view: View? = convertView
        val inflater = LayoutInflater.from(context)
        if(convertView == null) {
            view = inflater.inflate(R.layout.species_list_item, null)
        }
        val species: Species? = filteredItems.getOrNull(position)
        species.let { bird ->
            view?.findViewById<TextView>(R.id.name)?.text = bird?.displayName
        }
        return view
    }

    override fun getFilter(): Filter {
        return filter ?: synchronized(this){
            val f = SpeciesFilter()
            filter = f
            return f
        }
    }

    fun setItems(items: List<Species>) {
        originalItems.clear()
        originalItems.addAll(items)
        filteredItems = items
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return filteredItems.size
    }

    override fun getItem(position: Int): Species? {
        return filteredItems.getOrNull(position)
    }

    inner class SpeciesFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val prefix: String? = constraint?.toString()
            if(prefix == null || prefix.isBlank()) {
                results.values = originalItems
                results.count = originalItems.size
                Timber.d("Result: all original ${originalItems.size} items")
            } else {
                val filteredResults = originalItems.filter {
                    val dName = it.displayName
                    if(dName == null) {
                        return@filter false
                    } else {
                        return@filter dName.contains(prefix, true)
                    }
                }
                results.values = filteredResults
                results.count = filteredResults.size
                Timber.d("Result for $prefix: ${filteredResults.size} items")
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val fList = results?.values as? List<*>
            val fItems = fList?.map { it as Species }

            fItems?.let { items ->
                filteredItems = items
                Timber.d("Setting ${items.size} species to filteredItems")
            }
            notifyDataSetChanged()
        }

    }


}
