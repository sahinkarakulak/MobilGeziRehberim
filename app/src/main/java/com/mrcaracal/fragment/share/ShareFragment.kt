package com.mrcaracal.fragment.share

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.activity.homePage.HomePageActivity
import com.mrcaracal.extensions.loadUrl
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragShareBinding
import com.mrcaracal.utils.Constants
import com.squareup.picasso.Picasso

class ShareFragment : Fragment() {
    private lateinit var viewModel: ShareViewModel
    private var _binding: FragShareBinding? = null
    private val binding get() = _binding!!

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    private lateinit var addres: String
    lateinit var postCode: String

    var latitude = 0f
    var longitude = 0f
    private lateinit var picturePath: Uri

    private fun init() {
        GET = requireActivity().getSharedPreferences(
            getString(R.string.map_key),
            Context.MODE_PRIVATE
        )
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        init()
        _binding = FragShareBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        initClickListeners()
        observeContactState()
        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(ShareViewModel::class.java)
    }

    fun initClickListeners() {
        binding.imgSharePictureSelected.setOnClickListener {
            choosePictureFromGallery()
        }

        binding.btnAddTag.setOnClickListener {
            createTag()
        }

        binding.selectLocation.setOnClickListener {

            //val selectedFragment = SelectFragmentOnHomePageActivity.selectFragment(Constants.SELECT_MAP_FRAGMENT)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, SelectLocationMapFragment())
                .commit()
            viewModel.turnOnOrOffLocation(context = requireContext())
            //startActivity(Intent(activity, SelectMapActivity::class.java))
        }

        binding.btnShareSend.setOnClickListener {
            shareSend()
        }
    }

    private fun observeContactState() {
        viewModel.shareState.observe(viewLifecycleOwner) { shareViewState ->
            when (shareViewState) {
                is ShareViewState.ShowToastMessageAndBtnState -> {
                    toast(requireContext(), getString(R.string.fill_in_the_required_fields))
                }
                is ShareViewState.ShowExceptionAndBtnState -> {
                    toast(requireContext(), shareViewState.exception.toString())
                }
                is ShareViewState.ShowToastMessage -> {
                    toast(requireContext(), getString(R.string.gps_on))
                }
                is ShareViewState.OpenHomePage -> {
                    val intent =
                        Intent(activity, HomePageActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                is ShareViewState.GetTags -> {
                    binding.tvPrintTags.text = shareViewState.tags
                }
                is ShareViewState.PicassoPross -> {
                    binding.imgSharePictureSelected.loadUrl(shareViewState.picturePath)

                    /*Picasso.get()
                        .load(shareViewState.picturePath)
                        .centerCrop()
                        .fit()
                        .into(binding.imgSharePictureSelected)*/
                }
            }
        }
    }

    private fun shareSend() {
        binding.btnShareSend.isEnabled = true
        val strPlaceName = binding.edtSharePlaceName.text.toString()
        val strComment = binding.edtShareComment.text.toString()
        val strLocation = binding.edtLocation.text.toString()
        val strAddress = binding.edtAddres.text.toString()
        val strCity = binding.edtCity.text.toString()
        viewModel.shareThePost(
            getPlaceName = strPlaceName,
            getComment = strComment,
            getLocation = strLocation,
            getAddress = strAddress,
            getCity = strCity
        )
    }

    private fun createTag() {
        val tagsTakenByEditText =
            binding.edtShareTag.text.toString().lowercase().split(" ").toTypedArray()
        viewModel.createTagForPost(tagsTakenByEditText = tagsTakenByEditText)
    }

    private fun choosePictureFromGallery() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            val intentGaleri =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentGaleri, 2)
        }
    }

    override fun onResume() {
        super.onResume()
        latitude = GET.getFloat("enlem", 0f)
        longitude = GET.getFloat("boylam", 0f)
        addres = GET.getString("adres", "Türkiye Üsküdar")!!
        postCode = GET.getString("postaKodu", "12000")!!
        binding.edtLocation.setText("$latitude,$longitude")
        binding.edtAddres.setText("" + addres)
        viewModel.sendPostCode(postCode = postCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            picturePath = data.data!!
            viewModel.imagePathForPostToBeShared(picturePath = picturePath)
        }

        if (requestCode == Constants.REQUEST_CHECK_SETTING) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    toast(requireContext(), "GPS is turned on")
                }
                Activity.RESULT_CANCELED -> {
                    toast(requireContext(), "GPS is required to be turned on")
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}