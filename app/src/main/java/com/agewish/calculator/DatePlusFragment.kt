package com.agewish.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.agewish.calculator.ads.AdManager
import com.agewish.calculator.databinding.FragmentDatePlusBinding
import com.agewish.calculator.pdf.ImageSharer
import com.agewish.calculator.pdf.PdfSaver
import com.agewish.calculator.pdf.PdfSharer
import com.agewish.calculator.util.DatePlusResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.ResultFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class DatePlusFragment : Fragment() {

    private var _binding: FragmentDatePlusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DatePlusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatePlusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManager.loadBanner(binding.adView)
        setupInputs()
        setupButtons()
        observeViewModel()
    }

    private fun setupInputs() {
        binding.etBaseDate.setText(viewModel.baseDate.value?.format(DateUtils.DISPLAY_FORMATTER))

        binding.etBaseDate.setOnClickListener {
            performHaptic()
            val initial = viewModel.baseDate.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_base_date)
                .setSelection(initial)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setBaseDate(date)
                binding.etBaseDate.setText(date.format(DateUtils.DISPLAY_FORMATTER))
            }
            picker.show(parentFragmentManager, "BASE_DATE_PICKER")
        }

        binding.etAmount.doAfterTextChanged { text ->
            val num = text?.toString()?.toLongOrNull() ?: 0L
            viewModel.setAmount(num)
        }

        binding.switchMonthsMode.setOnCheckedChangeListener { _, isChecked ->
            performHaptic()
            viewModel.setIsMonthsMode(isChecked)
            binding.tilAmount.hint = if (isChecked) getString(R.string.label_months_count) else getString(R.string.label_days_count)
            if (viewModel.result.value != null) {
                viewModel.calculate()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCalculatePlus.setOnClickListener {
            performHaptic()
            viewModel.calculate()
        }

        binding.btnGeneratePlusPdf.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            AdManager.showInterstitialIfDue(requireActivity()) {
                viewModel.generatePdf(requireContext(), app.repository) { generatedFile ->
                    PdfSharer.sharePdf(requireContext(), generatedFile)
                }
            }
        }

        binding.btnSavePlusDownloads.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            val currentFile = viewModel.lastGeneratedPdf.value
            if (currentFile != null && currentFile.exists()) {
                PdfSaver.saveToDownloads(requireContext(), currentFile)
            } else {
                viewModel.generatePdf(requireContext(), app.repository) { generatedFile ->
                    PdfSaver.saveToDownloads(requireContext(), generatedFile)
                }
            }
        }

        binding.btnSharePlusImage.setOnClickListener {
            performHaptic()
            ImageSharer.shareViewAsImage(requireContext(), binding.plusResultContentLayout)
        }

        binding.btnCopyPlus.setOnClickListener {
            performHaptic()
            val currentResult = viewModel.result.value ?: return@setOnClickListener
            val text = ResultFormatter.formatDatePlusClipboard(currentResult)
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("AgeWish Date Math", text))
            Toast.makeText(requireContext(), R.string.toast_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                displayResults(result)
            } else {
                binding.cardPlusResults.visibility = View.GONE
            }
        }
    }

    private fun displayResults(result: DatePlusResult) {
        binding.cardPlusResults.visibility = View.VISIBLE

        binding.tvPlusResultPrimary.text = getString(
            R.string.result_date_format,
            result.resultDate.format(DateUtils.DATE_FORMATTER),
            result.dayOfWeek
        )

        val modeWord = if (result.isMonthsMode) "month(s)" else "day(s)"
        val sign = if (result.amount >= 0) "+${result.amount}" else "${result.amount}"
        binding.tvPlusExplanation.text = "$sign $modeWord applied to base date (${result.baseDate.format(DateUtils.DATE_FORMATTER)})"
    }

    private fun performHaptic() {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
