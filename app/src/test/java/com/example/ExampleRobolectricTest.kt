package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches Rafiq AI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rafiq AI", appName)
    }

    @Test
    fun `onboarding to home and main navigation paths work`() {
        composeTestRule.waitForIdle()

        // Complete Onboarding
        composeTestRule.onNodeWithTag("onboarding_start_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        // Verify all 6 modules on Home
        val grid = composeTestRule.onNodeWithTag("home_modules_grid")
        grid.performScrollToNode(hasTestTag("module_card_vision"))
        composeTestRule.onNodeWithTag("module_card_vision").assertIsDisplayed()
        grid.performScrollToNode(hasTestTag("module_card_voice"))
        composeTestRule.onNodeWithTag("module_card_voice").assertIsDisplayed()
        grid.performScrollToNode(hasTestTag("module_card_communication"))
        composeTestRule.onNodeWithTag("module_card_communication").assertIsDisplayed()
        grid.performScrollToNode(hasTestTag("module_card_listener"))
        composeTestRule.onNodeWithTag("module_card_listener").assertIsDisplayed()
        grid.performScrollToNode(hasTestTag("module_card_reader"))
        composeTestRule.onNodeWithTag("module_card_reader").assertIsDisplayed()
        grid.performScrollToNode(hasTestTag("module_card_learning"))
        composeTestRule.onNodeWithTag("module_card_learning").assertIsDisplayed()

        // Navigate to Communication and back
        grid.performScrollToNode(hasTestTag("module_card_communication"))
        composeTestRule.onNodeWithTag("module_card_communication").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("comm_speak_sentence_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_back_button").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Smart Reader, load sample text, and go back
        composeTestRule.onNodeWithTag("home_modules_grid")
            .performScrollToNode(hasTestTag("module_card_reader"))
        composeTestRule.onNodeWithTag("module_card_reader").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("reader_load_sample_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("reader_sentences_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_back_button").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Smart Learning and back
        composeTestRule.onNodeWithTag("home_modules_grid")
            .performScrollToNode(hasTestTag("module_card_learning"))
        composeTestRule.onNodeWithTag("module_card_learning").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("learning_activity_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_back_button").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Settings and back
        composeTestRule.onNodeWithTag("home_settings_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_delete_data_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_back_button").performClick()
        composeTestRule.waitForIdle()
    }
}
