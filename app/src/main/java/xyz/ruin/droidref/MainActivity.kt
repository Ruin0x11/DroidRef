package xyz.ruin.droidref

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xiaopo.flying.sticker.*
import com.xiaopo.flying.sticker.StickerView.OnStickerOperationListener
import com.xiaopo.flying.sticker.extensions.withUnderline
import timber.log.Timber
import xyz.ruin.droidref.util.FileUtil

class MainActivity : AppCompatActivity() {
    private lateinit var stickerView: StickerView
    private lateinit var sticker: TextSticker
    private lateinit var lockIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stickerView = findViewById(R.id.sticker_view)!!
        val toolbar = findViewById<Toolbar>(R.id.toolbar)!!
        lockIcon = findViewById(R.id.lockIcon)

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
        val heartIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp),
            BitmapStickerIcon.LEFT_BOTTOM
        )
        heartIcon.iconEvent = HelloIconEvent()
        stickerView.icons = listOf(deleteIcon, zoomIcon, flipIcon, heartIcon)

        //default icon layout
        //stickerView.configDefaultIcons();
        stickerView.setBackgroundColor(Color.WHITE)
        stickerView.isLocked = false
        stickerView.isConstrained = true

        stickerView.onStickerOperationListener = MyStickerOperationListener(stickerView)

        setupToolbar(toolbar)

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

        updateGui()
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setTitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.menu_save)
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.item_save) {
                val file = FileUtil.getNewFile(this@MainActivity, "Sticker")
                if (file != null) {
                    stickerView.save(file)
                    Toast.makeText(
                        this@MainActivity, "saved in " + file.absolutePath,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "the file is null",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            false
        }
    }

    private fun loadSticker() {
        val drawable =
            ContextCompat.getDrawable(this, R.drawable.haizewang_215)
        val drawable1 =
            ContextCompat.getDrawable(this, R.drawable.haizewang_23)
        stickerView.addSticker(DrawableSticker(drawable))
        stickerView.addSticker(DrawableSticker(drawable1), Sticker.Position.BOTTOM or Sticker.Position.RIGHT)
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

    fun testReplace(view: View?) {
        if (stickerView.replace(sticker)) {
            Toast.makeText(
                this@MainActivity,
                "Replace Sticker successfully!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Replace Sticker failed!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun updateGui() {
        if (stickerView.isLocked) {
            lockIcon.visibility = View.VISIBLE
        } else {
            lockIcon.visibility = View.GONE
        }
    }

    fun testLock(view: View) {
        stickerView.isLocked = !stickerView.isLocked
        updateGui()
    }

    fun testRemove(view: View) {
        if (stickerView.removeCurrentSticker()) {
            Toast.makeText(
                this@MainActivity,
                "Remove current Sticker successfully!",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Remove current Sticker failed!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    fun testRemoveAll(view: View) {
        stickerView.removeAllStickers()
    }

    fun reset(view: View) {
        stickerView.removeAllStickers()
        loadSticker()
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
        private val TAG = MainActivity::class.java.simpleName
        const val PERM_RQST_CODE = 110
    }

    class MyStickerOperationListener(private val stickerView: StickerView) : OnStickerOperationListener {
        override fun onStickerAdded(sticker: Sticker) {
            Timber.d("onStickerAdded")
        }

        override fun onStickerClicked(sticker: Sticker) {
            //stickerView.removeAllSticker();
            if (sticker is TextSticker) {
                sticker.setTextColor(Color.RED)
                stickerView.replace(sticker)
                stickerView.invalidate()
            }
            Timber.d("onStickerClicked")
        }

        override fun onStickerDeleted(sticker: Sticker) {
            Timber.d("onStickerDeleted")
        }

        override fun onStickerDragFinished(sticker: Sticker) {
            Timber.d("onStickerDragFinished")
        }

        override fun onStickerTouchedDown(sticker: Sticker) {
            Timber.d("onStickerTouchedDown")
        }

        override fun onStickerZoomFinished(sticker: Sticker) {
            Timber.d("onStickerZoomFinished")
        }

        override fun onStickerFlipped(sticker: Sticker) {
            Timber.d("onStickerFlipped")
        }

        override fun onStickerDoubleTapped(sticker: Sticker) {
            Timber.d("onDoubleTapped: double tap will be with two click")
        }

        override fun onStickerMoved(sticker: Sticker) {
            Timber.d("onStickerMoved")
        }

        override fun onStickerTouchedAuxiliaryLines(sticker: Sticker) {
            Timber.d("onStickerTouchedAuxiliaryLines")
        }
    }
}