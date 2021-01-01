package xyz.ruin.droidref

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.xiaopo.flying.sticker.*
import com.xiaopo.flying.sticker.StickerView.OnStickerOperationListener
import timber.log.Timber
import xyz.ruin.droidref.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var stickerViewModel: StickerViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        stickerViewModel = StickerViewModel(object : OnStickerOperationListener {
            override fun onStickerAdded(sticker: Sticker, direction: Int) {
                binding.stickerView.layoutSticker(sticker, direction)
                binding.stickerView.invalidate()
            }

            override fun onStickerClicked(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerDeleted(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerDragFinished(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerTouchedDown(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerZoomFinished(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerFlipped(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerDoubleTapped(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onStickerMoved(sticker: Sticker) {
                binding.stickerView.invalidate()
            }

            override fun onInvalidateView() {
                binding.stickerView.invalidate()
            }
        })
        binding.viewModel = stickerViewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        setupIcons()
        setupButtons()
        loadSticker()
    }

    private fun setupIcons() {
        //currently you can config your own icons and icon event
        //the event you can custom
        val deleteIcon = BitmapStickerIcon(
                ContextCompat.getDrawable(
                        this,
                        com.xiaopo.flying.sticker.R.drawable.sticker_ic_close_white_18dp
                ),
                BitmapStickerIcon.LEFT_TOP
        )
        deleteIcon.iconEvent = DeleteIconEvent()
        val zoomIcon = BitmapStickerIcon(
                ContextCompat.getDrawable(
                        this,
                        com.xiaopo.flying.sticker.R.drawable.sticker_ic_scale_white_18dp
                ),
                BitmapStickerIcon.RIGHT_BOTTOM
        )
        zoomIcon.iconEvent = ZoomIconEvent()
        val flipIcon = BitmapStickerIcon(
                ContextCompat.getDrawable(
                        this,
                        com.xiaopo.flying.sticker.R.drawable.sticker_ic_flip_white_18dp
                ),
                BitmapStickerIcon.RIGHT_TOP
        )
        flipIcon.iconEvent = FlipHorizontallyEvent()
        val resetCropIcon = BitmapStickerIcon(
                ContextCompat.getDrawable(this, R.drawable.ic_refresh_black_18dp),
                BitmapStickerIcon.LEFT_BOTTOM
        )
        resetCropIcon.iconEvent = ResetCropIconEvent()
        stickerViewModel.icons.value = arrayListOf(deleteIcon, zoomIcon, flipIcon, resetCropIcon)
    }

    private fun save() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERM_RQST_CODE
            )
        }

        // Create a path where we will place our private file on external
        // storage.
        val file = File(getExternalFilesDir(null), "test.zip")

        try {
//            StickerViewSerializer().serialize(stickerView, file)
            Toast.makeText(this, "Saved to $file", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
            Toast.makeText(this, "Error writing $file", Toast.LENGTH_LONG).show()
        }
    }

    private fun load() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERM_RQST_CODE
            )
        }

        val intent = Intent()
        intent.type = "application/zip"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Open Saved File"), INTENT_PICK_SAVED_FILE)
    }

    private fun doLoad(file: Uri) {
        try {
            val stream = contentResolver.openInputStream(file)!!
//            if (!StickerViewSerializer().deserialize(stickerView, stream, resources)) {
//                Toast.makeText(stickerView.context, "Invalid file", Toast.LENGTH_LONG).show()
//                return
//            }
            Toast.makeText(this, "Loaded from $file", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing $file", e)
            Toast.makeText(this, "Error reading $file", Toast.LENGTH_LONG).show()
        }
    }

    private fun addSticker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), INTENT_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                INTENT_PICK_IMAGE -> {
                    val selectedImage = data!!.data!!

                    val imageStream = contentResolver.openInputStream(selectedImage)
                    val bitmap = BitmapFactory.decodeStream(imageStream)
                    if (bitmap == null) {
                        Toast.makeText(this, "Could not decode image", Toast.LENGTH_SHORT).show()
                    } else {
                        val drawable = BitmapDrawable(resources, bitmap)
                        stickerViewModel.addSticker(DrawableSticker(drawable))
                    }
                }
                INTENT_PICK_SAVED_FILE -> {
                    val selectedFile = data!!.data!!

                    doLoad(selectedFile)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupButtons() {
        val buttonOpen = findViewById<ImageButton>(R.id.buttonOpen)
        buttonOpen.setOnClickListener { _ -> load() }

        val buttonSave = findViewById<ImageButton>(R.id.buttonSave)
        buttonSave.setOnClickListener { _ -> save() }

        val buttonNew = findViewById<ImageButton>(R.id.buttonNew)
        buttonNew.setOnClickListener { _ ->
            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            stickerViewModel.removeAllStickers()
                            stickerViewModel.resetView()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to create a new board?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        val buttonAdd = findViewById<ImageButton>(R.id.buttonAdd)
        buttonAdd.setOnClickListener { _ ->
            addSticker()
        }

        val buttonLock = findViewById<ToggleButton>(R.id.buttonLock)
        buttonLock.setOnCheckedChangeListener { _, isToggled ->
            stickerViewModel.isLocked.value = isToggled
        }

        val buttonReset = findViewById<ImageButton>(R.id.buttonReset)
        buttonReset.setOnClickListener { _ -> stickerViewModel.resetView() }

        val buttonCrop = findViewById<ToggleButton>(R.id.buttonCrop)
        buttonCrop.setOnCheckedChangeListener { _, isToggled ->
            stickerViewModel.isCropActive.value = isToggled
        }

        val buttonResetZoom = findViewById<ImageButton>(R.id.buttonResetZoom)
        buttonResetZoom.setOnClickListener { _ ->
            stickerViewModel.resetCurrentStickerZoom()
        }

        val buttonResetCrop = findViewById<ImageButton>(R.id.buttonResetCrop)
        buttonResetCrop.setOnClickListener { _ ->
            stickerViewModel.resetCurrentStickerCropping()
        }
    }

    private fun loadSticker() {
        val drawable =
            ContextCompat.getDrawable(this, R.drawable.h250280)
        val drawable1 =
            ContextCompat.getDrawable(this, R.drawable.h2037984)
        val drawable2 =
            ContextCompat.getDrawable(this, R.drawable.h2037349)
        stickerViewModel.addSticker(DrawableSticker(drawable), Sticker.Position.TOP or Sticker.Position.LEFT)
        stickerViewModel.addSticker(DrawableSticker(drawable1), Sticker.Position.BOTTOM or Sticker.Position.RIGHT)
        stickerViewModel.addSticker(DrawableSticker(drawable2), Sticker.Position.BOTTOM or Sticker.Position.LEFT)
        val bubble =
            ContextCompat.getDrawable(this, R.drawable.bubble)
//        val textSticker = TextSticker(applicationContext)
//        textSticker.withUnderline(true)
//        stickerView.addSticker(
//            textSticker
//                .setDrawable(bubble!!)
//                .setText("Sticker\n")
//                .setMaxTextSize(14f)
//                .resizeText()
//            , Sticker.Position.TOP
//        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_RQST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSticker()
        }
    }

    companion object {
        const val PERM_RQST_CODE = 110

        const val INTENT_PICK_IMAGE = 1
        const val INTENT_PICK_SAVED_FILE = 2
    }
}