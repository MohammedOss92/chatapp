@file:Suppress("DEPRECATION")

package com.sarrawi.chat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.firestore.FirebaseFirestore
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.databinding.FragmentSettingBinding
import com.sarrawi.chat.mvvm.ChatAppViewModel
import com.sarrawi.chat.uploadImage.Resource
import com.sarrawi.chat.uploadImage.RetrofitClientInstance
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SettingFragment : Fragment() {

    lateinit var viewModel: ChatAppViewModel
    lateinit var binding : FragmentSettingBinding

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







        viewModel.imageUrl2.observe(viewLifecycleOwner, Observer { imageUrl ->
            imageUrl?.let {
                loadImage(it) // تحميل الصورة عند تغيير imageUrl
            }
        })
//        viewModel.uploadResult.observe(viewLifecycleOwner, Observer { result ->
//            when (result) {
//                is Resource.Success -> {
//                    val imageUrl = result.data
//                    // Update UI with the new image URL (e.g., Glide)
//                    Glide.with(requireContext()).load(imageUrl).into(binding.settingUpdateImage)
//                }
//                is Resource.Error -> {
//                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
//                }
//                is Resource.Loading -> {
//                    // Show loading state if necessary
//                }
//            }
//        })

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



//    private fun loadImage(imageUrl: String) {
//
//
//
//
//        Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.person).dontAnimate()
//            .into(binding.settingUpdateImage)
//
//
//    }

    private fun loadImage(imageUrl: String) {
        // تحميل الصورة باستخدام Glide
        Glide.with(requireContext())
            .load(imageUrl) // استخدام imageUrl مباشرة
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
                Utils.REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    viewModel.uploadImageAndUpdateProfile(imageBitmap)
                }
                Utils.REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    val imageBitmap =
                        MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                    viewModel.uploadImageAndUpdateProfile(imageBitmap)
                }
            }
        }
    }

//
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == AppCompatActivity.RESULT_OK) {
//            when (requestCode) {
//                Utils.REQUEST_IMAGE_CAPTURE -> {
//                    val imageBitmap = data?.extras?.get("data") as Bitmap
//                    val imageFile = convertBitmapToFile(imageBitmap) // تحويل Bitmap إلى File
//
//                    val userId = Utils.getUidLoggedIn() // احصل على الـ userId الحالي
//                    if (imageFile != null) {
//                        viewModel.uploadImageToServer(imageFile, userId)
//                    }
//                }
//                Utils.REQUEST_IMAGE_PICK -> {
//                    val imageUri = data?.data
//                    if (imageUri != null) {
//                        val imageFile = convertUriToFile(imageUri) // تحويل Uri إلى File
//                        val userId = Utils.getUidLoggedIn()
//                        if (imageFile != null) {
//                            viewModel.uploadImageToServer(imageFile, userId)
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun convertBitmapToFile(bitmap: Bitmap): File? {
        val filesDir = requireContext().filesDir
        val imageFile = File(filesDir, "profile_image_${System.currentTimeMillis()}.jpg")

        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertUriToFile(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

        return try {
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override fun onResume() {
        super.onResume()


        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })



    }






}