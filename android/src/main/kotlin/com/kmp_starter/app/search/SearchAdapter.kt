package com.kmp_starter.app.search

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kmp_starter.app.R
import com.kmp_starter.app.userinfo.inflate
import com.kmp_starter.core.search.SearchAdapterItem
import com.kmp_starter.core.search.SearchEvent
import com.kmp_starter.core.userinfo.displayValue
import kotlinx.android.synthetic.main.view_search_header.view.*
import kotlinx.android.synthetic.main.view_search_result.view.*
import kotlinx.coroutines.channels.Channel

internal class SearchDiffer : DiffUtil.ItemCallback<SearchAdapterItem>() {
    override fun areItemsTheSame(
        oldItem: SearchAdapterItem,
        newItem: SearchAdapterItem
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: SearchAdapterItem,
        newItem: SearchAdapterItem
    ): Boolean = areItemsTheSame(oldItem, newItem)

}

internal class SearchAdapter(
    val relay: Channel<SearchEvent>
) : ListAdapter<SearchAdapterItem, SearchVH<SearchAdapterItem>>(
    SearchDiffer()
) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is SearchAdapterItem.Header) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH<SearchAdapterItem>  =
        when (viewType) {
            0 -> HeaderVH.create(
                parent,
                relay
            ) as SearchVH<SearchAdapterItem>
            else -> SearchResultVH.create(
                parent,
                relay
            ) as SearchVH<SearchAdapterItem>
        }

    override fun onBindViewHolder(holder: SearchVH<SearchAdapterItem>, position: Int) {
        holder.bind(getItem(position))
    }
}

internal abstract class SearchVH<T : SearchAdapterItem>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}

private class HeaderVH(view: View, val relay: Channel<SearchEvent>)
    : SearchVH<SearchAdapterItem.Header>(view) {

    override fun bind(item: SearchAdapterItem.Header) {
        itemView.apply {
            searchHeader_button.setOnClickListener {
                val distance = searchHeader_input.text.toString().toDoubleOrNull() ?: 0.0
                relay.offer(SearchEvent.SearchClick(distance))
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, relay: Channel<SearchEvent>)
                = HeaderVH(parent.inflate(R.layout.view_search_header), relay)
    }
}

private class SearchResultVH(view: View, val relay: Channel<SearchEvent>)
    : SearchVH<SearchAdapterItem.SearchResult>(view) {

    override fun bind(item: SearchAdapterItem.SearchResult) {
        itemView.apply {
            searchResult_label1.text = item.user.name
            searchResult_label2.text = item.user.address.displayValue
            searchResult_label3.text = resources.getString(
                R.string.distance_miles_format, item.distanceMiles.format())
        }
    }

    companion object {
        fun create(parent: ViewGroup, relay: Channel<SearchEvent>)
                = SearchResultVH(parent.inflate(R.layout.view_search_result), relay)
    }
}

fun Double.format(digits: Int = 2) = "%.${digits}f".format(this)