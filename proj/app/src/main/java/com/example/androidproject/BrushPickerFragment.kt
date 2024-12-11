package com.example.androidproject

import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider

class BrushPickerFragment : Fragment() {

    private lateinit var viewModel: DrawingViewModel

    enum class BrushType {
        NORMAL,
        CIRCLE,
        SQUARE
    }

    private var currentBrushType: BrushType = BrushType.NORMAL
    private var currentPenSize: Float = 5f


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_brush_picker, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(DrawingViewModel::class.java)

        val normalPenButton: ImageButton = view.findViewById(R.id.normalBrush)
        val circlePenButton: ImageButton = view.findViewById(R.id.circleBrush)
        val squarePenButton: ImageButton = view.findViewById(R.id.squareBrush)
        val sizeButton1: Button = view.findViewById(R.id.size1)
        val sizeButton2: Button = view.findViewById(R.id.size2)
        val sizeButton3: Button = view.findViewById(R.id.size3)

        normalPenButton.setOnClickListener {
            currentBrushType = BrushType.NORMAL
            viewModel.changePenType(currentBrushType)
        }

        circlePenButton.setOnClickListener {
            currentBrushType = BrushType.CIRCLE
            viewModel.changePenType(currentBrushType)
        }

        squarePenButton.setOnClickListener {
            currentBrushType = BrushType.SQUARE
            viewModel.changePenType(currentBrushType)
        }

        sizeButton1.setOnClickListener {
            currentPenSize = 5f
            viewModel.changePenSize(currentPenSize)
        }

        sizeButton2.setOnClickListener {
            currentPenSize = 10f
            viewModel.changePenSize(currentPenSize)
        }

        sizeButton3.setOnClickListener {
            currentPenSize = 15f
            viewModel.changePenSize(currentPenSize)
        }
        return view
        }

    }
