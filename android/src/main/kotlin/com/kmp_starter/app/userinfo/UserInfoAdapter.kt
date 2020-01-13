package com.kmp_starter.app.userinfo

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kmp_starter.app.R
import com.kmp_starter.core.userinfo.UserInfoAdapterItem
import com.kmp_starter.core.userinfo.UserInfoAdapterItemType
import com.kmp_starter.core.userinfo.UserInfoEvent
import com.kmp_starter.core.userinfo.UserInfoMode
import kotlinx.android.synthetic.main.view_input.view.*
import kotlinx.coroutines.channels.Channel

internal class UserInfoDiffer : DiffUtil.ItemCallback<UserInfoAdapterItem>() {
    override fun areItemsTheSame(
        oldItem: UserInfoAdapterItem,
        newItem: UserInfoAdapterItem
    ): Boolean = true //TODO

    override fun areContentsTheSame(
        oldItem: UserInfoAdapterItem,
        newItem: UserInfoAdapterItem
    ): Boolean = true
        //!(newItem is UserInfoAdapterItem.User && oldItem is UserInfoAdapterItem.User
        //    && newItem.addressSuggestions != oldItem.addressSuggestions)

}

internal class UserInfoAdapter(
    val context: Context,
    val mode: UserInfoMode,
    val relay: Channel<UserInfoEvent>
) : ListAdapter<UserInfoAdapterItem, UserInfoVH<UserInfoAdapterItem>>(
    UserInfoDiffer()
) {

    //TODO
    //val addresses1 = mutableListOf<com.kmp_starter.data.Address>()
    private val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line,
        mutableListOf())
//        addresses.map { it.displayValue })

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserInfoVH<UserInfoAdapterItem> =
        when(viewType) {
            UserInfoAdapterItemType.INPUT.ordinal -> InputVH.create(
                parent,
                relay
            )
            else -> InputVH.create(
                parent,
                relay
            )
        }

    override fun onBindViewHolder(holder: UserInfoVH<UserInfoAdapterItem>, position: Int) {
        holder.bind(getItem(position))
    }

    fun update(items: List<UserInfoAdapterItem>) {
        submitList(items)
        adapter.clear()
        items.forEach {
            (it as? UserInfoAdapterItem.UserAddress)?.let {
                adapter.addAll(it.addressSuggestions)
                adapter.notifyDataSetChanged()
            }
        }
    }
}

fun ViewGroup.inflate(@LayoutRes layoutResId: Int) = LayoutInflater.from(context).inflate(layoutResId, this, false)

internal abstract class UserInfoVH<T : UserInfoAdapterItem>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: T)
}

private class InputVH(view: View, val relay: Channel<UserInfoEvent>)
    : UserInfoVH<UserInfoAdapterItem>(view) {

    private var textChangedListener: TextChangedListener? = null

    override fun bind(item: UserInfoAdapterItem) {
        itemView.input_edit.apply {
            removeTextChangedListener(textChangedListener)
            textChangedListener = TextChangedListener(relay, item)
            if (text.toString().isEmpty()) {
                setText(item.text)
                setSelection(item.text.length)
            }
            addTextChangedListener(textChangedListener)
            setOnFocusChangeListener { view, visible ->
                if (!visible)
                    relay.offer(UserInfoEvent.FocusLost(text.toString(), item))
            }
            hint = item.hint.toString(context)
            inputType = item.inputType.type
        }
    }

    companion object {
        fun create(parent: ViewGroup, relay: Channel<UserInfoEvent>)
                = InputVH(
            parent.inflate(R.layout.view_input),
            relay
        )
    }
}

private class TextChangedListener(
    val relay: Channel<UserInfoEvent>,
    val item: UserInfoAdapterItem
) : TextWatcher {
    override fun afterTextChanged(p0: Editable) {
        relay.offer(UserInfoEvent.TextChange(p0.toString(), item))
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) { }
}