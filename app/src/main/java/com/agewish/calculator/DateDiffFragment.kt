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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.agewish.calculator.ads.AdManager
import com.agewish.calculator.databinding.FragmentDateDiffBinding
import com.agewish.calculator.pdf.ImageSharer
import com.agewish.calculator.pdf.PdfSaver
import com.agewish.calculator.pdf.PdfSharer
import com.agewish.calculator.util.DateDifferenceResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.ResultFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class DateDiffFragment : Fragment() {

    private var _binding: FragmentDateDiffBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DateDiffViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDateDiffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManager.loadBanner(binding.adView)
        setupDatePickers()
        setupSwitches()
        setupButtons()
        observeViewModel()
    }

    private fun setupDatePickers() {
        binding.etStartDate.setText(viewModel.startDate.value?.format(DateUtils.DISPLAY_FORMATTER))
        binding.etEndDate.setText(viewModel.endDate.value?.format(DateUtils.DISPLAY_FORMATTER))

        binding.etStartDate.setOnClickListener {
            performHaptic()
            val initial = viewModel.startDate.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_start_date)
                .setSelection(initial)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setStartDate(date)
                binding.etStartDate.setText(date.format(DateUtils.DISPLAY_FORMATTER))
            }
            picker.show(parentFragmentManager, "START_DATE_PICKER")
        }

        binding.etEndDate.setOnClickListener {
            performHaptic()
            val initial = viewModel.endDate.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_end_date)
                .setSelection(initial)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setEndDate(date)
                binding.etEndDate.setText(date.format(DateUtils.DISPLAY_FORMATTER))
            }
            picker.show(parentFragmentManager, "END_DATE_PICKER")
        }
    }

    private fun setupSwitches() {
        binding.switchIncludeEndDay.setOnCheckedChangeListener { _, isChecked ->
            performHaptic()
            viewModel.setIncludeEndDay(isChecked)
            if (viewModel.result.value != null) {
                viewModel.calculate()
            }
        }

        binding.switchWorkingDays.setOnCheckedChangeListener { _, isChecked ->
            performHaptic()
            viewModel.setCountWorkingDays(isChecked)
            if (viewModel.result.value != null) {
                viewModel.calculate()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCalculateDiff.setOnClickListener {
            performHaptic()
            val success = viewModel.calculate()
            if (!success) {
                Toast.makeText(requireContext(), R.string.toast_start_end_order, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGenerateDiffPdf.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            AdManager.showInterstitialIfDue(requireActivity()) {
                viewModel.generatePdf(requireContext(), app.repository) { generatedFile ->
                    PdfSharer.sharePdf(requireContext(), generatedFile)
                }
            }
        }

        binding.btnSaveDiffDownloads.setOnClickListener {
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

        binding.btnShareDiffImage.setOnClickListener {
            performHaptic()
            ImageSharer.shareViewAsImage(requireContext(), binding.diffResultContentLayout)
        }

        binding.btnCopyDiff.setOnClickListener {
            performHaptic()
            val currentResult = viewModel.result.value ?: return@setOnClickListener
            val text = ResultFormatter.formatDateDiffClipboard(currentResult)
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("AgeWish Difference", text))
            Toast.makeText(requireContext(), R.string.toast_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                displayResults(result)
            } else {
                binding.cardDiffResults.visibility = View.GONE
            }
        }
    }

    private fun displayResults(result: DateDifferenceResult) {
        binding.cardDiffResults.visibility = View.VISIBLE

        binding.tvDiffPrimary.text = getString(
            R.string.diff_result_format,
            result.years,
            result.months,
            result.days
        )

        binding.tvDiffTotalDays.text = getString(
            R.string.diff_totals_format,
            result.totalDays,
            result.totalWeeks
        )

        result.workingDays?.let {
            binding.tvWorkingDays.visibility = View.VISIBLE
            binding.tvWorkingDays.text = getString(R.string.working_days_format, it)
        } ?: run {
            binding.tvWorkingDays.visibility = View.GONE
        }
    }

    private fun performHaptic() {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
