package com.kmp_starter.app.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.kmp_starter.app.App
import com.kmp_starter.app.R
import com.kmp_starter.app.com.kmp_starter.core.app
import com.kmp_starter.app.launchAndCollect
import com.kmp_starter.app.userInfoMode
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.core.data.hasToken
import com.kmp_starter.app.userinfo.UserInfoActivity
import com.kmp_starter.core.search.SearchEffect
import com.kmp_starter.core.search.SearchEvent
import com.kmp_starter.core.search.SearchState
import com.kmp_starter.core.search.SearchVM
import com.kmp_starter.core.userinfo.UserInfoEvent
import com.kmp_starter.core.userinfo.UserInfoVM
import com.kmp_starter.core.vmfactory.SearchVMFactory
import com.kmp_starter.core.vmfactory.UserInfoVMFactory
import com.russhwolf.settings.Settings
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.kodein.di.erased.instance
import java.util.*

class SearchActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val relay = Channel<SearchEvent>()

    private val settings: Settings by app.kodein.instance()
    private val repo: UserRepo by app.kodein.instance()

    private val adapter = SearchAdapter(relay)

    private val vm: SearchVM by lazy {
        ViewModelProviders.of(this,
            SearchVMFactory(repo)
        ).get(SearchVM::class.java)
    }

    private var jobs: List<Job> = emptyList()

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (!settings.hasToken) {
            startActivity(Intent(this, UserInfoActivity::class.java))
            finish()
            return
        }

        val events = flowOf(
            relay.consumeAsFlow(),
            flow {
                emit(SearchEvent.ScreenLoad)
            }
        )
            .flattenMerge()

        jobs += launchAndCollect(vm.viewState, ::render)
        jobs += launchAndCollect(vm.viewEffects, ::trigger)
        jobs += launchAndCollect(events, vm::processInput)

        search_recycler.adapter = adapter
        search_recycler.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> (application as App).logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun render(state: SearchState) {
        setTitle(state.title.toString(this))
        adapter.submitList(state.items)
    }

    private fun trigger(effect: SearchEffect) {
        //TODO
    }
}