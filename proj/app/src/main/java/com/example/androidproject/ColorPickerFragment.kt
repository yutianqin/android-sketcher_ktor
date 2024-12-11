package com.example.androidproject

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider

class ColorPickerFragment : Fragment() {

    private lateinit var viewModel: DrawingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_color_picker, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(DrawingViewModel::class.java)

        val redButton: Button = view.findViewById(R.id.redButton)
        val blueButton: Button = view.findViewById(R.id.blueButton)
        val greenButton: Button = view.findViewById(R.id.greenButton)
        val yellowButton: Button = view.findViewById(R.id.yellowButton)
        val blackButton: Button = view.findViewById(R.id.blackButton)

        redButton.setOnClickListener {
            viewModel.changeColor(Color.RED)
        }

        blueButton.setOnClickListener {
            viewModel.changeColor(Color.BLUE)
        }

        greenButton.setOnClickListener {
            viewModel.changeColor(Color.GREEN)
        }

        yellowButton.setOnClickListener {
            viewModel.changeColor(Color.YELLOW)
        }

        blackButton.setOnClickListener {
            viewModel.changeColor(Color.BLACK)
        }

        return view
    }
}