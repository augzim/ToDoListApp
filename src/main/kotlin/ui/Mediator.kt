package ui

import database.Item
import javax.swing.JComponent


sealed class Event {
    sealed class ItemCreated : Event() {
        object TaskCreated : ItemCreated()
        object CategoryCreated : ItemCreated()
    }

    sealed class ItemDeleted : Event() {
        object TaskDeleted : ItemDeleted()
        object CategoryDeleted : ItemDeleted()
    }

    class ItemUpdated(val item: Item) : Event()
    class ViewModeChanged(val viewMode: ViewMode) : Event()
}

interface Mediator {
    fun notify(sender: JComponent, event: Event)
}


class MainMediator : Mediator {
    lateinit var sidebar: Sidebar
    lateinit var contentPanel: ContentPanel
    lateinit var itemCreationPanel: ItemCreationPanel

    override fun notify(sender: JComponent, event: Event) {
        when (event) {
            is Event.ItemCreated.CategoryCreated,
            is Event.ItemDeleted.CategoryDeleted -> {
                sidebar.refresh(category = true)
                contentPanel.refresh()
            }

            is Event.ItemCreated.TaskCreated,
            is Event.ItemDeleted.TaskDeleted -> {
                contentPanel.refresh()
            }

            is Event.ItemUpdated -> itemCreationPanel.editItemPanel(event.item)

            is Event.ViewModeChanged -> {
                when (val view = event.viewMode) {
                    is ViewMode.Empty -> {}
                    is ViewMode.Categories -> contentPanel.showCategories()
                    is ViewMode.Tasks -> contentPanel.showTasks()
                    is ViewMode.TasksByTime -> contentPanel.showTasksByTime(view.filter)
                    is ViewMode.TasksByCategory -> contentPanel.showTasksByCategory(view.categoryId)
                    is ViewMode.TasksByDeadline -> contentPanel.showTasksByDeadline(view.filter)
                    is ViewMode.TasksWithDeadline -> contentPanel.showTasksWithDeadline(view.filter)
                }
            }
        }
    }
}