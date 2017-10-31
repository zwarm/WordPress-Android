package org.wordpress.android.util;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Design guidelines for Calypso-styled Site Settings (and likely other screens)
 */

public class WPPrefUtils {

    /**
     * Length of a {@link String} (representing a language code) when there is no region included.
     * For example: "en" contains no region, "en_US" contains a region (US)
     *
     * Used to parse a language code {@link String} when creating a {@link Locale}.
     */
    private static final int NO_REGION_LANG_CODE_LEN = 2;

    /**
     * Index of a language code {@link String} where the region code begins. The language code
     * format is cc_rr, where cc is the country code (e.g. en, es, az) and rr is the region code
     * (e.g. us, au, gb).
     */
    private static final int REGION_SUBSTRING_INDEX = 3;

    /**
     * Gets a preference and sets the {@link android.preference.Preference.OnPreferenceChangeListener}.
     */
    public static Preference getPrefAndSetClickListener(PreferenceFragment prefFrag,
                                                         int id,
                                                         Preference.OnPreferenceClickListener listener) {
        Preference pref = prefFrag.findPreference(prefFrag.getString(id));
        if (pref != null) pref.setOnPreferenceClickListener(listener);
        return pref;
    }

    public static String getDayOfMonthSuffix(int dayOfMonth) {
        switch (dayOfMonth % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String formatDateWithPhpCodes(final String wpFormat, @Nullable Calendar date) {
        if (TextUtils.isEmpty(wpFormat)) {
            return null;
        }

        if (date == null) {
            date = Calendar.getInstance();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wpFormat.length(); ++i) {
            final char character = wpFormat.charAt(i);
            switch (character) {
                case 'd':
                    // day of month with leading zeroes
                    SimpleDateFormat lzDay = new SimpleDateFormat("dd", Locale.getDefault());
                    builder.append(lzDay.format(date.getTime()));
                    break;
                case 'j':
                    // day of month without leading zeroes
                    SimpleDateFormat nlzDay = new SimpleDateFormat("d", Locale.getDefault());
                    builder.append(nlzDay.format(date.getTime()));
                    break;
                case 'S':
                    // English suffix for day of month (ie "st" for 1st, "nd" for 2nd, etc...)
                    SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
                    int day = Integer.valueOf(dayFormat.format(date.getTime()));
                    builder.append(getDayOfMonthSuffix(day));
                    break;
                case 'l':
                    // full name of day (Saturday, Sunday, etc...)
                    SimpleDateFormat fullDay = new SimpleDateFormat("EEEE", Locale.getDefault());
                    builder.append(fullDay.format(date.getTime()));
                    break;
                case 'D':
                    // three letter name of day (Sat, Sun, etc...)
                    SimpleDateFormat shortDay = new SimpleDateFormat("EEE", Locale.getDefault());
                    builder.append(shortDay.format(date.getTime()));
                    break;
                case 'm':
                    // numeric month with leading zeroes
                    SimpleDateFormat lzMonth = new SimpleDateFormat("MM", Locale.getDefault());
                    builder.append(lzMonth.format(date.getTime()));
                    break;
                case 'n':
                    // numeric month without leading zeroes
                    SimpleDateFormat nlzMonth = new SimpleDateFormat("M", Locale.getDefault());
                    builder.append(nlzMonth.format(date.getTime()));
                    break;
                case 'F':
                    // full text name of month (February, November, etc...)
                    SimpleDateFormat fullMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
                    builder.append(fullMonth.format(date.getTime()));
                    break;
                case 'M':
                    // three letter name of month
                    SimpleDateFormat shortMonth = new SimpleDateFormat("MMM", Locale.getDefault());
                    builder.append(shortMonth.format(date.getTime()));
                    break;
                case 'Y':
                    // four digit year
                    SimpleDateFormat longYear = new SimpleDateFormat("yyyy", Locale.getDefault());
                    builder.append(longYear.format(date.getTime()));
                    break;
                case 'y':
                    // two digit year
                    SimpleDateFormat shortYear = new SimpleDateFormat("yy", Locale.getDefault());
                    builder.append(shortYear.format(date.getTime()));
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }

        return builder.toString();
    }

    /**
     * @param wpFormat
     *  WP sites recognize PHP time format options specified https://codex.wordpress.org/Formatting_Date_and_Time
     * @param time
     *  timestamp to format, if null the current time is used via {@link Calendar#getInstance()}
     * @return
     *  the given time formatted with the given format, null if format is malformed
     */
    public static String formatTimeWithPhpCodes(final String wpFormat, @Nullable Calendar time) {
        if (TextUtils.isEmpty(wpFormat)) {
            return null;
        }

        if (time == null) {
            time = Calendar.getInstance();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wpFormat.length(); ++i) {
            final char character = wpFormat.charAt(i);
            switch (character) {
                case 'a':
                    // lower-case am/pm
                    SimpleDateFormat lAmPm = new SimpleDateFormat("a", Locale.getDefault());
                    builder.append(lAmPm.format(time.getTime()).toLowerCase());
                    break;
                case 'A':
                    // upper-case AM/PM
                    SimpleDateFormat uAmPm = new SimpleDateFormat("a", Locale.getDefault());
                    builder.append(uAmPm.format(time.getTime()));
                    break;
                case 'g':
                    // no leading zeroes 12 hour format
                    SimpleDateFormat nlzTwelve = new SimpleDateFormat("h", Locale.getDefault());
                    builder.append(nlzTwelve.format(time.getTime()));
                    break;
                case 'h':
                    // leading zeroes 12 hour format
                    SimpleDateFormat lzTwelve = new SimpleDateFormat("hh", Locale.getDefault());
                    builder.append(lzTwelve.format(time.getTime()));
                    break;
                case 'G':
                    // no leading zeroes 24 hour format
                    SimpleDateFormat nlzTwoFour = new SimpleDateFormat("H", Locale.getDefault());
                    builder.append(nlzTwoFour.format(time.getTime()));
                    break;
                case 'H':
                    // leading zeroes 24 hour format
                    SimpleDateFormat lzTwoFour = new SimpleDateFormat("HH", Locale.getDefault());
                    builder.append(lzTwoFour.format(time.getTime()));
                    break;
                case 'i':
                    // minutes with leading zeroes
                    SimpleDateFormat lzMinutes = new SimpleDateFormat("mm", Locale.getDefault());
                    builder.append(lzMinutes.format(time.getTime()));
                    break;
                case 's':
                    // seconds with leading zeroes
                    SimpleDateFormat lzSeconds = new SimpleDateFormat("ss", Locale.getDefault());
                    builder.append(lzSeconds.format(time.getTime()));
                    break;
                case 'v':
                    // milliseconds with leading zeroes
                    SimpleDateFormat millis = new SimpleDateFormat("SSS", Locale.getDefault());
                    builder.append(millis.format(time.getTime()));
                    break;
                case 'T':
                    // timezone abbreviation
                    builder.append(time.getTimeZone().getID());
                    break;
                default:
                    builder.append(character);
                    break;
            }
        }

        return builder.toString();
    }

    /**
     * * Gets a preference and sets the {@link android.preference.Preference.OnPreferenceChangeListener}.
     */
    public static Preference getPrefAndSetChangeListener(PreferenceFragment prefFrag,
                                                         int id,
                                                         Preference.OnPreferenceChangeListener listener) {
        Preference pref = prefFrag.findPreference(prefFrag.getString(id));
        if (pref != null) pref.setOnPreferenceChangeListener(listener);
        return pref;
    }

    /**
     * Removes a {@link Preference} from the {@link PreferenceCategory} with the given key.
     */
    public static void removePreference(PreferenceFragment prefFrag, int parentKey, int prefKey) {
        String parentName = prefFrag.getString(parentKey);
        String prefName = prefFrag.getString(prefKey);
        PreferenceGroup parent = (PreferenceGroup) prefFrag.findPreference(parentName);
        Preference child = prefFrag.findPreference(prefName);

        if (parent != null && child != null) {
            parent.removePreference(child);
        }
    }

    /**
     * Styles a {@link TextView} to display a large title against a dark background.
     */
    public static void layoutAsLightTitle(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_extra_large);
        setTextViewAttributes(view, size, R.color.white);
    }

    /**
     * Styles a {@link TextView} to display a large title against a light background.
     */
    public static void layoutAsDarkTitle(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_extra_large);
        setTextViewAttributes(view, size, R.color.grey_dark);
    }

    /**
     * Styles a {@link TextView} to display medium sized text as a header with sub-elements.
     */
    public static void layoutAsSubhead(TextView view) {
        int color = view.isEnabled() ? R.color.grey_dark : R.color.grey_lighten_10;
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_large);
        setTextViewAttributes(view, size, color);
    }

    /**
     * Styles a {@link TextView} to display smaller text.
     */
    public static void layoutAsBody1(TextView view) {
        int color = view.isEnabled() ? R.color.grey_darken_10 : R.color.grey_lighten_10;
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_medium);
        setTextViewAttributes(view, size, color);
    }

    /**
     * Styles a {@link TextView} to display smaller text with a dark grey color.
     */
    public static void layoutAsBody2(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_medium);
        setTextViewAttributes(view, size, R.color.grey_darken_10);
    }

    /**
     * Styles a {@link TextView} to display very small helper text.
     */
    public static void layoutAsCaption(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_small);
        setTextViewAttributes(view, size, R.color.grey_darken_10);
    }

    /**
     * Styles a {@link TextView} to display text in a button.
     */
    public static void layoutAsFlatButton(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_medium);
        setTextViewAttributes(view, size, R.color.blue_medium);
    }

    /**
     * Styles a {@link TextView} to display text in a button.
     */
    public static void layoutAsRaisedButton(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_medium);
        setTextViewAttributes(view, size, R.color.white);
    }

    /**
     * Styles a {@link TextView} to display text in an editable text field.
     */
    public static void layoutAsInput(EditText view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_large);
        setTextViewAttributes(view, size, R.color.grey_dark);
        view.setHintTextColor(view.getResources().getColor(R.color.grey_lighten_10));
        view.setTextColor(view.getResources().getColor(R.color.grey_dark));
        view.setSingleLine(true);
    }

    /**
     * Styles a {@link TextView} to display selected numbers in a {@link android.widget.NumberPicker}.
     */
    public static void layoutAsNumberPickerSelected(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_triple_extra_large);
        setTextViewAttributes(view, size, R.color.blue_medium);
    }

    /**
     * Styles a {@link TextView} to display non-selected numbers in a {@link android.widget.NumberPicker}.
     */
    public static void layoutAsNumberPickerPeek(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_large);
        setTextViewAttributes(view, size, R.color.grey_dark);
    }

    /**
     * Styles a {@link TextView} to display text in a dialog message.
     */
    public static void layoutAsDialogMessage(TextView view) {
        int size = view.getResources().getDimensionPixelSize(R.dimen.text_sz_small);
        setTextViewAttributes(view, size, R.color.grey_darken_10);
    }

    public static void setTextViewAttributes(TextView textView, int size, int colorRes) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setTextColor(textView.getResources().getColor(colorRes));
    }

    /**
     * Gets a locale for the given language code.
     */
    public static Locale languageLocale(String languageCode) {
        if (TextUtils.isEmpty(languageCode)) return LanguageUtils.getCurrentDeviceLanguage(WordPress.getContext());

        if (languageCode.length() > NO_REGION_LANG_CODE_LEN) {
            return new Locale(languageCode.substring(0, NO_REGION_LANG_CODE_LEN),
                    languageCode.substring(REGION_SUBSTRING_INDEX));
        }

        return new Locale(languageCode);
    }

    /**
     * Creates a map from language codes to WordPress language IDs.
     */
    public static Map<String, String> generateLanguageMap(Activity activity) {
        String[] languageIds = activity.getResources().getStringArray(R.array.lang_ids);
        String[] languageCodes = activity.getResources().getStringArray(R.array.language_codes);

        Map<String, String> languageMap = new HashMap<>();
        for (int i = 0; i < languageIds.length && i < languageCodes.length; ++i) {
            languageMap.put(languageCodes[i], languageIds[i]);
        }

        return languageMap;
    }

    /**
     * Generates display strings for given language codes. Used as entries in language preference.
     */
    @Nullable
    public static Pair<String[], String[]> createSortedLanguageDisplayStrings(CharSequence[] languageCodes,
                                                                              Locale locale) {
        if (languageCodes == null || languageCodes.length < 1) return null;

        ArrayList<String> entryStrings = new ArrayList<>(languageCodes.length);
        for (int i = 0; i < languageCodes.length; ++i) {
            // "__" is used to sort the language code with the display string so both arrays are sorted at the same time
            entryStrings.add(i, StringUtils.capitalize(
                    getLanguageString(languageCodes[i].toString(), locale)) + "__" + languageCodes[i]);
        }

        Collections.sort(entryStrings, Collator.getInstance(locale));

        String[] sortedEntries = new String[languageCodes.length];
        String[] sortedValues = new String[languageCodes.length];

        for (int i = 0; i < entryStrings.size(); ++i) {
            // now, we can split the sorted array to extract the display string and the language code
            String[] split = entryStrings.get(i).split("__");
            sortedEntries[i] = split[0];
            sortedValues[i] = split[1];
        }

        return new Pair<>(sortedEntries, sortedValues);
    }

    /**
     * Generates detail display strings in the currently selected locale. Used as detail text
     * in language preference dialog.
     */
    @Nullable
    public static String[] createLanguageDetailDisplayStrings(CharSequence[] languageCodes) {
        if (languageCodes == null || languageCodes.length < 1) return null;

        String[] detailStrings = new String[languageCodes.length];
        for (int i = 0; i < languageCodes.length; ++i) {
            detailStrings[i] = StringUtils.capitalize(getLanguageString(
                    languageCodes[i].toString(), WPPrefUtils.languageLocale(languageCodes[i].toString())));
        }

        return detailStrings;
    }

    /**
     * Return a non-null display string for a given language code.
     */
    public static String getLanguageString(String languageCode, Locale displayLocale) {
        if (languageCode == null || languageCode.length() < 2 || languageCode.length() > 6) {
            return "";
        }

        Locale languageLocale = WPPrefUtils.languageLocale(languageCode);
        String displayLanguage = StringUtils.capitalize(languageLocale.getDisplayLanguage(displayLocale));
        String displayCountry = languageLocale.getDisplayCountry(displayLocale);

        if (!TextUtils.isEmpty(displayCountry)) {
            return displayLanguage + " (" + displayCountry + ")";
        }
        return displayLanguage;
    }
}
