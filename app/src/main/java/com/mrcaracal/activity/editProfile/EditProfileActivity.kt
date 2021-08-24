package com.mrcaracal.activity.editProfile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.*
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityEditProfileBinding
import com.squareup.picasso.Picasso
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
        viewModel.initialize()
        initClickListeners()
        observeEditProfileState()
        viewModel.getProfileData()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
    }

    private fun initClickListeners() {
        binding.tvUserChangePicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        binding.imgUserPicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        binding.btnUpdate.setOnClickListener {
            viewModel.updateUser(
                binding.edtGetUserName.text.toString(),
                binding.edtGetBiography.text.toString()
            )
        }
    }

    private fun observeEditProfileState() {
        viewModel.editProfileViewState.observe(this) { editProfileViewState ->
            when(editProfileViewState){
                is EditProfileViewState.OpenHomePageActivity -> {
                    startActivity(Intent(this@EditProfileActivity, HomePageActivity::class.java))
                }
                is EditProfileViewState.ShowErrorOccuredMessage -> {
                    toast(getString(R.string.error_occurred))
                }
                is EditProfileViewState.ShowExceptionErrorMessage -> {
                    toast(editProfileViewState.exception.toString())
                }
                is EditProfileViewState.PicassoPross -> {
                    Picasso.get().load(editProfileViewState.documentSnapshot.getString(editProfileViewState.firebaseDocValueUserPic))
                        .into(binding.imgUserPicture)
                }
                is EditProfileViewState.BindingTvUserEmailChangeText -> {
                    binding.tvUserEmail.text = editProfileViewState.firebaseUserName
                }
                is EditProfileViewState.GetFirebaseDocValUserName -> {
                    binding.edtGetUserName.setText(
                        editProfileViewState.documentSnapshot.getString(
                            editProfileViewState.firebaseDocValUserName
                        )
                    )
                }
                is EditProfileViewState.GetFirebaseDocValBio -> {
                    binding.edtGetBiography.setText(
                        editProfileViewState.documentSnapshot.getString(
                            editProfileViewState.firebaseDocValBio
                        )
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            viewModel.mImageUri = result.uri
            viewModel.uploadImage(contentResolver)
            toast(getString(R.string.updated))
        } else {
            toast(getString(R.string.did_you_give_up))
        }
    }
}