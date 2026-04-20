package ui

import database.Category
import database.Item
import database.Task
import net.miginfocom.swing.MigLayout
import service.ItemService
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.SpinnerDateModel
import javax.swing.SwingConstants


sealed class DateFormatPattern(val format: String) {
    object Date : DateFormatPattern("yyyy-MM-dd")
    object Time : DateFormatPattern("HH:mm")
}


class ItemCreationPanel(
    val itemService: ItemService,
    val mediator: Mediator,
) : JPanel() {

    init {
        preferredSize = Dimension(200, 600)
        showCreateItem()
    }

    // ------------- HEADER -------------
    fun showCreateItem() {
        removeAll()
        layout = MigLayout(
            "wrap 1, gap 25",
            "[grow, fill]",
            "[] []"
        )
        showCreateItemHeader()
    }

    fun showCreateItemHeader() {
        val categoryButton = JButton("New Category").apply {
            addActionListener {
                showCreateCategory()
                revalidate()
                repaint()
            }
        }
        val taskButton = JButton("New Task").apply {
            addActionListener {
                showCreateTask()
                revalidate()
                repaint()
            }
        }
        add(categoryButton)
        add(taskButton)
    }

    // ------------ CATEGORY ------------
    fun showCreateCategory() {
        removeAll()
        layout = MigLayout(
            "wrap 1, gap 25",
            "[grow, fill]",
            "[] [] [] [] push []"
        )

        showCreateItemHeader()

        val header = createHeader("New Category")
        val titleField = createTitleField()

        add(header)
        add(titleField)
        add(createCategoryDoneButton(titleField))

        revalidate()
        repaint()
    }

    private fun createCategoryDoneButton(
        titleField: JTextArea
    ) = JButton("Done").apply {

        addActionListener {
            // todo duplicate code
            val title = titleField.text.trim()
            if (!isTitleValid(title)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Title length must be > 0 and < 150 symbols!",
                    "Invalid title",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            itemService.createCategory(title = titleField.text)
            mediator.notify(this, Event.ItemCreated)
            clearCategoryForm(titleField)
        }
    }

    private fun clearCategoryForm(titleField: JTextArea) {
        titleField.text = ""
    }

    // -------------- TASK --------------
    fun showCreateTask() {
        removeAll()
        layout = MigLayout(
            "wrap 1, gap 25",
            "[grow, fill]",
            "[] [] [] [] [] [] [] push []"
        )

        showCreateItemHeader()

        val header = createHeader("New Task")
        val titleField = createTitleField()
        val categoryButtonState = CategoryButtonState(itemService)
        val categoryPanel = createCategoryPanel(categoryButtonState)
        val dateSpinner = createDateSpinner(DateFormatPattern.Date)
        val timeSpinner = createDateSpinner(DateFormatPattern.Time)

        add(header)
        add(titleField, "growx")
        add(categoryPanel)
        add(createDatePanel("Date", dateSpinner))
        add(createDatePanel("Time", timeSpinner))
        add(createTaskDoneButton(titleField, dateSpinner, timeSpinner, categoryButtonState))

        revalidate()
        repaint()
    }

    // todo this class does too many things, refactor it later!
    // ------------- STATES -------------
    private class CategoryButtonState(val itemService: ItemService) {
        val button = JButton("Category").apply {
            addActionListener {
                val categories = itemService.getAllCategories().sortedBy { it.title }

                val list = JList(categories.toTypedArray()).apply {
                    selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                    cellRenderer = TitleListCellRenderer()
                    selectedIndices = categories
                        .mapIndexedNotNull { index, cat ->
                            if (cat.id in ids) index else null
                        }
                        .toIntArray()
                }

                val result = JOptionPane.showConfirmDialog(
                    null,
                    JScrollPane(list),
                    "Select Categories",
                    JOptionPane.OK_CANCEL_OPTION
                )

                if (result == JOptionPane.OK_OPTION) {
                    ids = list.selectedValuesList.map { it.id }.toSet()
                }
            }
        }

        var ids: Set<Int> = emptySet()
            set(value) {
                field = value
                updateButtonText()
            }

        private fun updateButtonText() {
            button.text = if (ids.isEmpty()) "Category" else "${ids.size} selected"
        }
    }

    // ---------- UI BUILDERS ----------
    private fun createHeader(text: String) =
        JLabel(text).apply {
            horizontalAlignment = SwingConstants.CENTER
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )
        }

    private fun createTitleField() =
        JTextArea(5, 20).apply {
            lineWrap = true
            wrapStyleWord = true
        }

    private fun createCategoryPanel(state: CategoryButtonState) =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            add(JLabel("Category"))
            add(Box.createHorizontalGlue())
            add(state.button)
        }

    // todo look for 3rd party libs for Spinners
    private fun createDateSpinner(dateFormatPattern: DateFormatPattern) =
        JSpinner(SpinnerDateModel()).apply {
            editor = JSpinner.DateEditor(this, dateFormatPattern.format)
        }

    private fun createDatePanel(text: String, component: JComponent) =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            add(JLabel(text))
            add(Box.createHorizontalGlue())
            add(component)
        }

    private fun createTaskDoneButton(
        titleField: JTextArea,
        dateSpinner: JSpinner,
        timeSpinner: JSpinner,
        categoryButtonState: CategoryButtonState
    ) = JButton("Done").apply {

        addActionListener {
            val deadline = extractDeadline(dateSpinner, timeSpinner)

            if (!isDeadlineValid(deadline)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Deadline must be in the future!",
                    "Invalid date",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val title = titleField.text.trim()
            if (!isTitleValid(title)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Title length must be > 0 and < 150 symbols!",
                    "Invalid title",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            itemService.createTask(
                title = title,
                description = null,
                deadline = deadline,
                categoryIds = categoryButtonState.ids
            )

            mediator.notify(this, Event.ItemCreated)
            clearTaskForm(titleField, dateSpinner, timeSpinner, categoryButtonState)
        }
    }

    // ---------- HELPERS ----------
    private fun extractDeadline(dateSpinner: JSpinner, timeSpinner: JSpinner): Timestamp {
        val date = (dateSpinner.value as Date).toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val time = (timeSpinner.value as Date).toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        return Timestamp.valueOf(LocalDateTime.of(date, time))
    }

    // todo this func (static) seems to be better removed from class
    private fun isDeadlineValid(deadline: Timestamp): Boolean {
        val now = Date()
        return deadline.after(now)
    }

    private fun isTitleValid(title: String) =
        !(title.isEmpty() || title.length > 150)

    private fun clearTaskForm(
        titleField: JTextArea,
        dateSpinner: JSpinner,
        timeSpinner: JSpinner,
        state: CategoryButtonState
    ) {
        titleField.text = ""
        state.ids = emptySet()
        dateSpinner.value = Date()
        timeSpinner.value = Date()
    }

    // -------- UPDATE ITEM --------
    fun editItemPanel(item: Item) {
        when (item) {
            is Category -> showEditCategory(item)
            is Task -> showEditTask(item)
        }
    }

    private fun showEditCategory(category: Category) {
        removeAll()
        layout = MigLayout(
            "wrap 1, gap 25",
            "[grow, fill]",
            "[] [] [] [] push []"
        )

        showCreateItemHeader()

        val header = createHeader("New Category")
        val titleField = createTitleField().apply {
            text = category.title
        }

        add(header)
        add(titleField)
        add(createCategoryEditButton(category.id, titleField))

        revalidate()
        repaint()
    }

    private fun createCategoryEditButton(
        categoryId: Int,
        titleField: JTextArea
    ) = JButton("Done").apply {

        addActionListener {
            // todo duplicate code
            val title = titleField.text.trim()
            if (!isTitleValid(title)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Title length must be > 0 and < 150 symbols!",
                    "Invalid title",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            itemService.updateCategory(categoryId, title = titleField.text)
            mediator.notify(this, Event.ItemCreated)
            clearCategoryForm(titleField)
        }
    }

    private fun showEditTask(task: Task) {
        removeAll()
        layout = MigLayout(
            "wrap 1, gap 25",
            "[grow, fill]",
            "[] [] [] [] [] [] [] push []"
        )

        showCreateItemHeader()

        val header = createHeader("Edit Task")
        val titleField = createTitleField().apply {
            text = task.title
        }
        val categoryButtonState = CategoryButtonState(itemService).apply {
            ids = task.categories.map { it.id }.toSet()
        }
        val categoryPanel = createCategoryPanel(categoryButtonState)

        // todo  deadline == null is not supported in entire app so far (will be in the future)
        val dateSpinner = createDateSpinner(DateFormatPattern.Date)
        val timeSpinner = createDateSpinner(DateFormatPattern.Time)

        val dateTime = task.deadline
        if (dateTime != null) {
            val date = Date.from(dateTime.toInstant())
            dateSpinner.value = date
            timeSpinner.value = date
        } else {
            dateSpinner.value = Date()
            timeSpinner.value = Date()
        }

        add(header)
        add(titleField, "growx")
        add(categoryPanel)
        add(createDatePanel("Date", dateSpinner))
        add(createDatePanel("Time", timeSpinner))
        add(createTaskEditButton(task.id, titleField, dateSpinner, timeSpinner, categoryButtonState))

        revalidate()
        repaint()
    }

    private fun createTaskEditButton(
        taskId: Int,
        titleField: JTextArea,
        dateSpinner: JSpinner,
        timeSpinner: JSpinner,
        categoryButtonState: CategoryButtonState
    ) = JButton("Done").apply {

        addActionListener {
            val deadline = extractDeadline(dateSpinner, timeSpinner)

            if (!isDeadlineValid(deadline)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Deadline must be in the future!",
                    "Invalid date",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val title = titleField.text.trim()
            if (!isTitleValid(title)) {
                JOptionPane.showMessageDialog(
                    null,
                    "Title length must be > 0 and < 150 symbols!",
                    "Invalid title",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            itemService.updateTask(
                taskId,
                title = title,
                description = null,
                deadline = deadline,
                categoryIds = categoryButtonState.ids
            )

            mediator.notify(this, Event.ItemCreated)
            clearTaskForm(titleField, dateSpinner, timeSpinner, categoryButtonState)
        }
    }
}