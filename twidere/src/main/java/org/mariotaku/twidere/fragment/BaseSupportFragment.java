/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.text.BidiFormatter;
import android.view.View;

import com.squareup.otto.Bus;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DebugModeUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.NotificationManagerWrapper;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereValidator;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public class BaseSupportFragment extends Fragment implements IBaseFragment, Constants {

    // Utility classes
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected MediaLoaderWrapper mMediaLoader;
    @Inject
    protected Bus mBus;
    @Inject
    protected AsyncTaskManager mAsyncTaskManager;
    @Inject
    protected MultiSelectManager mMultiSelectManager;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;
    @Inject
    protected NotificationManagerWrapper mNotificationManager;
    @Inject
    protected BidiFormatter mBidiFormatter;
    @Inject
    protected ErrorInfoStore mErrorInfoStore;
    @Inject
    protected TwidereValidator mValidator;

    private final ActionHelper mActionHelper = new ActionHelper(this);

    public BaseSupportFragment() {

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        requestFitSystemWindows();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        GeneralComponentHelper.build(context).inject(this);
    }

    public ContentResolver getContentResolver() {
        final Activity activity = getActivity();
        if (activity != null) return activity.getContentResolver();
        return null;
    }

    public void invalidateOptionsMenu() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.supportInvalidateOptionsMenu();
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(final BroadcastReceiver receiver) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.unregisterReceiver(receiver);
    }

    @Override
    public Bundle getExtraConfiguration() {
        return null;
    }

    @Override
    public int getTabPosition() {
        final Bundle args = getArguments();
        return args != null ? args.getInt(EXTRA_TAB_POSITION, -1) : -1;
    }

    @Override
    public void requestFitSystemWindows() {
        final Activity activity = getActivity();
        final Fragment parentFragment = getParentFragment();
        final SystemWindowsInsetsCallback callback;
        if (parentFragment instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) parentFragment;
        } else if (activity instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) activity;
        } else {
            return;
        }
        final Rect insets = new Rect();
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets);
        }
    }

    @Override
    public void executeAfterFragmentResumed(Action action) {
        mActionHelper.executeAfterFragmentResumed(action);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionHelper.dispatchOnResumeFragments();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugModeUtils.watchReferenceLeak(this);
    }

    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    }
}
