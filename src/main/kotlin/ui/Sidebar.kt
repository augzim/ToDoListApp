package ui

import database.Category
import database.DeadlineFilter
import database.TimeFilter
import service.ItemService
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel


// todo remove color from constructor?
// todo refactor class
class Sidebar(
    val color: Color,
    val itemService: ItemService,
    val mediator: Mediator
) : JPanel() {
    private var tasksPanel: JPanel
    private var tasksByCategoryPanel: JPanel

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = color

        // categories
        createButtonAndAddToPanel("categories", ViewMode.Categories, this)

        // tasks
        tasksPanel = createCollapsibleSubPanel("tasks", this)

        // all
        createButtonAndAddToPanel("all", ViewMode.Tasks, tasksPanel)

        // tasks by time
        val tasksByTimePanel = createCollapsibleSubPanel("by time", tasksPanel)
        TimeFilter.entries.forEach { timeFilter ->
            createButtonAndAddToPanel(
                timeFilter.toString().lowercase(),
                ViewMode.TasksByTime(timeFilter),
                tasksByTimePanel
            )
        }

        // tasks by deadline
        val tasksByDeadlinePanel = createCollapsibleSubPanel("by deadline", tasksPanel)
        DeadlineFilter.entries.forEach { deadlineFilter ->
            createButtonAndAddToPanel(
                deadlineFilter.toString().lowercase().replace('_', ' '),
                ViewMode.TasksByDeadline(deadlineFilter),
                tasksByDeadlinePanel
            )
        }

        // tasks by category
        tasksByCategoryPanel = createCollapsibleSubPanel("by category", tasksPanel)
        // todo: duplicates below
        itemService.getAllCategories().forEach { category ->
            createButtonAndAddToPanel(
                category.title,
                ViewMode.TasksByCategory(category.id),
                tasksByCategoryPanel
            )
        }
    }

    private fun createButton(title: String, onClick: () -> Unit) =
        JButton(title).apply {
            addActionListener { onClick() }
        }

    private fun createButtonAndAddToPanel(title: String, viewMode: ViewMode, parentPanel: JPanel) {
        parentPanel.add(
            createButton(title) {
                mediator.notify(
                    this,
                    Event.ViewModeChanged(viewMode)
                )
            }
        )
    }

    private fun createEmptySubPanel(color: Color, leftIndent: Int = 20) =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(0, leftIndent, 0, 0)
            background = color
            isVisible = false
        }

    private fun createCollapsibleSubPanel(
        title: String,
        parentPanel: JPanel
    ): JPanel {
        val contentPanel = createEmptySubPanel(color)
        val toggleButton = createButton(title) {
            contentPanel.isVisible = !contentPanel.isVisible
            refresh()
        }
        parentPanel.add(toggleButton)
        parentPanel.add(contentPanel)
        return contentPanel
    }

    fun refreshTasksByCategoryPanel() {
        tasksByCategoryPanel.removeAll()
        itemService.getAllCategories().forEach { category ->
            createButtonAndAddToPanel(
                category.title,
                ViewMode.TasksByCategory(category.id),
                tasksByCategoryPanel
            )
        }
    }

    fun refresh(category: Boolean = false) {
        if (category) {
            refreshTasksByCategoryPanel()
        }
        revalidate()
        repaint()
    }
}