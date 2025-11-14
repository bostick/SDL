package org.libsdl.app;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

/* This is a fake invisible editor view that receives the input and defines the
 * pan&scan region
 */
public class SDLDummyEdit extends View implements View.OnKeyListener, View.OnApplyWindowInsetsListener
{
    InputConnection ic;
    int input_type;

    SDLDummyEdit(Context context) {
        super(context);
        setFocusableInTouchMode(true);
        setFocusable(true);
        setOnKeyListener(this);
        setOnApplyWindowInsetsListener(this);
    }

    void setInputType(int input_type) {
        this.input_type = input_type;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return SDLActivity.handleKeyEvent(v, keyCode, event, ic);
    }

    //
    @Override
    public boolean onKeyPreIme (int keyCode, KeyEvent event) {
        // As seen on StackOverflow: http://stackoverflow.com/questions/7634346/keyboard-hide-event
        // FIXME: Discussion at http://bugzilla.libsdl.org/show_bug.cgi?id=1639
        // FIXME: This is not a 100% effective solution to the problem of detecting if the keyboard is showing or not
        // FIXME: A more effective solution would be to assume our Layout to be RelativeLayout or LinearLayout
        // FIXME: And determine the keyboard presence doing this: http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
        // FIXME: An even more effective way would be if Android provided this out of the box, but where would the fun be in that :)
        if (event.getAction()==KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            if (SDLActivity.mTextEdit != null && SDLActivity.mTextEdit.getVisibility() == View.VISIBLE) {
                SDLActivity.onNativeKeyboardFocusLost();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        ic = new SDLInputConnection(this, true);

        outAttrs.inputType = input_type;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI |
                              EditorInfo.IME_FLAG_NO_FULLSCREEN /* API 11 */;

        return ic;
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {

        //
        // try to handle this case:
        // Space Hockey
        // Side B Joining screen
        // keyboard is open
        // tap the close button in the bottom left corner of keyboard
        //
        // now cannot re-open keyboard
        //
        // onKeyPreIme() does not get called because no key is hit
        //

        WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets);

        //
        // this is the recommended method for checking is keyboard is visible
        //
        // https://developer.android.com/develop/ui/views/layout/sw-keyboard#check-visibility
        //
        // When running on devices with API Level 29 and before, the returned value is an
        // approximation based on the information available. This is especially true for the
        // Type#ime IME type, which currently only works when running on devices with SDK level 23
        // and above.
        //
        boolean imeVisible = insetsCompat.isVisible(WindowInsetsCompat.Type.ime());

        if (!imeVisible) {
            if (SDLActivity.mTextEdit != null && SDLActivity.mTextEdit.getVisibility() == VISIBLE) {
                SDLActivity.onNativeKeyboardFocusLost();
            }
        }

        return super.onApplyWindowInsets(insets);
    }
}

