package ui

import database.Category
import database.DeadlineFilter
import database.Item
import database.Task
import database.TimeFilter
import service.ItemService
import java.awt.Color
import java.text.SimpleDateFormat
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities


class ContentPanel(
    val itemService: ItemService,
    val mediator: Mediator,
) : JPanel() {
    // todo what should be in init and what inside property block here?
    var currentView: ViewMode = ViewMode.Empty

    init {
        background = Color.GREEN
    }

    // ---------- PUBLIC API ----------
    fun showCategories() {
        currentView = ViewMode.Categories
        refresh()
    }

    fun showTasksByTime(filter: TimeFilter) {
        currentView = ViewMode.TasksByTime(filter)
        refresh()
    }

    fun showTasksByCategory(categoryId: Int) {
        currentView = ViewMode.TasksByCategory(categoryId)
        refresh()
    }

    fun showTasksByDeadline(filter: DeadlineFilter) {
        currentView = ViewMode.TasksByDeadline(filter)
        refresh()
    }

    fun refresh() {
        val items: List<Item> = when (val view = currentView) {
            is ViewMode.Empty -> emptyList()
            is ViewMode.Categories -> itemService.getAllCategories()
            is ViewMode.TasksByTime -> itemService.getTasksByTime(view.filter)
            is ViewMode.TasksByCategory -> itemService.getTasksByCategory(view.categoryId)
            is ViewMode.TasksByDeadline -> itemService.getTasksByDeadline(view.filter)
        }

        render(items)
    }

    // ---------- RENDER ----------
    private fun render(items: List<Item>) {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        items.forEach {
            add(createItemPanel(it))
        }

        revalidate()
        repaint()
    }

    // ---------- UI BUILDERS ----------
    private fun createItemTextPanel(item: Item): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel(item.title))

            if (item is Task) {
                item.deadline?.let {
                    val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
                    add(JLabel(format.format(it)))
                }

                if (item.categories.isNotEmpty()) {
                    add(JLabel(item.categories.joinToString(" ") { it.title }))
                }
            }
        }
    }

    // todo refactor slightly
    private fun createItemPanel(item: Item): JPanel {
        val textPanel = createItemTextPanel(item)

        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )

            add(textPanel)
            add(Box.createHorizontalGlue())

            val editButton = JButton("Edit").apply {
                addActionListener {
                    mediator.notify(this, Event.ItemUpdated(item))
                }
            }

            val deleteButton = JButton("Delete").apply {
                addActionListener {
                    deleteItemPanel(item)
                    itemService.delete(item)
                    when (item) {
                        is Task -> mediator.notify(this, Event.ItemDeleted.TaskDeleted)
                        is Category -> mediator.notify(this, Event.ItemDeleted.CategoryDeleted)
                    }
                }
            }

            add(editButton)
            add(deleteButton)
        }
    }

    // ---------- ACTIONS ----------
    // todo rename to deleteItem
    private fun deleteItemPanel(item: Item) {
        val parentWindow = SwingUtilities.getWindowAncestor(this)

        val option = JOptionPane.showConfirmDialog(
            parentWindow,
            "Do you want to delete the item?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )

        if (option == JOptionPane.YES_OPTION) {
            itemService.delete(item)
            refresh()
        }
    }
}