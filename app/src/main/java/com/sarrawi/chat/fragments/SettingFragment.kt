@file:Suppress("DEPRECATION")

package com.sarrawi.chat.fragments


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.databinding.FragmentSettingBinding
import com.sarrawi.chat.mvvm.ChatAppViewModel
import com.sarrawi.chat.uploadImage.up2.ApiService
import com.sarrawi.chat.uploadImage.up2.UploadRequestBody
import com.sarrawi.chat.uploadImage.up2.ImageUploadResponse
import com.sarrawi.chat.uploadImage.up2.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class SettingFragment : Fragment() {
    private var selectedImageUri: Uri? = null

    lateinit var viewModel: ChatAppViewModel
    lateinit var binding : FragmentSettingBinding

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



    private fun loadImage(imageUrl: String) {




        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.person).dontAnimate()
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

    override fun onResume() {
        super.onResume()


        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })



    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                Utils.REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // حفظ الصورة الملتقطة في الـ Uri
                    val uri = getImageUriFromBitmap(imageBitmap)
                    selectedImageUri = uri
                    uploadImage()
                }
                Utils.REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    selectedImageUri = imageUri
                    uploadImage()
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "title", null)
        return Uri.parse(path)
    }




    private fun uشploadImage() {
        val selectedUri = selectedImageUri
        if (selectedUri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(
            selectedUri, "r", null
        ) ?: run {
            Toast.makeText(requireContext(), "Unable to open image", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // احصل على context من الـ Fragment للوصول إلى cacheDir
            val context = requireContext()

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(context.cacheDir, context.contentResolver.getFileName(selectedUri))
            val outputStream = FileOutputStream(file)

            // نسخ البيانات من المدخلات إلى المخرجات
            inputStream.copyTo(outputStream)

            val body = UploadRequestBody(file, "image", requireContext())

            ApiService().uploadImage(
                MultipartBody.Part.createFormData(
                    "image", file.name, body
                )
            ).enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        val uploadedImageUrl = body?.image_url

                        if (!uploadedImageUrl.isNullOrEmpty()) {
                            // 1️⃣ حفظ الرابط في ViewModel
                            Log.d("ImageUpload", "رابط الصورة المرفوعة: $uploadedImageUrl")

                            viewModel.setImageUrl(uploadedImageUrl)

                            // 2️⃣ بعد حفظ الرابط، استدعِ updateProfile()
                            viewModel.updateProfile()

                            Toast.makeText(requireContext(), "تم رفع الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "فشل في جلب الرابط من الاستجابة!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "حدث خطأ: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }




//                override fun onResponse(
//                    call: Call<ImageUploadResponse>,
//                    response: Response<ImageUploadResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        response.body()?.let {
//                            val uploadedImageUrl = it.imageUrl  // افترض أن الجسم يحتوي على رابط للصورة
//
//                            // تحقق من الرابط قبل التحديث
//                            if (uploadedImageUrl != null) {
//                                // تحديث الـ LiveData في الـ ViewModel
//                                viewModel.setImageUrl(uploadedImageUrl)
//
//                                // إظهار رسالة تفيد بأن الصورة تم رفعها بنجاح
//                                Toast.makeText(requireContext(), "تم رفع الصورة بنجاح: $uploadedImageUrl", Toast.LENGTH_SHORT).show()
//                            } else {
//                                Toast.makeText(requireContext(), "الرابط فارغ", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    } else {
//                        Toast.makeText(requireContext(), "حدث خطأ: ${response.message()}", Toast.LENGTH_SHORT).show()
//                    }
////
////                        response.body()?.let {
////
////                            // التعامل مع الاستجابة الناجحة
////                            // افترض أن الجسم يحتوي على رابط للصورة
////                            val uploadedImageUrl = it.imageUrl  // ضع هنا كيفية استخراج الرابط من الاستجابة
////
////                            // هنا يمكنك استخدام الرابط كما تشاء
////                            Toast.makeText(requireContext(), "تم رفع الصورة بنجاح: $uploadedImageUrl", Toast.LENGTH_SHORT).show()
////
////                            // مثلا تخزين الرابط في ViewModel
////                            viewModel.setImageUrl(uploadedImageUrl)
////                            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
////                        }
////                    } else {
////                        Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
////                    }
//                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage() {
        val selectedUri = selectedImageUri
        if (selectedUri == null) {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(
            selectedUri, "r", null
        ) ?: run {
            Toast.makeText(requireContext(), "Unable to open image", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // احصل على context من الـ Fragment للوصول إلى cacheDir
            val context = requireContext()

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(context.cacheDir, context.contentResolver.getFileName(selectedUri))
            val outputStream = FileOutputStream(file)

            // نسخ البيانات من المدخلات إلى المخرجات
            inputStream.copyTo(outputStream)

            val body = UploadRequestBody(file, "image", requireContext())

            ApiService().uploadImage(
                MultipartBody.Part.createFormData(
                    "image", file.name, body
                )
            ).enqueue(object : Callback<UploadResponse> {
                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        val uploadedImageUrl = body?.image_url

                        if (!uploadedImageUrl.isNullOrEmpty()) {
                            // 1️⃣ حفظ الرابط في ViewModel
                            Log.d("ImageUpload", "رابط الصورة المرفوعة: $uploadedImageUrl")

                            viewModel.setImageUrl(uploadedImageUrl)

                            // 2️⃣ بعد حفظ الرابط، استدعِ updateProfile()
                            viewModel.updateProfile()

                            Toast.makeText(requireContext(), "تم رفع الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "فشل في جلب الرابط من الاستجابة!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "حدث خطأ: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "فشل: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "حدث خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}