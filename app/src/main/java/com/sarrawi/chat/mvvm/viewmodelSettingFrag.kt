package com.sarrawi.chat.mvvm

/*class SettingFragment : Fragment() {

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







//        viewModel.imageUrl2.observe(viewLifecycleOwner, Observer { imageUrl ->
//            imageUrl?.let {
//                loadImage(it) // تحميل الصورة عند تغيير imageUrl
//            }
//        })

        viewModel.imageUrl2.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.settingUpdateImage)
            }
        }


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

//            viewModel.imageUrl2.observe(viewLifecycleOwner) { newImageUrl ->
//                if (!newImageUrl.isNullOrEmpty()) {
//                    viewModel.updateProfilea(newImageUrl)
//                }
//            }
            viewModel.imageUrl2.value?.let { imageUrl ->
                viewModel.updateProfilea(imageUrl)
            }
//            viewModel.updateProfilea()



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
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                val file = File(getRealPathFromURI(it))
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // استدعاء uploadImage
                viewModel.uploadImage(imagePart) { response ->
                    if (response != null) {
                        Toast.makeText(requireContext(), "تم تحميل الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "فشل في تحميل الصورة", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = it.getString(index)
            }
            it.close()
        }
        return path
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 100
    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == AppCompatActivity.RESULT_OK) {
//            when (requestCode) {
//                Utils.REQUEST_IMAGE_CAPTURE -> {
//                    val imageBitmap = data?.extras?.get("data") as Bitmap
////                    viewModel.uploadImageAndUpdateProfile(imageBitmap)
//                }
//                Utils.REQUEST_IMAGE_PICK -> {
//                    val imageUri = data?.data
//                    val imageBitmap =
//                        MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
//                    viewModel.uploadImageAndUpdateProfile(imageBitmap)
//                }
//            }
//        }
//    }

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

//    private fun convertBitmapToFile(bitmap: Bitmap): File? {
//        val filesDir = requireContext().filesDir
//        val imageFile = File(filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
//
//        return try {
//            val outputStream = FileOutputStream(imageFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            imageFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun convertUriToFile(uri: Uri): File? {
//        val inputStream = requireContext().contentResolver.openInputStream(uri)
//        val tempFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
//
//        return try {
//            val outputStream = FileOutputStream(tempFile)
//            inputStream?.copyTo(outputStream)
//            outputStream.flush()
//            outputStream.close()
//            tempFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }


    override fun onResume() {
        super.onResume()


        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            loadImage(it)




        })



    }



class ChatAppViewModel : ViewModel() {

    private val _imageUrls = MutableLiveData<String>()
    val imageUrls: LiveData<String> get() = _imageUrls

    val message = MutableLiveData<String>()
    val firestore = FirebaseFirestore.getInstance()
    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()


    val usersRepo = UsersRepo()
    val messageRepo = MessageRepo()
    var token: String? = null
    val chatlistRepo = ChatListRepo()
    private val _imageUrl2 = MutableLiveData<String>()
    val imageUrl2: LiveData<String> get() = _imageUrl2
    private val _uploadResult = MutableLiveData<Resource<String>>()
    val uploadResult: LiveData<Resource<String>> get() = _uploadResult

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }


    init {

        getCurrentUser()
        getRecentUsers()
    }

    fun getUsers(): LiveData<List<Users>> {
        return usersRepo.getUsers()


    }


    // sendMessage

    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val context = MyApplication.instance.applicationContext

            val hashMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to message.value!!,
                "time" to Utils.getTime()
            )


            val uniqueId = listOf(sender, receiver).sorted()
            uniqueId.joinToString(separator = "")


            val friendnamesplit = friendname.split("\\s".toRegex())[0]
            val mysharedPrefs = SharedPrefs(context)
            mysharedPrefs.setValue("friendid", receiver)
            mysharedPrefs.setValue("chatroomid", uniqueId.toString())
            mysharedPrefs.setValue("friendname", friendnamesplit)
            mysharedPrefs.setValue("friendimage", friendimage)




            firestore.collection("Messages").document(uniqueId.toString()).collection("chats")
                .document(Utils.getTime()).set(hashMap).addOnCompleteListener { taskmessage ->


                    val setHashap = hashMapOf<String, Any>(
                        "friendid" to receiver,
                        "time" to Utils.getTime(),
                        "sender" to Utils.getUidLoggedIn(),
                        "message" to message.value!!,
                        "friendsimage" to friendimage,
                        "name" to friendname,
                        "person" to "you"
                    )


                    firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(receiver)
                        .set(setHashap)



                    firestore.collection("Conversation${receiver}").document(Utils.getUidLoggedIn())
                        .update(
                            "message",
                            message.value!!,
                            "time",
                            Utils.getTime(),
                            "person",
                            name.value!!
                        )



                      firestore.collection("Tokens").document(receiver).addSnapshotListener { value, error ->


                          if (value != null && value.exists()) {


                              val tokenObject = value.toObject(Token::class.java)


                              token = tokenObject?.token!!


                              val loggedInUsername =
                                  mysharedPrefs.getValue("username")!!.split("\\s".toRegex())[0]



                              if (message.value!!.isNotEmpty() && receiver.isNotEmpty()) {

                                  val notificationRequest = NotificationRequest(
                                      token!!,  // التوكن المستلم من Firestore
                                      message.value!!,  // الرسالة
                                      loggedInUsername  // اسم المرسل
                                  )
//                                      sendNotificationToDjango(notificationRequest)
                                  }

                              } else {


                                  Log.e("ChatAppViewModel", "NO TOKEN, NO NOTIFICATION")
                              }


                          }

                          Log.e("ViewModel", token.toString())



                          if (taskmessage.isSuccessful){

                              message.value = ""



                          }


                      }
                   }










    // getting messages

    fun getMessages(friend: String): LiveData<List<Messages>> {

        return messageRepo.getMessages(friend)
    }


    // get RecentUsers




    fun getRecentUsers(): LiveData<List<RecentChats>> {


        return chatlistRepo.getAllChatList()

    }






    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext


        firestore.collection("Users").document(Utils.getUidLoggedIn())
            .addSnapshotListener { value, error ->


                if (value!!.exists() && value != null) {

                    val users = value.toObject(Users::class.java)
                    name.value = users?.username!!
                    imageUrl.value = users.imageUrl!!


                    val mysharedPrefs = SharedPrefs(context)
                    mysharedPrefs.setValue("username", users.username!!)


                }


            }


    }





    private val apiService = RetrofitClient.getInstance().create(ApiService::class.java)



    // دالة لحفظ الرابط في Firestore



    fun updateProfile() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext

        val hashMapUser =
            hashMapOf<String, Any>("username" to name.value!!, "imageUrl" to imageUrl.value!!)

        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(hashMapUser).addOnCompleteListener {

            if (it.isSuccessful){

                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT ).show()


            }

        }


        val mysharedPrefs = SharedPrefs(context)
        val friendid = mysharedPrefs.getValue("friendid")

        val hashMapUpdate = hashMapOf<String, Any>("friendsimage" to imageUrl.value!!, "name" to name.value!!, "person" to name.value!!)



        // updating the chatlist and recent list message, image etc

        firestore.collection("Conversation${friendid}").document(Utils.getUidLoggedIn()).update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(friendid!!).update("person", "you")



    }



    fun uploadImageToServer(imageFile: File, userId: String) {
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

        viewModelScope.launch {
            _uploadResult.postValue(Resource.Loading())

            try {
                val response =messageRepo .uploadImage(imagePart)
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.imageUrl

                    // تحديث Firestore برابط الصورة
                    firestore.collection("users").document(userId)
                        .update("profileImage", imageUrl)
                        .addOnSuccessListener {
                            _uploadResult.postValue(Resource.Success(imageUrl))
                        }
                        .addOnFailureListener {
                            _uploadResult.postValue(Resource.Error("Failed to update Firestore"))
                        }
                } else {
                    _uploadResult.postValue(Resource.Error("Upload failed"))
                }
            } catch (e: Exception) {
                _uploadResult.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }

    fun uploadImage(imagePart: MultipartBody.Part, callback: (UploadResponse?) -> Unit) {
        viewModelScope.launch {
            val response = messageRepo.uploadImage2(imagePart)
            callback(response)
        }
    }
    //deep

    fun uploadImageAndUpdateProfile(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), data)
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sa(imagePart)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    imageUrl?.let {
                        withContext(Dispatchers.Main) {
                            _imageUrl2.value = it // تحديث LiveData
                            updateProfilea(it)   // تحديث البروفايل إذا لزم الأمر
                        }
                    }
                } else {
                    Log.w("Retrofit", "فشل رفع الصورة: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "حدث خطأ أثناء رفع الصورة: ${e.message}")
            }
        }
    }

    fun uploadImageAndUpdateProfile2(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), data)
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sa(imagePart)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    imageUrl?.let {
                        withContext(Dispatchers.Main) {
                            this@ChatAppViewModel.imageUrl.value = it // حفظ الرابط
                            updateProfilea(it) // تمرير رابط الصورة إلى دالة تحديث البروفايل
                        }
                    }
                } else {
                    Log.w("Retrofit", "فشل رفع الصورة: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "حدث خطأ أثناء رفع الصورة: ${e.message}")
            }
        }
    }

    fun updateProfilea(imageUrl: String) = viewModelScope.launch(Dispatchers.IO) {
        val context = MyApplication.instance.applicationContext

        val hashMapUser = hashMapOf<String, Any>(
            "username" to (name.value ?: ""),
            "imageUrl" to imageUrl
        )

        firestore.collection("Users").document(Utils.getUidLoggedIn())
            .update(hashMapUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "تم تحديث الملف الشخصي بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحديث الملف الشخصي", Toast.LENGTH_SHORT).show()
                }
            }

        val mySharedPrefs = SharedPrefs(context)
        val friendId = mySharedPrefs.getValue("friendid")

        val hashMapUpdate = hashMapOf<String, Any>(
            "friendsimage" to imageUrl,
            "name" to (name.value ?: ""),
            "person" to (name.value ?: "")
        )

        firestore.collection("Conversation${friendId}")
            .document(Utils.getUidLoggedIn())
            .update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
            .document(friendId ?: "")
            .update("person", "you")
    }



}*/