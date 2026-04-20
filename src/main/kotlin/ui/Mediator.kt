package ui

import database.Item
import javax.swing.JComponent


sealed class Event {
    object ItemCreated : Event()
    object ItemDeleted : Event()
    class ItemUpdated(val item: Item) : Event()
    // todo viewMode: ViewMode does not look good! change this!
    class ListItemsButtonClicked(val viewMode: ViewMode) : Event()
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
            is Event.ItemUpdated -> itemCreationPanel.editItemPanel(event.item)
            is Event.ItemCreated, Event.ItemDeleted -> contentPanel.refresh()
            is Event.ListItemsButtonClicked -> {
                when (val view = event.viewMode) {
                    is ViewMode.Empty -> {}
                    is ViewMode.Categories -> contentPanel.showCategories()
                    is ViewMode.TasksByTime -> contentPanel.showTasksByTime(view.filter)
                    is ViewMode.TasksByCategory -> contentPanel.showTasksByCategory(view.categoryId)
                    is ViewMode.TasksByDeadline -> contentPanel.showTasksByDeadline(view.filter)
                }
            }
        }
    }
}