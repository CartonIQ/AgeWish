package com.agewish.calculator

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.agewish.calculator.ads.AdManager
import com.agewish.calculator.databinding.FragmentWishBinding
import com.agewish.calculator.pdf.ImageSharer
import com.agewish.calculator.pdf.PdfSaver
import com.agewish.calculator.pdf.PdfSharer
import com.agewish.calculator.util.ThemePreset
import com.agewish.calculator.util.WishTemplates

class WishFragment : Fragment() {

    private var _binding: FragmentWishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WishViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AdManager.loadBanner(binding.adView)
        setupOccasionDropdown()
        setupInputs()
        setupThemeChips()
        setupButtons()
        observeViewModel()
    }

    private fun setupOccasionDropdown() {
        val occasions = WishTemplates.OCCASIONS.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, occasions)
        binding.actvOccasion.setAdapter(adapter)

        binding.actvOccasion.setOnItemClickListener { _, _, position, _ ->
            performHaptic()
            val selected = occasions[position]
            viewModel.setOccasion(selected)
            updateChipSelectionForOccasion(selected)
        }
    }

    private fun setupInputs() {
        binding.etRecipientName.doAfterTextChanged { text ->
            viewModel.setRecipientName(text?.toString().orEmpty())
        }

        binding.etSenderName.doAfterTextChanged { text ->
            viewModel.setSenderName(text?.toString().orEmpty())
        }

        binding.etMessage.doAfterTextChanged { text ->
            viewModel.setMessage(text?.toString().orEmpty())
        }
    }

    private fun setupThemeChips() {
        binding.chipGroupThemes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            performHaptic()
            val theme = when (checkedIds.first()) {
                R.id.chipThemeCoral -> WishTemplates.THEME_PRESETS[0]
                R.id.chipThemePurple -> WishTemplates.THEME_PRESETS[1]
                R.id.chipThemeGold -> WishTemplates.THEME_PRESETS[2]
                R.id.chipThemeNavy -> WishTemplates.THEME_PRESETS[3]
                R.id.chipThemeTeal -> WishTemplates.THEME_PRESETS[4]
                R.id.chipThemeFestive -> WishTemplates.THEME_PRESETS[5]
                else -> WishTemplates.THEME_PRESETS[0]
            }
            viewModel.setTheme(theme)
        }
    }

    private fun updateChipSelectionForOccasion(occasion: String) {
        val config = WishTemplates.getOccasionByName(occasion)
        val chipId = when (config.defaultThemeIndex) {
            0 -> R.id.chipThemeCoral
            1 -> R.id.chipThemePurple
            2 -> R.id.chipThemeGold
            3 -> R.id.chipThemeNavy
            4 -> R.id.chipThemeTeal
            5 -> R.id.chipThemeFestive
            else -> R.id.chipThemeCoral
        }
        binding.chipGroupThemes.check(chipId)
    }

    private fun setupButtons() {
        // Generate Wish Card
        binding.btnGenerateCard.setOnClickListener {
            performHaptic()
            val recipient = binding.etRecipientName.text?.toString()?.trim().orEmpty()
            if (recipient.isEmpty()) {
                binding.tilRecipientName.error = getString(R.string.toast_enter_recipient)
                Toast.makeText(requireContext(), R.string.toast_enter_recipient, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.tilRecipientName.error = null

            val app = requireActivity().application as AgeWishApp
            AdManager.showInterstitialIfDue(requireActivity()) {
                viewModel.generateCardPdf(requireContext(), app.repository) { generatedFile ->
                    PdfSharer.sharePdf(requireContext(), generatedFile)
                    binding.layoutCardActions.visibility = View.VISIBLE
                }
            }
        }

        // Share Wish PDF
        binding.btnShareWishPdf.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            val currentFile = viewModel.lastGeneratedPdf.value
            if (currentFile != null && currentFile.exists()) {
                PdfSharer.sharePdf(requireContext(), currentFile)
            } else {
                viewModel.generateCardPdf(requireContext(), app.repository) { file ->
                    PdfSharer.sharePdf(requireContext(), file)
                }
            }
        }

        // Save Wish to Downloads
        binding.btnSaveWishDownloads.setOnClickListener {
            performHaptic()
            val app = requireActivity().application as AgeWishApp
            val currentFile = viewModel.lastGeneratedPdf.value
            if (currentFile != null && currentFile.exists()) {
                PdfSaver.saveToDownloads(requireContext(), currentFile)
            } else {
                viewModel.generateCardPdf(requireContext(), app.repository) { file ->
                    PdfSaver.saveToDownloads(requireContext(), file)
                }
            }
        }

        // Share Wish as Image
        binding.btnShareWishImage.setOnClickListener {
            performHaptic()
            ImageSharer.shareViewAsImage(requireContext(), binding.wishCardView)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedOccasion.observe(viewLifecycleOwner) { occ ->
            val config = WishTemplates.getOccasionByName(occ)
            binding.tvCardEmoji.text = config.emoji
            updateLivePreview()
        }

        viewModel.recipientName.observe(viewLifecycleOwner) {
            updateLivePreview()
        }

        viewModel.senderName.observe(viewLifecycleOwner) {
            updateLivePreview()
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (binding.etMessage.text.toString() != msg) {
                binding.etMessage.setText(msg)
            }
            binding.tvCardMessage.text = msg
        }

        viewModel.selectedTheme.observe(viewLifecycleOwner) { theme ->
            applyThemeToPreview(theme)
        }

        viewModel.lastGeneratedPdf.observe(viewLifecycleOwner) { file ->
            binding.layoutCardActions.visibility = if (file != null && file.exists()) View.VISIBLE else View.GONE
        }
    }

    private fun updateLivePreview() {
        val occ = viewModel.selectedOccasion.value ?: "Birthday"
        val recipient = viewModel.recipientName.value?.ifBlank { "Friend" } ?: "Friend"
        val sender = viewModel.senderName.value

        binding.tvCardTitle.text = if (occ.equals("Birthday", ignoreCase = true)) {
            "Happy Birthday, $recipient!"
        } else if (occ.equals("Anniversary", ignoreCase = true)) {
            "Happy Anniversary, $recipient!"
        } else if (occ.equals("New Year", ignoreCase = true)) {
            "Happy New Year, $recipient!"
        } else {
            "For Dear $recipient"
        }

        if (sender.isNullOrBlank()) {
            binding.tvCardSender.visibility = View.GONE
        } else {
            binding.tvCardSender.visibility = View.VISIBLE
            binding.tvCardSender.text = "— With love, $sender"
        }
    }

    private fun applyThemeToPreview(theme: ThemePreset) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(theme.startColor, theme.endColor)
        ).apply {
            cornerRadius = 40f
        }
        binding.wishCardView.background = gradient
        binding.tvCardTitle.setTextColor(theme.textColor)
        binding.tvCardMessage.setTextColor(theme.textColor)
        binding.tvCardSender.setTextColor(theme.accentColor)
    }

    private fun performHaptic() {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
