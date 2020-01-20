package com.kmp_starter.app.userinfo

import android.Manifest
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.kmp_starter.app.R
import com.kmp_starter.app.com.kmp_starter.core.app
import com.kmp_starter.app.launchAndCollect
import com.kmp_starter.core.data.UserRepo
import com.kmp_starter.core.vmfactory.UserInfoVMFactory
import com.kmp_starter.app.search.SearchActivity
import com.kmp_starter.app.userInfoMode
import com.kmp_starter.app.withUserInfoMode
import com.kmp_starter.core.base.Geocoder
import com.kmp_starter.core.userinfo.*
import kotlinx.android.synthetic.main.activity_user_registration.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.kodein.di.erased.instance
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class UserInfoActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  private val relay = Channel<UserInfoEvent>()

  private val userRepo: UserRepo by app.kodein.instance()
  private val geocoder: Geocoder by app.kodein.instance()

  private val vm: UserInfoVM by lazy {
    ViewModelProviders.of(this,
      UserInfoVMFactory(intent.userInfoMode(), userRepo, geocoder)
    ).get(UserInfoVM::class.java)
  }

  private val adapter by lazy {
    UserInfoAdapter(
      this,
      intent.userInfoMode(),
      relay
    )
  }
  private val easyImage by lazy { EasyImage.Builder(this).build() }

  private var jobs: List<Job> = emptyList()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user_registration)

    val events = flowOf(
      relay.consumeAsFlow(),
      flow {
        emit(UserInfoEvent.ScreenLoad)
      }
    )
      .flattenMerge()

    jobs += launchAndCollect(vm.viewState, ::render)
    jobs += launchAndCollect(vm.viewEffects, ::trigger)
    jobs += launchAndCollect(events, vm::processInput)

    userRegistration_primaryButton.setOnClickListener {
      relay.offer(UserInfoEvent.PrimaryButtonClick)
    }
    userRegistration_secondaryButton.setOnClickListener {
      relay.offer(UserInfoEvent.SecondaryButtonClick)
    }

    userRegistration_recycler.adapter = adapter
  }

  override fun onDestroy() {
    super.onDestroy()
    jobs.filter { !it.isCancelled }.forEach { it.cancel() }
  }

  private fun render(state: UserInfoState) {
    userRegistration_primaryButton.isEnabled = state.primaryButtonEnabled

    adapter.update(state.items)

    state.mode.let {
      title = it.title.toString(this)
      userRegistration_primaryButton.text = it.primaryButton.toString(this)
      userRegistration_secondaryButton.text = it.secondaryButton.toString(this)
    }
  }

  private fun trigger(effect: UserInfoEffect) = when (effect) {
      is UserInfoEffect.Error -> {
        AlertDialog.Builder(this)
          .setMessage(effect.throwable?.message)
          .setPositiveButton(R.string.ok, null)
          .show().let {  }
      }
      is UserInfoEffect.ConflictError -> {
        Toast.makeText(this, R.string.register_accountExists, Toast.LENGTH_LONG).show()
        startActivity(Intent(this, UserInfoActivity::class.java)
          .withUserInfoMode(UserInfoMode.LOGIN))
      }
      is UserInfoEffect.StartLogin -> {
        startActivity(Intent(this, UserInfoActivity::class.java)
          .withUserInfoMode(UserInfoMode.LOGIN))
      }
      is UserInfoEffect.ShowHome -> startActivity(
        Intent(this, SearchActivity::class.java)
          .setFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK))
    }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
      override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
        onPhotosReturned(imageFiles)
      }

      override fun onImagePickerError(error: Throwable, source: MediaSource) {
        error.printStackTrace()
      }

      override fun onCanceled(source: MediaSource) { }
    })
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }

  @AfterPermissionGranted(RC_STORAGE)
  private fun pickAvatar() {
    val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (EasyPermissions.hasPermissions(this, *perms)) {
      easyImage.openChooser(this)
    } else {
      EasyPermissions.requestPermissions(this, getString(R.string.request_permission),
        RC_STORAGE, *perms)
    }
  }

  private fun onPhotosReturned(imageFiles: Array<MediaFile>) {

  }

  companion object {
    private const val RC_STORAGE = 3242
    private const val RC_PICK = 3243
  }
}
