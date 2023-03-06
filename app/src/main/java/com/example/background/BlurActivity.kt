package com.example.background

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.example.background.databinding.ActivityBlurBinding
import com.example.background.permission.Permissions

private const val TAG = "BlurActivity"

class BlurActivity : AppCompatActivity() {
    private val blurLevel: Int
        get() = when (binding.radioBlurGroup.checkedRadioButtonId) {
            R.id.radio_blur_lv_1 -> 1
            R.id.radio_blur_lv_2 -> 2
            R.id.radio_blur_lv_3 -> 3
            else -> 1
        }
    private val viewModel: BlurViewModel by viewModels {
        BlurViewModel.BlurViewModelFactory(
            application
        )
    }
    private lateinit var binding: ActivityBlurBinding
    private val permission = Permissions(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
        permission.requestPermission()

        viewModel.outputWorkInfo.observe(this, workInfoObserver())

        binding.goButton.setOnClickListener {
            viewModel.applyBlur(blurLevel)
        }

        binding.seeFileButton.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }
    }

    private fun workInfoObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) return@Observer

            val workInfo = listOfWorkInfo[0]
            if (workInfo.state.isFinished) {
                showWorkFinished()
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                Log.d(TAG, "workInfoObserver: $outputImageUri")
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else showWorkInProgress()
        }
    }

    private fun requestPermission() {
        permission.permissionResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permission.isReadPermissionGranted =
                permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            permission.isWritePermissionGranted =
                permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
        }
    }


}
