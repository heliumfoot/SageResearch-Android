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

package org.sagebionetworks.research.mobile_ui.show_step.view.forms

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.sagebionetworks.research.domain.form.InputUIHint
import org.sagebionetworks.research.domain.form.implementations.ChoiceInputField
import org.sagebionetworks.research.domain.form.interfaces.InputField
import org.sagebionetworks.research.domain.result.implementations.CollectionResultBase
import org.sagebionetworks.research.domain.result.interfaces.CollectionResult
import org.sagebionetworks.research.domain.result.interfaces.Result
import org.sagebionetworks.research.domain.step.interfaces.FormUIStep
import org.sagebionetworks.research.domain.step.interfaces.Step
import org.sagebionetworks.research.domain.step.interfaces.UIStep
import org.sagebionetworks.research.mobile_ui.show_step.view.forms.FormDataSourceAdapter.ViewHolder
import org.slf4j.LoggerFactory
import org.threeten.bp.Instant

/**
 * Based on iOS' RSDTableDataSource, [FormDataSourceAdapter] is the model for ShowFormUIStepFragment's RecyclerView.
 * It provides the [RecyclerView.Adapter], manages and stores answers provided through user input,
 * and provides a [Result] with those answers upon request.
 *
 * It also provides several convenience methods for saving or selecting answers, checking if all answers
 * are valid, and retrieving specific model objects that may be needed by the Fragment.
 *
 * The [FormDataSourceAdapter] is comprised of 3 objects:
 *
 * 1. [FormAdapterSection]: An object representing a section in the RecyclerView. It has one or more
 *    [DataSourceItem] objects.
 *
 * 2. [Item]: An object representing a specific [RecyclerView.ViewHolder] cell.
 *    There will be one [DataSourceItem] for each (section, item) combo in the RecyclerView.
 *
 * 3. [FormAdapterItemGroup]: An object representing a specific question supplied by [Step] as an [InputField].
 *     Upon init(), the FormAdapterItemGroup will create one or more [DataSourceItem] objects representing the
 *     answer options for the [InputField]. The FormAdapterItemGroup is responsible for storing/computing the
 *     answers for its [InputField].
 */
open class FormDataSourceAdapter(
        /**
         * @property step the step driving the creation of the sections and groups.
         */
        val step: UIStep,
        /**
         * @property initialResult The initial result when the data source adapter was first displayed.
         */
        var collectionResult: CollectionResult? =
                CollectionResultBase(step.identifier, Instant.now(), null, listOf())):

        RecyclerView.Adapter<ViewHolder>() {

    companion object {
        val logger = LoggerFactory.getLogger(FormDataSourceAdapter::class.java)
    }

    /**
     * @property listener  The listener associated with this data source.
     */
    var listener: Listener? = null

    /**
     * @property itemGroups the table item groups displayed in this table.
     */
    var itemGroups: List<FormAdapterItemGroup<*>> = listOf()

    /**
     * @property supportedHints The UI hints supported by this data source.
     */
    val supportedHints: Set<String> = setOf()

    /// Initialize a new `RSDFormStepDataSourceObject`.
    /// - parameters:
    ///     - step:             The RSDStep for this data source.
    ///     - taskViewModel:         The current task path for this data source.
    ///     - supportedHints:   The supported UI hints for this data source.
//    public init(step: RSDStep, parent: RSDPathComponent?, supportedHints: Set<RSDFormUIHint>? = nil) {
//        self.supportedHints = supportedHints ?? RSDFormUIHint.allStandardHints
//        super.init(step: step, parent: parent)
//
//        // Set the initial result if available.
//        if let taskViewModel = parent as? RSDHistoryPathComponent,
//        let previousResult = taskViewModel.previousResult(for: step) {
//            if let collectionResult = (previousResult as? RSDCollectionResult) {
//                self.initialResult = collectionResult
//            } else {
//                var collectionResult = self.instantiateCollectionResult()
//                collectionResult.startDate = previousResult.startDate
//                collectionResult.endDate = previousResult.endDate
//                collectionResult.appendInputResults(with: previousResult)
//                self.initialResult = collectionResult
//            }
//        }
//
//        // Populate the sections and initial results.
//        let (sections, groups) = self.buildSections()
//        self.sections = sections
//        self.itemGroups = groups
//        populateInitialResults()
//    }
    init {
        buildSections()
    }

    /// The collection result associated with this data source. The default implementation is to search the `taskViewModel`
    /// for a matching result and if that fails to return a new instance created using `instantiateCollectionResult()`.
    ///
    /// - returns: The appropriate collection result.
//    open func collectionResult() -> RSDCollectionResult {
//        if let collectionResult = taskResult.stepHistory.last(where: { $0.identifier == step.identifier }) as? RSDCollectionResult {
//            return collectionResult
//        }
//        else {
//            return instantiateCollectionResult()
//        }
//    }

    /**
     * This function is used by [RecyclerView] to create a [RecyclerView.ViewHolder]
     * associated with the specified recyclerViewIndex.
     * This will always be called once to create the pool of [RecyclerView.ViewHolder] that
     * the [RecyclerView] will re-use with [onBindViewHolder] until it needs more, and
     * then this function may be called again to add as many to the pool as it needs.
     * @param parent [ViewGroup] of the the [RecyclerView.ViewHolder] that is returned.
     * @param recyclerViewIndex used to create the [ViewHolder]
     * @return a [ViewHolder] that is associated with the recyclerViewIndex
     */
    override fun onCreateViewHolder(parent: ViewGroup, recyclerViewIndex: Int): ViewHolder {
        item(recyclerViewIndex)?.let {
            return it.createViewHolder(parent)
        }
        throw IllegalArgumentException("Cannot find form item for recyclerViewIndex $recyclerViewIndex")
    }

    /**
     * This function is used by [RecyclerView] to fill the [RecyclerView.ViewHolder]
     * with the current state of its contents, which is in our case, the corresponding [Item].
     * The [RecyclerView] re-uses viewHolderItems so make sure reset the UI completely,
     * or the UI of for some rows will copy over into other rows.
     * @param viewHolderItem to bind.
     * @param recyclerViewIndex associated with this [ViewHolder]
     */
    override fun onBindViewHolder(viewHolderItem: ViewHolder, recyclerViewIndex: Int) {
        item(recyclerViewIndex)?.let {
            viewHolderItem.bindViewHolder(it)
        } ?: run {
            logger.warn("Could not find DataSourceAdapter.Item for recyclerViewIndex $recyclerViewIndex")
        }
    }

    /**
     * This function is used by the [RecyclerView] to determine how many [RecyclerView.ViewHolder]'s to create.
     * @return the total items in the sections.
     */
    override fun getItemCount(): Int {
        return sections.sumBy { it.rowCount }
    }

    override fun getItemViewType(recyclerViewIndex: Int): Int {
        return item(recyclerViewIndex)?.identifier?.hashCode() ?: 0
    }

    open fun item(recyclerViewIndex: Int): FormAdapterItem? {
        indexPath(recyclerViewIndex)?.let {
            // Protect against IndexOutOfBoundExceptions
            if (!sections.isEmpty() &&
                    it.sectionIndex < sections.size &&
                    sections[it.sectionIndex].rowCount < it.rowIndex) {
                return sections[it.sectionIndex].items[it.rowIndex]
            }
        }
        logger.warn("Could not find item at recyclerViewIndex $recyclerViewIndex")
        return null
    }

    /**
     * RecyclerView works with linear indexes by default.
     * The DataSourceAdapter works with sections and rows by default.
     * We must be able to convert between the two concepts to make the class work.
     * This function is for converting from a RecyclerView concept of index, to our IndexPath concept.
     * @param recyclerViewIndex how the RecyclerView treats linear indexes.
     * @return the section and row indexes from the linear index.
     */
    open fun indexPath(recyclerViewIndex: Int): IndexPath? {
        var indexSum = 0
        sections.forEachIndexed { index, section ->
            val newIndexSum = indexSum + section.rowCount
            if (indexSum > recyclerViewIndex) {
                val rowIndex = recyclerViewIndex - indexSum
                return IndexPath(
                        index, rowIndex)
            }
            indexSum = newIndexSum
        }
        logger.warn("Could not create indexPath based on recyclerViewIndex $recyclerViewIndex")
        return null
    }

    /**
     * RecyclerView works with linear indexes by default.
     * The DataSourceAdapter works with sections and rows by default.
     * We must be able to convert between the two concepts to make the class work.
     * This function is for converting from our IndexPath concept to the RecyclerView concept of index.
     * @param indexPath how our class treats indexes with sections and rows.
     * @return the correct index to feed into any base []RecyclerView] index values.
     */
    open fun recyclerViewIndex(indexPath: IndexPath): Int {
        var recyclerViewIndex = 0
        for (i in 0 until indexPath.sectionIndex) {
            if (i >= sections.size) {
                logger.warn("indexPath section $i is out of bounds for sections\' size ${sections.size}")
                return 0
            }
            recyclerViewIndex += sections[i].rowCount
        }
        return recyclerViewIndex + indexPath.rowIndex
    }

    open fun buildSections(step: Step, initialResult: Result?): SectionsInitializerReturn {
        if (step !is UIStep) {
            logger.error("FormStepSectionsInitializer only works with FormUISteps")
            return SectionsInitializerReturn(
                    emptyList(), emptyList())
        }
        val sectionBuilders = mutableListOf<SectionBuilder>()
        ((step as? FormUIStep)?.inputFields ?: emptyList<InputField<*>>()).forEach { inputField ->
            val lastSectionBuilder = sectionBuilders.lastOrNull()
            // Get the next row index
            var rowIndex = 0
            lastSectionBuilder?.let {
                if (!it.singleFormItem) {
                    rowIndex = it.section.rowCount
                }
            }

            // Call open function to get the appropriate item group.
            val itemGroup = instantiateItemGroup(inputField, rowIndex)
            val needExclusiveSection = (itemGroup as? InputFieldItemGroup)?.requiresExclusiveSection ?: false

            // If we don't need an exclusive section and we have an existing section and it's not exclusive
            // ('singleFormItem'), then add this item to that existing section, otherwise create a new one.
            if (!needExclusiveSection && (lastSectionBuilder != null && !lastSectionBuilder.singleFormItem)) {
                lastSectionBuilder.appendGroup(itemGroup)
            } else {
                val section = SectionBuilder(mutableListOf(itemGroup), sectionBuilders.size, needExclusiveSection)
                (itemGroup as? ChoiceItemGroup)?.let {
                    if (it.items.size > 1) {
                        section.title = it.inputField.getPrompt()
                        section.subtitle = it.inputField.getPromptDetail()
                    }
                }
                sectionBuilders.add(section)
            }
        }

        var sections = sectionBuilders.map { it.section }
        val itemGroups = sectionBuilders.map { it.itemGroups }.flatMap { it }

        // TODO: mdephillips 12/1/18 add these in once we create ImageAdapterItem and TextAdapterItem
//      // add image below and footnote
//      var items: [RSDTableItem] = []
//      if let imageTheme = (step as? RSDThemedUIStep)?.imageTheme, imageTheme.placementType == .iconAfter {
//          items.append(RSDImageTableItem(rowIndex: items.count, imageTheme: imageTheme))
//      }
//      if let footnote = uiStep.footnote {
//          items.append(RSDTextTableItem(rowIndex: items.count, text: footnote))
//      }
//      if items.count > 0 {
//          let sectionIndex = sections.count
//                  let section = RSDTableSection(identifier: "\(sectionIndex)", sectionIndex: sectionIndex, tableItems: items)
//          sections.append(section)
//      }

        return SectionsInitializerReturn(sections, itemGroups)
    }

    /**
     * Instantiate the appropriate item group for this input field.
     * @param inputField The input field to convert to an item group.
     * @param beginningRowIndex The beginning row index for this item group.
     * @return The instantiated item group.
     */
    open fun instantiateItemGroup(inputField: InputField<*>, beginningRowIndex: Int): FormAdapterItemGroup<*> {
        val uiHint = preferredUIHint(inputField)
        (inputField as? ChoiceInputField<*>)?.let {
            return ChoiceItemGroup.create(beginningRowIndex, it, uiHint)
        }
        // TODO: mdephillips 12/1/18 add in the other types
        //        let uiHint = preferredUIHint(for: inputField)
//        if case .measurement(_,_) = inputField.dataType {
//            return RSDHumanMeasurementTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint)
//        }
//        else if let pickerSource = inputField.pickerSource as? RSDChoiceOptions {
//            return RSDChoicePickerTableItemGroup(beginningRowIndex: 0, inputField: inputField, uiHint: uiHint, choicePicker: pickerSource)
//        }
//        else if let pickerSource = inputField.pickerSource as? RSDMultipleComponentPickerDataSource {
//            return RSDMultipleComponentTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint, pickerSource: pickerSource)
//        } else {
//            switch inputField.dataType.baseType {
//                case .boolean:
//                return RSDBooleanTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint)
//                case .string, .codable:
//                return RSDTextFieldTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint)
//                case .date:
//                return RSDDateTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint)
//                case .decimal, .integer, .year, .fraction, .duration:
//                return RSDNumberTableItemGroup(beginningRowIndex: beginningRowIndex, inputField: inputField, uiHint: uiHint)
//            }
//        }
        logger.error("InputField ${inputField.getFormDataType()} not supported by FormDataSourceAdapter yet.")
        return FormAdapterItemGroupBase(emptyList(), FormAdapterItemGroup.Info())
    }

    /**
     * What is the preferred ui hint for this input field that is supported by this adapter? By default,
     * this will look for the uiHint from the inputField to be included in the supported hints and if
     * not, will return the preferred ui hint for the data type.
     * @param inputField The inputField to check.
     * @return The ui hint to return, must be a String of type [InputUIHint].
     */
    open fun preferredUIHint(inputField: InputField<*>): String {
        val uiHint = inputField.getFormUIHint()
        if (uiHint != null) {
            return uiHint
        }
        // TODO: mdephillips 12/1/2018 this code copied from iOS does is not supported yet
        //        if (uiHint != null && supportedHints.contains(uiHint)) {
//            return uiHint
//        }
//        if let choiceInput = inputField.pickerSource as? RSDChoiceOptions, choiceInput.hasImages {
//            standardType = supportedHints.contains(.slider) ? .slider : nil
//        } else {
//            standardType = inputField.dataType.validStandardUIHints.first(where:{ supportedHints.contains($0) })
//        }
//        return standardType ?? .textfield
        return InputUIHint.TEXTFIELD
    }

    /**
     * Retrieve the [FormAdapterItemGroup] for a specific section index.
     * @param sectionIndex for the group in the adapter.
     * @return the requested [FormAdapterItemGroup], or null if it cannot be found.
     */
    open fun itemGroup(indexPath: IndexPath): FormAdapterItemGroup<*>? {
        return itemGroups.firstOrNull {
            isMatching(it, indexPath)
        }
    }

    private fun isMatching(itemGroup: FormAdapterItemGroup<*>, indexPath: IndexPath): Boolean {
        return itemGroup.sectionIndex == indexPath.sectionIndex &&
                indexPath.rowIndex >= itemGroup.beginningRowIndex &&
                indexPath.rowIndex < (itemGroup.beginningRowIndex + itemGroup.items.size)
    }

    /**
     * Retrieve the [FormAdapterItemGroup] with a specific [InputField] identifier.
     * @param inputFieldIdentifier The identifier of the [InputField] assigned to the item group.
     * @return The requested [FormAdapterItemGroup], or nil if it cannot be found.
     */
    open fun itemGroup(inputFieldIdentifier: String): FormAdapterItemGroup<*>? {
        return itemGroups.firstOrNull {
            inputFieldIdentifier == (it as? InputFieldItemGroup)?.inputField?.getIdentifier()
        }
    }

    /**
     * Determine if all answers are valid.
     * Also checks the case where answers are required but one has not been provided.
     * @return a [Boolean] indicating if all answers are valid.
     */
    open fun allAnswersValid(): Boolean {

    }

    /**
     * Save an answer for a specific sectionIndex and rowIndex.
     * @param answer the object to be saved as the answer.
     * @param indexPath the represents the [FormAdapterItem] in the adapter.
     */
    open fun saveAnswer(answer: Any, indexPath: IndexPath) {
        val itemGroup = (itemGroup(indexPath) as? InputFieldItemGroup) ?: run {
            logger.error("Could not find item group at indexPath $indexPath to save answer.")
            return
        }
        // TODO: mdephillips 12/2/18 address this when TextAdapterItem is added
        // RSDTextInputTableItem has different set answer with try clause, we may not need this on Android
//        let newAnswer = (answer is NSNull) ? nil : answer
//        if let tableItem = self.tableItem(at: indexPath) as? RSDTextInputTableItem {
//            // If this is a text input table item then store the answer on the table item instead of on the group.
//            try tableItem.setAnswer(newAnswer)
//            } else {
//            try itemGroup.setAnswer(newAnswer)
//            }
//        _answerDidChange(for: itemGroup, at: indexPath)
        itemGroup.setAnswer(answer)

    }

    private fun answerDidChange(itemGroup: FormAdapterItemGroup<*>, indexPath: IndexPath) {
        // Update the answers
        var stepResult = collectionResult

        if let result = self.instantiateAnswerResult(for: itemGroup) {
            stepResult.appendInputResults(with: result)
        } else {
            stepResult.removeInputResult(with: itemGroup.identifier)
        }
        self.taskResult.appendStepHistory(with: stepResult)

        // inform delegate that answers have changed
        delegate?.tableDataSource(self, didChangeAnswersIn: indexPath.section)
    }

    /**
     * Select or deselect the answer option for a specific sectionIndex and rowIndex.
     * @param item The adapter item that was selected or deselected.
     * @param sectionIndex the represents the [Item] in the adapter.
     * @param rowIndex the represents the [Item] in the adapter.
     * @return a pair of booleans for if the item is now selected and if it needs reloaded visually.
     */
    open fun selectAnswer(item: FormAdapterItem, indexPath: IndexPath): SelectAnswerReturn {

    }

    /**
     * [ViewHolder] is the base class to be used with [FormDataSourceAdapter].
     */
    abstract class ViewHolder(
            /**
             * @property item that this [ViewHolder] represents.
             */
            var item: FormAdapterItem,
            /**
             * The View for this item, it is used to construct the base [RecyclerView.ViewHolder].
             */
            itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        /**
         * This is a pass through function from [RecyclerView.Adapter.bindViewHolder]
         * @param item associated with the content of this [ViewHolder]
         */
        abstract fun bindViewHolder(item: FormAdapterItem)
    }

    /**
     * [DataSourceAdapter.Listener] handles callbacks from the adapter to the owner.
     */
    interface Listener {
        /**
         * Called when the answers tracked by the data source change.
         * @param dataSource The calling data source.
         * @param indexPath Of the item that changed.
         */
        fun didChangeAnswer(dataSource: FormDataSourceAdapter, indexPath: IndexPath)
    }

    data class SelectAnswerReturn(
        /**
         * @property isSelected if the item is selected or not now.
         */
        val isSelected: Boolean,
        /**
         * @property reload if the item should be reloaded visually.
         */
        val reload: Boolean)

    data class IndexPath(
        /**
         * @property sectionIndex the [FormAdapterSection] index for the adapter.
         */
        val sectionIndex: Int,
        /**
         * @property rowIndex the [Item] index within the [FormAdapterSection] for the adapter.
         */
        val rowIndex: Int)

    data class SectionsInitializerReturn(
            val sections: List<FormAdapterSection>,
            val itemGroups: List<FormAdapterItemGroup<*>>)

    /**
     * [SectionBuilder] is data class used to make the [FormStepSectionsInitializer.buildSections]
     * function easier to understand.
     */
    private data class SectionBuilder(
            var itemGroups: MutableList<FormAdapterItemGroup<*>> = mutableListOf(),
            val index: Int,
            val singleFormItem: Boolean,
            var title: String? = null,
            var subtitle: String? = null) {

        val section: FormAdapterSection
            get() {
            val items = itemGroups.map { it.items }.flatMap { it }
            return FormAdapterSection("$index",
                    items, index, title, subtitle)
        }

        fun appendGroup(itemGroup: FormAdapterItemGroup<*>) {
            itemGroup.sectionIndex = index
            itemGroups.add(itemGroup)
        }
    }
}