/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.domain.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;

import org.sagebionetworks.research.domain.form.InputField;
import org.sagebionetworks.research.domain.step.ui.FormUIStep;

import java.util.List;

public class FormUIStepBase extends UIStepBase implements FormUIStep {
    @NonNull
    private final List<InputField> inputFields;

    public static final String TYPE_KEY = "form";

    public FormUIStepBase(@NonNull final String identifier, @Nullable final String title,
            @Nullable final String text, @Nullable final String detail, @Nullable final String footnote,
            @NonNull final List<InputField> inputFields) {
        super(identifier, title, text, detail, footnote);
        this.inputFields = inputFields;
    }

    @NonNull
    @Override
    public List<InputField> getInputFields() {
        return this.inputFields;
    }

    @NonNull
    @Override
    public String getType() {
        return TYPE_KEY;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 3 * Objects.hashCode(this.inputFields);
    }

    /**
     * Override equalsHelper to also compare the input fields.
     * @param o The object to check for equality with this.
     * @return True if this is equal to o, false otherwise.
     */
    @Override
    protected boolean equalsHelper(Object o) {
        FormUIStepBase formStep = (FormUIStepBase) o;
        return super.equalsHelper(o) &&
                Objects.equal(this.getInputFields(), formStep.getInputFields());
    }

    /**
     * Override toStringHelper to also add the input fields.
     * @return The ToStringHelper that can produce the toString for this.
     */
    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("inputFields", this.getInputFields());
    }
}