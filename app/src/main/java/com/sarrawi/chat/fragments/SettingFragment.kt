@file:Suppress("DEPRECATION")

package com.sarrawi.chat.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

import java.io.ByteArrayOutputStream
import java.util.*

class SettingFragment : Fragment() {

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


//        binding.settingUpdateImage.setOnClickListener {
//
//            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
//            val builder = AlertDialog.Builder(requireContext())
//            builder.setTitle("Choose your profile picture")
//            builder.setItems(options) { dialog, item ->
//                when {
//                    options[item] == "Take Photo" -> {
//
//                        takePhotoWithCamera()
//
//
//                    }
//                    options[item] == "Choose from Gallery" -> {
//                        pickImageFromGallery()
//                    }
//                    options[item] == "Cancel" -> dialog.dismiss()
//                }
//            }
//            builder.show()
//
//
//
//
//
//
//        }
//


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


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                Utils.REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap

                }
                Utils.REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    val imageBitmap =
                        MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                }
            }
        }


    }




    override fun onResume() {
        super.onResume()


        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })



    }


}