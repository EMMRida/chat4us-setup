/*
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4ussetup.gui;

/**
 * Installation wizard step interface. All steps must implement this interface
 * to be added to the wizard.
 *
 * @author El Mhadder Mohamed Rida
 */
public interface WizardStep {
	/**
     * Returns the title of the step.
     */
    String getTitle();

    /**
     * Returns the description of the step.
     */
	String getDescription();

    /**
     * Returns true if the step is validated.
     */
    boolean validateStep();

    /**
     * Called when the step is activated.
     * @param nextClicked True if the next button was clicked, false if the back button was clicked or programmatically.
     */
    void onStepActivated(boolean nextClicked);

    /**
     * Called when the step is deactivated (before the next step is activated).
     * @param nextClicked True if the next button was clicked, false if the back button was clicked or programmatically.
     */
    void onStepDeactivated(boolean nextClicked);
}
