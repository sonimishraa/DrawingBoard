package com.iotric.drawingboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.iotric.drawingboard.databinding.PrescriptionSaveDialogueBinding
import me.panavtec.drawableview.DrawableView
import me.panavtec.drawableview.DrawableViewConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

class MainActivity : AppCompatActivity() {
    lateinit var config: DrawableViewConfig
    lateinit var mPaint: Paint
    lateinit var drawableView: DrawableView
    lateinit var binding1:PrescriptionSaveDialogueBinding
    lateinit var alertDialogue:AlertDialog
    lateinit var name:String
    lateinit var date:String

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawableView = findViewById(R.id.drawingPad)
        val draw = findViewById<AppCompatImageView>(R.id.draw)
        val erase = findViewById<AppCompatImageView>(R.id.erase)
        val color = findViewById<AppCompatImageView>(R.id.color)
        val back = findViewById<AppCompatImageView>(R.id.back)
        val save = findViewById<AppCompatImageView>(R.id.save)

        draw.setOnClickListener {
            config = DrawableViewConfig()
            config.strokeColor = resources.getColor(android.R.color.black)
            config.isShowCanvasBounds = true // If the view is bigger than canvas, with this the user will see the bounds (Recommended)
            config.strokeWidth = 5.0f
            config.minZoom = 1.0f
            config.maxZoom = 3.0f
            config.canvasHeight = 1920
            config.canvasWidth = 1920
            drawableView.setConfig(config)
        }
        color.setOnClickListener {
            /* val bitmap: Bitmap = resources.getDrawable(R.drawable.rose).toBitmap(1920, 1920, null)
             //val palette: Palette = Palette.from(bitmap).maximumColorCount(numberOfColors).generate()
             createPaletteSync(bitmap)*/
            val random: Random = Random()
            config.strokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
        erase.setOnClickListener {
            drawableView.clear()
        }
        save.setOnClickListener {
            drawableView.obtainBitmap()
            dialogue()
        }
        back.setOnClickListener {

            drawableView.undo()
        }
    }

    /*  fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()
      *//*  fun createPaletteAsync(bitmap: Bitmap) {
          Palette.from(bitmap).generate { palette ->
              // Use generated instance
          }
      }*/


    private fun dialogue() {
        val saveDialog: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        saveDialog.setTitle("Save drawing")
        saveDialog.setMessage("Save drawing to device Gallery?")
        saveDialog.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which -> // First check for permissions
            if (ContextCompat.checkSelfPermission(this@MainActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                saveDrawing()

            } else {
                saveDrawing()
            }

        })
        saveDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> // First check for permissions
            dialog.dismiss()

        })
        saveDialog.show()
    }

    private fun alertDialogue(){
        val builder = AlertDialog.Builder(this)
        binding1 = PrescriptionSaveDialogueBinding.inflate(layoutInflater)
        val view = binding1.root
        builder.setCancelable(false)
        builder.setView(view)
        alertDialogue = builder.create()
        alertDialogue.show()
        val btnCancel = binding1.btCancel
        val btnSend = binding1.btSave

        btnSend.setOnClickListener {
            if (validateDialogue()) {
                checkPermissionAndSave()
                alertDialogue.dismiss()
            }

        }
        btnCancel.setOnClickListener {
            alertDialogue.dismiss()
        }
        alertDialogue.setCanceledOnTouchOutside(true)
    }

    /* private fun dialogue() {
         val saveDialog: android.app.AlertDialog.Builder =
             android.app.AlertDialog.Builder(requireContext())
         saveDialog.setTitle("Save Prescription")
         saveDialog.setMessage("Save Prescription to device Gallery?")
         saveDialog.setPositiveButton(
             "Yes",
             DialogInterface.OnClickListener { dialog, which -> // First check for permissions
                 checkPermissionAndSave()

             })
         saveDialog.setNegativeButton(
             "Cancel",
             DialogInterface.OnClickListener { dialog, which -> // First check for permissions
                 dialog.dismiss()

             })
         saveDialog.show()
     }
 */
    private fun checkPermissionAndSave() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
               this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
            saveDrawing()

        } else {
            saveDrawing()
        }
    }

    private fun validateDialogue(): Boolean {
        var isAllFieldValidate = true
        name = binding1.editName.text.toString().trim()
        date = binding1.editDate.text.toString().trim()

        if (name.isEmpty()) {
            binding1.layoutEditName.setError("Field Empty")
            isAllFieldValidate = false
        } else {
            binding1.layoutEditName.setError(null)
        }

        if (date.isEmpty()) {
            binding1.layoutEditDate.setError("Field Empty")
            isAllFieldValidate = false
        } else {
            binding1.layoutEditDate.setError(null)
        }
        return isAllFieldValidate
    }

    private fun saveDrawing() {
        drawableView.setDrawingCacheEnabled(true)
        val bitmap: Bitmap = drawableView.obtainBitmap()
        drawableView.setConfig(config)
        val storedImagePath: File = generateImagePath("Images", "jpg")!!
        if (compressAndSaveImage(storedImagePath, drawableView.drawToBitmap())) {
            val savedToast = Toast.makeText(
                this,
                "Prescription saved to Gallery!", Toast.LENGTH_SHORT
            )
            savedToast.show()
        } else {
            //toastMessage("Oops! Prescription could not be saved.")
        }

        val url: Uri =
           contentResolver?.let { addImageToGallery(it, "jpg", storedImagePath) }!!
        drawableView.destroyDrawingCache()
    }

    private fun addImageToGallery(cr: ContentResolver, imgType: String, filepath: File): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Prescription")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/$imgType")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATA, filepath.toString())
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    private fun generateImagePath(title: String, imgType: String): File? {
        val sdf = SimpleDateFormat("yyyyMMdd-hhmmss")
        return File(getImagesDirectory(), title + "_" + sdf.format(Date()) + "." + imgType)
    }

    private fun getImagesDirectory(): File? {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + File.separator + "Downloads"
        )
        if (!file.mkdirs() && !file.isDirectory) {
            System.err.println("Directory could not be created")
        }
        return file
    }

    private fun compressAndSaveImage(file: File, bitmap: Bitmap): Boolean {
        var result = false
        try {
            val fos = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos).also { result = it }) {
                println("Compression success")
            }
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    saveDrawing()
                } else {
                    // Permission denied
                    //toastMessage("Storage permissions denied")
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                return
            }
        }
    }

}