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
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xiaopo.flying.sticker.*
import com.xiaopo.flying.sticker.StickerView.OnStickerOperationListener
import com.xiaopo.flying.sticker.extensions.withUnderline
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var stickerView: StickerView
    private lateinit var sticker: TextSticker

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stickerView = findViewById(R.id.sticker_view)!!

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
        stickerView.icons = listOf(deleteIcon, zoomIcon, flipIcon, resetCropIcon)

        //default icon layout
        //stickerView.configDefaultIcons();
        stickerView.setBackgroundColor(Color.WHITE)
        stickerView.isLocked = false
        stickerView.isConstrained = false

        stickerView.onStickerOperationListener = MyStickerOperationListener(stickerView)

        setupButtons()

        sticker = TextSticker(this)
        sticker.drawable = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.sticker_transparent_background
        )!!
        sticker.text = "Elad\nאלעד"
        sticker.setTextColor(Color.BLACK)
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER)
        sticker.resizeText()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERM_RQST_CODE
            )
        } else {
            loadSticker()
        }
    }

    private fun setupButtons() {
        val buttonOpen = findViewById<ImageButton>(R.id.buttonOpen)
        val buttonSave = findViewById<ImageButton>(R.id.buttonSave)
        val buttonNew = findViewById<ImageButton>(R.id.buttonNew)
        buttonNew.setOnClickListener { _ ->
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            stickerView.removeAllStickers()
                            stickerView.resetView()
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
            stickerView.setLocked(isToggled)
        }

        val buttonReset = findViewById<ImageButton>(R.id.buttonReset)
        buttonReset.setOnClickListener { _ -> stickerView.resetView() }

        val buttonCrop = findViewById<ToggleButton>(R.id.buttonCrop)
        buttonCrop.setOnCheckedChangeListener { _, isToggled ->
            stickerView.setCropActive(isToggled)
        }

        val buttonResetZoom = findViewById<ImageButton>(R.id.buttonResetZoom)
        buttonResetZoom.setOnClickListener { _ ->
            stickerView.resetCurrentStickerZoom()
        }

        val buttonResetCrop = findViewById<ImageButton>(R.id.buttonResetCrop)
        buttonResetCrop.setOnClickListener { _ ->
            stickerView.resetCurrentStickerCropping()
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
                        Toast.makeText(stickerView.context, "Could not decode image", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val drawable = BitmapDrawable(resources, bitmap)
                        stickerView.addSticker(DrawableSticker(drawable))
                    }
                }
            }
        }
    }

    private fun loadSticker() {
        val drawable =
            ContextCompat.getDrawable(this, R.drawable.h250280)
        val drawable1 =
            ContextCompat.getDrawable(this, R.drawable.h2037984)
        val drawable2 =
            ContextCompat.getDrawable(this, R.drawable.h2037349)
        stickerView.addSticker(DrawableSticker(drawable), Sticker.Position.TOP or Sticker.Position.LEFT)
        stickerView.addSticker(DrawableSticker(drawable1), Sticker.Position.BOTTOM or Sticker.Position.RIGHT)
        stickerView.addSticker(DrawableSticker(drawable2), Sticker.Position.BOTTOM or Sticker.Position.LEFT)
        val bubble =
            ContextCompat.getDrawable(this, R.drawable.bubble)
        val textSticker = TextSticker(applicationContext)
        textSticker.withUnderline(true)
        stickerView.addSticker(
            textSticker
                .setDrawable(bubble!!)
                .setText("Sticker\n")
                .setMaxTextSize(14f)
                .resizeText()
            , Sticker.Position.TOP
        )
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

    fun testAdd(view: View) {
        val sticker = TextSticker(this)
        sticker.text = "Hello, world!\n Selfix"
        sticker.setTextColor(Color.BLUE)
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER)
        sticker.resizeText()
        stickerView.addSticker(sticker)
    }

    companion object {
        const val PERM_RQST_CODE = 110

        const val INTENT_PICK_IMAGE = 1
    }

    class MyStickerOperationListener(private val stickerView: StickerView) : OnStickerOperationListener {
        override fun onStickerAdded(sticker: Sticker) {
//            Timber.d("onStickerAdded")
        }

        override fun onStickerClicked(sticker: Sticker) {
            //stickerView.removeAllSticker();
            if (sticker is TextSticker) {
                sticker.setTextColor(Color.RED)
                stickerView.replace(sticker)
                stickerView.invalidate()
            }
//            Timber.d("onStickerClicked")
        }

        override fun onStickerDeleted(sticker: Sticker) {
//            Timber.d("onStickerDeleted")
        }

        override fun onStickerDragFinished(sticker: Sticker) {
//            Timber.d("onStickerDragFinished")
        }

        override fun onStickerTouchedDown(sticker: Sticker) {
//            Timber.d("onStickerTouchedDown")
        }

        override fun onStickerZoomFinished(sticker: Sticker) {
//            Timber.d("onStickerZoomFinished")
        }

        override fun onStickerFlipped(sticker: Sticker) {
//            Timber.d("onStickerFlipped")
        }

        override fun onStickerDoubleTapped(sticker: Sticker) {
//            Timber.d("onDoubleTapped: double tap will be with two click")
        }

        override fun onStickerMoved(sticker: Sticker) {
//            Timber.d("onStickerMoved")
        }

        override fun onStickerTouchedAuxiliaryLines(sticker: Sticker) {
//            Timber.d("onStickerTouchedAuxiliaryLines")
        }
    }
}