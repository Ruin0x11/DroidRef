package xyz.ruin.droidref

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.OpenableColumns
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.xiaopo.flying.sticker.*
import com.xiaopo.flying.sticker.StickerView.OnStickerOperationListener
import com.xiaopo.flying.sticker.iconEvents.DeleteIconEvent
import com.xiaopo.flying.sticker.iconEvents.FlipHorizontallyEvent
import com.xiaopo.flying.sticker.iconEvents.FlipVerticallyEvent
import com.xiaopo.flying.sticker.iconEvents.ZoomIconEvent
import timber.log.Timber
import xyz.ruin.droidref.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var stickerViewModel: StickerViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val stickerOperationListener = object : OnStickerOperationListener {
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
        }

        stickerViewModel = ViewModelProvider(this).get(StickerViewModel::class.java)
        stickerViewModel.stickerOperationListener = stickerOperationListener
        binding.viewModel = stickerViewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        setupIcons()
        setupButtons()

        if (stickerViewModel.isFirstRun) {
            stickerViewModel.isFirstRun = false
            loadSticker()
        }
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImages(intent)
            }
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let(this@MainActivity::doAddSticker)
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let { images ->
            images.forEach {
                (it as? Uri)?.let(this@MainActivity::doAddSticker)
            }
        }
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
        val flipVerticallyIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                com.xiaopo.flying.sticker.R.drawable.sticker_ic_flip_vert_white_18dp
            ),
            BitmapStickerIcon.LEFT_BOTTOM
        )
        flipVerticallyIcon.iconEvent = FlipVerticallyEvent()
        stickerViewModel.icons.value = arrayListOf(deleteIcon, zoomIcon, flipIcon, flipVerticallyIcon)
        stickerViewModel.activeIcons.value = stickerViewModel.icons.value
    }

    private fun save() {
        if (stickerViewModel.currentFileName != null) {
            doSave(stickerViewModel.currentFileName!!)
        } else {
            saveAs()
        }
    }

    private fun saveAs() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERM_RQST_CODE
            )
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose File Name")

        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val defaultFileName: String = formatter.format(Calendar.getInstance().time) + SAVE_FILE_EXTENSION

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(defaultFileName, TextView.BufferType.EDITABLE)
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { dialog, which -> doSave(input.text.toString()) }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun doSave(fileName: String) {
        val file = File(getExternalFilesDir(null), fileName)

        try {
            StickerViewSerializer().serialize(stickerViewModel, file)
            stickerViewModel.currentFileName = fileName
            Toast.makeText(this, "Saved to $file", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Timber.e(e, "Error writing %s", file)
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
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Open Saved File"),
            INTENT_PICK_SAVED_FILE
        )
    }

    private fun getFileNameOfUri(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun doLoad(file: Uri) {
        try {
            val stream = contentResolver.openInputStream(file)!!
            StickerViewSerializer().deserialize(stickerViewModel, stream, resources)
            val fileName = getFileNameOfUri(file)
            Toast.makeText(this, "Loaded from $fileName", Toast.LENGTH_SHORT).show()
            stickerViewModel.currentFileName = fileName
        } catch (e: IOException) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Timber.e(e, "Error writing %s", file)
            Toast.makeText(this, "Error reading $file", Toast.LENGTH_LONG).show()
        }
    }

    private fun newBoard() {
        stickerViewModel.removeAllStickers()
        stickerViewModel.resetView()
        stickerViewModel.currentFileName = null
    }

    private fun addSticker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), INTENT_PICK_IMAGE)
    }

    private fun doAddSticker(file: Uri) {
        val imageStream = contentResolver.openInputStream(file)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        if (bitmap == null) {
            Toast.makeText(this, "Could not decode image", Toast.LENGTH_SHORT).show()
        } else {
            val drawable = BitmapDrawable(resources, bitmap)
            stickerViewModel.addSticker(DrawableSticker(drawable))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                INTENT_PICK_IMAGE -> {
                    val selectedImage = data!!.data!!
                    doAddSticker(selectedImage)
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
        buttonOpen.setOnClickListener { load() }

        val buttonSave = findViewById<ImageButton>(R.id.buttonSave)
        buttonSave.setOnClickListener { save() }

        val buttonSaveAs = findViewById<ImageButton>(R.id.buttonSaveAs)
        buttonSaveAs.setOnClickListener { saveAs() }

        val buttonNew = findViewById<ImageButton>(R.id.buttonNew)
        buttonNew.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            newBoard()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to create a new board?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        val buttonAdd = findViewById<ImageButton>(R.id.buttonAdd)
        buttonAdd.setOnClickListener {
            addSticker()
        }

        val buttonLock = findViewById<ToggleButton>(R.id.buttonLock)
        buttonLock.setOnCheckedChangeListener { _, isToggled ->
            stickerViewModel.isLocked.value = isToggled
        }

        val buttonReset = findViewById<ImageButton>(R.id.buttonReset)
        buttonReset.setOnClickListener { stickerViewModel.resetView() }

        val buttonCrop = findViewById<ToggleButton>(R.id.buttonCrop)
        buttonCrop.setOnCheckedChangeListener { _, isToggled ->
            stickerViewModel.isCropActive.value = isToggled
        }

        val buttonResetZoom = findViewById<ImageButton>(R.id.buttonResetZoom)
        buttonResetZoom.setOnClickListener {
            stickerViewModel.resetCurrentStickerZoom()
        }

        val buttonResetCrop = findViewById<ImageButton>(R.id.buttonResetCrop)
        buttonResetCrop.setOnClickListener {
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
        stickerViewModel.addSticker(
            DrawableSticker(drawable),
            Sticker.Position.TOP or Sticker.Position.LEFT
        )
        stickerViewModel.addSticker(
            DrawableSticker(drawable1),
            Sticker.Position.BOTTOM or Sticker.Position.RIGHT
        )
        stickerViewModel.addSticker(
            DrawableSticker(drawable2),
            Sticker.Position.BOTTOM or Sticker.Position.LEFT
        )
    }

    companion object {
        const val PERM_RQST_CODE = 110
        const val SAVE_FILE_EXTENSION: String = ".ref"

        const val INTENT_PICK_IMAGE = 1
        const val INTENT_PICK_SAVED_FILE = 2
    }
}