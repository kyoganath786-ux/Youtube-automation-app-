package com.example.ui.navigation

enum class Screen(val titleEn: String, val titleTa: String) {
    SPLASH("Startup", "துவக்கம்"),
    ONBOARDING("Welcome", "வரவேற்பு"),
    SIGN_IN("Sign In", "உள்நுழைவு"),
    HOME("Studio Home", "முகப்பு"),
    CHAT("AI Creator Chat", "AI அரட்டை"),
    WIZARD("Video Wizard", "வீடியோ வழிகாட்டி"),
    EDITOR("Video Editor", "வீடியோ திருத்தி"),
    SHORTS("Shorts Studio", "ஷார்ட்ஸ் ஸ்டுடியோ"),
    THUMBNAILS("Thumbnail Studio", "முகப்புப்படம்"),
    VOICE("Voice Studio", "குரல் ஸ்டுடியோ"),
    PROJECTS("My Projects", "என் திட்டங்கள்"),
    AUTOMATION("Automation Builder", "தானியங்கி"),
    CHANNEL("YouTube Channel", "யூடியூப் சேனல்"),
    PUBLISH("Publish Video", "வீடியோ வெளியீடு"),
    ANALYTICS("Channel Analytics", "பகுப்பாய்வு"),
    SETTINGS("Settings", "அமைப்புகள்")
}
