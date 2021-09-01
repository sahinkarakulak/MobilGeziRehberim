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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.activity.selectMap.SelectMapActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragShareBinding
import com.squareup.picasso.Picasso

private const val TAG = "ShareFragment"

class ShareFragment : Fragment() {

    private lateinit var viewModel: ShareViewModel
    private var _binding: FragShareBinding? = null
    private val binding get() = _binding!!

    lateinit var GET: SharedPreferences
    lateinit var SET: SharedPreferences.Editor
    lateinit var addres: String
    lateinit var postCode: String

    var latitude = 0f
    var longitude = 0f
    private lateinit var picturePath : Uri


    private fun init() {
        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
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
        viewModel.init()
        initClickListeners()
        observeContactState()

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(ShareViewModel::class.java)
    }

    fun initClickListeners() {
        binding.imgSharePictureSelected.setOnClickListener(View.OnClickListener {
            choosePictureFromGallery()
        })

        binding.btnAddTag.setOnClickListener(View.OnClickListener {
            createTag()
        })

        binding.selectLocation.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity, SelectMapActivity::class.java))
        })

        binding.btnShareSend.setOnClickListener(View.OnClickListener {
            shareSend()
        })
    }

    fun observeContactState() {
        viewModel.shareState.observe(viewLifecycleOwner) { shareViewState ->
            when (shareViewState) {
                is ShareViewState.ShowToastMessageAndBtnState -> {
                    activity?.let { toast(it, getString(R.string.fill_in_the_required_fields)) }
                    //binding.btnShareSend.isEnabled = false
                }
                is ShareViewState.ShowExceptionAndBtnState -> {
                    Toast.makeText(
                        activity,
                        shareViewState.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    //binding.btnShareSend.isEnabled = false
                    Log.i(TAG, "observeContactState: " + shareViewState.exception.toString())
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
                    Picasso.get()
                        .load(shareViewState.picturePath)
                        .centerCrop()
                        .fit()
                        .into(binding.imgSharePictureSelected)
                }
            }
        }
    }

    fun shareSend() {
        binding.btnShareSend.isEnabled = true
        val strPlaceName = binding.edtSharePlaceName.text.toString()
        val strComment = binding.edtShareComment.text.toString()
        val strLocation = binding.edtLocation.text.toString()
        val strAddress = binding.edtAddres.text.toString()
        val strCity = binding.edtCity.text.toString()
        viewModel.shareSend(strPlaceName, strComment, strLocation, strAddress, strCity)
    }

    fun createTag() {
        val tagler = binding.edtShareTag.text.toString().lowercase().split(" ").toTypedArray()
        viewModel.createTag(tagler)
    }

    private fun choosePictureFromGallery() {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
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
        viewModel.sendPostCode(postCode)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            viewModel.picturePath(picturePath = picturePath!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}