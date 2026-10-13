/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blackberry.settings;

import android.os.Bundle;
import android.provider.Settings;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

public class DeviceSettingsActivity extends CollapsingToolbarBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                            new DeviceSettingsFragment())
                    .commit();
        }
    }

    public static class DeviceSettingsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        private static final String KEY_SHOW_IME = "show_ime_with_hard_keyboard";
        private static final String KEY_IME_SWITCHER = "ime_switcher_shortcut";
        private static final String KEY_KEYBOARD_BRIGHTNESS = "keyboard_brightness";
        private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
        private static final String KEY_BUTTON_TIMEOUT = "button_timeout";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.device_settings, rootKey);

            // Show on-screen keyboard with hardware keyboard
            SwitchPreference showIme = findPreference(KEY_SHOW_IME);
            if (showIme != null) {
                int current = Settings.Secure.getInt(
                        getContext().getContentResolver(),
                        Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD, 0);
                showIme.setChecked(current == 1);
                showIme.setOnPreferenceChangeListener(this);
            }

            // IME switcher shortcut
            ListPreference imeSwitcher = findPreference(KEY_IME_SWITCHER);
            if (imeSwitcher != null) {
                int currentValue = Settings.Secure.getInt(
                        getContext().getContentResolver(),
                        "ime_switcher_shortcut", 0);
                imeSwitcher.setValue(String.valueOf(currentValue));
                imeSwitcher.setSummary(imeSwitcher.getEntry());
                imeSwitcher.setOnPreferenceChangeListener(this);
            }

            // Keyboard brightness (0.0 - 1.0 stored, 0-100 displayed)
            SeekBarPreference kbdBright = findPreference(KEY_KEYBOARD_BRIGHTNESS);
            if (kbdBright != null) {
                float current = Settings.Secure.getFloat(
                        getContext().getContentResolver(),
                        "keyboard_brightness", -1.0f);
                kbdBright.setValue(current >= 0 ? Math.round(current * 100) : 100);
                kbdBright.setOnPreferenceChangeListener(this);
            }

            // Button brightness (0.0 - 1.0 stored, 0-100 displayed)
            SeekBarPreference btnBright = findPreference(KEY_BUTTON_BRIGHTNESS);
            if (btnBright != null) {
                float current = Settings.Secure.getFloat(
                        getContext().getContentResolver(),
                        "button_brightness", -1.0f);
                btnBright.setValue(current >= 0 ? Math.round(current * 100) : 100);
                btnBright.setOnPreferenceChangeListener(this);
            }

            // Button timeout
            ListPreference btnTimeout = findPreference(KEY_BUTTON_TIMEOUT);
            if (btnTimeout != null) {
                int current = Settings.Secure.getInt(
                        getContext().getContentResolver(),
                        "button_backlight_timeout", 0);
                btnTimeout.setValue(String.valueOf(current));
                btnTimeout.setSummary(btnTimeout.getEntry());
                btnTimeout.setOnPreferenceChangeListener(this);
            }

            // Make sure stale "only when pressed" overrides don't leak through
            // after we removed the toggle from the UI.
            Settings.Secure.putInt(
                    getContext().getContentResolver(),
                    "button_backlight_only_when_pressed", 0);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();

            switch (key) {
                case KEY_SHOW_IME: {
                    boolean checked = (boolean) newValue;
                    Settings.Secure.putInt(
                            getContext().getContentResolver(),
                            Settings.Secure.SHOW_IME_WITH_HARD_KEYBOARD,
                            checked ? 1 : 0);
                    return true;
                }
                case KEY_IME_SWITCHER: {
                    int value = Integer.parseInt((String) newValue);
                    Settings.Secure.putInt(
                            getContext().getContentResolver(),
                            "ime_switcher_shortcut", value);
                    ListPreference lp = (ListPreference) preference;
                    int idx = lp.findIndexOfValue((String) newValue);
                    lp.setSummary(lp.getEntries()[idx]);
                    return true;
                }
                case KEY_KEYBOARD_BRIGHTNESS: {
                    int percent = (int) newValue;
                    Settings.Secure.putFloat(
                            getContext().getContentResolver(),
                            "keyboard_brightness", percent / 100.0f);
                    return true;
                }
                case KEY_BUTTON_BRIGHTNESS: {
                    int percent = (int) newValue;
                    Settings.Secure.putFloat(
                            getContext().getContentResolver(),
                            "button_brightness", percent / 100.0f);
                    return true;
                }
                case KEY_BUTTON_TIMEOUT: {
                    int value = Integer.parseInt((String) newValue);
                    Settings.Secure.putInt(
                            getContext().getContentResolver(),
                            "button_backlight_timeout", value);
                    ListPreference lp = (ListPreference) preference;
                    int idx = lp.findIndexOfValue((String) newValue);
                    lp.setSummary(lp.getEntries()[idx]);
                    return true;
                }
            }
            return false;
        }
    }
}
