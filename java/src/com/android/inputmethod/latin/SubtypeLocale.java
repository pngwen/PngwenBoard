/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.inputmethod.latin;

import static com.android.inputmethod.latin.Constants.Subtype.ExtraValue.KEYBOARD_LAYOUT_SET;

import android.content.Context;
import android.content.res.Resources;
import android.view.inputmethod.InputMethodSubtype;

import java.util.HashMap;
import java.util.Locale;

public class SubtypeLocale {
    private static final String TAG = SubtypeLocale.class.getSimpleName();
    // This class must be located in the same package as LatinIME.java.
    private static final String RESOURCE_PACKAGE_NAME =
            DictionaryFactory.class.getPackage().getName();

    // Special language code to represent "no language".
    public static final String NO_LANGUAGE = "zz";

    public static final String QWERTY = "qwerty";

    public static final int UNKNOWN_KEYBOARD_LAYOUT = R.string.subtype_generic;

    private static String[] sPredefinedKeyboardLayoutSet;
    // Keyboard layout to its display name map.
    private static final HashMap<String, String> sKeyboardKayoutToDisplayNameMap =
            new HashMap<String, String>();
    // Keyboard layout to subtype name resource id map.
    private static final HashMap<String, Integer> sKeyboardLayoutToNameIdsMap =
            new HashMap<String, Integer>();
    private static final String SUBTYPE_NAME_RESOURCE_GENERIC_PREFIX =
            "string/subtype_generic_";
    private static final String SUBTYPE_NAME_RESOURCE_NO_LANGUAGE_PREFIX =
            "string/subtype_no_language_";
    // Exceptional locales to display name map.
    private static final HashMap<String, String> sExceptionalDisplayNamesMap =
            new HashMap<String, String>();

    private SubtypeLocale() {
        // Intentional empty constructor for utility class.
    }

    public static void init(Context context) {
        final Resources res = context.getResources();

        final String[] predefinedLayoutSet = res.getStringArray(R.array.predefined_layouts);
        sPredefinedKeyboardLayoutSet = predefinedLayoutSet;
        final String[] layoutDisplayNames = res.getStringArray(
                R.array.predefined_layout_display_names);
        for (int i = 0; i < predefinedLayoutSet.length; i++) {
            final String layoutName = predefinedLayoutSet[i];
            sKeyboardKayoutToDisplayNameMap.put(layoutName, layoutDisplayNames[i]);
            final String resourceName = SUBTYPE_NAME_RESOURCE_GENERIC_PREFIX + layoutName;
            final int resId = res.getIdentifier(resourceName, null, RESOURCE_PACKAGE_NAME);
            sKeyboardLayoutToNameIdsMap.put(layoutName, resId);
            // Register subtype name resource id of "No language" with key "zz_<layout>"
            final String noLanguageResName = SUBTYPE_NAME_RESOURCE_NO_LANGUAGE_PREFIX + layoutName;
            final int noLanguageResId = res.getIdentifier(
                    noLanguageResName, null, RESOURCE_PACKAGE_NAME);
            final String key = getNoLanguageLayoutKey(layoutName);
            sKeyboardLayoutToNameIdsMap.put(key, noLanguageResId);
        }

        final String[] exceptionalLocales = res.getStringArray(
                R.array.subtype_locale_exception_keys);
        final String[] exceptionalDisplayNames = res.getStringArray(
                R.array.subtype_locale_exception_values);
        for (int i = 0; i < exceptionalLocales.length; i++) {
            sExceptionalDisplayNamesMap.put(exceptionalLocales[i], exceptionalDisplayNames[i]);
        }
    }

    public static String[] getPredefinedKeyboardLayoutSet() {
        return sPredefinedKeyboardLayoutSet;
    }

    private static final String getNoLanguageLayoutKey(String keyboardLayoutName) {
        return NO_LANGUAGE + "_" + keyboardLayoutName;
    }

    public static int getSubtypeNameId(String localeString, String keyboardLayoutName) {
        final String key = localeString.equals(NO_LANGUAGE)
                ? getNoLanguageLayoutKey(keyboardLayoutName)
                : keyboardLayoutName;
        final Integer nameId = sKeyboardLayoutToNameIdsMap.get(key);
        return nameId == null ? UNKNOWN_KEYBOARD_LAYOUT : nameId;
    }

    public static String getSubtypeLocaleDisplayName(String localeString) {
        final String exceptionalValue = sExceptionalDisplayNamesMap.get(localeString);
        if (exceptionalValue != null) {
            return exceptionalValue;
        }
        final Locale locale = LocaleUtils.constructLocaleFromString(localeString);
        return StringUtils.toTitleCase(locale.getDisplayName(locale), locale);
    }

    // InputMethodSubtype's display name in its locale.
    //        isAdditionalSubtype (T=true, F=false)
    // locale layout |  display name
    // ------ ------ - ----------------------
    //  en_US qwerty F  English (US)            exception
    //  en_GB qwerty F  English (UK)            exception
    //  fr    azerty F  Français
    //  fr_CA qwerty F  Français (Canada)
    //  de    qwertz F  Deutsch
    //  zz    qwerty F  No language (QWERTY)
    //  fr    qwertz T  Français (QWERTZ)
    //  de    qwerty T  Deutsch (QWERTY)
    //  en_US azerty T  English (US) (AZERTY)
    //  zz    azerty T  No language (AZERTY)

    public static String getSubtypeDisplayName(InputMethodSubtype subtype, Resources res) {
        final String language = getSubtypeLocaleDisplayName(subtype.getLocale());
        return res.getString(subtype.getNameResId(), language);
    }

    public static boolean isNoLanguage(InputMethodSubtype subtype) {
        final String localeString = subtype.getLocale();
        return localeString.equals(NO_LANGUAGE);
    }

    public static Locale getSubtypeLocale(InputMethodSubtype subtype) {
        final String localeString = subtype.getLocale();
        return LocaleUtils.constructLocaleFromString(localeString);
    }

    public static String getKeyboardLayoutSetDisplayName(InputMethodSubtype subtype) {
        final String layoutName = getKeyboardLayoutSetName(subtype);
        return sKeyboardKayoutToDisplayNameMap.get(layoutName);
    }

    public static String getKeyboardLayoutSetName(InputMethodSubtype subtype) {
        final String keyboardLayoutSet = subtype.getExtraValueOf(KEYBOARD_LAYOUT_SET);
        // TODO: Remove this null check when InputMethodManager.getCurrentInputMethodSubtype is
        // fixed.
        if (keyboardLayoutSet == null) {
            android.util.Log.w(TAG, "KeyboardLayoutSet not found, use QWERTY: " +
                    "locale=" + subtype.getLocale() + " extraValue=" + subtype.getExtraValue());
            return QWERTY;
        }
        return keyboardLayoutSet;
    }
}
