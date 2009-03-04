/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.email.activity;

import com.android.email.Account;
import com.android.email.Email;
import com.android.email.MessagingController;
import com.android.email.Preferences;
import com.android.email.R;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Various instrumentation tests for MessageCompose.  
 * 
 * It might be possible to convert these to ActivityUnitTest, which would be faster.
 */
@MediumTest
public class MessageViewTests 
        extends ActivityInstrumentationTestCase2<MessageView> {
    
    // copied from MessageView (could be package class)
    private static final String EXTRA_ACCOUNT = "com.android.email.MessageView_account";
    private static final String EXTRA_FOLDER = "com.android.email.MessageView_folder";
    private static final String EXTRA_MESSAGE = "com.android.email.MessageView_message";
    private static final String EXTRA_FOLDER_UIDS = "com.android.email.MessageView_folderUids";
    private static final String EXTRA_NEXT = "com.android.email.MessageView_next";
    
    // used by the mock controller
    private static final String FOLDER_NAME = "folder";
    private static final String MESSAGE_UID = "message_uid";
    
    private Account mAccount;
    private TextView mToView;
    private TextView mSubjectView;
    private WebView mMessageContentView;
    
    public MessageViewTests() {
        super("com.android.email", MessageView.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getInstrumentation().getTargetContext();
        Account[] accounts = Preferences.getPreferences(context).getAccounts();
        if (accounts.length > 0)
        {
            // This depends on getDefaultAccount() to auto-assign the default account, if necessary
            mAccount = Preferences.getPreferences(context).getDefaultAccount();
            Email.setServicesEnabled(context);
        }
        
        // configure a mock controller
        MessagingController mockController = new MockMessagingController();
        MessagingController.injectMockController(mockController);
        
        // setup an intent to spin up this activity with something useful
        ArrayList<String> FOLDER_UIDS = new ArrayList<String>(
                Arrays.asList(new String[]{ "why", "is", "java", "so", "ugly?" }));
        Intent i = new Intent()
            .putExtra(EXTRA_ACCOUNT, mAccount)
            .putExtra(EXTRA_FOLDER, FOLDER_NAME)
            .putExtra(EXTRA_MESSAGE, MESSAGE_UID)
            .putStringArrayListExtra(EXTRA_FOLDER_UIDS, FOLDER_UIDS);
        this.setActivityIntent(i);

        final MessageView a = getActivity();
        mToView = (TextView) a.findViewById(R.id.to);
        mSubjectView = (TextView) a.findViewById(R.id.subject);
        mMessageContentView = (WebView) a.findViewById(R.id.message_content);
    }

    /**
     * The name 'test preconditions' is a convention to signal that if this
     * test doesn't pass, the test case was not set up properly and it might
     * explain any and all failures in other tests.  This is not guaranteed
     * to run before other tests, as junit uses reflection to find the tests.
     */
    public void testPreconditions() {
        assertNotNull(mToView);
        assertEquals(0, mToView.length());
        assertNotNull(mSubjectView);
        assertEquals(0, mSubjectView.length());
        assertNotNull(mMessageContentView);
    }
    
    /**
     * Tests that various UI calls can be made safely even before the messaging controller
     * has completed loading the message.  This catches various race conditions.
     */
    public void testUiRaceConditions() {
        
        MessageView a = getActivity();
        
        // on-streen controls
        a.onClick(a.findViewById(R.id.reply));
        a.onClick(a.findViewById(R.id.reply_all));
        a.onClick(a.findViewById(R.id.delete));
        a.onClick(a.findViewById(R.id.next));
        a.onClick(a.findViewById(R.id.previous));
//      a.onClick(a.findViewById(R.id.download));    // not revealed yet, so unfair test
//      a.onClick(a.findViewById(R.id.view));        // not revealed yet, so unfair test
        a.onClick(a.findViewById(R.id.show_pictures));
        
        // menus
        a.handleMenuItem(R.id.delete);
        a.handleMenuItem(R.id.reply);
        a.handleMenuItem(R.id.reply_all);
        a.handleMenuItem(R.id.forward);
        a.handleMenuItem(R.id.mark_as_unread);
    }
    
    /**
     * Mock Messaging controller, so we can drive its callbacks.  This probably should be
     * generalized since we're likely to use for other tests eventually.
     */
    private static class MockMessagingController extends MessagingController {

        private MockMessagingController() {
            super(null);
        }
    }
    
}