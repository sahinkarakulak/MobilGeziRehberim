package com.mrcaracal.activity.editProfile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.*
import com.mrcaracal.activity.homePage.HomePageActivity
import com.mrcaracal.extensions.loadUrl
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityEditProfileBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initClickListeners()
        observeEditProfileState()
        viewModel.getProfileData()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
    }

    private fun initClickListeners() {
        binding.tvUserChangePicture.setOnClickListener {
            changePicture()
        }
        binding.imgUserPicture.setOnClickListener {
            changePicture()
        }
        binding.btnUpdate.setOnClickListener {
            viewModel.updateUser(
                u_name = binding.edtGetUserName.text.toString(),
                u_bio = binding.edtGetBiography.text.toString()
            )
        }
    }

    private fun changePicture() {
        CropImage
            .activity()
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this@EditProfileActivity)
    }

    private fun observeEditProfileState() {
        viewModel.editProfileViewState.observe(this) { editProfileViewState ->
            when (editProfileViewState) {
                is EditProfileViewState.OpenHomePageActivity -> {
                    startActivity(Intent(this@EditProfileActivity, HomePageActivity::class.java))
                }
                is EditProfileViewState.ShowErrorOccuredMessage -> {
                    toast(getString(R.string.error_occurred))
                }
                is EditProfileViewState.ShowExceptionErrorMessage -> {
                    toast(editProfileViewState.exception.toString())
                }
                is EditProfileViewState.OnUserImage -> {
                    binding.imgUserPicture.loadUrl(editProfileViewState.userImageUrl)
                    /*Picasso.get()
                        .load(editProfileViewState.userImageUrl)
                        .into(binding.imgUserPicture)*/
                }
                is EditProfileViewState.BindingTvUserEmailChangeText -> {
                    binding.tvUserEmail.text = editProfileViewState.firebaseUserName
                }
                is EditProfileViewState.OnUserInfo -> {
                    binding.edtGetUserName.setText(
                        editProfileViewState.userName
                    )
                    binding.edtGetBiography.setText(
                        editProfileViewState.userBio
                    )
                }
                else -> {
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            viewModel.mImageUri = result.uri
            viewModel.uploadImage(contentResolver = contentResolver)
            toast(getString(R.string.updated))
        } else {
            toast(getString(R.string.did_you_give_up))
        }
    }
}