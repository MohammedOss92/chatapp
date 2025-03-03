@file:Suppress("DEPRECATION")

package com.sarrawi.chat.fragments

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.snackbar.Snackbar
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.databinding.FragmentSettingBinding
import com.sarrawi.chat.mvvm.ChatAppViewModel
import com.sarrawi.chat.uploadImage.up2.ApiService
import com.sarrawi.chat.uploadImage.up2.UploadRequestBody
import com.sarrawi.chat.uploadImage.up2.UploadResponse
import okhttp3.MultipartBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class SettingFragment : Fragment() , UploadRequestBody.UploadCallback{

    lateinit var viewModel: ChatAppViewModel
    lateinit var binding : FragmentSettingBinding
    private var selectedImageUri: Uri? = null
//    private lateinit var storageRef: StorageReference
//    lateinit var storage: FirebaseStorage
    var uri: Uri? = null

    lateinit var bitmap: Bitmap



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel



//        storage = FirebaseStorage.getInstance()
//        storageRef = storage.reference



        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })

        binding.settingBackBtn.setOnClickListener {

            view.findNavController().navigate(R.id.action_settingFragment_to_homeFragment)


        }

        binding.settingUpdateButton.setOnClickListener {

            viewModel.updateProfile()


        }


        binding.settingUpdateImage.setOnClickListener {

            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose your profile picture")
            builder.setItems(options) { dialog, item ->
                when {
                    options[item] == "Take Photo" -> {

                        takePhotoWithCamera()


                    }
                    options[item] == "Choose from Gallery" -> {
                        pickImageFromGallery()
                    }
                    options[item] == "Cancel" -> dialog.dismiss()
                }
            }
            builder.show()






        }



    }



    private fun load2Image(imageUrl: String) {




        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.person).dontAnimate()
            .into(binding.settingUpdateImage)


    }

    private fun loadImage(imageUrl: String) {
        //val imageUrl = imageUrl.trim().replace("%20", "")  // حذف %20 تمامًا من الرابط

        val imageUrl = imageUrl.trim()
            .replace("%20", "") // إزالة المسافات
            .replace("media/media", "media") // إزالة التكرار في كلمة media
        Glide.with(requireContext())
            .load(imageUrl) // إزالة أي مسافات إضافية
            .listener(object : RequestListener<Drawable> {




                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("GlideError", "Failed to load image: ${e?.message}")
                    return false
                }
            })
            .into(binding.settingUpdateImage)


        val fixedUrl = imageUrl.trim().replace(" ", "")
        Glide.with(requireContext())
            .load(fixedUrl)
            .placeholder(R.drawable.person) // صورة افتراضية أثناء التحميل
            .into(binding.settingUpdateImage)

    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun pickImageFromGallery() {

        val pickPictureIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(pickPictureIntent, Utils.REQUEST_IMAGE_PICK)
        }
    }

    // To take a photo with the camera, you can use this code
    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhotoWithCamera() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Utils.REQUEST_IMAGE_CAPTURE)


    }





    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                Utils.REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    if (imageUri == null) {
                        Log.e("SettingFragment", "Image URI is null")
                        return
                    }

                    Log.d("SettingFragment", "Original Image URI: $imageUri")

                    val realPath = getRealPathFromURI(requireContext(), imageUri)
                    val imageBitmap: Bitmap? = if (realPath != null) {
                        BitmapFactory.decodeFile(realPath)
                    } else {
                        requireActivity().contentResolver.openInputStream(imageUri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }

                    if (imageBitmap != null) {
                        uploadImage(imageBitmap)
                    } else {
                        Log.e("SettingFragment", "Failed to load image")
                        Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
    var name = ""
    val returnCursor = this.query(selectedImageUri,null,null,null,null)
    if (returnCursor!=null){
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}

    private fun uploadImage(bitmap: Bitmap) {
        try {
            // تحويل `Bitmap` إلى ملف مؤقت
            val file = bitmapToFile(bitmap) ?: run {
                Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
                Log.e("SettingFragment", "bitmapToFile returned null")
                return
            }

            Log.d("SettingFragment", "Created file for upload: ${file.absolutePath}")

            // إنشاء `RequestBody` لرفع الصورة
            val body = UploadRequestBody(file, "image", requireContext())

            // استدعاء API لرفع الصورة
            ApiService().uploadImage(
                MultipartBody.Part.createFormData("image", file.name, body)
            ).enqueue(object : Callback<UploadResponse> {
                override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { uploadResponse ->
                            Log.d("SettingFragment", "Image uploaded successfully: ${uploadResponse.message}")
                            binding.settingChatContainer.snackbar(uploadResponse.message)
                        } ?: Log.e("SettingFragment", "Response body is null")
                    } else {
                        Log.e("SettingFragment", "Image upload failed: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    Log.e("SettingFragment", "Image upload error: ${t.localizedMessage}", t)

                    val errorMessage = when (t) {
                        is IOException -> "Network error. Check your connection."
                        is SocketTimeoutException -> "Connection timed out. Try again."
                        else -> "Unexpected error occurred."
                    }

                    binding.settingChatContainer.snackbar(errorMessage)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SettingFragment", "Error processing image: ${e.message}")
        }
    }




    private fun View.snackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK") {
                snackbar.dismiss()
            }
        }.show()}


    override fun onResume() {
        super.onResume()


        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })



    }

    override fun onProgressUpdate(percentage: Int) {

    }
    private fun bitmapToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(requireContext().cacheDir, "uploaded_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SettingFragment", "Failed to save bitmap: ${e.message}")
            null
        }
    }
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var realPath: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val documentId = DocumentsContract.getDocumentId(uri)
            if (documentId.startsWith("raw:")) {
                return documentId.removePrefix("raw:")
            }
        }

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                realPath = cursor.getString(columnIndex)
            }
        }
        return realPath
    }


}