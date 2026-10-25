package com.example.ui.util

import com.example.data.model.AppLanguage

object Strings {
    fun get(key: String, lang: AppLanguage): String {
        val isTa = lang == AppLanguage.TAMIL
        return when (key) {
            "app_name" -> "YouTube Automation AI"
            "app_tagline" -> if (isTa) "முழுமையான இருமொழி AI வீடியோ & ஆட்டோமேஷன் ஸ்டுடியோ" else "Complete Bilingual AI Video & Automation Studio"
            "language_select" -> if (isTa) "மொழியைத் தேர்ந்தெடுக்கவும்" else "Select Language"
            "english" -> "English"
            "tamil" -> "தமிழ்"

            // Buttons
            "create_video" -> if (isTa) "வீடியோ உருவாக்கு" else "Create Video"
            "create_shorts" -> if (isTa) "ஷார்ட்ஸ் உருவாக்கு" else "Create Shorts"
            "generate_ai" -> if (isTa) "AI மூலம் உருவாக்கு" else "Generate with AI"
            "render_video" -> if (isTa) "ரெண்டர் செய்" else "Render Video"
            "publish_yt" -> if (isTa) "யூடியூபில் வெளியிடு" else "Publish to YouTube"
            "save" -> if (isTa) "சேமி" else "Save"
            "cancel" -> if (isTa) "ரத்து செய்" else "Cancel"
            "edit" -> if (isTa) "திருத்து" else "Edit"
            "delete" -> if (isTa) "நீக்கு" else "Delete"
            "duplicate" -> if (isTa) "நகலெடு" else "Duplicate"

            // Dashboard
            "quick_studios" -> if (isTa) "விரைவு ஸ்டுடியோ கருவிகள்" else "Quick Studio Tools"
            "recent_projects" -> if (isTa) "சமீபத்திய திட்டங்கள்" else "Recent Projects"
            "active_automations" -> if (isTa) "செயலில் உள்ள தானியங்கி பணிகள்" else "Active Automations"
            "channel_health" -> if (isTa) "சேனல் நிலை & வளர்ச்சி" else "Channel Status & Growth"

            // Wizard
            "topic_label" -> if (isTa) "வீடியோ தலைப்பு / குறிக்கோள்" else "Video Topic or Niche"
            "topic_hint" -> if (isTa) "எ.கா: 2026 இல் 5 முக்கிய விண்வெளி உண்மைகள்..." else "e.g., 5 Mind-Blowing AI Tools in 2026..."
            "format_label" -> if (isTa) "வடிவம் (Format)" else "Video Format"
            "duration_label" -> if (isTa) "கால அளவு" else "Target Duration"
            "voice_label" -> if (isTa) "குரல் மாதிரி" else "Narrator Voice"

            // Voice Studio
            "voice_studio_desc" -> if (isTa) "தமிழ் மற்றும் ஆங்கில நியூரல் குரல் தொகுப்பு" else "Neural voice synthesis in Tamil and English"
            "test_voice" -> if (isTa) "குரலை சோதிக்கவும்" else "Test Narration"
            "speed_slider" -> if (isTa) "பேச்சு வேகம்" else "Speaking Speed"
            "pitch_slider" -> if (isTa) "குரல் சுருதி (Pitch)" else "Pitch Level"

            // Automation
            "auto_desc" -> if (isTa) "தினசரி வீடியோக்களை தானாகவே உருவாக்கி பதிவேற்றும் அட்டவணை" else "Recurring automated pipelines with scheduled YouTube publishing"
            "schedule_label" -> if (isTa) "அட்டவணை நேரம்" else "Schedule Frequency"
            "status_label" -> if (isTa) "நிலை" else "Status"

            // YouTube Connection
            "yt_connected" -> if (isTa) "இணைக்கப்பட்டுள்ளது" else "Connected"
            "yt_subscribers" -> if (isTa) "சப்ஸ்கிரைபர்கள்" else "Subscribers"
            "yt_views" -> if (isTa) "மொத்த பார்வைகள்" else "Total Views"
            "yt_videos" -> if (isTa) "பதிவேற்றிய வீடியோக்கள்" else "Total Videos"

            // Publishing
            "publish_title" -> if (isTa) "வீடியோ தலைப்பு" else "Video Title"
            "publish_desc" -> if (isTa) "விளக்கம் (Description)" else "Description"
            "publish_tags" -> if (isTa) "குறிச்சொற்கள் (Tags)" else "Tags"
            "privacy_label" -> if (isTa) "தனியுரிமை நிலை" else "Privacy Setting"
            "schedule_publish" -> if (isTa) "அட்டவணை வெளியீடு" else "Schedule Publish Time"

            // Settings
            "app_settings" -> if (isTa) "பயன்பாட்டு அமைப்புகள்" else "Application Settings"
            "theme_mode" -> if (isTa) "தீம் நிறம் (Dark/Light)" else "Theme Mode"
            "dark_theme" -> if (isTa) "இருண்ட தீம் (Dark)" else "Dark Theme"
            "clear_cache" -> if (isTa) "தற்காலிக சேமிப்பை அழி (Clear Cache)" else "Clear Temporary Cache"
            "sign_out" -> if (isTa) "வெளியேறு (Sign Out)" else "Sign Out"

            else -> key
        }
    }
}
