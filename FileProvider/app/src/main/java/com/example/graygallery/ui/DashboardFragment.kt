/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.graygallery.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.graygallery.databinding.FragmentDashboardBinding
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {
    private val viewModel by viewModels<AppViewModel>()
    private lateinit var binding: FragmentDashboardBinding
    private val takePicture = registerForActivityResult(TakePicturePreview()) { bitmap ->
        // 写真撮影後、画像データがbitmapに入る。キャンセル時はnullが渡ってくる。
        viewModel.saveImageFromCamera(bitmap)
    }

    private val selectPicture = registerForActivityResult(GetContentWithMimeTypes()) { uri ->
        uri?.let {
            viewModel.copyImageFromUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.takePicture.setOnClickListener {
            // TakePicturePreviewは、 引数がVoidなので、nullを渡すでOK.
            takePicture.launch(null)
        }

        binding.selectPicture.setOnClickListener {
            selectPicture.launch(ACCEPTED_MIMETYPES)
        }

        binding.addRandomImage.setOnClickListener {
            viewModel.saveRandomImageFromInternet()
        }

        binding.clearFiles.setOnClickListener {
            viewModel.clearFiles()
        }

        viewModel.notification.observe(viewLifecycleOwner, Observer {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
        })

        return binding.root
    }
}

// 引数の型: Array<String>
// 戻り値の型: Uri?
class GetContentWithMimeTypes : ActivityResultContract<Array<String>, Uri?>() {
    // {@link Activity#startActivityForResult} で使うインテントの作成.
    override fun createIntent(
        context: Context,
        input: Array<String>
    ): Intent {
        return Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, input);

    }

    override fun getSynchronousResult(
        context: Context,
        input: Array<String>
    ): ActivityResultContract.SynchronousResult<Uri?>? {
        return null
    }

    // 戻り値の処理.
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        // intent.dataに選択した画像のuriが渡ってくる.
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}