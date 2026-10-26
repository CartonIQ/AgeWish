package com.agewish.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.agewish.calculator.ads.AdManager
import com.agewish.calculator.databinding.FragmentAgeBinding
import com.agewish.calculator.pdf.ImageSharer
import com.agewish.calculator.pdf.PdfSaver
import com.agewish.calculator.pdf.PdfSharer
import com.agewish.calculator.util.AgeCalculationResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.ResultFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AdMob Banner
        AdManager.loadBanner(binding.adView)

        setupDatePickers()
        setupButtons()
        observeViewModel()
    }

    private fun setupDatePickers() {
        // Date of Birth Picker
        binding.etDob.setOnClickListener {
            performHaptic()
            val initialSelection = viewModel.dob.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_dob)
                .setSelection(initialSelection)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setDob(date)
                binding.etDob.setText(date.format(DateUtils.DISPLAY_FORMATTER))
            }
            datePicker.show(parentFragmentManager, "DOB_PICKER")
        }

        // As Of Date Picker (Default today)
        binding.etAsOf.setText(LocalDate.now().format(DateUtils.DISPLAY_FORMATTER))
        binding.etAsOf.setOnClickListener {
            performHaptic()
            val initialSelection = viewModel.asOf.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_as_of_date)
                .setSelection(initialSelection)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setAsOf(date)
                binding.etAsOf.setText(date.format(DateUtils.DISPLAY_FORMATTER))
            }
            datePicker.show(parentFragmentManager, "AS_OF_PICKER")
        }

        // Anniversary Date Picker (Optional)
        binding.etAnniversary.setOnClickListener {
            performHaptic()
            val initialSelection = viewModel.anniversary.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.label_anniversary_date)
                .setSelection(initialSelection)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                viewModel.setAnniversary(date)
                binding.etAnniversary.setText(date.format(DateUtils.DISPLAY_FORMATTER))
                binding.btnClearAnniversary.visibility = View.VISIBLE
            }
            datePicker.show(parentFragmentManager, "ANNIV_PICKER")
        }

        binding.btnClearAnniversary.setOnClickListener {
            performHaptic()
            viewModel.setAnniversary(null)
            binding.etAnniversary.setText("")
            binding.btnClearAnniversary.visibility = View.GONE
            viewModel.calculate()
        }
    }

    private fun setupButtons() {
        // Calculate
        binding.btnCalculateAge.setOnClickListener {
            performHaptic()
            if (viewModel.dob.value == null) {
                Toast.makeText(requireContext(), R.string.toast_dob_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val success = viewModel.calculate()
            if (!success) {
                Toast.makeText(requireContext(), R.string.toast_dob_required, Toast.LENGTH_SHORT).show()
            }
        }

        // Generate PDF (with interstitial ad trigger)
        binding.btnGeneratePdf.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            AdManager.showInterstitialIfDue(requireActivity()) {
                viewModel.generatePdf(requireContext(), app.repository) { generatedFile ->
                    PdfSharer.sharePdf(requireContext(), generatedFile)
                }
            }
        }

        // Save to Downloads
        binding.btnSaveDownloads.setOnClickListener {
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

        // Share as Image
        binding.btnShareImage.setOnClickListener {
            performHaptic()
            ImageSharer.shareViewAsImage(requireContext(), binding.resultContentLayout)
        }

        // Copy to Clipboard
        binding.btnCopy.setOnClickListener {
            performHaptic()
            val currentResult = viewModel.result.value ?: return@setOnClickListener
            val text = ResultFormatter.formatAgeClipboard(currentResult)
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("AgeWish Result", text))
            Toast.makeText(requireContext(), R.string.toast_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                displayResults(result)
            } else {
                binding.cardResults.visibility = View.GONE
                binding.bannerCard.visibility = View.GONE
            }
        }
    }

    private fun displayResults(result: AgeCalculationResult) {
        binding.cardResults.visibility = View.VISIBLE

        binding.tvAgePrimary.text = getString(
            R.string.age_result_format,
            result.years,
            result.months,
            result.days
        )

        binding.tvTotals.text = getString(
            R.string.totals_format,
            ResultFormatter.formatNumber(result.totalDays),
            ResultFormatter.formatNumber(result.totalWeeks),
            ResultFormatter.formatNumber(result.totalMonths),
            ResultFormatter.formatNumber(result.totalHours),
            ResultFormatter.formatNumber(result.totalMinutes)
        )

        if (result.isBirthdayToday) {
            binding.tvNextBirthday.text = "🎂 Today is your birthday! Happy Birthday!"
        } else {
            binding.tvNextBirthday.text = getString(
                R.string.next_birthday_format,
                result.daysToNextBirthday,
                result.nextBirthdayDate.format(DateUtils.DATE_FORMATTER)
            )
        }

        result.anniversaryResult?.let { anniv ->
            binding.tvNextAnniversary.visibility = View.VISIBLE
            if (anniv.isAnniversaryToday) {
                binding.tvNextAnniversary.text = "💍 Today is your anniversary! Happy Anniversary!"
            } else {
                binding.tvNextAnniversary.text = getString(
                    R.string.next_anniversary_format,
                    anniv.daysToNextAnniversary,
                    anniv.nextAnniversaryDate.format(DateUtils.DATE_FORMATTER)
                )
            }
        } ?: run {
            binding.tvNextAnniversary.visibility = View.GONE
        }

        binding.tvZodiac.text = getString(
            R.string.zodiac_format,
            result.westernZodiac,
            result.chineseZodiac
        )

        binding.tvBornDay.text = getString(
            R.string.born_on_format,
            result.dayOfWeekBorn
        )

        // Auto-wish Banner with fade-in animation
        when {
            result.isBirthdayToday -> {
                showBanner(getString(R.string.banner_bday_today), R.color.banner_bday_bg, R.color.banner_bday_text)
            }
            result.isBirthdayWithin7Days -> {
                showBanner(
                    getString(R.string.banner_bday_soon, result.daysToNextBirthday),
                    R.color.banner_bday_bg,
                    R.color.banner_bday_text
                )
            }
            result.anniversaryResult?.isAnniversaryToday == true -> {
                showBanner(
                    getString(R.string.banner_anniversary_today),
                    R.color.banner_anniv_bg,
                    R.color.banner_anniv_text
                )
            }
            else -> {
                binding.bannerCard.visibility = View.GONE
            }
        }
    }

    private fun showBanner(text: String, bgRes: Int, textRes: Int) {
        binding.tvBannerTitle.text = text
        binding.tvBannerTitle.setTextColor(resources.getColor(textRes, null))
        binding.bannerCard.setCardBackgroundColor(resources.getColor(bgRes, null))
        binding.bannerCard.strokeColor = resources.getColor(textRes, null)
        binding.bannerCard.visibility = View.VISIBLE

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 450
            fillAfter = true
        }
        binding.bannerCard.startAnimation(fadeIn)
    }

    private fun performHaptic() {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
