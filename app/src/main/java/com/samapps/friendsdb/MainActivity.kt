package com.samapps.friendsdb

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private val GALLERY = 1
    private val CAMERA = 2

    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null

    var currentUri : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseDatabase = FirebaseDatabase.getInstance()

        databaseReference = firebaseDatabase!!.reference.child("FriendDB")

        btn_add_friend.setOnClickListener {
            if (validate().first) {
                validate().second?.let { addFriendToFirebase(it) }
            }
        }

        img_avtar.setOnClickListener {
            showPictureDialog()
        }

    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY) {

                val contentURI = data?.data
                currentUri = contentURI
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
//                    val path = saveImage(bitmap)
                    Toast.makeText(this@MainActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
                    img_avtar.rotation = 0F
                    img_avtar!!.setImageBitmap(bitmap)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }



        } else if (requestCode == CAMERA) {

            val thumbnail = data!!.extras!!.get("data") as Bitmap
            img_avtar.rotation = 90F
            img_avtar!!.setImageBitmap(thumbnail)
//            saveImage(thumbnail)

            Toast.makeText(this@MainActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    /*private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .timeInMillis).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }*/

    companion object {
        private val IMAGE_DIRECTORY = "/demonuts"
    }


    private fun validate(): Pair<Boolean, Friend?> {
        val name = name_Edit.text.toString()
        val radioGroup = gender_radio_group.checkedRadioButtonId
        val phone = mobile_edit.text.toString()
        val address = address_edit.text.toString()

        val radioValue: String = if (radioGroup != -1) {
            val radio: RadioButton = findViewById(radioGroup)
            radio.text.toString()
        } else {
            ""
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please Enter Friend Name", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if (radioGroup == -1) {
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please Enter Mobile Number", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please Enter Address", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }

        val friend = Friend(name = name, gender = radioValue, mobile = phone, address = address)

        return Pair(true, friend)

    }

    private fun addFriendToFirebase(friend: Friend) {
        val key = databaseReference?.push()?.key

        key?.let {
            databaseReference?.child(it)?.setValue(friend).also {
                Toast.makeText(this@MainActivity, "data added", Toast.LENGTH_SHORT).show()
            }
        }
    }
}